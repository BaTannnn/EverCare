/* eslint-disable react-hooks/set-state-in-effect */
import { useCallback, useEffect, useMemo, useState } from "react";
import { Button, Card, Form } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import EmptyState from "../../components/common/EmptyState";
import ErrorState from "../../components/common/ErrorState";
import LoadingState from "../../components/common/LoadingState";
import { getDoctorAppointmentsByDate } from "../../services/doctor/doctorAppointmentApi";
import { getDoctorSchedules } from "../../services/doctor/doctorScheduleApi";
import {
  formatTime,
  getErrorMessage,
  todayInputValue,
} from "./doctorPageUtils";

const hourMarks = ["08:00", "10:00", "12:00", "14:00", "16:00", "18:00"];
const weekdayLabels = ["T2", "T3", "T4", "T5", "T6", "T7", "CN"];

const shiftMeta = {
  AVAILABLE: { label: "Đang mở", className: "open" },
  FULL: { label: "Đã đầy", className: "full" },
  CLOSED: { label: "Đã đóng", className: "locked" },
  CANCELLED: { label: "Đã hủy", className: "locked" },
  COMPLETED: { label: "Hoàn thành", className: "completed" },
};

const SCHEDULE_STATUSES = ["ALL", "AVAILABLE", "FULL", "CLOSED", "CANCELLED"];

const scheduleStatusLabels = {
  ALL: "Tất cả trạng thái",
  AVAILABLE: "Đang mở",
  FULL: "Đã đầy",
  CLOSED: "Đã đóng",
  CANCELLED: "Đã hủy",
};

const toDateValue = (date) => date.toISOString().slice(0, 10);

const startOfWeek = (value) => {
  const date = new Date(`${value}T00:00:00`);
  const day = date.getDay() || 7;
  date.setDate(date.getDate() - day + 1);
  return date;
};

const getWeekDays = (value) => {
  const monday = startOfWeek(value);

  return Array.from({ length: 7 }, (_, index) => {
    const date = new Date(monday);
    date.setDate(monday.getDate() + index);

    return {
      label: weekdayLabels[index],
      date,
      dateValue: toDateValue(date),
      dayNumber: date.getDate(),
      weekday: index + 1,
      weekend: index >= 5,
    };
  });
};

const toHour = (value) => {
  const [hour, minute] = String(value || "00:00").split(":").map(Number);
  return hour + (minute || 0) / 60;
};

const formatLongDate = (value) =>
  new Intl.DateTimeFormat("vi-VN", {
    weekday: "long",
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  }).format(new Date(`${value}T00:00:00`));

const normalizeSchedule = (schedule) => ({
  id: schedule.id,
  workDate: schedule.workDate,
  startTime: formatTime(schedule.startTime),
  endTime: formatTime(schedule.endTime),
  maxPatients: schedule.maxPatients || 0,
  status: schedule.status || "AVAILABLE",
  statusLabel: schedule.statusLabel,
  note: schedule.note,
  doctorName: schedule.doctorName,
  departmentName: schedule.departmentName,
});

const getScheduleDateValue = (schedule) => String(schedule.workDate || "").slice(0, 10);

