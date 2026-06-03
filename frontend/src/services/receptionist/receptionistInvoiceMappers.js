import { unwrapObject, normalizeNumber, normalizeText } from "./receptionistApiUtils";

const mapPatient = (patient = {}) => ({
  id: patient.id || patient.patientId || null,
  patientCode: normalizeText(patient.patientCode, ""),
  fullName: normalizeText(patient.fullName || patient.patientName, ""),
  phone: normalizeText(patient.phone || patient.patientPhone, ""),
});

const mapMedicalRecord = (medicalRecord = {}) => ({
  id: medicalRecord.id || medicalRecord.medicalRecordId || null,
  recordCode: normalizeText(medicalRecord.recordCode || medicalRecord.medicalRecordCode, ""),
  visitDate: normalizeText(medicalRecord.visitDate, ""),
});

const mapPayment = (payment = {}) => ({
  id: payment.id || payment.paymentId || payment.transactionCode || null,
  amount: normalizeNumber(payment.amount),
  paymentMethod: normalizeText(payment.paymentMethod, ""),
  paymentProvider: normalizeText(payment.paymentProvider, ""),
  transactionCode: normalizeText(payment.transactionCode, ""),
  paymentStatus: normalizeText(payment.paymentStatus, ""),
  paidAt: normalizeText(payment.paidAt, ""),
  paymentUrl: normalizeText(payment.paymentUrl, ""),
  qrCodeUrl: normalizeText(payment.qrCodeUrl, ""),
});

export const mapReceptionistInvoice = (rawInvoice) => {
  const invoice = unwrapObject(rawInvoice);

  return {
    id: invoice.id || invoice.invoiceId || null,
    invoiceCode: normalizeText(invoice.invoiceCode, ""),
    patientId: invoice.patientId || invoice.patient?.id || null,
    patientCode: normalizeText(invoice.patientCode || invoice.patient?.patientCode, ""),
    patientName: normalizeText(invoice.patientName || invoice.patient?.fullName, ""),
    patientPhone: normalizeText(invoice.patientPhone || invoice.patient?.phone, ""),
    medicalRecordId: invoice.medicalRecordId || invoice.medicalRecord?.id || null,
    medicalRecordCode: normalizeText(invoice.medicalRecordCode || invoice.medicalRecord?.recordCode, ""),
    totalServiceAmount: normalizeNumber(invoice.totalServiceAmount),
    totalMedicineAmount: normalizeNumber(invoice.totalMedicineAmount),
    discountAmount: normalizeNumber(invoice.discountAmount),
    totalAmount: normalizeNumber(invoice.totalAmount),
    paymentMethod: normalizeText(invoice.paymentMethod, ""),
    paymentStatus: normalizeText(invoice.paymentStatus, "UNPAID"),
    paidAt: normalizeText(invoice.paidAt, ""),
    note: normalizeText(invoice.note, ""),
  };
};

export const mapReceptionistInvoiceDetail = (rawInvoice) => {
  const invoice = unwrapObject(rawInvoice);
  const payments = Array.isArray(invoice.payments) ? invoice.payments : [];

  return {
    ...mapReceptionistInvoice(invoice),
    patient: mapPatient(invoice.patient || {}),
    medicalRecord: mapMedicalRecord(invoice.medicalRecord || {}),
    payments: payments.map(mapPayment),
  };
};

export const mapReceptionistPaymentResult = (rawResult) => {
  const result = unwrapObject(rawResult);
  const payment = mapPayment(result.payment || result);
  const invoice = mapReceptionistInvoice(result.invoice || {});

  return {
    payment,
    invoice,
    paymentUrl: normalizeText(result.paymentUrl || payment.paymentUrl || payment.qrCodeUrl, ""),
    qrCodeUrl: normalizeText(result.qrCodeUrl || payment.qrCodeUrl, ""),
  };
};

