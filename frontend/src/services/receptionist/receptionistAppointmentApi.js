import { authApis, endpoints } from "../../configs/Apis";
import { unwrapList } from "./receptionistApiUtils";
import { mapReceptionistAppointment, mapReceptionistAppointmentDetail } from "./receptionistAppointmentMappers";

export const getReceptionistAppointments = (params = {}) => authApis().get(endpoints["receptionist-appointments"], { params }).then((response) => ({
  ...response,
  data: unwrapList(response).map(mapReceptionistAppointment),
}));

export const getReceptionistAppointmentDetail = (appointmentId) => authApis().get(endpoints["receptionist-appointment-detail"](appointmentId)).then((response) => ({
  ...response,
  data: mapReceptionistAppointmentDetail(response.data),
}));

export const createReceptionistAppointment = (payload) => authApis().post(endpoints["receptionist-appointments"], payload).then((response) => ({
  ...response,
  data: mapReceptionistAppointmentDetail(response.data),
}));

export const updateReceptionistAppointment = (appointmentId, payload) => authApis().put(endpoints["receptionist-appointment-detail"](appointmentId), payload).then((response) => ({
  ...response,
  data: mapReceptionistAppointmentDetail(response.data),
}));

export const checkInReceptionistAppointment = (appointmentId, payload = {}) => authApis().patch(endpoints["receptionist-appointment-check-in"](appointmentId), payload).then((response) => ({
  ...response,
  data: mapReceptionistAppointmentDetail(response.data),
}));

