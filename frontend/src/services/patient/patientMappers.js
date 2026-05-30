import { makeAvatarDataUri } from "../../data/patientMockData";
import { formatShortDate } from "../../pages/patient/patientPageUtils";

const unwrapList = (value) => {
  if (Array.isArray(value)) return value;
  if (Array.isArray(value?.content)) return value.content;
  if (Array.isArray(value?.data)) return value.data;
  if (Array.isArray(value?.items)) return value.items;
  return [];
};

const unwrapObject = (value) => {
  if (!value) return null;
  if (value.data && !Array.isArray(value.data) && typeof value.data === "object") return value.data;
  if (value.result && !Array.isArray(value.result) && typeof value.result === "object") return value.result;
  return value;
};

const normalizeText = (value, fallback = "") => {
  if (value === null || value === undefined || value === "") return fallback;
  return value;
};

const normalizeCurrency = (value) => Number(value || 0);

const normalizeTime = (value) => {
  if (!value) return "";
  if (typeof value !== "string") return String(value);
  const match = value.match(/^(\d{2}):(\d{2})/);
  if (match) {
    return `${match[1]}:${match[2]}`;
  }
  return value;
};

const deriveStatusLabel = (status) => {
  if (!status) return "Không rõ";
  return String(status).replaceAll("_", " ");
};

const createAvatar = (name) => makeAvatarDataUri(name || "EverCare");

const pickAvatarUrl = (source) =>
  source?.avatarUrl ||
  source?.avatar ||
  source?.photoUrl ||
  source?.imageUrl ||
  source?.profilePicture ||
  source?.avatarPath ||
  "";

const toDoctorName = (doctor) => doctor?.fullName || doctor?.doctorName || doctor?.name || "Bác sĩ EverCare";

export const mapPatientProfile = (rawProfile) => {
  const profile = unwrapObject(rawProfile) || {};
  const fullName = normalizeText(profile.fullName, "Bệnh nhân EverCare");

  return {
    id: profile.id || profile.patientCode || "PATIENT",
    patientCode: profile.patientCode || profile.id || "PATIENT",
    fullName,
    avatar: pickAvatarUrl(profile) || createAvatar(fullName),
    dateOfBirth: formatShortDate(profile.dateOfBirth),
    gender: normalizeText(profile.gender, "Chưa cập nhật"),
    phone: normalizeText(profile.phone, "Chưa cập nhật"),
    email: normalizeText(profile.email, "Chưa cập nhật"),
    address: normalizeText(profile.address, "Chưa cập nhật"),
    citizenId: normalizeText(profile.citizenId, ""),
    bloodType: normalizeText(profile.bloodType, "Chưa cập nhật"),
    allergyNote: normalizeText(profile.allergyNote, "Không ghi nhận"),
    medicalHistoryNote: normalizeText(profile.medicalHistoryNote, "Không ghi nhận"),
    healthInsuranceNo: normalizeText(profile.healthInsuranceNo, "Chưa cập nhật"),
    emergencyContactName: normalizeText(profile.emergencyContactName, "Chưa cập nhật"),
    emergencyContactPhone: normalizeText(profile.emergencyContactPhone, "Chưa cập nhật"),
    active: profile.active ?? true,
  };
};

