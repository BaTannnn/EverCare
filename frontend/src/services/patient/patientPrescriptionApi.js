import Apis, { endpoints, authApis } from "../../configs/Apis";
import { unwrapPatientList } from "./patientApiHelpers";
import { mapPrescription } from "./patientMappers";

export const getPatientPrescriptions = () => {
  return authApis().get(endpoints["patient-prescriptions"]).then((response) => ({
    ...response,
    data: unwrapPatientList(response).map(mapPrescription),
  }));
};

export const getPatientPrescriptionById = (id) => {
  return authApis().get(endpoints["patient-prescription-detail"](id)).then((response) => ({
    ...response,
    data: mapPrescription(response.data),
  }));
};

export default Apis;
