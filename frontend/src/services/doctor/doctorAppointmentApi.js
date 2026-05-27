import Apis, { authApis, endpoints } from "../../configs/Apis";

export const getDoctorAppointmentsByDate = (date) => {
  return authApis().get(endpoints["doctor-appointments"], {
    params: { date },
  });
};

export const getTodayAppointments = () => {
  return authApis().get(endpoints["doctor-appointments-today"]);
};

export const getAppointmentDetail = (id) => {
  return authApis().get(endpoints["doctor-appointment-detail"](id));
};

export const startExamination = (id, payload) => {
  return authApis().post(endpoints["doctor-start-examination"](id), payload);
};

export default Apis;
