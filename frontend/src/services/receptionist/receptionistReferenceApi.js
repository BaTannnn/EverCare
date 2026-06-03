import { authApis, endpoints } from "../../configs/Apis";
import { unwrapList } from "./receptionistApiUtils";

const buildListConfig = (params) => {
  if (params && Object.keys(params).length > 0) {
    return {
      params: {
        ...params,
        noPaging: true,
      },
    };
  }

  return {
    params: {
      noPaging: true,
    },
  };
};

export const getReceptionistDepartments = (params) => authApis().get(endpoints.departments, buildListConfig(params)).then((response) => ({
  ...response,
  data: unwrapList(response),
}));

export const getReceptionistDoctors = (params) => authApis().get(endpoints.doctors, buildListConfig(params)).then((response) => ({
  ...response,
  data: unwrapList(response),
}));

export const getReceptionistMedicalServices = (params) => authApis().get(endpoints["medical-services"], buildListConfig(params)).then((response) => ({
  ...response,
  data: unwrapList(response),
}));
