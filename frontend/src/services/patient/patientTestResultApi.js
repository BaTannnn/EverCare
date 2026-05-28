import Apis, { endpoints, authApis } from "../../configs/Apis";
import { unwrapPatientList } from "./patientApiHelpers";
import { mapTestResult } from "./patientMappers";

export const getPatientTestResults = () => {
  return authApis().get(endpoints["patient-test-results"]).then((response) => ({
    ...response,
    data: unwrapPatientList(response).map(mapTestResult),
  }));
};

export const getPatientTestResultById = (id) => {
  return authApis().get(`${endpoints["patient-test-results"]}/${id}`).then((response) => ({
    ...response,
    data: mapTestResult(response.data),
  }));
};

export default Apis;
