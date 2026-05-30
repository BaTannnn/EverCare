import { NavLink, useNavigate } from "react-router-dom";
import cookies from "react-cookies";

const menuItems = [
  { to: "/receptionist/dashboard", icon: "▦", label: "Dashboard" },
  { to: "/receptionist/appointments", icon: "▤", label: "Lịch hẹn" },
  { to: "/receptionist/invoices", icon: "¥", label: "Hóa đơn" },
];

function ReceptionistSidebar() {
  const navigate = useNavigate();

  const handleLogout = () => {
    cookies.remove("token", { path: "/" });
    cookies.remove("user", { path: "/" });
    cookies.remove("role", { path: "/" });
    navigate("/login", { replace: true });
  };

  return (
    <aside className="doctor-sidebar receptionist-sidebar">
      <div className="doctor-brand receptionist-brand">
        <strong>EverCare</strong>
        <span>Cổng lễ tân</span>
      </div>

      <nav className="doctor-nav">
        {menuItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.to === "/receptionist/dashboard"}
            className={({ isActive }) => `doctor-nav-link${isActive ? " active" : ""}`}
          >
            <span aria-hidden="true">{item.icon}</span>
            {item.label}
          </NavLink>
        ))}
      </nav>

      <div className="doctor-sidebar-footer">
        <div className="receptionist-note">
          <strong>Điều phối tại quầy</strong>
          <span>Quản lý lịch hẹn, check-in và ghi nhận thanh toán ngay trong cùng một giao diện.</span>
        </div>
        <button type="button" className="doctor-logout" onClick={handleLogout}>
          <span aria-hidden="true">←</span>
          Đăng xuất
        </button>
      </div>
    </aside>
  );
}

export default ReceptionistSidebar;
