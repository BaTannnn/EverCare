import Apis, { authApis, endpoints } from "../../configs/Apis";

export const getStaffTestRequests = () => {
  return authApis().get(endpoints["staff-test-requests"]);
};

export const getStaffTestRequestDetail = (recordId) => {
  return authApis().get(endpoints["staff-test-request-detail"](recordId));
};

export const getStaffTestResults = (params = {}) => {
  return authApis().get(endpoints["staff-test-results"], { params });
};

export const getStaffTestResultDetail = (id) => {
  return authApis().get(endpoints["staff-test-result"](id));
};

export const createStaffTestResult = (recordId, payload) => {
  return authApis().post(endpoints["staff-medical-record-test-results"](recordId), payload);
};

export const updateStaffTestResult = (id, payload) => {
  return authApis().put(endpoints["staff-test-result"](id), payload);
};

export default Apis;
