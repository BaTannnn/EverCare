import Apis, { authApis, endpoints } from "../../configs/Apis";

export const getDoctorDashboardSummary = () => {
  return authApis().get(endpoints["doctor-dashboard-summary"]);
};

export const getTodayAppointments = () => {
  return authApis().get(endpoints["doctor-appointments-today"]);
};

export default Apis;
