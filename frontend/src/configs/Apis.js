import axios from "axios";
import cookies from "react-cookies";

const BASE_URL = "http://localhost:8080/backend/api/";

export const endpoints = {
  login: "/login",
  register: "/auth/register",
  profile: "/secure/profile",
};

export const authApis = () => {
  const token = cookies.load("token");

  return axios.create({
    baseURL: BASE_URL,
    headers: token
      ? {
          Authorization: `Bearer ${token}`,
        }
      : {},
  });
};

export default axios.create({
  baseURL: BASE_URL,
});
