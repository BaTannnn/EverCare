import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../../../contexts/useAuth";

const menuItems = [
  { to: "/staff/dashboard", icon: "▦", label: "Tổng quan", active: (path) => path === "/staff/dashboard" },
  { to: "/staff/test-requests", icon: "□", label: "Chỉ định chờ xử lý", active: (path) => path === "/staff/test-requests" },
  { to: "/staff/test-results", icon: "▤", label: "Lịch sử kết quả", active: (path) => path === "/staff/test-results" },
];

function StaffSidebar() {
  const navigate = useNavigate();
  const location = useLocation();
  const { logout } = useAuth();

  const handleLogout = () => {
    logout();
    navigate("/login", { replace: true });
  };

  return (
    <aside className="doctor-sidebar">
      <div className="doctor-brand">
        <strong>EverCare</strong>
        <span>Cổng nhân viên y tế</span>
      </div>

      <nav className="doctor-nav">
        {menuItems.map((item, index) => (
          <Link
            key={`${item.to}-${index}`}
            to={item.to}
            className={`doctor-nav-link${item.active(location.pathname) ? " active" : ""}`}
          >
            <span aria-hidden="true">{item.icon}</span>
            {item.label}
          </Link>
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

export default StaffSidebar;
