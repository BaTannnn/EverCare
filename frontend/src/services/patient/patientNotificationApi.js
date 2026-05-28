import Apis, { endpoints, authApis } from "../../configs/Apis";
import { unwrapPatientList } from "./patientApiHelpers";
import { mapNotification } from "./patientMappers";

export const getPatientNotifications = () => {
  return authApis().get(endpoints["patient-notifications"]).then((response) => ({
    ...response,
    data: unwrapPatientList(response).map(mapNotification),
  }));
};

export const markPatientNotificationAsRead = (notificationId) => {
  return authApis().patch(`${endpoints["patient-notifications"]}/${notificationId}/read`).then((response) => ({
    ...response,
    data: mapNotification(response.data),
  }));
};

export default Apis;
