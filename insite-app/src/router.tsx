import MainPage from "@pages/main/MainPage";
import UserPage from "@pages/user/UserPage";
import { createBrowserRouter } from "react-router-dom";

const routePath = [
  {
    id: "main-page",
    path: "",
    element: <MainPage />,
  },
  {
    id: "muser-page",
    path: "/user",
    element: <UserPage />,
  },
];

const router = createBrowserRouter(routePath);

export default router;