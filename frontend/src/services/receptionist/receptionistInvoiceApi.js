import { authApis, endpoints } from "../../configs/Apis";
import { unwrapList } from "./receptionistApiUtils";
import {
  mapReceptionistInvoice,
  mapReceptionistInvoiceDetail,
  mapReceptionistPaymentResult,
} from "./receptionistInvoiceMappers";

export const getReceptionistInvoices = (params = {}) => authApis().get(endpoints["receptionist-invoices"], { params }).then((response) => ({
  ...response,
  data: unwrapList(response).map(mapReceptionistInvoice),
}));

export const getReceptionistInvoiceDetail = (invoiceId) => authApis().get(endpoints["receptionist-invoice-detail"](invoiceId)).then((response) => ({
  ...response,
  data: mapReceptionistInvoiceDetail(response.data),
}));

export const createReceptionistInvoicePayment = (invoiceId, payload) => authApis().post(endpoints["receptionist-invoice-payments"](invoiceId), payload).then((response) => ({
  ...response,
  data: mapReceptionistPaymentResult(response.data),
}));

