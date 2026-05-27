/* eslint-disable react-hooks/set-state-in-effect */
import { useCallback, useEffect, useState } from "react";
import { Alert, Button, Card, Col, Form, Row } from "react-bootstrap";
import { useNavigate, useParams } from "react-router-dom";
import ErrorState from "../../components/common/ErrorState";
import LoadingState from "../../components/common/LoadingState";
import PageHeader from "../../components/common/PageHeader";
import StatusBadge from "../../components/common/StatusBadge";
import { getAppointmentDetail, startExamination } from "../../services/doctor/doctorAppointmentApi";
import {
  canEnterExamination,
  canStartExamination,
  formatDate,
  formatTime,
  getErrorMessage,
} from "./doctorPageUtils";

const InfoRow = ({ label, value }) => (
  <div className="info-row">
    <span>{label}</span>
    <strong>{value || "--"}</strong>
  </div>
);

function DoctorAppointmentDetailPage() {
  const { appointmentId } = useParams();
  const navigate = useNavigate();
  const [appointment, setAppointment] = useState(null);
  const [form, setForm] = useState({ chiefComplaint: "", initialNote: "" });
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const [formError, setFormError] = useState("");

  const loadAppointment = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      const response = await getAppointmentDetail(appointmentId);
      const detail = response.data;
      setAppointment(detail);
      setForm({
        chiefComplaint: detail?.medicalRecord?.chiefComplaint || "",
        initialNote: detail?.medicalRecord?.doctorNote || "",
      });
    } catch (err) {
      if (err.response?.status === 401) {
        navigate("/login", { replace: true });
        return;
      }

      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [appointmentId, navigate]);

  useEffect(() => {
    loadAppointment();
  }, [loadAppointment]);

  const updateField = (field, value) => {
    setForm((current) => ({ ...current, [field]: value }));
  };

  const handleStart = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setFormError("");

    try {
      await startExamination(appointmentId, form);
      navigate(`/doctor/examination/${appointmentId}`);
    } catch (err) {
      if (err.response?.status === 401) {
        navigate("/login", { replace: true });
        return;
      }

      setFormError(getErrorMessage(err));
      if (err.response?.status === 409) {
        loadAppointment();
      }
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return <LoadingState />;
  }

  if (error) {
    return <ErrorState message={error} onRetry={loadAppointment} />;
  }

  if (!appointment) {
    return <ErrorState message="Không tìm thấy lịch hẹn." />;
  }

  const patient = appointment.patient || {};
  const service = appointment.service || {};

  return (
    <>
      <PageHeader
        title={patient.fullName || "Chi tiết lịch hẹn"}
        description={`Mã lịch hẹn: ${appointment.appointmentCode || "--"}`}
        actions={<StatusBadge status={appointment.status} label={appointment.statusLabel} />}
      />

      <Row className="g-3">
        <Col lg={6}>
          <Card className="doctor-card h-100">
            <Card.Header>
              <h2>Thông tin bệnh nhân</h2>
            </Card.Header>
            <Card.Body>
              <InfoRow label="Mã bệnh nhân" value={patient.patientCode} />
              <InfoRow label="Họ tên" value={patient.fullName} />
              <InfoRow label="Giới tính" value={patient.gender} />
              <InfoRow label="Ngày sinh" value={formatDate(patient.dateOfBirth)} />
              <InfoRow label="Số điện thoại" value={patient.phone} />
              <InfoRow label="Email" value={patient.email} />
            </Card.Body>
          </Card>
        </Col>
        <Col lg={6}>
          <Card className="doctor-card h-100">
            <Card.Header>
              <h2>Thông tin lịch hẹn</h2>
            </Card.Header>
            <Card.Body>
              <InfoRow label="Ngày khám" value={formatDate(appointment.appointmentDate)} />
              <InfoRow label="Giờ bắt đầu" value={formatTime(appointment.startTime)} />
              <InfoRow label="Giờ kết thúc" value={formatTime(appointment.endTime)} />
              <InfoRow label="Dịch vụ" value={service.name} />
              <InfoRow label="Lý do khám" value={appointment.reason} />
              <InfoRow label="Ghi chú triệu chứng" value={appointment.symptomNote} />
            </Card.Body>
          </Card>
        </Col>
      </Row>

      <Card className="doctor-card">
        <Card.Header>
          <h2>Trạng thái luồng khám</h2>
        </Card.Header>
        <Card.Body>
          <div className="flow-steps">
            {["BOOKED", "WAITING", "IN_PROGRESS", "COMPLETED"].map((status) => (
              <div className={`flow-step${appointment.status === status ? " active" : ""}`} key={status}>
                <StatusBadge status={status} />
              </div>
            ))}
          </div>
        </Card.Body>
      </Card>

      {canStartExamination(appointment.status) && (
        <Card className="doctor-card">
          <Card.Header>
            <h2>Bắt đầu khám</h2>
          </Card.Header>
          <Card.Body>
            {formError && <Alert variant="danger">{formError}</Alert>}
            <Form onSubmit={handleStart}>
              <Form.Group className="mb-3" controlId="chiefComplaint">
                <Form.Label>Chief complaint</Form.Label>
                <Form.Control
                  as="textarea"
                  rows={3}
                  value={form.chiefComplaint}
                  onChange={(e) => updateField("chiefComplaint", e.target.value)}
                  disabled={submitting}
                />
              </Form.Group>
              <Form.Group className="mb-3" controlId="initialNote">
                <Form.Label>Initial note</Form.Label>
                <Form.Control
                  as="textarea"
                  rows={3}
                  value={form.initialNote}
                  onChange={(e) => updateField("initialNote", e.target.value)}
                  disabled={submitting}
                />
              </Form.Group>
              <Button type="submit" disabled={submitting}>
                {submitting ? "Đang bắt đầu..." : "Bắt đầu khám"}
              </Button>
            </Form>
          </Card.Body>
        </Card>
      )}

      {canEnterExamination(appointment.status) && (
        <Card className="doctor-card">
          <Card.Body className="action-card-body">
            <div>
              <h2>{appointment.status === "COMPLETED" ? "Hồ sơ đã hoàn tất" : "Ca khám đang diễn ra"}</h2>
              <p>
                {appointment.status === "COMPLETED"
                  ? "Bạn có thể xem lại hồ sơ ở chế độ chỉ đọc."
                  : "Tiếp tục cập nhật bệnh án, chỉ định và đơn thuốc."}
              </p>
            </div>
            <Button type="button" onClick={() => navigate(`/doctor/examination/${appointmentId}`)}>
              {appointment.status === "COMPLETED" ? "Xem lại hồ sơ" : "Vào phòng khám"}
            </Button>
          </Card.Body>
        </Card>
      )}
    </>
  );
}

export default DoctorAppointmentDetailPage;
