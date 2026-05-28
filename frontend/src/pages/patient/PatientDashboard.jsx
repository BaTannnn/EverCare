import { useEffect, useMemo, useState } from "react";
import { Button, Card } from "react-bootstrap";
import { BsArrowRight, BsCalendar2Check, BsCashCoin, BsClipboard2Pulse, BsFileEarmarkMedical, BsHeartPulse } from "react-icons/bs";
import { Link, useOutletContext } from "react-router-dom";
import { getPatientAppointments } from "../../services/patient/patientAppointmentApi";
import { getPatientInvoices } from "../../services/patient/patientInvoiceApi";
import { getPatientMedicalRecords } from "../../services/patient/patientMedicalRecordApi";
import { getPatientNotifications } from "../../services/patient/patientNotificationApi";
import { getPatientPrescriptions } from "../../services/patient/patientPrescriptionApi";
import { formatCurrency, getPatientStatusMeta } from "./patientPageUtils";

const summaryIcons = [BsCalendar2Check, BsFileEarmarkMedical, BsHeartPulse, BsCashCoin];

const quickActions = [
  { id: "book", title: "Đặt lịch khám", description: "Tìm bác sĩ và đặt lịch nhanh", to: "/patient/book-appointment" },
  { id: "appointments", title: "Xem lịch hẹn", description: "Theo dõi trạng thái các cuộc hẹn", to: "/patient/appointments" },
  { id: "records", title: "Xem hồ sơ bệnh án", description: "Tra cứu lịch sử khám bệnh", to: "/patient/medical-records" },
  { id: "invoice", title: "Thanh toán hóa đơn", description: "Kiểm tra các hóa đơn chưa thanh toán", to: "/patient/invoices" },
];

const supportCard = {
  title: "Cần hỗ trợ?",
  description: "Đội ngũ chăm sóc khách hàng EverCare luôn sẵn sàng hỗ trợ bạn về lịch hẹn, hồ sơ và thanh toán.",
  action: "Liên hệ ngay",
};

const sortByLatestDate = (items = [], field) =>
  [...items].sort((a, b) => new Date(b[field] || 0).getTime() - new Date(a[field] || 0).getTime());

