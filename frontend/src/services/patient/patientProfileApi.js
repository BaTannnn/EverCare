import Apis, { endpoints, authApis } from "../../configs/Apis";
import { mapPatientProfile } from "./patientMappers";

export const getPatientProfile = () => {
  return authApis().get(endpoints["patient-profile"]).then((response) => ({
    ...response,
    data: mapPatientProfile(response.data),
  })).catch((error) => {
    if (error?.response?.status === 404) {
      return {
        data: null,
        status: 404,
        statusText: "Not Found",
        headers: {},
        config: error.config,
      };
    }

    throw error;
  });
};

export const updatePatientProfile = (payload) => {
  return authApis().put(endpoints["patient-profile"], payload).then((response) => ({
    ...response,
    data: mapPatientProfile(response.data),
  }));
};

export const createPatientProfile = (payload) => {
  return authApis().post(endpoints["patient-create-profile"], payload).then((response) => ({
    ...response,
    data: mapPatientProfile(response.data),
  }));
};

export default Apis;
