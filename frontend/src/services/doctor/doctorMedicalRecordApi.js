import Apis, { authApis, endpoints } from "../../configs/Apis";

export const updateMedicalRecord = (recordId, payload) => {
  return authApis().put(endpoints["doctor-medical-record"](recordId), payload);
};

export const getMedicalRecordServices = (recordId) => {
  return authApis().get(endpoints["doctor-medical-record-services"](recordId));
};

export const addMedicalRecordService = (recordId, payload) => {
  return authApis().post(endpoints["doctor-medical-record-services"](recordId), payload);
};

export default Apis;
