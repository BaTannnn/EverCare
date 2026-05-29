import Apis, { endpoints, authApis } from "../../configs/Apis";
import { unwrapPatientList, unwrapPatientPage } from "./patientApiHelpers";
import { mapMedicalRecord, mapMedicalRecordDetail } from "./patientMappers";

export const getPatientMedicalRecords = (params = {}) => {
  return authApis().get(endpoints["patient-medical-records"], { params }).then((response) => {
    const { items, pageInfo } = unwrapPatientPage(response);

    return {
      ...response,
      data: {
        items: items.map(mapMedicalRecord),
        pageInfo,
      },
    };
  });
};

export const getPatientMedicalRecordById = (id) => {
  return authApis().get(endpoints["patient-medical-record-detail"](id)).then((response) => ({
    ...response,
    data: mapMedicalRecordDetail(response.data),
  }));
};

export default Apis;
