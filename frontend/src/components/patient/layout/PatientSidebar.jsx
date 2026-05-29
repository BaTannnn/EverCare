import cookies from "react-cookies";
import { useNavigate, NavLink } from "react-router-dom";
import {
  BsCalendar2Plus,
  BsCalendarCheck,
  BsClipboard2Pulse,
  BsCreditCard2Front,
  BsFileEarmarkMedical,
  BsGrid1X2,
  BsBell,
  BsPerson,
  BsBoxArrowRight,
  BsFlask,
  BsCapsule,
} from "react-icons/bs";
import { getAvatarSource } from "../../../pages/patient/patientPageUtils";

const navItems = [
  { to: "/patient/dashboard", label: "Tổng quan", icon: BsGrid1X2, end: true },
  { to: "/patient/book-appointment", label: "Đặt lịch khám", icon: BsCalendar2Plus },
  { to: "/patient/appointments", label: "Lịch hẹn", icon: BsCalendarCheck },
  { to: "/patient/medical-records", label: "Hồ sơ bệnh án", icon: BsFileEarmarkMedical },
  { to: "/patient/test-results", label: "Kết quả xét nghiệm", icon: BsFlask },
  { to: "/patient/prescriptions", label: "Đơn thuốc", icon: BsCapsule },
  { to: "/patient/invoices", label: "Hóa đơn", icon: BsCreditCard2Front },
  { to: "/patient/notifications", label: "Thông báo", icon: BsBell },
  { to: "/patient/profile", label: "Hồ sơ cá nhân", icon: BsPerson },
];

function PatientSidebar({ profile }) {
  const navigate = useNavigate();
  const avatarSource = getAvatarSource(profile, profile?.fullName || "Bệnh nhân EverCare");

  const handleLogout = () => {
    cookies.remove("token", { path: "/" });
    cookies.remove("role", { path: "/" });
    cookies.remove("user", { path: "/" });
    navigate("/login", { replace: true });
  };

  return (
    <aside className="patient-sidebar">
      <div>
        <div className="patient-brand">
          <div className="patient-brand-mark">EverCare</div>
          <p>Cổng thông tin bệnh nhân</p>
        </div>

        <nav className="patient-nav">
          {navItems.map(({ to, label, icon: Icon, end }) => (
            <NavLink key={to} to={to} end={end} className={({ isActive }) => `patient-nav-link ${isActive ? "active" : ""}`}>
              <Icon aria-hidden="true" />
              <span>{label}</span>
            </NavLink>
          ))}
        </nav>
      </div>

      <div className="patient-sidebar-footer">
        <div className="patient-profile-chip">
          <img
            src={avatarSource}
            alt={profile?.fullName}
            onError={(event) => {
              event.currentTarget.src = getAvatarSource(null, profile?.fullName || "Bệnh nhân EverCare");
            }}
          />
          <div>
            <strong>{profile?.fullName}</strong>
            <span>ID: {profile?.patientCode}</span>
          </div>
        </div>

        <button type="button" className="patient-logout-btn" onClick={handleLogout}>
          <BsBoxArrowRight aria-hidden="true" />
          <span>Đăng xuất</span>
        </button>
      </div>
    </aside>
  );
}

export default PatientSidebar;
