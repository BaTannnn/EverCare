import Apis, { authApis, endpoints } from "../../configs/Apis";

export const getPharmacistMedicines = (params = {}) => {
  return authApis().get(endpoints["pharmacist-medicines"], { params });
};

export const getPharmacistMedicineDetail = (id) => {
  return authApis().get(endpoints["medicine-detail"](id));
};

export const getLowStockMedicines = () => {
  return authApis().get(endpoints["pharmacist-low-stock"]);
};

export const createPharmacistMedicine = (payload) => {
  return authApis().post(endpoints["pharmacist-medicine-create"], payload);
};

export const updatePharmacistMedicine = (id, payload) => {
  return authApis().put(endpoints["pharmacist-medicine-update"](id), payload);
};

export const updatePharmacistMedicineStatus = (id, active) => {
  return authApis().patch(endpoints["pharmacist-medicine-status"](id), { active });
};

export default Apis;
