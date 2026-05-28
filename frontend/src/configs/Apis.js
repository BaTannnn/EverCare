import axios from "axios";
import cookies from "react-cookies";

const BASE_URL = "http://localhost:8080/backend/api/";

export const endpoints = {
  login: "/login",
  register: "/auth/register",
  profile: "/secure/profile",
  "doctor-dashboard-summary": "/doctor/dashboard/summary",
  "doctor-appointments-today": "/doctor/appointments/today",
  "doctor-appointments": "/doctor/appointments",
  "doctor-appointment-detail": (id) => `/doctor/appointments/${id}`,
  "doctor-start-examination": (id) => `/doctor/appointments/${id}/start-examination`,
  "doctor-schedules": "/doctor/schedules",
  "doctor-schedules-today": "/doctor/schedules/today",
  "doctor-medical-record": (id) => `/doctor/medical-records/${id}`,
  "doctor-medical-record-services": (id) => `/doctor/medical-records/${id}/services`,
  "doctor-medical-record-prescriptions": (id) => `/doctor/medical-records/${id}/prescriptions`,
  "doctor-prescription": (id) => `/doctor/prescriptions/${id}`,
  "staff-test-requests": "/staff/test-requests",
  "staff-test-request-detail": (recordId) => `/staff/test-requests/${recordId}`,
  "staff-test-results": "/staff/test-results",
  "staff-medical-record-test-results": (recordId) => `/staff/medical-records/${recordId}/test-results`,
  "staff-test-result": (id) => `/staff/test-results/${id}`,
  "patient-profile": "/secure/patients/profile",
  "patient-appointments": "/secure/appointments",
  "patient-appointment-detail": (id) => `/secure/appointments/${id}`,
  "patient-book-appointment": "/secure/appointments",
  "patient-medical-records": "/secure/patient/medical-records",
  "patient-medical-record-detail": (id) => `/secure/patient/medical-records/${id}`,
  "patient-test-results": "/secure/patient/test-results",
  "patient-prescriptions": "/secure/patient/prescriptions",
  "patient-prescription-detail": (id) => `/secure/patient/prescriptions/${id}`,
  "patient-notifications": "/secure/notifications",
  "patient-invoices": "/secure/patient/invoices",
  "patient-invoice-detail": (id) => `/secure/patient/invoices/${id}`,
  "medical-services": "/medical-services",
  doctors: "/doctors",
  medicines: "/medicines",
  "medicine-detail": (id) => `/medicines/${id}`,
};

export const authApis = () => {
  const token = cookies.load("token");

  return axios.create({
    baseURL: BASE_URL,
    headers: token
      ? {
          Authorization: `Bearer ${token}`,
        }
      : {},
  });
};

export default axios.create({
  baseURL: BASE_URL,
});
