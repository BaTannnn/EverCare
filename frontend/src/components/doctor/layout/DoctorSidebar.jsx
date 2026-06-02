import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../../../contexts/useAuth";

const menuItems = [
  { to: "/doctor/dashboard", icon: "▦", label: "Tổng quan" },
  { to: "/doctor/appointments", icon: "□", label: "Lịch làm việc" },
  { to: "/doctor/patient-appointments", icon: "▤", label: "Lịch hẹn bệnh nhân" },
  { to: "/doctor/examination", icon: "+", label: "Khám bệnh" },
  { to: "/doctor/prescriptions", icon: "Rx", label: "Đơn thuốc" },
];

function DoctorSidebar() {
  const navigate = useNavigate();
  const { logout } = useAuth();

  const handleLogout = () => {
    logout();
    navigate("/login", { replace: true });
  };

  return (
    <aside className="doctor-sidebar">
      <div className="doctor-brand">
        <strong>EverCare</strong>
        <span>Cổng bác sĩ</span>
      </div>

      <nav className="doctor-nav">
        {menuItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.to === "/doctor/dashboard"}
            className={({ isActive }) => `doctor-nav-link${isActive ? " active" : ""}`}
          >
            <span aria-hidden="true">{item.icon}</span>
            {item.label}
          </NavLink>
        ))}
      </nav>

      <div className="doctor-sidebar-footer">
        <button type="button" className="doctor-logout" onClick={handleLogout}>
          <span aria-hidden="true">←</span>
          Đăng xuất
        </button>
      </div>
    </aside>
  );
}

export default DoctorSidebar;
