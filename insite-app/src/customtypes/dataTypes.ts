type SideBarMenuType = {
  id: number;
  image: string;
  menu: string;
  route: string;
};

type IconsType = {
  [key: string]: string;
};

type LogosType = {
  [key: string]: string;
};

type ItemType = {
  id: number;
  name: string;
};

type DateSelectionType = {
  start: string;
  end: string;
  past: string;
  latest: string;
};

type UserCountDtoType = {
  id: number;
  count: number;
  percentage: number;
  currentPage: string;
  responseTime: string;
};

type StyleType = {
  fontSize: string;
};

type ChartDtoType = {
  name: string;
  y: number;
  dataLables: {
    enabled: boolean;
    format: string;
    style?: StyleType;
    textOutline?: string;
  };
};

type UserRefDtoType = {
  id: number;
  referrer: string;
  rank: number;
  count: number;
  percentage: number;
};

type ButtonType = {
  id: number;
  name: string;
  counts: number;
  date: string;
};

type ButtonCountDtoType = {
  id: number;
  name: string;
  count: number;
  countPerUser: number;
};

type AbnormalType = {
  id: number;
  cookieId: string;
  time: string;
  currentUrl: string;
  language: string;
  osId: string;
};

export type {
  SideBarMenuType,
  IconsType,
  LogosType,
  ItemType,
  DateSelectionType,
  UserCountDtoType,
  ChartDtoType,
  StyleType,
  UserRefDtoType,
  ButtonCountDtoType,
  AbnormalType,
  ButtonType,
};
