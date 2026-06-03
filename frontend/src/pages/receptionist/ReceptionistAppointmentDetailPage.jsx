import { useEffect, useState } from "react";
import { Alert, Button, Card } from "react-bootstrap";
import { BsArrowLeft, BsPencilSquare, BsPersonCheck } from "react-icons/bs";
import { Link, useLocation, useNavigate, useParams } from "react-router-dom";
import ErrorState from "../../components/common/ErrorState";
import LoadingState from "../../components/common/LoadingState";
import PageHeader from "../../components/common/PageHeader";
import StatusBadge from "../../components/common/StatusBadge";
import { checkInReceptionistAppointment, getReceptionistAppointmentDetail } from "../../services/receptionist/receptionistAppointmentApi";
import { appointmentStatusMeta, formatDate, formatDateTime, formatTime, getErrorMessage, todayInputValue } from "./receptionistPageUtils";

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
  const updatedAt = appointment.updatedAt || appointment.createdAt;

  return (
    <>
      <PageHeader
        eyebrow="Lễ tân / Chi tiết lịch hẹn"
        title={appointment.appointmentCode}
        description="Thông tin đầy đủ của lịch hẹn, trạng thái hồ sơ bệnh nhân và thao tác nhanh tại quầy."
        actions={(
          <>
            <Button as={Link} to="/receptionist/appointments" type="button" variant="outline-primary">
              <BsArrowLeft /> Về danh sách
            </Button>
            <Button as={Link} to={`/receptionist/appointments/${appointment.id}/edit`} type="button" variant="outline-primary" disabled={!canEdit}>
              <BsPencilSquare /> Sửa lịch
            </Button>
            <Button type="button" onClick={handleCheckIn} disabled={!canCheckIn || actionLoading}>
              <BsPersonCheck /> Check-in
            </Button>
          </>
        )}
      />

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
            <InfoRow label="Mã bệnh nhân" value={patient.patientCode} />
            <InfoRow label="Họ và tên" value={patient.fullName} />
            <InfoRow label="Số điện thoại" value={patient.phone} />
            <InfoRow label="Giới tính" value={patient.gender} />
            <InfoRow label="Ngày sinh" value={formatDate(patient.dateOfBirth)} />
            <InfoRow label="Email" value={patient.email} />
            <InfoRow label="CCCD / CMT" value={patient.citizenId} />
            <InfoRow label="Hồ sơ đầy đủ" value={patient.profileComplete ? "Đầy đủ" : "Thiếu thông tin"} />
            <InfoRow
              label="Thiếu thông tin"
              value={Array.isArray(patient.missingFields) && patient.missingFields.length ? patient.missingFields.join(", ") : "Không có"}
            />
          </Card.Body>
        </Card>

        <Card className="doctor-card receptionist-detail-card">
          <Card.Header>
            <h2>Thông tin lịch hẹn</h2>
            <StatusBadge status={appointment.status} label={meta.label} />
          </Card.Header>
          <Card.Body>
            <InfoRow label="Ngày khám" value={formatDate(appointment.appointmentDate)} />
            <InfoRow label="Giờ bắt đầu" value={formatTime(appointment.startTime)} />
            <InfoRow label="Giờ kết thúc" value={formatTime(appointment.endTime)} />
            <InfoRow label="Bác sĩ" value={doctor.fullName} />
            <InfoRow label="Khoa" value={doctor.departmentName || appointment.departmentName} />
            <InfoRow label="Dịch vụ" value={service.name} />
            <InfoRow label="Lý do khám" value={appointment.reason} />
            <InfoRow label="Triệu chứng" value={appointment.symptomNote} />
            <InfoRow label="Lý do hủy" value={appointment.cancelReason} />
            <InfoRow label="Cập nhật gần nhất" value={formatDateTime(updatedAt)} />
          </Card.Body>
        </Card>
      </section>
    </>
  );
}

export default ReceptionistAppointmentDetailPage;
