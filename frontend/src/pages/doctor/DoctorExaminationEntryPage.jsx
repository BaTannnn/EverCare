/* eslint-disable react-hooks/set-state-in-effect */
import { useCallback, useEffect, useMemo, useState } from "react";
import { Alert, Button, Card, Col, Container, Form, Modal, Row, Spinner, Table } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import EmptyState from "../../components/common/EmptyState";
import StatusBadge from "../../components/common/StatusBadge";
import { getTodayAppointments, startExamination } from "../../services/doctor/doctorAppointmentApi";
import { formatDate, formatTime, getErrorMessage, isDoctorVisibleAppointment } from "./doctorPageUtils";

const emptyStartForm = {
  chiefComplaint: "",
  initialNote: "",
};

function DoctorExaminationEntryPage() {
  const navigate = useNavigate();
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [startError, setStartError] = useState("");
  const [starting, setStarting] = useState(false);
  const [selectedAppointment, setSelectedAppointment] = useState(null);
  const [startForm, setStartForm] = useState(emptyStartForm);

  const loadAppointments = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      const response = await getTodayAppointments();
      setAppointments((response.data || []).filter(isDoctorVisibleAppointment));
    } catch (err) {
      if (err.response?.status === 401) {
        navigate("/login", { replace: true });
        return;
      }

      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [navigate]);

  useEffect(() => {
    loadAppointments();
  }, [loadAppointments]);

  const inProgressAppointments = useMemo(
    () => appointments.filter((appointment) => appointment.status === "IN_PROGRESS"),
    [appointments]
  );

  const waitingAppointments = useMemo(
    () => appointments.filter((appointment) => appointment.status === "WAITING"),
    [appointments]
  );

  const openStartModal = (appointment) => {
    setSelectedAppointment(appointment);
    setStartForm({
      chiefComplaint: appointment.reason || "",
      initialNote: appointment.symptomNote || "",
    });
    setStartError("");
  };

  const closeStartModal = () => {
    if (starting) {
      return;
    }

    setSelectedAppointment(null);
    setStartForm(emptyStartForm);
    setStartError("");
  };

  const updateStartField = (field, value) => {
    setStartForm((current) => ({ ...current, [field]: value }));
  };

  const handleStartExamination = async (e) => {
    e.preventDefault();

    if (!selectedAppointment?.id) {
      return;
    }

    setStarting(true);
    setStartError("");

    try {
      await startExamination(selectedAppointment.id, startForm);
      navigate(`/doctor/examination/${selectedAppointment.id}`);
    } catch (err) {
      if (err.response?.status === 401) {
        navigate("/login", { replace: true });
        return;
      }

      setStartError(getErrorMessage(err));
      if (err.response?.status === 409) {
        loadAppointments();
      }
    } finally {
      setStarting(false);
    }
  };

  const hasEntryAppointments = inProgressAppointments.length > 0 || waitingAppointments.length > 0;

  return (
    <Container fluid className="px-0">
      <div className="exam-page-header">
        <div className="doctor-breadcrumb">
          Bác sĩ <span>/</span> <strong>Khám bệnh</strong>
        </div>
        <h1>Khám bệnh</h1>
        <p>Chọn một lịch hẹn đang chờ hoặc đang khám để vào khu vực khám.</p>
      </div>

      <Card className="doctor-card mb-3">
        <Card.Body className="action-card-body">
          <div>
            <h2>Vào ca khám từ lịch hẹn</h2>
            <p>Khu vực khám bệnh cần một mã lịch hẹn để tải bệnh nhân, bệnh án, chỉ định và đơn thuốc.</p>
          </div>
          <Button type="button" onClick={() => navigate("/doctor/appointments")}>
            Xem lịch hẹn hôm nay
          </Button>
        </Card.Body>
      </Card>

      {loading ? (
        <Card className="doctor-card">
          <Card.Body className="text-center py-5">
            <Spinner animation="border" role="status" />
            <div className="mt-3">Đang tải lịch hẹn hôm nay...</div>
          </Card.Body>
        </Card>
      ) : error ? (
        <Alert variant="danger">
          <div className="d-flex align-items-center justify-content-between gap-3">
            <span>{error}</span>
            <Button type="button" variant="outline-danger" onClick={loadAppointments}>
              Thử lại
            </Button>
          </div>
        </Alert>
      ) : !hasEntryAppointments ? (
        <Card className="doctor-card">
          <Card.Body>
            <EmptyState
              title="Hiện chưa có bệnh nhân nào đang chờ khám."
              description="Không có ca đang khám hoặc bệnh nhân chờ trong hôm nay."
            />
            <div className="mt-3">
              <Button type="button" onClick={() => navigate("/doctor/appointments")}>
                Xem lịch hẹn
              </Button>
            </div>
          </Card.Body>
        </Card>
      ) : (
        <Row className="g-3">
          <Col xs={12}>
            <Card className="doctor-card">
              <Card.Header>
                <h2>Ca đang khám</h2>
              </Card.Header>
              <Card.Body className="p-0">
                {inProgressAppointments.length === 0 ? (
                  <EmptyState title="Không có ca đang khám" description="Các ca đang khám sẽ xuất hiện tại đây." />
                ) : (
                  <Table responsive hover className="doctor-table mb-0">
                    <thead>
                      <tr>
                        <th>Mã lịch</th>
                        <th>Tên bệnh nhân</th>
                        <th>Giờ khám</th>
                        <th>Lý do khám</th>
                        <th>Trạng thái</th>
                        <th>Thao tác</th>
                      </tr>
                    </thead>
                    <tbody>
                      {inProgressAppointments.map((appointment) => (
                        <tr key={appointment.id}>
                          <td>#{appointment.appointmentCode || appointment.id}</td>
                          <td>{appointment.patient?.fullName || "--"}</td>
                          <td>
                            <strong>{formatTime(appointment.startTime)}</strong>
                            <span className="muted-cell">{formatDate(appointment.appointmentDate)}</span>
                          </td>
                          <td>{appointment.reason || appointment.symptomNote || "--"}</td>
                          <td>
                            <StatusBadge status={appointment.status} label={appointment.statusLabel} />
                          </td>
                          <td>
                            <Button
                              type="button"
                              size="sm"
                              onClick={() => navigate(`/doctor/examination/${appointment.id}`)}
                            >
                              Tiếp tục khám
                            </Button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </Table>
                )}
              </Card.Body>
            </Card>
          </Col>

          <Col xs={12}>
            <Card className="doctor-card">
              <Card.Header>
                <h2>Bệnh nhân đang chờ</h2>
              </Card.Header>
              <Card.Body className="p-0">
                {waitingAppointments.length === 0 ? (
                  <EmptyState title="Không có bệnh nhân đang chờ" description="Chỉ lịch hẹn đã được lễ tân xác nhận là đang chờ mới xuất hiện tại đây." />
                ) : (
                  <Table responsive hover className="doctor-table mb-0">
                    <thead>
                      <tr>
                        <th>Mã lịch</th>
                        <th>Tên bệnh nhân</th>
                        <th>Giờ khám</th>
                        <th>Dịch vụ</th>
                        <th>Lý do khám</th>
                        <th>Thao tác</th>
                      </tr>
                    </thead>
                    <tbody>
                      {waitingAppointments.map((appointment) => (
                        <tr key={appointment.id}>
                          <td>#{appointment.appointmentCode || appointment.id}</td>
                          <td>{appointment.patient?.fullName || "--"}</td>
                          <td>
                            <strong>{formatTime(appointment.startTime)}</strong>
                            <span className="muted-cell">{formatDate(appointment.appointmentDate)}</span>
                          </td>
                          <td>{appointment.service?.name || "--"}</td>
                          <td>{appointment.reason || appointment.symptomNote || "--"}</td>
                          <td>
                            <Button type="button" size="sm" onClick={() => openStartModal(appointment)}>
                              Bắt đầu khám
                            </Button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </Table>
                )}
              </Card.Body>
            </Card>
          </Col>
        </Row>
      )}

      <Modal show={Boolean(selectedAppointment)} onHide={closeStartModal} centered>
        <Form onSubmit={handleStartExamination}>
          <Modal.Header closeButton={!starting}>
            <Modal.Title>Bắt đầu khám</Modal.Title>
          </Modal.Header>
          <Modal.Body>
            {startError && <Alert variant="danger">{startError}</Alert>}
            <Form.Group className="mb-3" controlId="entryChiefComplaint">
              <Form.Label>Triệu chứng chính</Form.Label>
              <Form.Control
                as="textarea"
                rows={3}
                value={startForm.chiefComplaint}
                onChange={(e) => updateStartField("chiefComplaint", e.target.value)}
                disabled={starting}
              />
            </Form.Group>
            <Form.Group controlId="entryInitialNote">
              <Form.Label>Ghi chú ban đầu</Form.Label>
              <Form.Control
                as="textarea"
                rows={3}
                value={startForm.initialNote}
                onChange={(e) => updateStartField("initialNote", e.target.value)}
                disabled={starting}
              />
            </Form.Group>
          </Modal.Body>
          <Modal.Footer>
            <Button type="button" variant="outline-secondary" onClick={closeStartModal} disabled={starting}>
              Hủy
            </Button>
            <Button type="submit" disabled={starting}>
              {starting ? "Đang bắt đầu..." : "Bắt đầu khám"}
            </Button>
          </Modal.Footer>
        </Form>
      </Modal>
    </Container>
  );
}

export default DoctorExaminationEntryPage;
