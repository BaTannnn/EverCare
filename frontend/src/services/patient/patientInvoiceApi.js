import Apis, { endpoints, authApis } from "../../configs/Apis";
import { unwrapPatientList } from "./patientApiHelpers";
import { mapInvoice, mapPaymentResult } from "./patientMappers";

export const getPatientInvoices = (params = {}) => {
  return authApis().get(endpoints["patient-invoices"], { params }).then((response) => ({
    ...response,
    data: unwrapPatientList(response).map(mapInvoice),
  }));
};


export const getPatientInvoiceDetail = (invoiceId) => {
  return authApis().get(endpoints["patient-invoice-detail"](invoiceId)).then((response) => ({
    ...response,
    data: mapInvoice(response.data),
  }));
};

export const payPatientInvoice = (invoiceId, payload) => {
  return authApis().post(`${endpoints["patient-invoice-detail"](invoiceId)}/payments`, payload).then((response) => ({
    ...response,
    data: mapPaymentResult(response.data),
  }));
};

export default Apis;
