import Apis, { endpoints, authApis } from "../../configs/Apis";
import { unwrapPatientList } from "./patientApiHelpers";
import { mapInvoice, mapPaymentResult } from "./patientMappers";

export const getPatientInvoices = () => {
  return authApis().get(endpoints["patient-invoices"]).then((response) => ({
    ...response,
    data: unwrapPatientList(response).map(mapInvoice),
  }));
};

export const payPatientInvoice = (invoiceId, payload) => {
  return authApis().post(`${endpoints["patient-invoice-detail"](invoiceId)}/payments`, payload).then((response) => ({
    ...response,
    data: mapPaymentResult(response.data),
  }));
};

export default Apis;
