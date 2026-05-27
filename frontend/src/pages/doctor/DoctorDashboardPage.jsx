/* eslint-disable react-hooks/set-state-in-effect */
import { useCallback, useEffect, useMemo, useState } from "react";
import { Button, Card, Table } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import EmptyState from "../../components/common/EmptyState";
import ErrorState from "../../components/common/ErrorState";
import LoadingState from "../../components/common/LoadingState";
import PageHeader from "../../components/common/PageHeader";
import StatusBadge from "../../components/common/StatusBadge";
import { getDoctorDashboardSummary, getTodayAppointments } from "../../services/doctor/doctorDashboardApi";
import {
  canEnterExamination,
  canStartExamination,
  formatTime,
  getErrorMessage,
} from "./doctorPageUtils";

const defaultSummary = {
  todayAppointments: 0,
  waitingAppointments: 0,
  inProgressAppointments: 0,
  completedAppointments: 0,
  cancelledAppointments: 0,
};

const buildSummaryFromAppointments = (appointments) => ({
  todayAppointments: appointments.length,
  waitingAppointments: appointments.filter((item) => ["BOOKED", "WAITING"].includes(item.status)).length,
  inProgressAppointments: appointments.filter((item) => item.status === "IN_PROGRESS").length,
  completedAppointments: appointments.filter((item) => item.status === "COMPLETED").length,
  cancelledAppointments: appointments.filter((item) => item.status === "CANCELLED").length,
});

function DoctorDashboardPage() {
  const navigate = useNavigate();
  const [summary, setSummary] = useState(defaultSummary);
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadDashboard = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      const [summaryResponse, appointmentsResponse] = await Promise.all([
        getDoctorDashboardSummary(),
        getTodayAppointments(),
      ]);

      const todayAppointments = appointmentsResponse.data || [];
      setAppointments(todayAppointments);
      setSummary(summaryResponse.data || buildSummaryFromAppointments(todayAppointments));
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
    loadDashboard();
  }, [loadDashboard]);

  const nextAppointment = useMemo(
    () => appointments.find((item) => ["BOOKED", "WAITING", "IN_PROGRESS"].includes(item.status)),
    [appointments]
  );

  const openAppointment = (appointment) => {
    if (canEnterExamination(appointment.status)) {
      navigate(`/doctor/examination/${appointment.id}`);
      return;
    }

    navigate(`/doctor/appointments/${appointment.id}`);
  };

  if (loading) {
    return <LoadingState />;
  }

  if (error) {
    return <ErrorState message={error} onRetry={loadDashboard} />;
  }

  return (
    <>
      <PageHeader
        title="Tổng quan bác sĩ"
        description="Theo dõi lịch hẹn hôm nay và tiếp tục các ca đang khám."
      />

      <div className="summary-grid">
        {[
          ["Tổng lịch hôm nay", summary.todayAppointments, "blue"],
          ["Bệnh nhân đang chờ", summary.waitingAppointments, "rose"],
          ["Ca đang khám", summary.inProgressAppointments, "mint"],
          ["Ca đã khám xong", summary.completedAppointments, "green"],
          ["Ca đã hủy", summary.cancelledAppointments, "gray"],
        ].map(([label, value, tone]) => (
          <Card className={`summary-card ${tone}`} key={label}>
            <Card.Body>
              <span className="summary-dot" aria-hidden="true" />
              <p>{label}</p>
              <strong>{value}</strong>
            </Card.Body>
          </Card>
        ))}
      </div>

      {nextAppointment && (
        <Card className="doctor-card highlight-card">
          <Card.Body>
            <div>
              <p className="card-kicker">Lịch khám tiếp theo</p>
              <h2>{nextAppointment.patient?.fullName || "Bệnh nhân"}</h2>
              <span>
                {formatTime(nextAppointment.startTime)} - {formatTime(nextAppointment.endTime)} ·{" "}
                {nextAppointment.service?.name || "Dịch vụ khám"}
              </span>
            </div>
            <Button type="button" onClick={() => openAppointment(nextAppointment)}>
              {nextAppointment.status === "IN_PROGRESS" ? "Tiếp tục khám" : "Bắt đầu khám"}
            </Button>
          </Card.Body>
        </Card>
      )}

      <Card className="doctor-card">
        <Card.Header>
          <h2>Lịch hẹn hôm nay</h2>
          <Button type="button" variant="link" onClick={() => navigate("/doctor/appointments")}>
            Xem tất cả
          </Button>
        </Card.Header>
        <Card.Body className="p-0">
          {appointments.length === 0 ? (
            <EmptyState title="Hôm nay chưa có lịch hẹn" description="Các lịch mới sẽ xuất hiện tại đây." />
          ) : (
            <Table responsive hover className="doctor-table mb-0">
              <thead>
                <tr>
                  <th>Mã lịch</th>
                  <th>Bệnh nhân</th>
                  <th>Giờ khám</th>
                  <th>Lý do khám</th>
                  <th>Dịch vụ</th>
                  <th>Trạng thái</th>
                  <th>Hành động</th>
                </tr>
              </thead>
              <tbody>
                {appointments.map((appointment) => (
                  <tr key={appointment.id}>
                    <td>{appointment.appointmentCode}</td>
                    <td>{appointment.patient?.fullName || "--"}</td>
                    <td>
                      {formatTime(appointment.startTime)} - {formatTime(appointment.endTime)}
                    </td>
                    <td>{appointment.reason || "--"}</td>
                    <td>{appointment.service?.name || "--"}</td>
                    <td>
                      <StatusBadge status={appointment.status} label={appointment.statusLabel} />
                    </td>
                    <td>
                      <div className="table-actions">
                        <Button
                          type="button"
                          size="sm"
                          variant="outline-primary"
                          onClick={() => navigate(`/doctor/appointments/${appointment.id}`)}
                        >
                          Chi tiết
                        </Button>
                        {(canStartExamination(appointment.status) || canEnterExamination(appointment.status)) && (
                          <Button type="button" size="sm" onClick={() => openAppointment(appointment)}>
                            {appointment.status === "COMPLETED"
                              ? "Xem lại"
                              : appointment.status === "IN_PROGRESS"
                                ? "Tiếp tục"
                                : "Bắt đầu"}
                          </Button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </Table>
          )}
        </Card.Body>
      </Card>
    </>
  );
}

export default DoctorDashboardPage;