function PatientDashboard() {
  const { profile } = useOutletContext() || {};
  const [loading, setLoading] = useState(true);
  const [appointments, setAppointments] = useState([]);
  const [records, setRecords] = useState([]);
  const [prescriptions, setPrescriptions] = useState([]);
  const [notifications, setNotifications] = useState([]);
  const [invoices, setInvoices] = useState([]);

  useEffect(() => {
    let mounted = true;

    const loadDashboard = async () => {
      try {
        const [appointmentsResult, recordsResult, prescriptionsResult, notificationsResult, invoicesResult] = await Promise.allSettled([
          getPatientAppointments(),
          getPatientMedicalRecords(),
          getPatientPrescriptions(),
          getPatientNotifications(),
          getPatientInvoices(),
        ]);

        if (!mounted) {
          return;
        }

        if (appointmentsResult.status === "fulfilled") {
          setAppointments(appointmentsResult.value.data || []);
        }
        if (recordsResult.status === "fulfilled") {
          setRecords(recordsResult.value.data || []);
        }
        if (prescriptionsResult.status === "fulfilled") {
          setPrescriptions(prescriptionsResult.value.data || []);
        }
        if (notificationsResult.status === "fulfilled") {
          setNotifications(notificationsResult.value.data || []);
        }
        if (invoicesResult.status === "fulfilled") {
          setInvoices(invoicesResult.value.data || []);
        }
      } finally {
        if (mounted) {
          setLoading(false);
        }
      }
    };

    loadDashboard();

    return () => {
      mounted = false;
    };
  }, []);

  const dashboard = useMemo(() => {
    const upcomingAppointment = sortByLatestDate(
      appointments.filter((item) => ["PENDING", "CONFIRMED"].includes(item.status)),
      "appointmentDate",
    )[0];
    const latestRecord = sortByLatestDate(records, "visitDate")[0];
    const latestPrescription = sortByLatestDate(prescriptions, "date")[0];
    const unpaidInvoice = invoices.find((invoice) => invoice.status === "UNPAID") || invoices[0];

    return {
      upcomingAppointment,
      latestRecord,
      latestPrescription,
      unpaidInvoice,
      notifications,
      summary: {
        upcomingAppointments: appointments.length,
        recentRecords: records.length,
        newPrescriptions: prescriptions.length,
        unpaidInvoices: invoices.filter((invoice) => invoice.status === "UNPAID").length,
      },
    };
  }, [appointments, invoices, notifications, prescriptions, records]);

  const summaryCards = useMemo(
    () => [
      {
        label: "Lịch hẹn sắp tới",
        value: dashboard.summary.upcomingAppointments,
        hint: dashboard.upcomingAppointment ? `${dashboard.upcomingAppointment.displayDate} • ${dashboard.upcomingAppointment.displayTime}` : "Chưa có lịch hẹn",
      },
      {
        label: "Hồ sơ bệnh án gần đây",
        value: dashboard.summary.recentRecords,
        hint: dashboard.latestRecord?.diagnosis || "Chưa có hồ sơ mới",
      },
      {
        label: "Đơn thuốc mới",
        value: dashboard.summary.newPrescriptions,
        hint: dashboard.latestPrescription?.prescriptionCode || "Chưa có đơn thuốc mới",
      },
      {
        label: "Hóa đơn chưa thanh toán",
        value: dashboard.summary.unpaidInvoices,
        hint: dashboard.unpaidInvoice ? formatCurrency(dashboard.unpaidInvoice.totalAmount) : "0đ",
      },
    ],
    [dashboard],
  );

  if (loading) {
    return (
      <div className="patient-loading-panel">
        <div className="patient-spinner" />
        <span>Đang tải dữ liệu tổng quan...</span>
      </div>
    );
  }

  const upcomingStatus = getPatientStatusMeta(dashboard.upcomingAppointment?.status || "CONFIRMED");

  return (
    <div className="patient-page">
      <section className="patient-hero">
        <div>
          <p className="patient-eyebrow">Bảng điều khiển bệnh nhân</p>
          <h2>Chào mừng, {profile?.fullName || "bạn"}</h2>
          <p>Theo dõi lịch hẹn, hồ sơ bệnh án, đơn thuốc và thanh toán trong một nơi duy nhất.</p>
        </div>

        <div className="patient-hero-actions">
          <Link to="/patient/book-appointment" className="patient-link-button primary">
            Đặt lịch khám
          </Link>
          <Link to="/patient/appointments" className="patient-link-button">
            Xem lịch hẹn
          </Link>
        </div>
      </section>

      <section className="patient-summary-grid">
        {summaryCards.map((card, index) => {
          const Icon = summaryIcons[index];

          return (
            <Card key={card.label} className="patient-summary-card">
              <Card.Body>
                <div className="patient-summary-icon">
                  <Icon />
                </div>
                <div className="patient-summary-copy">
                  <span>{card.label}</span>
                  <strong>{typeof card.value === "number" ? card.value : card.value}</strong>
                  <p>{card.hint}</p>
                </div>
              </Card.Body>
            </Card>
          );
        })}
      </section>

      <section className="patient-dashboard-columns">
        <div className="patient-dashboard-main">
          <div className="patient-section-head">
            <div>
              <h3>Lịch hẹn tiếp theo</h3>
              <p>Thông tin cuộc hẹn quan trọng nhất của bạn.</p>
            </div>
            <Link to="/patient/appointments">Tất cả lịch hẹn <BsArrowRight /></Link>
          </div>

          <Card className="patient-next-appointment">
            <Card.Body>
              <div className="patient-next-appointment-badge">{upcomingStatus.label}</div>
                <div className="patient-next-appointment-content">
                <img src={dashboard.upcomingAppointment?.doctorAvatar || profile?.avatar} alt={dashboard.upcomingAppointment?.doctorName} />
                <div className="patient-next-appointment-copy">
                  <span>{dashboard.upcomingAppointment?.departmentName}</span>
                  <h4>{dashboard.upcomingAppointment?.doctorName}</h4>
                  <p>{dashboard.upcomingAppointment?.serviceName}</p>
                  <div className="patient-chip-row">
                    <span className="patient-time-chip">{dashboard.upcomingAppointment?.displayTime}</span>
                    <span className="patient-time-chip">{dashboard.upcomingAppointment?.displayDate}</span>
                  </div>
                </div>
                <div className="patient-next-appointment-actions">
                  <Button type="button" variant="light" className="patient-outline-strong">
                    Xem chi tiết
                  </Button>
                  <Button type="button" variant="primary" className="patient-primary-soft">
                    Dời lịch
                  </Button>
                </div>
              </div>
            </Card.Body>
          </Card>

          <div className="patient-section-head mt-4">
            <div>
              <h3>Thông báo mới</h3>
              <p>Những cập nhật gần đây từ EverCare.</p>
            </div>
            <Link to="/patient/notifications">Xem tất cả <BsArrowRight /></Link>
          </div>

          <div className="patient-notification-stack">
            {dashboard.notifications.slice(0, 3).map((notification) => (
              <div key={notification.id} className="patient-notification-row">
                <div className="patient-notification-dot-icon">
                  <BsClipboard2Pulse />
                </div>
                <div>
                  <h4>{notification.title}</h4>
                  <p>{notification.content}</p>
                </div>
                <span>{notification.time}</span>
              </div>
            ))}
          </div>
        </div>

        <aside className="patient-dashboard-aside">
          <Card className="patient-quick-actions-card">
            <Card.Body>
              <h3>Thao tác nhanh</h3>
              <div className="patient-quick-action-list">
                {quickActions.map((action) => (
                  <Link key={action.id} to={action.to} className="patient-quick-action-item">
                    <div>
                      <strong>{action.title}</strong>
                      <p>{action.description}</p>
                    </div>
                    <BsArrowRight />
                  </Link>
                ))}
              </div>
            </Card.Body>
          </Card>

          <Card className="patient-support-card">
            <Card.Body>
              <p className="patient-support-title">{supportCard.title}</p>
              <p>{supportCard.description}</p>
              <Button type="button" className="patient-primary-soft w-100">
                {supportCard.action}
              </Button>
            </Card.Body>
          </Card>

          <Card className="patient-story-card">
            <Card.Body>
              <div className="patient-story-tag">Mẹo sức khỏe</div>
              <h3>5 bài tập giãn cơ tại chỗ cho dân văn phòng</h3>
              <p>Giảm đau vai gáy và cải thiện sự tập trung sau mỗi 60 phút làm việc.</p>
              <Button type="button" variant="link" className="patient-story-link">
                Đọc thêm <BsArrowRight />
              </Button>
            </Card.Body>
          </Card>
        </aside>
      </section>
    </div>
  );
}

export default PatientDashboard;