export const mapAppointment = (rawAppointment) => {
  const appointment = unwrapObject(rawAppointment) || {};
  const doctorName = normalizeText(appointment.doctorName, "Bác sĩ EverCare");
  const appointmentDate = appointment.appointmentDate || appointment.date || "";
  const startTime = appointment.startTime || appointment.time || "";

  return {
    id: appointment.id || appointment.appointmentCode || appointment.appointmentId,
    appointmentCode: normalizeText(appointment.appointmentCode, appointment.id || ""),
    doctorId: appointment.doctorId,
    doctorName,
    doctorAvatar: appointment.doctorAvatar || createAvatar(doctorName),
    departmentName: normalizeText(appointment.departmentName, normalizeText(appointment.specialty, "Chuyên khoa")),
    serviceId: appointment.serviceId,
    serviceName: normalizeText(appointment.serviceName, "Dịch vụ khám"),
    appointmentDate,
    startTime,
    endTime: normalizeText(appointment.endTime, ""),
    displayDate: appointmentDate ? formatShortDate(appointmentDate) : "",
    displayTime: normalizeTime(startTime),
    status: normalizeText(appointment.status, "PENDING"),
    statusLabel: normalizeText(appointment.statusLabel, deriveStatusLabel(appointment.status)),
    reason: normalizeText(appointment.reason, ""),
    symptomNote: normalizeText(appointment.symptomNote, ""),
    invoiceId: appointment.invoiceId || appointment.invoice?.id || null,
    invoiceCode: normalizeText(appointment.invoiceCode, appointment.invoice?.invoiceCode || ""),
    paymentMethod: normalizeText(appointment.paymentMethod, appointment.invoice?.paymentMethod || ""),
    paymentProvider: normalizeText(appointment.paymentProvider, ""),
    paymentStatus: normalizeText(appointment.paymentStatus, appointment.invoice?.paymentStatus || ""),
    paymentUrl: normalizeText(appointment.paymentUrl, appointment.invoice?.paymentUrl || ""),
  };
};

export const mapMedicalRecord = (rawRecord) => {
  const record = unwrapObject(rawRecord) || {};
  const doctorName = normalizeText(record.doctorName, "Bác sĩ EverCare");

  return {
    id: record.id,
    recordCode: normalizeText(record.recordCode, record.id || ""),
    visitDate: record.visitDate || "",
    displayVisitDate: record.visitDate ? formatShortDate(record.visitDate) : "",
    doctorId: record.doctorId,
    doctorName,
    departmentName: normalizeText(record.departmentName, normalizeText(record.specialty, "Chuyên khoa")),
    diagnosis: normalizeText(record.diagnosis, ""),
    chiefComplaint: normalizeText(record.chiefComplaint, ""),
    treatmentPlan: normalizeText(record.treatmentPlan, ""),
    doctorNote: normalizeText(record.doctorNote, ""),
    paymentStatus: normalizeText(record.paymentStatus, "UNPAID"),
    status: normalizeText(record.paymentStatus, "UNPAID"),
    appointmentId: record.appointmentId,
    patientId: record.patientId,
  };
};

export const mapMedicalRecordDetail = (rawRecord) => {
  const record = mapMedicalRecord(rawRecord);
  const detail = unwrapObject(rawRecord) || {};

  return {
    ...record,
    appointmentCode: normalizeText(detail.appointmentCode, ""),
    patient: detail.patient || null,
    doctor: detail.doctor || null,
    department: detail.department || null,
    services: unwrapList(detail.services).map((service) => ({
      id: service.id || service.code || service.name,
      name: normalizeText(service.name, normalizeText(service.serviceName, "Dịch vụ")),
      price: normalizeCurrency(service.price),
      description: normalizeText(service.description, ""),
    })),
    testResults: unwrapList(detail.testResults).map((testResult) => ({
      id: testResult.id || testResult.resultCode || testResult.resultTitle,
      resultCode: normalizeText(testResult.resultCode, ""),
      resultTitle: normalizeText(testResult.resultTitle, "Kết quả xét nghiệm"),
      conclusion: normalizeText(testResult.conclusion, normalizeText(testResult.resultContent, "")),
      resultDate: testResult.resultDate || "",
    })),
    prescription: detail.prescription ? {
      id: detail.prescription.id,
      prescriptionCode: normalizeText(detail.prescription.prescriptionCode, ""),
    } : null,
  };
};

const inferTestStatus = (result) => {
  const text = `${result?.conclusion || ""} ${result?.resultContent || ""}`.toLowerCase();
  if (text.includes("bình thường") || text.includes("ổn")) return "NORMAL";
  if (text.includes("cần") || text.includes("lưu ý") || text.includes("theo dõi")) return "ATTENTION";
  return "NORMAL";
};

