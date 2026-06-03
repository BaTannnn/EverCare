import { authApis, endpoints } from "../../configs/Apis";
import { unwrapList } from "./receptionistApiUtils";

export const searchReceptionistPatients = (keyword, limit = 10) => authApis().get(endpoints["receptionist-patient-search"], {
  params: {
    keyword,
    limit,
  },
}).then((response) => ({
  ...response,
  data: unwrapList(response),
}));
