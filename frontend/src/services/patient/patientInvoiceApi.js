import Apis, { endpoints } from "../../configs/Apis";
import { createPatientGet, createPatientPost } from "./patientApiHelpers";
import { patientInvoices } from "../../data/patientMockData";

export const getPatientInvoices = () => {
  // TODO: Backend currently has no invoice controller. Keep this UI fallback only.
  return createPatientGet(endpoints["patient-invoices"], patientInvoices, "patient-invoices");
};

export const payPatientInvoice = (invoiceId, payload) => {
  // TODO: Backend currently has no payment controller. Keep this UI fallback only.
  return createPatientPost(endpoints["patient-invoice-detail"](invoiceId), payload, { invoiceId, ...payload }, "patient-invoice-payment");
};

export default Apis;
