import Apis, { endpoints, authApis } from "../../configs/Apis";
import { unwrapPatientList } from "./patientApiHelpers";
import { mapMedicalRecord, mapMedicalRecordDetail } from "./patientMappers";

export const getPatientMedicalRecords = () => {
  return authApis().get(endpoints["patient-medical-records"]).then((response) => ({
    ...response,
    data: unwrapPatientList(response).map(mapMedicalRecord),
  }));
};

export const getPatientMedicalRecordById = (id) => {
  return authApis().get(endpoints["patient-medical-record-detail"](id)).then((response) => ({
    ...response,
    data: mapMedicalRecordDetail(response.data),
  }));
};

export default Apis;
