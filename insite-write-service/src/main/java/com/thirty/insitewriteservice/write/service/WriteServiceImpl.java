package com.thirty.insitewriteservice.write.service;

import com.thirty.insitewriteservice.global.service.KafkaProducer;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.influxdb.query.dsl.Flux;
import com.influxdb.query.dsl.functions.restriction.Restrictions;
import com.thirty.insitewriteservice.feignclient.ApplicationServiceClient;
import com.thirty.insitewriteservice.feignclient.dto.request.ApplicationVerifyReqDto;
import com.thirty.insitewriteservice.write.dto.ButtonReqDto;
import com.thirty.insitewriteservice.write.dto.DataReqDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class WriteServiceImpl implements WriteService {
    @Resource
    private InfluxDBClient influxDBClient;
    @Value("${influxdb.bucket}")
    private String bucket;

    private final KafkaProducer kafkaProducer;
    private final ApplicationServiceClient applicationServiceClient;

    @Override
    public void writeData(DataReqDto dataReqDto) {
        // applicationToken 과 applicationUrl 유효성 검증
        applicationServiceClient.validationApplication(ApplicationVerifyReqDto.create(dataReqDto.getApplicationToken(), dataReqDto.getApplicationUrl()));

        // activityId 및 abnormal requestCnt 갱신
        String[] activityId_requestCnt = getActivityIdAndRequestCnt(dataReqDto.getCookieId(), "data");
        dataReqDto.updateActivityId(activityId_requestCnt[0]);
        dataReqDto.updateRequestCnt(activityId_requestCnt[1]);

        // kafka 전송
        kafkaProducer.sendData("data", dataReqDto);
    }

    @Override
    public void writeButton(ButtonReqDto buttonReqDto) {
        // applicationToken 과 applicationUrl 유효성 검증
        applicationServiceClient.validationApplication(ApplicationVerifyReqDto.create(buttonReqDto.getApplicationToken(), buttonReqDto.getApplicationUrl()));

        // activityId 및 abnormal requestCnt 갱신
        String[] activityId_requestCnt = getActivityIdAndRequestCnt(buttonReqDto.getCookieId(), "button");
        buttonReqDto.updateActivityId(activityId_requestCnt[0]);
        buttonReqDto.updateRequestCnt(activityId_requestCnt[1]);

        // kafka 전송
        kafkaProducer.sendButton("button", buttonReqDto);
    }

    public String generateNewActivityId() {
        return UUID.randomUUID().toString();
    }
    public String[] getActivityIdAndRequestCnt(String cookieId, String measurementType) {
        QueryApi queryApi = influxDBClient.getQueryApi();
        Restrictions restrictions = Restrictions.and(
                Restrictions.measurement().equal(measurementType),
                Restrictions.tag("cookieId").equal(cookieId)
        );
        Flux query = Flux.from(bucket)
                .range(-30L, ChronoUnit.MINUTES)
                .filter(restrictions)
                .groupBy("_time")
                .sort(new String[] {"_time"}, false);

        List<FluxTable> tables = queryApi.query(query.toString());
        String[] activityId_requestCnt;

        if(tables.size() == 0) return new String[] {generateNewActivityId(), "1"};
        List<FluxRecord> records = tables.get(tables.size() - 1).getRecords();
        if(tables.isEmpty() || records.isEmpty()){
            activityId_requestCnt = new String[] {generateNewActivityId(), "1"};
            return activityId_requestCnt;
        }

        FluxRecord lastActivity = records.get(0);
        Instant lastActivityTime = Instant.parse(lastActivity.getValueByKey("_time").toString());
        Duration duration = Duration.between(lastActivityTime, Instant.now());

        if (duration.getSeconds() < 5) { // 5초 내 다시 request 발생할 때
            int currentRequestCnt = Integer.parseInt(lastActivity.getValueByKey("requestCnt").toString());
            activityId_requestCnt = new String[] {lastActivity.getValueByKey("activityId").toString(), String.valueOf(currentRequestCnt + 1)};
        } else {
            activityId_requestCnt = new String[] {lastActivity.getValueByKey("activityId").toString(), "1"};
        }
        return activityId_requestCnt;
    }
}

