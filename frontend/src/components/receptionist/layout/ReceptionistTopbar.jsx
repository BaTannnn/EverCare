import { useState } from "react";
import { Form } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../../contexts/useAuth";

function ReceptionistTopbar() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [keyword, setKeyword] = useState("");

  const handleSubmit = (e) => {
    e.preventDefault();
    const value = keyword.trim();
    if (!value) {
      navigate("/receptionist/appointments");
      return;
    }
    navigate(`/receptionist/appointments?keyword=${encodeURIComponent(value)}`);
  };

  return (
    <header className="doctor-topbar receptionist-topbar">
      <Form className="doctor-search" onSubmit={handleSubmit}>
        <span aria-hidden="true">⌕</span>
        <Form.Control
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          placeholder="Tìm lịch hẹn, bệnh nhân, số điện thoại..."
          aria-label="Tìm nhanh lịch hẹn"
        />
      </Form>

      <div className="doctor-top-actions">
        <button type="button" className="icon-button" aria-label="Mở dashboard" onClick={() => navigate("/receptionist/dashboard")}>
          ↻
        </button>
        <div className="doctor-profile">
          <div className="doctor-avatar">{(user?.fullName || user?.username || "LT").slice(0, 2).toUpperCase()}</div>
          <div>
            <strong>{user?.fullName || user?.username || "Lễ tân"}</strong>
            <span>Cổng lễ tân</span>
          </div>
        </div>
      </div>
    </header>
  );
}

export default ReceptionistTopbar;
