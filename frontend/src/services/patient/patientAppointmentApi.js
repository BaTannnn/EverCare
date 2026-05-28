import Apis, { endpoints, authApis } from "../../configs/Apis";
import { unwrapPatientList } from "./patientApiHelpers";
import { mapAppointment } from "./patientMappers";

export const getPatientAppointments = () => {
  return authApis().get(endpoints["patient-appointments"]).then((response) => ({
    ...response,
    data: unwrapPatientList(response).map(mapAppointment),
  }));
};

export const bookPatientAppointment = (payload) => {
  return authApis().post(endpoints["patient-book-appointment"], payload).then((response) => ({
    ...response,
    data: mapAppointment(response.data),
  }));
};

export const cancelPatientAppointment = (appointmentId, reason) => {
  return authApis().patch(endpoints["patient-appointment-detail"](appointmentId) + "/cancel", { reason }).then((response) => ({
    ...response,
    data: mapAppointment(response.data),
  }));
};

export default Apis;
