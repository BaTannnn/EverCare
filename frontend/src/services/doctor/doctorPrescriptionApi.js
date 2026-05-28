import Apis, { authApis, endpoints } from "../../configs/Apis";

export const getDoctorPrescriptions = (params = {}) => {
  return authApis().get(endpoints["doctor-prescriptions"], { params });
};

export const createDoctorPrescription = (recordId, payload) => {
  return authApis().post(endpoints["doctor-medical-record-prescriptions"](recordId), payload);
};

export const updateDoctorPrescription = (prescriptionId, payload) => {
  return authApis().put(endpoints["doctor-prescription"](prescriptionId), payload);
};

export default Apis;
