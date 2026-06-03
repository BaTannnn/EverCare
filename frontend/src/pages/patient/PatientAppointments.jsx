import { useEffect, useMemo, useState } from "react";
import { Badge, Button, Card, Modal, Nav } from "react-bootstrap";
import { BsArrowRepeat, BsCalendar2Check, BsClock, BsEye, BsPerson, BsXLg } from "react-icons/bs";
import { useNavigate } from "react-router-dom";
import { cancelPatientAppointment, getPatientAppointments } from "../../services/patient/patientAppointmentApi";
import { countByStatus, getPatientStatusMeta } from "./patientPageUtils";

const statusOrder = {
  IN_PROGRESS: 0,
  WAITING: 1,
  BOOKED: 2,
  COMPLETED: 3,
  CANCELLED: 4,
  NO_SHOW: 5,
};

const tabs = [
  { key: "ALL", label: "Tất cả" },
  { key: "IN_PROGRESS", label: "Đang khám" },
  { key: "WAITING", label: "Đang chờ khám" },
  { key: "BOOKED", label: "Chờ xác nhận" },
  { key: "COMPLETED", label: "Đã khám xong" },
  { key: "CANCELLED", label: "Đã hủy" },
  { key: "NO_SHOW", label: "Không đến" },
];

function PatientAppointments() {
  const navigate = useNavigate();
  const [appointments, setAppointments] = useState([]);
  const [activeTab, setActiveTab] = useState("ALL");
  const [selectedAppointment, setSelectedAppointment] = useState(null);

  useEffect(() => {
    let mounted = true;

    const loadAppointments = async () => {
      try {
        const response = await getPatientAppointments();
        if (mounted) {
          setAppointments(response.data || []);
        }
      } catch (error) {
        console.error(error);
        if (mounted) {
          setAppointments([]);
        }
      }
    };

    loadAppointments();

    return () => {
      mounted = false;
    };
  }, []);

  const summary = useMemo(
    () => ({
      total: appointments.length,
      booked: countByStatus(appointments, "BOOKED"),
      waiting: countByStatus(appointments, "WAITING"),
      completed: countByStatus(appointments, "COMPLETED"),
    }),
    [appointments],
  );

  const filteredAppointments = useMemo(() => {
    const visibleAppointments = activeTab === "ALL"
      ? appointments
      : appointments.filter((appointment) => appointment.status === activeTab);

    return [...visibleAppointments].sort((left, right) => {
      const leftOrder = statusOrder[left.status] ?? Number.MAX_SAFE_INTEGER;
      const rightOrder = statusOrder[right.status] ?? Number.MAX_SAFE_INTEGER;

      if (leftOrder !== rightOrder) {
        return leftOrder - rightOrder;
      }

      const leftDate = `${left.appointmentDate || ""} ${left.startTime || ""}`;
      const rightDate = `${right.appointmentDate || ""} ${right.startTime || ""}`;
      return rightDate.localeCompare(leftDate);
    });
  }, [appointments, activeTab]);

  const handleCancel = async (appointment) => {
    try {
      const response = await cancelPatientAppointment(appointment.id, "Bệnh nhân chủ động hủy lịch");
      const updatedAppointment = response.data || { ...appointment, status: "CANCELLED", statusLabel: "Đã hủy" };
      setAppointments((current) => current.map((item) => (item.id === appointment.id ? updatedAppointment : item)));
    } catch (error) {
      console.error(error);
      setAppointments((current) => current.map((item) => (item.id === appointment.id ? { ...item, status: "CANCELLED", statusLabel: "Đã hủy" } : item)));
    }
  };

  return (
    <div className="patient-page">
      <div className="patient-summary-grid appointments">
        <Card className="patient-summary-card light-blue">
          <Card.Body>
            <div className="patient-summary-icon">
              <BsCalendar2Check />
            </div>
            <div className="patient-summary-copy">
              <span>Tổng lịch</span>
              <strong>{summary.total}</strong>
            </div>
          </Card.Body>
        </Card>
        <Card className="patient-summary-card light-green">
          <Card.Body>
            <div className="patient-summary-icon">
              <BsClock />
            </div>
            <div className="patient-summary-copy">
              <span>Chờ xác nhận</span>
              <strong>{summary.booked}</strong>
            </div>
          </Card.Body>
        </Card>
        <Card className="patient-summary-card light-teal">
          <Card.Body>
            <div className="patient-summary-icon">
              <BsPerson />
            </div>
            <div className="patient-summary-copy">
              <span>Đang chờ khám</span>
              <strong>{summary.waiting}</strong>
            </div>
          </Card.Body>
        </Card>
        <Card className="patient-summary-card light-gray">
          <Card.Body>
            <div className="patient-summary-icon">
              <BsCalendar2Check />
            </div>
            <div className="patient-summary-copy">
              <span>Hoàn thành</span>
              <strong>{summary.completed}</strong>
            </div>
          </Card.Body>
        </Card>
      </div>

      <div className="patient-tab-header">
        <Nav variant="tabs" activeKey={activeTab} onSelect={(eventKey) => setActiveTab(eventKey || "ALL")} className="patient-tabs">
          {tabs.map((tab) => (
            <Nav.Item key={tab.key}>
              <Nav.Link eventKey={tab.key}>{tab.label}</Nav.Link>
            </Nav.Item>
          ))}
        </Nav>
      </div>

      <section className="patient-appointment-list">
        {filteredAppointments.length > 0 ? filteredAppointments.map((appointment) => {
          const meta = getPatientStatusMeta(appointment.status);

          return (
            <Card key={appointment.id} className="patient-appointment-card">
              <Card.Body>
                <div className="patient-appointment-top">
                  <div className="patient-appointment-doctor">
                    <img src={appointment.doctorAvatar} alt={appointment.doctorName} />
                    <div>
                      <h3>{appointment.doctorName}</h3>
                      <span>{appointment.departmentName}</span>
                    </div>
                  </div>
                  <Badge bg={meta.variant} className="patient-status-badge">
                    {meta.label}
                  </Badge>
                </div>

                <div className="patient-appointment-meta">
                  <span>
                    <BsCalendar2Check /> {appointment.displayDate || appointment.appointmentDate}
                  </span>
                  <span>
                    <BsClock /> {appointment.displayTime || appointment.startTime}
                  </span>
                  <span>{appointment.serviceName}</span>
                  <span>{appointment.statusLabel || appointment.status}</span>
                </div>

                <div className="patient-appointment-actions">
                  <Button type="button" variant="light" className="patient-outline-button" onClick={() => setSelectedAppointment(appointment)}>
                    <BsEye /> Xem chi tiết
                  </Button>
                  {appointment.status !== "COMPLETED" && appointment.status !== "CANCELLED" && appointment.status !== "NO_SHOW" && (
                    <Button type="button" variant="outline-danger" className="patient-danger-outline" onClick={() => handleCancel(appointment)}>
                      <BsXLg /> Hủy lịch
                    </Button>
                  )}
                  {(appointment.status === "COMPLETED" || appointment.status === "CANCELLED" || appointment.status === "NO_SHOW") && (
                    <Button type="button" className="patient-primary-soft" onClick={() => navigate("/patient/book-appointment")}>
                      <BsArrowRepeat /> Đặt lại
                    </Button>
                  )}
                </div>
              </Card.Body>
            </Card>
          );
        }) : (
          <div className="patient-empty-state">
            <h4>Chưa có lịch hẹn</h4>
            <p>Bạn chưa có lịch hẹn nào trong tab này. Hãy đặt lịch để bắt đầu.</p>
          </div>
        )}
      </section>

      <Card className="patient-appointment-banner">
        <Card.Body>
          <div>
            <h3>Nhắc nhở y tế</h3>
            <p>Hãy nhớ chuẩn bị đầy đủ hồ sơ bệnh án cũ và các kết quả xét nghiệm liên quan trước khi đến khám.</p>
          </div>
          <Button type="button" className="patient-primary-soft">
            Xem hướng dẫn
          </Button>
        </Card.Body>
      </Card>

      <Modal show={Boolean(selectedAppointment)} onHide={() => setSelectedAppointment(null)} centered size="lg" animation={false}>
        <Modal.Header closeButton>
          <Modal.Title>Chi tiết lịch hẹn</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {selectedAppointment && (
            <div className="patient-modal-grid">
              <div>
                <strong>{selectedAppointment.doctorName}</strong>
                <p>{selectedAppointment.departmentName}</p>
              </div>
              <div>
                <span>Thời gian</span>
                <strong>
                  {selectedAppointment.displayDate} {selectedAppointment.displayTime}
                </strong>
              </div>
              <div>
                <span>Dịch vụ</span>
                <strong>{selectedAppointment.serviceName}</strong>
              </div>
              <div>
                <span>Chuyên khoa</span>
                <strong>{selectedAppointment.departmentName}</strong>
              </div>
              <div>
                <span>Lý do khám</span>
                <strong>{selectedAppointment.reason || "Chưa cập nhật"}</strong>
              </div>
              <div>
                <span>Trạng thái</span>
                <strong>{getPatientStatusMeta(selectedAppointment.status).label}</strong>
              </div>
            </div>
          )}
        </Modal.Body>
      </Modal>
    </div>
  );
}

export default PatientAppointments;
