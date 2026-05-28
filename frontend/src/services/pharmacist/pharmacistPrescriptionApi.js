import Apis, { authApis, endpoints } from "../../configs/Apis";

export const getPharmacistPrescriptions = (params = {}) => {
  return authApis().get(endpoints["pharmacist-prescriptions"], { params });
};

export const getPharmacistPrescriptionDetail = (id) => {
  return authApis().get(endpoints["pharmacist-prescription-detail"](id));
};

export const dispensePrescription = (id) => {
  return authApis().post(endpoints["pharmacist-prescription-dispense"](id));
};

export default Apis;
