import { authApis, endpoints } from "../../configs/Apis";
import { unwrapList } from "./receptionistApiUtils";

export const getReceptionistSupportConversations = (params = {}) =>
  authApis().get(endpoints["receptionist-support-conversations"], { params }).then((response) => ({
    ...response,
    data: unwrapList(response),
  }));

export const acceptReceptionistSupportConversation = (conversationId) =>
  authApis().patch(endpoints["receptionist-support-conversation-accept"](conversationId)).then((response) => ({
    ...response,
    data: response.data,
  }));

export const getReceptionistSupportMessages = (conversationId, params = {}) =>
  authApis().get(endpoints["receptionist-support-messages"](conversationId), { params }).then((response) => ({
    ...response,
    data: unwrapList(response),
  }));

export const sendReceptionistSupportMessage = (conversationId, payload) =>
  authApis().post(endpoints["receptionist-support-messages"](conversationId), payload).then((response) => ({
    ...response,
    data: response.data,
  }));

export const createReceptionistSupportSchedule = (conversationId, payload) =>
  authApis().post(endpoints["receptionist-support-schedules"](conversationId), payload).then((response) => ({
    ...response,
    data: response.data,
  }));

export const closeReceptionistSupportConversation = (conversationId) =>
  authApis().patch(endpoints["receptionist-support-close"](conversationId)).then((response) => ({
    ...response,
    data: response.data,
  }));
