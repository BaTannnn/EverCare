import { Navigate, Route, Routes } from "react-router-dom";
import DoctorLayout from "./components/doctor/layout/DoctorLayout";
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

      <Route element={<ProtectedRoute roles={["DOCTOR", "ROLE_DOCTOR"]} />}>
        <Route path="/doctor" element={<DoctorLayout />}>
          <Route index element={<Navigate to="/doctor/dashboard" replace />} />
          <Route path="dashboard" element={<DoctorDashboardPage />} />
          <Route path="appointments" element={<DoctorAppointmentsPage />} />
          <Route path="appointments/:appointmentId" element={<DoctorAppointmentDetailPage />} />
          <Route path="patient-appointments" element={<DoctorPatientAppointmentsPage />} />
          <Route path="examination" element={<DoctorExaminationIndexPage />} />
          <Route path="examination/:appointmentId" element={<ExaminationWorkspacePage />} />
          <Route path="prescriptions" element={<DoctorPrescriptionsPage />} />
          <Route path="settings" element={<DoctorSettingsPage />} />
        </Route>
      </Route>

      <Route path="/" element={<Navigate to="/doctor/dashboard" replace />} />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}

export default App;
