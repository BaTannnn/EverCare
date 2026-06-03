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

const buildTestResultPayload = (payload = {}) => {
  if (!payload.file) {
    return payload;
  }

  const formData = new FormData();
  Object.entries(payload).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      formData.append(key, value);
    }
  });

  return formData;
};

const multipartConfig = (payload) => (
  payload instanceof FormData
    ? { headers: { "Content-Type": "multipart/form-data" } }
    : undefined
);

export const createStaffTestResult = (recordId, payload) => {
  const requestPayload = buildTestResultPayload(payload);
  return authApis().post(
    endpoints["staff-medical-record-test-results"](recordId),
    requestPayload,
    multipartConfig(requestPayload)
  );
};

export const updateStaffTestResult = (id, payload) => {
  const requestPayload = buildTestResultPayload(payload);
  return authApis().put(
    endpoints["staff-test-result"](id),
    requestPayload,
    multipartConfig(requestPayload)
  );
};

export default Apis;
