import { authApis, endpoints } from "../../configs/Apis";
import { unwrapList } from "./receptionistApiUtils";

export const getReceptionistDoctorSchedules = (doctorId, params = {}) =>
  authApis()
    .get(endpoints["doctor-schedules-by-doctor"](doctorId), {
      params: {
        ...params,
        noPaging: true,
      },
    })
    .then((response) => ({
      ...response,
      data: unwrapList(response),
    }));
