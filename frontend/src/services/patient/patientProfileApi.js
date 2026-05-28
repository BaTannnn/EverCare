import Apis, { endpoints, authApis } from "../../configs/Apis";
import { mapPatientProfile } from "./patientMappers";

export const getPatientProfile = () => {
  return authApis().get(endpoints["patient-profile"]).then((response) => ({
    ...response,
    data: mapPatientProfile(response.data),
  }));
};

export const updatePatientProfile = (payload) => {
  return authApis().put(endpoints["patient-profile"], payload).then((response) => ({
    ...response,
    data: mapPatientProfile(response.data),
  }));
};

export default Apis;
