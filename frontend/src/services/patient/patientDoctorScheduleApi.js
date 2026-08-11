import Apis, { authApis, endpoints } from "../../configs/Apis";
import { unwrapPatientPage } from "./patientApiHelpers";
import { mapDoctorSchedule } from "./patientMappers";

export const getPatientDoctorSchedules = (doctorId, params = {}) => {
  const request = authApis().get(endpoints["doctor-schedules-by-doctor"](doctorId), {
    params: {
      ...params,
      noPaging: true,
    },
  });

  return request.catch(async () => {
    const legacyResponse = await authApis().get(endpoints["doctor-schedules"], {
      params: {
        doctorId,
        ...params,
        noPaging: true,
      },
    });

    return legacyResponse;
  }).then((response) => {
    const { items } = unwrapPatientPage(response);

    return {
      ...response,
      data: items.map(mapDoctorSchedule),
    };
  });
};

export default Apis;
