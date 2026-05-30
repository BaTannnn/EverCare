import { authApis, endpoints } from "../../configs/Apis";
import { unwrapList } from "./receptionistApiUtils";

export const getReceptionistDepartments = (params = {}) => authApis().get(endpoints.departments, { params }).then((response) => ({
  ...response,
  data: unwrapList(response),
}));

export const getReceptionistDoctors = (params = {}) => authApis().get(endpoints.doctors, { params }).then((response) => ({
  ...response,
  data: unwrapList(response),
}));

export const getReceptionistMedicalServices = (params = {}) => authApis().get(endpoints["medical-services"], { params }).then((response) => ({
  ...response,
  data: unwrapList(response),
}));