function DoctorAppointmentsPage() {
  const navigate = useNavigate();
  const [date, setDate] = useState(todayInputValue());
  const [status, setStatus] = useState("ALL");
  const [appointments, setAppointments] = useState([]);
  const [schedules, setSchedules] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const weekDays = useMemo(() => getWeekDays(date), [date]);
  const weekRange = useMemo(
    () => ({
      fromDate: weekDays[0]?.dateValue,
      toDate: weekDays[6]?.dateValue,
    }),
    [weekDays]
  );

  const loadPageData = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      const scheduleParams = {
        fromDate: weekRange.fromDate,
        toDate: weekRange.toDate,
      };

      if (status !== "ALL") {
        scheduleParams.status = status;
      }

      const [appointmentResponse, scheduleResponse] = await Promise.all([
        getDoctorAppointmentsByDate(date),
        getDoctorSchedules(scheduleParams),
      ]);

      setAppointments(appointmentResponse.data || []);
      setSchedules((scheduleResponse.data || []).map(normalizeSchedule));
    } catch (err) {
      if (err.response?.status === 401) {
        navigate("/login", { replace: true });
        return;
      }

      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [date, navigate, status, weekRange.fromDate, weekRange.toDate]);

  useEffect(() => {
    loadPageData();
  }, [loadPageData]);

  const todayShifts = useMemo(
    () => schedules.filter((shift) => getScheduleDateValue(shift) === date),
    [date, schedules]
  );

  const appointmentsByShift = useMemo(() => {
    const counter = new Map();

    todayShifts.forEach((shift) => {
      const start = toHour(shift.startTime);
      const end = toHour(shift.endTime);
      const count = appointments.filter((appointment) => {
        const appointmentTime = toHour(appointment.startTime);
        return appointmentTime >= start && appointmentTime < end;
      }).length;

      counter.set(shift.id, count);
    });

    return counter;
  }, [appointments, todayShifts]);

  return (
    <>
      <div className="appointments-page-header">
        <div>
          <div className="doctor-breadcrumb">
            Bác sĩ <span>/</span> <strong>Lịch làm việc</strong>
          </div>
          <h1>Lịch làm việc</h1>
          <p>Theo dõi ca làm việc theo tuần và sức chứa trong ngày chọn.</p>
        </div>
      </div>

      <Card className="doctor-card appointment-toolbar-card">
        <Card.Body>
          <div className="appointment-toolbar">
            <div className="appointment-date-group">
              <div className="appointment-date-display">
                <span aria-hidden="true">▣</span>
                <strong>{formatLongDate(date)}</strong>
              </div>
              <Form.Control
                className="appointment-date-input"
                type="date"
                value={date}
                onChange={(e) => setDate(e.target.value)}
                aria-label="Đổi ngày"
              />
              <Button type="button" variant="outline-primary" onClick={() => setDate(todayInputValue())}>
                Hôm nay
              </Button>
            </div>

            <Form.Select value={status} onChange={(e) => setStatus(e.target.value)} aria-label="Lọc trạng thái">
              {SCHEDULE_STATUSES.map((item) => (
                <option value={item} key={item}>
                  {scheduleStatusLabels[item]}
                </option>
              ))}
            </Form.Select>

            <div className="appointment-toolbar-note">Hiển thị lịch làm việc theo tuần của ngày đang chọn.</div>

            <Button type="button" disabled title="Backend chưa có API bác sĩ tự thêm ca làm việc">
              + Thêm ca làm việc
            </Button>
          </div>
        </Card.Body>
      </Card>

      {loading ? (
        <LoadingState />
      ) : error ? (
        <ErrorState message={error} onRetry={loadPageData} />
      ) : (
        <div className="appointment-workspace-grid">
          <Card className="doctor-card schedule-card">
            <Card.Body>
              <div className="schedule-board">
                <div className="schedule-head schedule-time-head">Giờ</div>
                {weekDays.map((day) => (
                  <button
                    type="button"
                    className={`schedule-head schedule-day-head${day.dateValue === date ? " active" : ""}${
                      day.weekend ? " weekend" : ""
                    }`}
                    key={day.dateValue}
                    onClick={() => setDate(day.dateValue)}
                  >
                    <strong>{day.label}</strong>
                    <span>{day.dayNumber}</span>
                  </button>
                ))}

                <div className="schedule-hours">
                  {hourMarks.map((hour) => (
                    <span key={hour}>{hour}</span>
                  ))}
                </div>

                {weekDays.map((day) => (
                  <div className="schedule-day-column" key={day.dateValue}>
                    {schedules
                      .filter((shift) => getScheduleDateValue(shift) === day.dateValue)
                      .map((shift) => {
                        const start = toHour(shift.startTime);
                        const end = toHour(shift.endTime);
                        const top = ((start - 8) / 11) * 100;
                        const height = ((end - start) / 11) * 100;
                        const meta = shiftMeta[shift.status] || shiftMeta.AVAILABLE;
                        const bookedCount =
                          day.dateValue === date ? appointmentsByShift.get(shift.id) || 0 : null;

                        return (
                          <div
                            className={`schedule-shift ${meta.className}`}
                            style={{ top: `${top}%`, height: `${height}%` }}
                            key={shift.id}
                          >
                            <strong>{shift.statusLabel || meta.label}</strong>
                            <span>
                              {shift.startTime} - {shift.endTime}
                            </span>
                            <small>{bookedCount === null ? `${shift.maxPatients} BN tối đa` : `${bookedCount}/${shift.maxPatients} BN`}</small>
                            {shift.note && <small>{shift.note}</small>}
                          </div>
                        );
                      })}
                  </div>
                ))}
              </div>

              <div className="schedule-legend">
                {Object.entries(shiftMeta).map(([key, meta]) => (
                  <span className={meta.className} key={key}>
                    {meta.label}
                  </span>
                ))}
              </div>
            </Card.Body>
          </Card>

          <aside className="appointment-side-panel schedule-only-panel">
            <Card className="doctor-card today-shifts-card">
              <Card.Header>
                <h2>Ca làm việc ngày chọn</h2>
                <span>{todayShifts.length} ca</span>
              </Card.Header>
              <Card.Body>
                {todayShifts.length === 0 ? (
                  <EmptyState title="Không có ca" description="Bác sĩ chưa có lịch làm việc trong ngày này." />
                ) : (
                  <div className="today-shift-list">
                    {todayShifts.map((shift) => {
                      const meta = shiftMeta[shift.status] || shiftMeta.AVAILABLE;
                      const bookedCount = appointmentsByShift.get(shift.id) || 0;

                      return (
                        <div className={`today-shift ${meta.className}`} key={shift.id}>
                          <strong>
                            {shift.startTime} - {shift.endTime}
                          </strong>
                          <span>
                            {bookedCount}/{shift.maxPatients} bệnh nhân
                          </span>
                          <em>{shift.statusLabel || meta.label}</em>
                        </div>
                      );
                    })}
                  </div>
                )}
              </Card.Body>
            </Card>
          </aside>
        </div>
      )}
    </>
  );
}

export default DoctorAppointmentsPage;
