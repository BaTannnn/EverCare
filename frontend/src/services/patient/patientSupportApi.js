import { authApis, endpoints } from "../../configs/Apis";
import { unwrapPatientList } from "./patientApiHelpers";

export const getPatientSupportConversations = () =>
  authApis().get(endpoints["patient-support-conversations"]).then((response) => ({
    ...response,
    data: unwrapPatientList(response),
  }));

export const createPatientSupportConversation = (payload) =>
  authApis().post(endpoints["patient-support-conversations"], payload).then((response) => ({
    ...response,
    data: response.data,
  }));

export const getPatientSupportMessages = (conversationId, params = {}) =>
  authApis().get(endpoints["patient-support-messages"](conversationId), { params }).then((response) => ({
    ...response,
    data: unwrapPatientList(response),
  }));

export const sendPatientSupportMessage = (conversationId, payload) =>
  authApis().post(endpoints["patient-support-messages"](conversationId), payload).then((response) => ({
    ...response,
    data: response.data,
  }));

export const closePatientSupportConversation = (conversationId) =>
  authApis().patch(endpoints["patient-support-close"](conversationId)).then((response) => ({
    ...response,
    data: response.data,
  }));