const inferTestCategory = (result) => {
  const text = `${result?.serviceName || ""} ${result?.resultTitle || ""} ${result?.resultContent || ""}`.toLowerCase();
  if (text.includes("huyết")) return "Huyết học";
  if (text.includes("sinh hóa") || text.includes("sinh hoc")) return "Sinh hóa";
  if (text.includes("nước tiểu") || text.includes("nuoc tieu")) return "Nước tiểu";
  return "Khác";
};

export const mapTestResult = (rawResult) => {
  const result = unwrapObject(rawResult) || {};
  return {
    id: result.id || result.resultCode,
    resultCode: normalizeText(result.resultCode, result.id || ""),
    name: normalizeText(result.resultTitle, "Xét nghiệm"),
    category: inferTestCategory(result),
    date: result.resultDate || "",
    doctorName: normalizeText(result.performedByName, "Bác sĩ EverCare"),
    conclusion: normalizeText(result.conclusion, normalizeText(result.resultContent, "")),
    fileUrl: result.fileUrl || "",
    status: inferTestStatus(result),
  };
};

export const mapPrescription = (rawPrescription) => {
  const prescription = unwrapObject(rawPrescription) || {};
  const items = unwrapList(prescription.items).map((item) => ({
    id: item.id || item.medicineId || item.medicineCode,
    name: normalizeText(item.medicineName, "Thuốc"),
    quantity: item.quantity ?? 0,
    dosage: normalizeText(item.dosage, ""),
    frequency: normalizeText(item.frequency, ""),
    duration: normalizeText(item.duration, ""),
    instruction: normalizeText(item.instruction, ""),
  }));

  return {
    id: prescription.id || prescription.prescriptionCode,
    prescriptionCode: normalizeText(prescription.prescriptionCode, prescription.id || ""),
    date: prescription.prescribedAt || "",
    displayDate: prescription.prescribedAt ? formatShortDate(prescription.prescribedAt) : "",
    doctorName: normalizeText(prescription.doctorName, "Bác sĩ EverCare"),
    status: normalizeText(prescription.status, "PRESCRIBED"),
    note: normalizeText(prescription.note, ""),
    patientName: normalizeText(prescription.patientName, ""),
    patientCode: normalizeText(prescription.patientCode, ""),
    items,
    medicineCount: items.length,
  };
};

export const mapNotification = (rawNotification) => {
  const notification = unwrapObject(rawNotification) || {};
  const read = Boolean(notification.readAt);

  return {
    id: notification.id,
    title: normalizeText(notification.title, "Thông báo"),
    content: normalizeText(notification.content, ""),
    type: normalizeText(notification.notificationType, "SYSTEM"),
    relatedId: notification.relatedId,
    createdAt: notification.createdAt || "",
    time: notification.createdAt ? formatShortDate(notification.createdAt) : "",
    read,
  };
};

export const mapInvoice = (rawInvoice) => {
  const invoice = unwrapObject(rawInvoice) || {};
  const paymentStatus = normalizeText(invoice.paymentStatus, "UNPAID");

  return {
    id: invoice.id || invoice.invoiceCode,
    invoiceCode: normalizeText(invoice.invoiceCode, invoice.id || ""),
    createdAt: invoice.createdAt || "",
    serviceAmount: normalizeCurrency(invoice.totalExamServiceAmount ?? invoice.totalServiceAmount),
    totalServiceAmount: normalizeCurrency(invoice.totalServiceAmount),
    medicineAmount: normalizeCurrency(invoice.totalMedicineAmount),
    testAmount: normalizeCurrency(invoice.totalTestAmount),
    discountAmount: normalizeCurrency(invoice.discountAmount),
    totalAmount: normalizeCurrency(invoice.totalAmount),
    paymentMethod: normalizeText(invoice.paymentMethod, ""),
    paymentStatus,
    status: paymentStatus,
    paidAt: normalizeText(invoice.paidAt, ""),
    note: normalizeText(invoice.note, ""),
    medicalRecordId: invoice.medicalRecordId,
    patientId: invoice.patientId,
  };
};

