import { Card } from "react-bootstrap";
import StatusBadge from "../../common/StatusBadge";
import { formatDate } from "../../../pages/doctor/doctorPageUtils";

function ExamPatientBanner({ appointment }) {
  const patient = appointment.patient || {};
  const appointmentService = appointment.service || {};

  return (
    <Card className="exam-patient-banner">
      <Card.Body>
        <div className="exam-avatar">◎</div>
        <div className="exam-patient-title">
          <h2>{patient.fullName || "Bệnh nhân"}</h2>
          <span>
            ID: {appointment.appointmentCode || appointment.id} · Ngày: {formatDate(appointment.appointmentDate)}
          </span>
        </div>
        <div className="exam-banner-meta">
          <span>Dịch vụ</span>
          <strong>{appointmentService.name || "--"}</strong>
        </div>
        <div className="exam-banner-meta">
          <span>Trạng thái</span>
          <StatusBadge status={appointment.status} label={appointment.statusLabel} />
        </div>
      </Card.Body>
    </Card>
  );
}

export default ExamPatientBanner;
