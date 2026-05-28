import { Form } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import cookies from "react-cookies";

const readSavedUser = () => {
  const savedUser = cookies.load("user");

  if (!savedUser) {
    return null;
  }

  if (typeof savedUser === "object") {
    return savedUser;
  }

  try {
    return JSON.parse(savedUser);
  } catch {
    return null;
  }
};

function StaffTopbar() {
  const navigate = useNavigate();
  const user = readSavedUser();

  const handleSubmit = (e) => {
    e.preventDefault();
    navigate("/staff/test-requests");
  };

  return (
    <header className="doctor-topbar">
      <Form className="doctor-search" onSubmit={handleSubmit}>
        <span aria-hidden="true">⌕</span>
        <Form.Control placeholder="Tìm chỉ định, mã bệnh án..." aria-label="Tìm kiếm chỉ định" />
      </Form>

      <div className="doctor-top-actions">
        <button type="button" className="icon-button" aria-label="Thông báo">
          !
        </button>
        <button type="button" className="icon-button" aria-label="Cài đặt" onClick={() => navigate("/staff/settings")}>
          ⚙
        </button>
        <div className="doctor-profile">
          <div className="doctor-avatar">{(user?.fullName || user?.username || "NV").slice(0, 2).toUpperCase()}</div>
          <div>
            <strong>{user?.fullName || user?.username || "Nhân viên y tế"}</strong>
            <span>Nhân viên y tế</span>
          </div>
        </div>
      </div>
    </header>
  );
}

export default StaffTopbar;
