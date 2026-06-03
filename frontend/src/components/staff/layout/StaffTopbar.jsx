import { Form } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../../contexts/useAuth";

function StaffTopbar() {
  const navigate = useNavigate();
  const { user } = useAuth();

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
