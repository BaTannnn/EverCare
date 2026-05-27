import { NavLink, useNavigate } from "react-router-dom";
import cookies from "react-cookies";

const menuItems = [
  { to: "/doctor/dashboard", icon: "▦", label: "Tổng quan" },
  { to: "/doctor/appointments", icon: "□", label: "Lịch làm việc" },
  { to: "/doctor/patient-appointments", icon: "▤", label: "Lịch hẹn bệnh nhân" },
  { to: "/doctor/examination", icon: "+", label: "Khám bệnh" },
  { to: "/doctor/prescriptions", icon: "Rx", label: "Đơn thuốc" },
  { to: "/doctor/settings", icon: "⚙", label: "Cài đặt" },
];

function DoctorSidebar() {
  const navigate = useNavigate();

  const handleLogout = () => {
    cookies.remove("token", { path: "/" });
    cookies.remove("user", { path: "/" });
    cookies.remove("role", { path: "/" });
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
        <NavLink to="/doctor/settings" className="doctor-nav-link">
          <span aria-hidden="true">◎</span>
          Hồ sơ cá nhân
        </NavLink>
        <button type="button" className="doctor-logout" onClick={handleLogout}>
          <span aria-hidden="true">←</span>
          Đăng xuất
        </button>
      </div>
    </aside>
  );
}

export default DoctorSidebar;
