import { unwrapObject, normalizeNumber, normalizeText } from "./receptionistApiUtils";

const mapPatient = (patient = {}) => ({
  id: patient.id || patient.patientId || null,
  patientCode: normalizeText(patient.patientCode, ""),
  fullName: normalizeText(patient.fullName, ""),
  phone: normalizeText(patient.phone, ""),
  gender: normalizeText(patient.gender, ""),
  dateOfBirth: normalizeText(patient.dateOfBirth, ""),
  profileComplete: Boolean(patient.profileComplete),
  missingFields: Array.isArray(patient.missingFields) ? patient.missingFields : [],
});

const mapDoctor = (doctor = {}) => ({
  id: doctor.id || doctor.doctorId || null,
  doctorCode: normalizeText(doctor.doctorCode, ""),
  fullName: normalizeText(doctor.fullName || doctor.doctorName, ""),
  departmentId: doctor.departmentId || null,
  departmentName: normalizeText(doctor.departmentName, ""),
  workStatus: normalizeText(doctor.workStatus, ""),
});

const mapService = (service = {}) => ({
  id: service.id || service.serviceId || null,
  code: normalizeText(service.code, ""),
  name: normalizeText(service.name, ""),
  price: normalizeNumber(service.price),
});

export const mapReceptionistAppointment = (rawAppointment) => {
  const appointment = unwrapObject(rawAppointment);

  return {
    id: appointment.id || appointment.appointmentId || null,
    appointmentCode: normalizeText(appointment.appointmentCode, ""),
    appointmentDate: normalizeText(appointment.appointmentDate, ""),
    startTime: normalizeText(appointment.startTime, ""),
    endTime: normalizeText(appointment.endTime, ""),
    status: normalizeText(appointment.status, "BOOKED"),
    statusLabel: normalizeText(appointment.statusLabel, ""),
    reason: normalizeText(appointment.reason, ""),
    symptomNote: normalizeText(appointment.symptomNote, ""),
    cancelReason: normalizeText(appointment.cancelReason, ""),
    doctorId: appointment.doctorId || appointment.doctor?.id || null,
    departmentId: appointment.departmentId || appointment.doctor?.departmentId || null,
    serviceId: appointment.serviceId || appointment.service?.id || null,
    patient: mapPatient(appointment.patient || {}),
    doctor: mapDoctor(appointment.doctor || {
      id: appointment.doctorId,
      doctorCode: appointment.doctorCode,
      fullName: appointment.doctorName,
      departmentId: appointment.departmentId,
      departmentName: appointment.departmentName,
      workStatus: appointment.workStatus,
    }),
    service: mapService(appointment.service || {
      id: appointment.serviceId,
      code: appointment.serviceCode,
      name: appointment.serviceName,
      price: appointment.servicePrice,
    }),
  };
};

export const mapReceptionistAppointmentDetail = mapReceptionistAppointment;

