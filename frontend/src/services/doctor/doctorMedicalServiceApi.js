import Apis, { authApis, endpoints } from "../../configs/Apis";

export const searchMedicalServices = (keyword = "") => {
  return authApis().get(endpoints["medical-services"], {
    params: keyword ? { kw: keyword } : {},
  });
};

export default Apis;
