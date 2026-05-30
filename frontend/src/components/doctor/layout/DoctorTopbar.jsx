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

function DoctorTopbar() {
  const navigate = useNavigate();
  const user = readSavedUser();
  const [keyword, setKeyword] = useState("");

  const handleSubmit = (e) => {
    e.preventDefault();
    navigate("/doctor/appointments", {
      state: { search: keyword.trim() },
    });
  };

  return (
    <header className="doctor-topbar">
      <Form className="doctor-search" onSubmit={handleSubmit}>
        <span aria-hidden="true">⌕</span>
        <Form.Control
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          placeholder="Tìm kiếm bệnh nhân, mã lịch..."
          aria-label="Tìm kiếm lịch hẹn"
        />
      </Form>

      <div className="doctor-top-actions">
        <button type="button" className="icon-button" aria-label="Thông báo">
          !
        </button>
        <div className="doctor-profile">
          <div className="doctor-avatar">{(user?.fullName || user?.username || "BS").slice(0, 2).toUpperCase()}</div>
          <div>
            <strong>{user?.fullName || user?.username || "Bác sĩ"}</strong>
            <span>Bác sĩ</span>
          </div>
        </div>
      </div>
    </header>
  );
}

export default DoctorTopbar;
