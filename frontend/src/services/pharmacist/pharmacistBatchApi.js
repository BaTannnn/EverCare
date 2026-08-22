import Apis, { authApis, endpoints } from "../../configs/Apis";

export const getMedicineBatches = (params = {}) => {
  return authApis().get(endpoints["pharmacist-medicine-batches"], { params });
};

export const getMedicineBatchesByMedicine = (medicineId) => {
  return authApis().get(endpoints["pharmacist-medicine-batches-by-medicine"](medicineId));
};

export const getNearExpiryBatches = (days = 30) => {
  return authApis().get(endpoints["pharmacist-near-expiry"], { params: { days } });
};

export const getExpiredBatches = () => {
  return authApis().get(endpoints["pharmacist-expired-batches"]);
};

export const importMedicineBatch = (payload) => {
  return authApis().post(endpoints["pharmacist-medicine-batches-import"], payload);
};

export const updateMedicineBatch = (id, payload) => {
  return authApis().put(endpoints["pharmacist-medicine-batch"](id), payload);
};

export const deleteMedicineBatch = (id) => {
  return authApis().delete(endpoints["pharmacist-medicine-batch"](id));
};

export default Apis;
