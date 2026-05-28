import { NavLink, useNavigate } from "react-router-dom";
import cookies from "react-cookies";

const menuItems = [
  { to: "/pharmacist/dashboard", icon: "▦", label: "Tổng quan" },
  { to: "/pharmacist/prescriptions", icon: "Rx", label: "Đơn chờ cấp phát" },
  { to: "/pharmacist/medicines", icon: "□", label: "Thuốc" },
  { to: "/pharmacist/batches", icon: "▤", label: "Lô thuốc" },
  { to: "/pharmacist/inventory", icon: "↕", label: "Nhập / xuất kho" },
  { to: "/pharmacist/settings", icon: "⚙", label: "Cài đặt" },
];

function PharmacistSidebar() {
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
        <span>Cổng dược sĩ</span>
      </div>

      <nav className="doctor-nav">
        {menuItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.to === "/pharmacist/dashboard"}
            className={({ isActive }) => `doctor-nav-link${isActive ? " active" : ""}`}
          >
            <span aria-hidden="true">{item.icon}</span>
            {item.label}
          </NavLink>
        ))}
      </nav>

      <div className="doctor-sidebar-footer">
        <NavLink to="/pharmacist/settings" className="doctor-nav-link">
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

export default PharmacistSidebar;
