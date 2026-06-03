import { useState } from "react";
import { Button, Form } from "react-bootstrap";
import { BsBell, BsSearch } from "react-icons/bs";
import { useNavigate } from "react-router-dom";
import { getAvatarSource } from "../../../pages/patient/patientPageUtils";

function PatientTopBar({ title, subtitle, searchPlaceholder = "Tìm kiếm dịch vụ, bác sĩ...", profile, unreadCount = 0 }) {
  const navigate = useNavigate();
  const [query, setQuery] = useState("");
  const avatarSource = getAvatarSource(profile, profile?.fullName || "Bệnh nhân EverCare");

  const handleSubmit = (e) => {
    e.preventDefault();
  };

  return (
    <header className="patient-topbar">
      <div className="patient-topbar-copy">
        <div>
          <h1>{title}</h1>
          {subtitle && <p>{subtitle}</p>}
        </div>
      </div>

      <Form className="patient-search" onSubmit={handleSubmit}>
        <BsSearch aria-hidden="true" />
        <Form.Control
          type="search"
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          placeholder={searchPlaceholder}
        />
      </Form>

      <button type="button" className="patient-notification-btn" onClick={() => navigate("/patient/notifications")}>
        <BsBell aria-hidden="true" />
        {unreadCount > 0 && <span className="patient-notification-dot">{unreadCount > 9 ? "9+" : unreadCount}</span>}
      </button>

      <Button type="button" className="patient-book-btn" onClick={() => navigate("/patient/book-appointment")}>
        Đặt lịch
      </Button>

      <div className="patient-user-chip">
        <div className="patient-user-meta">
          <strong>{profile?.fullName}</strong>
          <span>{profile?.patientCode}</span>
        </div>
        <img
          src={avatarSource}
          alt={profile?.fullName}
          onError={(event) => {
            event.currentTarget.src = getAvatarSource(null, profile?.fullName || "Bệnh nhân EverCare");
          }}
        />
      </div>
    </header>
  );
}

export default PatientTopBar;