export const mapPaymentResult = (rawPayment) => {
  const payment = unwrapObject(rawPayment) || {};

  return {
    id: payment.id || payment.transactionCode,
    invoiceId: payment.invoiceId,
    invoiceCode: normalizeText(payment.invoiceCode, ""),
    amount: normalizeCurrency(payment.amount),
    paymentMethod: normalizeText(payment.paymentMethod, ""),
    paymentProvider: normalizeText(payment.paymentProvider, ""),
    transactionCode: normalizeText(payment.transactionCode, ""),
    paymentStatus: normalizeText(payment.paymentStatus, ""),
    paidAt: normalizeText(payment.paidAt, ""),
    createdAt: normalizeText(payment.createdAt, ""),
    updatedAt: normalizeText(payment.updatedAt, ""),
    active: payment.active ?? true,
    paymentUrl: normalizeText(payment.paymentUrl, ""),
  };
};

export const mapDoctor = (rawDoctor) => {
  const doctor = unwrapObject(rawDoctor) || {};
  const fullName = toDoctorName(doctor);

  return {
    id: doctor.id || doctor.doctorCode,
    doctorCode: normalizeText(doctor.doctorCode, doctor.id || ""),
    fullName,
    avatar: doctor.avatarUrl || createAvatar(fullName),
    qualification: normalizeText(doctor.qualification, ""),
    specialization: normalizeText(doctor.specialization, normalizeText(doctor.departmentName, "Chuyên khoa")),
    departmentId: doctor.departmentId?.id ?? doctor.departmentId ?? null,
    departmentName: normalizeText(doctor.departmentName, ""),
    workStatus: normalizeText(doctor.workStatus, "AVAILABLE"),
    active: doctor.active ?? true,
    rating: 4.6 + (String(doctor.id || doctor.doctorCode || "").length % 4) * 0.1,
  };
};

export const mapDoctorSchedule = (rawSchedule) => {
  const schedule = unwrapObject(rawSchedule) || {};
  const remainingSlots = Number(schedule.remainingSlots ?? Math.max((schedule.maxPatients || 0) - (schedule.bookedCount || 0), 0));
  const scheduleStatus = normalizeText(schedule.status, remainingSlots > 0 ? "AVAILABLE" : "FULL");

  return {
    id: schedule.id || `${schedule.workDate || ""}-${schedule.startTime || ""}`,
    doctorId: schedule.doctorId,
    workDate: schedule.workDate || "",
    startTime: normalizeTime(schedule.startTime),
    endTime: normalizeTime(schedule.endTime),
    displayDate: schedule.workDate ? formatShortDate(schedule.workDate) : "",
    displayRange: `${normalizeTime(schedule.startTime)} - ${normalizeTime(schedule.endTime)}`.trim(),
    maxPatients: Number(schedule.maxPatients || 0),
    bookedCount: Number(schedule.bookedCount || 0),
    remainingSlots,
    status: scheduleStatus,
    statusLabel: normalizeText(schedule.statusLabel, deriveStatusLabel(scheduleStatus)),
    note: normalizeText(schedule.note, ""),
  };
};

export const mapMedicalService = (rawService) => {
  const service = unwrapObject(rawService) || {};
  return {
    id: service.id || service.code,
    code: normalizeText(service.code, service.id || ""),
    name: normalizeText(service.name, "Dịch vụ"),
    description: normalizeText(service.description, ""),
    price: normalizeCurrency(service.price),
    serviceType: normalizeText(service.serviceType, ""),
    departmentId: service.departmentId?.id ?? service.departmentId ?? null,
    departmentName: normalizeText(service.departmentName, ""),
    active: service.active ?? true,
  };
};
