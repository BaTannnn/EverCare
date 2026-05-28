/* eslint-disable react-hooks/set-state-in-effect */
import { useCallback, useEffect, useMemo, useState } from "react";
import { Button, Card, Form, Table } from "react-bootstrap";
import { useLocation, useNavigate } from "react-router-dom";
import EmptyState from "../../components/common/EmptyState";
import ErrorState from "../../components/common/ErrorState";
import LoadingState from "../../components/common/LoadingState";
import StatusBadge from "../../components/common/StatusBadge";
import { getDoctorAppointmentsByDate } from "../../services/doctor/doctorAppointmentApi";
import {
  appointmentSearchText,
  canEnterExamination,
  canStartExamination,
  formatDate,
  formatTime,
  getErrorMessage,
  isDoctorVisibleAppointment,
  normalizeText,
  todayInputValue,
} from "./doctorPageUtils";

const appointmentStatusOptions = [
  ["ALL", "Tất cả"],
  ["WAITING", "Đang chờ"],
  ["IN_PROGRESS", "Đang khám"],
  ["COMPLETED", "Đã khám"],
];

const serviceTypeLabels = {
  ALL: "Tất cả loại",
  EXAMINATION: "Khám bệnh",
  TEST: "Xét nghiệm",
  IMAGING: "Chẩn đoán hình ảnh",
  CONSULTATION: "Tư vấn",
};

const getInitials = (name = "") =>
  name
    .split(" ")
    .filter(Boolean)
    .slice(-2)
    .map((item) => item[0])
    .join("")
    .toUpperCase() || "BN";

const patientMeta = (patient = {}) =>
  [patient.dateOfBirth ? `${new Date().getFullYear() - Number(patient.dateOfBirth.slice(0, 4))}` : "--", patient.gender || "--"].join(
    " / "
  );

function DoctorPatientAppointmentsPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const [date, setDate] = useState(todayInputValue());
  const [status, setStatus] = useState("ALL");
  const [search, setSearch] = useState(location.state?.search || "");
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadAppointments = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      const response = await getDoctorAppointmentsByDate(date);
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
  }, [date, navigate]);

  useEffect(() => {
    loadAppointments();
  }, [loadAppointments]);

  const filteredAppointments = useMemo(() => {
    const keyword = normalizeText(search);

    return appointments.filter((appointment) => {
      const matchesStatus = status === "ALL" || appointment.status === status;
      const matchesKeyword = !keyword || appointmentSearchText(appointment).includes(keyword);

      return matchesStatus && matchesKeyword;
    });
  }, [appointments, search, status]);

  const goToWorkspace = (appointment) => {
    if (canStartExamination(appointment.status)) {
      navigate(`/doctor/appointments/${appointment.id}`);
      return;
    }

    if (canEnterExamination(appointment.status)) {
      navigate(`/doctor/examination/${appointment.id}`);
      return;
    }

    navigate(`/doctor/appointments/${appointment.id}`);
  };

  const actionLabel = (appointment) => {
    if (appointment.status === "BOOKED") {
      return "Chờ lễ tân";
    }
    if (canStartExamination(appointment.status)) {
      return "Bắt đầu khám";
    }
    if (appointment.status === "IN_PROGRESS") {
      return "Tiếp tục khám";
    }
    if (appointment.status === "COMPLETED") {
      return "Xem bệnh án";
    }
    return "Chi tiết";
  };

  return (
    <>
      <div className="appointments-page-header">
        <div>
          <div className="doctor-breadcrumb">
            Bác sĩ <span>/</span> <strong>Lịch hẹn bệnh nhân</strong>
          </div>
          <h1>Lịch hẹn bệnh nhân</h1>
          <p>Theo dõi lịch hẹn, trạng thái khám và thao tác vào ca khám.</p>
        </div>
      </div>

      <Card className="doctor-card patient-appointments-card">
        <div className="patient-filter-bar">
          <Form.Group>
            <Form.Label>Tìm kiếm</Form.Label>
            <Form.Control
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Tìm theo tên bệnh nhân, mã lịch"
            />
          </Form.Group>
          <Form.Group>
            <Form.Label>Ngày khám</Form.Label>
            <Form.Control type="date" value={date} onChange={(e) => setDate(e.target.value)} />
          </Form.Group>
          <Form.Group>
            <Form.Label>Trạng thái</Form.Label>
            <Form.Select value={status} onChange={(e) => setStatus(e.target.value)}>
              {appointmentStatusOptions.map(([value, label]) => (
                <option value={value} key={value}>
                  {label}
                </option>
              ))}
            </Form.Select>
          </Form.Group>
          <Button type="button" onClick={loadAppointments}>
            Lọc
          </Button>
          <Button
            type="button"
            variant="outline-secondary"
            onClick={() => {
              setSearch("");
              setStatus("ALL");
            }}
          >
            ↻
          </Button>
        </div>

        <Card.Body className="p-0">
          {loading ? (
            <LoadingState />
          ) : error ? (
            <ErrorState message={error} onRetry={loadAppointments} />
          ) : filteredAppointments.length === 0 ? (
            <EmptyState title="Không có lịch hẹn phù hợp" description="Thử đổi ngày hoặc bộ lọc." />
          ) : (
            <Table responsive hover className="doctor-table patient-appointments-table mb-0">
              <thead>
                <tr>
                  <th>Mã lịch hẹn</th>
                  <th>Bệnh nhân</th>
                  <th>Tuổi / giới tính</th>
                  <th>Dịch vụ</th>
                  <th>Thời gian</th>
                  <th>Loại</th>
                  <th>Trạng thái</th>
                  <th>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {filteredAppointments.map((appointment) => (
                  <tr key={appointment.id}>
                    <td>
                      <button
                        type="button"
                        className="link-button"
                        onClick={() => navigate(`/doctor/appointments/${appointment.id}`)}
                      >
                        #{appointment.appointmentCode || appointment.id}
                      </button>
                    </td>
                    <td>
                      <div className="patient-cell">
                        <div className="patient-photo">{getInitials(appointment.patient?.fullName)}</div>
                        <div>
                          <strong>{appointment.patient?.fullName || "--"}</strong>
                          <span>{appointment.patient?.phone || "--"}</span>
                        </div>
                      </div>
                    </td>
                    <td>{patientMeta(appointment.patient)}</td>
                    <td>{appointment.service?.name || "--"}</td>
                    <td>
                      <strong>{formatTime(appointment.startTime)}</strong>
                      <span className="muted-cell">
                        {formatDate(appointment.appointmentDate)}
                      </span>
                    </td>
                    <td>
                      <span className="soft-pill">{serviceTypeLabels[appointment.service?.serviceType] || "Trực tiếp"}</span>
                    </td>
                    <td>
                      <StatusBadge status={appointment.status} label={appointment.statusLabel} />
                    </td>
                    <td>
                      <div className="patient-actions">
                        <Button
                          type="button"
                          size="sm"
                          variant="outline-primary"
                          onClick={() => navigate(`/doctor/appointments/${appointment.id}`)}
                        >
                          Xem
                        </Button>
                        <Button
                          type="button"
                          size="sm"
                          disabled={
                            appointment.status === "BOOKED"
                              || appointment.status === "CANCELLED"
                              || appointment.status === "NO_SHOW"
                          }
                          onClick={() => goToWorkspace(appointment)}
                        >
                          {actionLabel(appointment)}
                        </Button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </Table>
          )}
        </Card.Body>

        <div className="patient-table-footer">
          <span>
            Hiển thị {filteredAppointments.length} trong số {appointments.length} lịch hẹn
          </span>
          <div>
            <button type="button" disabled>
              ‹
            </button>
            <button type="button" className="active">
              1
            </button>
            <button type="button" disabled>
              ›
            </button>
          </div>
        </div>
      </Card>
    </>
  );
}

export default DoctorPatientAppointmentsPage;
