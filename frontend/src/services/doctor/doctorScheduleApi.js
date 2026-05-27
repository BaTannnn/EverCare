import Apis, { authApis, endpoints } from "../../configs/Apis";

export const getDoctorSchedules = (params = {}) => {
  return authApis().get(endpoints["doctor-schedules"], { params });
};

export const getTodayDoctorSchedules = () => {
  return authApis().get(endpoints["doctor-schedules-today"]);
};

export default Apis;
