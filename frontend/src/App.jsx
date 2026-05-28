import { Navigate, Route, Routes } from "react-router-dom";
import PatientLayout from "./components/patient/layout/PatientLayout";
import DoctorLayout from "./components/doctor/layout/DoctorLayout";
import StaffLayout from "./components/staff/layout/StaffLayout";
import AdminDashboard from "./pages/AdminDashboard";
import DoctorAppointmentDetailPage from "./pages/doctor/DoctorAppointmentDetailPage";
import DoctorAppointmentsPage from "./pages/doctor/DoctorAppointmentsPage";
import DoctorDashboardPage from "./pages/doctor/DoctorDashboardPage";
import DoctorExaminationIndexPage from "./pages/doctor/DoctorExaminationIndexPage";
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
import BookAppointment from "./pages/patient/BookAppointment";
import Invoices from "./pages/patient/Invoices";
import MedicalRecordDetail from "./pages/patient/MedicalRecordDetail";
import MedicalRecords from "./pages/patient/MedicalRecords";
import Notifications from "./pages/patient/Notifications";
import PatientAppointments from "./pages/patient/PatientAppointments";
import PatientDashboard from "./pages/patient/PatientDashboard";
import PatientProfile from "./pages/patient/PatientProfile";
import Prescriptions from "./pages/patient/Prescriptions";
import TestResults from "./pages/patient/TestResults";
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
          <Route path="book-appointment" element={<BookAppointment />} />
          <Route path="appointments" element={<PatientAppointments />} />
          <Route path="medical-records" element={<MedicalRecords />} />
          <Route path="medical-records/:id" element={<MedicalRecordDetail />} />
          <Route path="test-results" element={<TestResults />} />
          <Route path="prescriptions" element={<Prescriptions />} />
          <Route path="invoices" element={<Invoices />} />
          <Route path="notifications" element={<Notifications />} />
        </Route>
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

      <Route element={<ProtectedRoute roles={["LAB_TECH", "ROLE_LAB_TECH"]} />}>
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
