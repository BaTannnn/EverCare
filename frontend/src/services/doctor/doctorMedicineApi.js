import Apis, { authApis, endpoints } from "../../configs/Apis";

export const searchMedicines = (keyword) => {
  return authApis().get(endpoints.medicines, {
    params: { keyword },
  });
};

export const getMedicines = (params = {}) => {
  return authApis().get(endpoints.medicines, { params });
};

export const getMedicineDetail = (id) => {
  return authApis().get(endpoints["medicine-detail"](id));
};

export default Apis;
