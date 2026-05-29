import { useEffect, useState } from "react";
import { Alert, Button, Card, Col, Row } from "react-bootstrap";
import { BsArrowLeft, BsCalendarCheck, BsPencilSquare, BsPersonCheck } from "react-icons/bs";
import { Link, useLocation, useNavigate, useParams } from "react-router-dom";
import ErrorState from "../../components/common/ErrorState";
import LoadingState from "../../components/common/LoadingState";
import StatusBadge from "../../components/common/StatusBadge";
import { checkInReceptionistAppointment, getReceptionistAppointmentDetail } from "../../services/receptionist/receptionistAppointmentApi";
import { appointmentStatusMeta, formatDate, formatTime, getErrorMessage, todayInputValue } from "./receptionistPageUtils";

const InfoRow = ({ label, value }) => (
  <div className="receptionist-info-row">
    <span>{label}</span>
    <strong>{value || "--"}</strong>
  </div>
);

function ReceptionistAppointmentDetailPage() {
  const { appointmentId } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const [appointment, setAppointment] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState(location.state?.notice || "");
  const [actionLoading, setActionLoading] = useState(false);

  const loadDetail = async () => {
    setLoading(true);
    setError("");

    try {
      const response = await getReceptionistAppointmentDetail(appointmentId);
      setAppointment(response.data);
    } catch (err) {
      if (err.response?.status === 401) {
        navigate("/login", { replace: true });
        return;
      }
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDetail();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [appointmentId]);

  const handleCheckIn = async () => {
    setActionLoading(true);
    setNotice("");
    try {
      const response = await checkInReceptionistAppointment(appointmentId, { note: "Tiếp nhận tại quầy" });
      setAppointment(response.data);
      setNotice("Đã check-in bệnh nhân.");
      await loadDetail();
    } catch (err) {
      if (err.response?.status === 401) {
        navigate("/login", { replace: true });
        return;
      }
      setError(getErrorMessage(err));
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) {
    return <LoadingState message="Đang tải chi tiết lịch hẹn..." />;
  }

  if (error && !appointment) {
    return <ErrorState message={error} onRetry={loadDetail} />;
  }

  if (!appointment) {
    return <ErrorState message="Không tìm thấy lịch hẹn." onRetry={loadDetail} />;
  }

  const patient = appointment.patient || {};
  const doctor = appointment.doctor || {};
  const service = appointment.service || {};
  const meta = appointmentStatusMeta(appointment.status);
  const canCheckIn = appointment.status === "BOOKED" && appointment.appointmentDate === todayInputValue();
  const canEdit = ["BOOKED", "WAITING"].includes(appointment.status);

  return (
    <>
      <div className="page-header receptionist-page-header">
        <div>
          <div className="page-eyebrow">Lễ tân / Chi tiết lịch hẹn</div>
          <h1>{appointment.appointmentCode}</h1>
          <p>Thông tin đầy đủ của lịch hẹn, trạng thái hồ sơ bệnh nhân và thao tác nhanh tại quầy.</p>
        </div>
        <div className="page-header-actions">
          <Button as={Link} to="/receptionist/appointments" type="button" variant="outline-primary">
            <BsArrowLeft /> Về danh sách
          </Button>
          <Button as={Link} to={`/receptionist/appointments/${appointment.id}/edit`} type="button" variant="outline-primary" disabled={!canEdit}>
            <BsPencilSquare /> Sửa lịch
          </Button>
          <Button type="button" onClick={handleCheckIn} disabled={!canCheckIn || actionLoading}>
            <BsPersonCheck /> Check-in
          </Button>
        </div>
      </div>

      {notice && <Alert variant="success">{notice}</Alert>}
      {error && <Alert variant="danger">{error}</Alert>}
      {!canCheckIn && appointment.status === "BOOKED" && (
        <Alert variant="warning">Lịch chỉ có thể check-in trong ngày hôm nay.</Alert>
      )}

      <section className="receptionist-detail-grid">
        <Card className="doctor-card receptionist-detail-card">
          <Card.Header>
            <h2>Thông tin bệnh nhân</h2>
          </Card.Header>
          <Card.Body>
            <InfoRow label="patientCode" value={patient.patientCode} />
            <InfoRow label="fullName" value={patient.fullName} />
            <InfoRow label="phone" value={patient.phone} />
            <InfoRow label="gender" value={patient.gender} />
            <InfoRow label="dateOfBirth" value={formatDate(patient.dateOfBirth)} />
            <InfoRow label="profileComplete" value={String(patient.profileComplete)} />
            <div className="receptionist-missing-fields">
              <span>missingFields</span>
              <strong>{Array.isArray(patient.missingFields) && patient.missingFields.length ? patient.missingFields.join(", ") : "--"}</strong>
            </div>
          </Card.Body>
        </Card>

        <Card className="doctor-card receptionist-detail-card">
          <Card.Header>
            <h2>Thông tin lịch hẹn</h2>
            <StatusBadge status={appointment.status} label={meta.label} />
          </Card.Header>
          <Card.Body>
            <InfoRow label="appointmentDate" value={formatDate(appointment.appointmentDate)} />
            <InfoRow label="startTime" value={formatTime(appointment.startTime)} />
            <InfoRow label="endTime" value={formatTime(appointment.endTime)} />
            <InfoRow label="doctor" value={doctor.fullName} />
            <InfoRow label="department" value={doctor.departmentName || appointment.departmentName} />
            <InfoRow label="service" value={service.name} />
            <InfoRow label="reason" value={appointment.reason} />
            <InfoRow label="symptomNote" value={appointment.symptomNote} />
            <InfoRow label="cancelReason" value={appointment.cancelReason} />
          </Card.Body>
        </Card>
      </section>
    </>
  );
}

export default ReceptionistAppointmentDetailPage;
