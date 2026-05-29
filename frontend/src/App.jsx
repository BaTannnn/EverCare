import { Navigate, Route, Routes } from "react-router-dom";
import PatientLayout from "./components/patient/layout/PatientLayout";
import DoctorLayout from "./components/doctor/layout/DoctorLayout";
import PharmacistLayout from "./components/pharmacist/layout/PharmacistLayout";
import StaffLayout from "./components/staff/layout/StaffLayout";
import AdminDashboard from "./pages/AdminDashboard";
import DoctorAppointmentDetailPage from "./pages/doctor/DoctorAppointmentDetailPage";
import DoctorAppointmentsPage from "./pages/doctor/DoctorAppointmentsPage";
import DoctorDashboardPage from "./pages/doctor/DoctorDashboardPage";
import DoctorExaminationEntryPage from "./pages/doctor/DoctorExaminationEntryPage";
import DoctorPatientAppointmentsPage from "./pages/doctor/DoctorPatientAppointmentsPage";
import DoctorPrescriptionsPage from "./pages/doctor/DoctorPrescriptionsPage";
import DoctorSettingsPage from "./pages/doctor/DoctorSettingsPage";
import ExaminationWorkspacePage from "./pages/doctor/ExaminationWorkspacePage";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import StaffDashboardPage from "./pages/staff/StaffDashboardPage";
import StaffSettingsPage from "./pages/staff/StaffSettingsPage";
import StaffTestRequestDetailPage from "./pages/staff/StaffTestRequestDetailPage";
import StaffTestRequestsPage from "./pages/staff/StaffTestRequestsPage";
import StaffTestResultsPage from "./pages/staff/StaffTestResultsPage";
import PatientBookAppointment from "./pages/patient/PatientBookAppointment";
import PatientInvoices from "./pages/patient/PatientInvoices";
import PatientMedicalRecordDetail from "./pages/patient/PatientMedicalRecordDetail";
import PatientMedicalRecords from "./pages/patient/PatientMedicalRecords";
import PatientNotifications from "./pages/patient/PatientNotifications";
import PatientAppointments from "./pages/patient/PatientAppointments";
import PatientDashboard from "./pages/patient/PatientDashboard";
import PatientProfile from "./pages/patient/PatientProfile";
import PatientPrescriptions from "./pages/patient/PatientPrescriptions";
import PatientTestResults from "./pages/patient/PatientTestResults";
import PaymentResultPage from "./pages/payment/PaymentResultPage";
import PharmacistBatchesPage from "./pages/pharmacist/PharmacistBatchesPage";
import PharmacistDashboardPage from "./pages/pharmacist/PharmacistDashboardPage";
import PharmacistInventoryPage from "./pages/pharmacist/PharmacistInventoryPage";
import PharmacistMedicinesPage from "./pages/pharmacist/PharmacistMedicinesPage";
import PharmacistPrescriptionDetailPage from "./pages/pharmacist/PharmacistPrescriptionDetailPage";
import PharmacistPrescriptionsPage from "./pages/pharmacist/PharmacistPrescriptionsPage";
import PharmacistSettingsPage from "./pages/pharmacist/PharmacistSettingsPage";
import ProtectedRoute from "./routes/ProtectedRoute";
import PublicOnlyRoute from "./routes/PublicOnlyRoute";
import "./App.css";

function App() {
  return (
    <Routes>
      <Route element={<PublicOnlyRoute />}>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
      </Route>

      <Route element={<ProtectedRoute />}>
        <Route path="/admin/dashboard" element={<AdminDashboard />} />
      </Route>

      <Route element={<ProtectedRoute roles={["PATIENT", "ROLE_PATIENT"]} />}>
        <Route path="/patient" element={<PatientLayout />}>
          <Route index element={<Navigate to="/patient/dashboard" replace />} />
          <Route path="dashboard" element={<PatientDashboard />} />
          <Route path="profile" element={<PatientProfile />} />
          <Route path="book-appointment" element={<PatientBookAppointment />} />
          <Route path="appointments" element={<PatientAppointments />} />
          <Route path="medical-records" element={<PatientMedicalRecords />} />
          <Route path="medical-records/:id" element={<PatientMedicalRecordDetail />} />
          <Route path="test-results" element={<PatientTestResults />} />
          <Route path="prescriptions" element={<PatientPrescriptions />} />
          <Route path="invoices" element={<PatientInvoices />} />
          <Route path="notifications" element={<PatientNotifications />} />
        </Route>
        <Route path="/payment/:provider/result" element={<PaymentResultPage />} />
      </Route>

      <Route element={<ProtectedRoute roles={["DOCTOR", "ROLE_DOCTOR"]} />}>
        <Route path="/doctor" element={<DoctorLayout />}>
          <Route index element={<Navigate to="/doctor/dashboard" replace />} />
          <Route path="dashboard" element={<DoctorDashboardPage />} />
          <Route path="appointments" element={<DoctorAppointmentsPage />} />
          <Route path="appointments/:appointmentId" element={<DoctorAppointmentDetailPage />} />
          <Route path="patient-appointments" element={<DoctorPatientAppointmentsPage />} />
          <Route path="examination" element={<DoctorExaminationEntryPage />} />
          <Route path="examination/:appointmentId" element={<ExaminationWorkspacePage />} />
          <Route path="prescriptions" element={<DoctorPrescriptionsPage />} />
          <Route path="settings" element={<DoctorSettingsPage />} />
        </Route>
      </Route>

      <Route element={<ProtectedRoute roles={["PHARMACIST", "ROLE_PHARMACIST"]} />}>
        <Route path="/pharmacist" element={<PharmacistLayout />}>
          <Route index element={<Navigate to="/pharmacist/dashboard" replace />} />
          <Route path="dashboard" element={<PharmacistDashboardPage />} />
          <Route path="prescriptions" element={<PharmacistPrescriptionsPage />} />
          <Route path="prescriptions/:prescriptionId" element={<PharmacistPrescriptionDetailPage />} />
          <Route path="medicines" element={<PharmacistMedicinesPage />} />
          <Route path="batches" element={<PharmacistBatchesPage />} />
          <Route path="inventory" element={<PharmacistInventoryPage />} />
          <Route path="settings" element={<PharmacistSettingsPage />} />
        </Route>
      </Route>

      <Route element={<ProtectedRoute roles={["LAB_TECH", "ROLE_LAB_TECH", "RECEPTIONIST", "ROLE_RECEPTIONIST", "CASHIER", "ROLE_CASHIER", "MANAGER", "ROLE_MANAGER"]} />}>
        <Route path="/staff" element={<StaffLayout />}>
          <Route index element={<Navigate to="/staff/dashboard" replace />} />
          <Route path="dashboard" element={<StaffDashboardPage />} />
          <Route path="test-requests" element={<StaffTestRequestsPage />} />
          <Route path="test-requests/:recordId" element={<StaffTestRequestDetailPage />} />
          <Route path="test-results" element={<StaffTestResultsPage />} />
          <Route path="settings" element={<StaffSettingsPage />} />
        </Route>
      </Route>

      <Route path="/" element={<Navigate to="/doctor/dashboard" replace />} />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}

export default App;
