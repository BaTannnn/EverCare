import Apis, { endpoints } from "../../configs/Apis";
import { authApis } from "../../configs/Apis";
import { unwrapPatientList } from "./patientApiHelpers";
import { mapDoctor, mapMedicalService } from "./patientMappers";

export const getPatientDoctors = async () => {
  const response = await authApis().get(endpoints.doctors, {
    params: {
      noPaging: true,
    },
  });
  return { ...response, data: unwrapPatientList(response).map(mapDoctor) };
};

export const getPatientMedicalServices = async () => {
  const response = await authApis().get(endpoints["medical-services"], 
    {
      params: {
        serviceTypes: "EXAMINATION",
        noPaging: true,
      }
    }
  );
  return { ...response, data: unwrapPatientList(response).map(mapMedicalService) };
};

export default Apis;
