import { useState } from "react";
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

function PharmacistTopbar() {
  const navigate = useNavigate();
  const user = readSavedUser();
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
        <button type="button" className="icon-button" aria-label="Cài đặt" onClick={() => navigate("/pharmacist/settings")}>
          ⚙
        </button>
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
