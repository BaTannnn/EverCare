import { useState } from "react";
import { Form } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../../contexts/useAuth";

function PharmacistTopbar() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [keyword, setKeyword] = useState("");

  const handleSubmit = (e) => {
    e.preventDefault();
    navigate("/pharmacist/prescriptions", { state: { search: keyword.trim() } });
  };

  return (
    <header className="doctor-topbar">
      <Form className="doctor-search" onSubmit={handleSubmit}>
        <span aria-hidden="true">⌕</span>
        <Form.Control
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          placeholder="Tìm mã đơn, bệnh nhân..."
          aria-label="Tìm đơn thuốc"
        />
      </Form>

      <div className="doctor-top-actions">
        <div className="doctor-profile">
          <div className="doctor-avatar">{(user?.fullName || user?.username || "DS").slice(0, 2).toUpperCase()}</div>
          <div>
            <strong>{user?.fullName || user?.username || "Dược sĩ"}</strong>
            <span>Dược sĩ</span>
          </div>
        </div>
      </div>
    </header>
  );
}

export default PharmacistTopbar;
