import Apis, { authApis, endpoints } from "../../configs/Apis";

const ORDERABLE_SERVICE_TYPES = "TEST,IMAGING";

export const searchMedicalServices = (keyword = "") => {
  return authApis().get(endpoints["medical-services"], {
    params: {
      serviceTypes: ORDERABLE_SERVICE_TYPES,
      ...(keyword ? { kw: keyword } : {}),
    },
  });
};

export default Apis;
