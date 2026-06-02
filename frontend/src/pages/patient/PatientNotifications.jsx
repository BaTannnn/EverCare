import { useEffect, useMemo, useState } from "react";
import { Badge, Button, Card, Nav } from "react-bootstrap";
import { BsBell, BsCalendar2Check, BsCapsule, BsCheck2All, BsCreditCard2Front, BsFileEarmarkText, BsInfoCircle } from "react-icons/bs";
import { usePatientShell } from "../../contexts/usePatientShell";
import { getPatientNotifications, markPatientNotificationAsRead } from "../../services/patient/patientNotificationApi";
import { filterByCategory, getPatientStatusMeta } from "./patientPageUtils";

const tabs = [
  { key: "ALL", label: "Tất cả" },
  { key: "UNREAD", label: "Chưa đọc" },
  { key: "APPOINTMENT", label: "Lịch hẹn" },
  { key: "PRESCRIPTION", label: "Đơn thuốc" },
  { key: "PAYMENT", label: "Thanh toán" },
  { key: "SYSTEM", label: "Hệ thống" },
];

const iconMap = {
  APPOINTMENT: BsCalendar2Check,
  PRESCRIPTION: BsCapsule,
  PAYMENT: BsCreditCard2Front,
  TEST: BsFileEarmarkText,
  SYSTEM: BsInfoCircle,
};

const NOTIFICATION_PAGE_SIZE = 6;

function PatientNotifications() {
  const { setNotifications: setShellNotifications } = usePatientShell();
  const [notifications, setNotifications] = useState([]);
  const [activeTab, setActiveTab] = useState("ALL");
  const [visibleCount, setVisibleCount] = useState(NOTIFICATION_PAGE_SIZE);
  const [updatingIds, setUpdatingIds] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    let mounted = true;

    const loadNotifications = async () => {
      try {
        const response = await getPatientNotifications();
        if (mounted) {
          setNotifications(response.data || []);
        }
      } catch (error) {
        console.error(error);
        if (mounted) {
          setNotifications([]);
        }
      }
    };

    loadNotifications();

    return () => {
      mounted = false;
    };
  }, []);

  const filteredNotifications = useMemo(() => {
    if (activeTab === "ALL") return notifications;
    if (activeTab === "UNREAD") return notifications.filter((notification) => !notification.read);
    return filterByCategory(notifications, activeTab);
  }, [activeTab, notifications]);

  const visibleNotifications = useMemo(() => filteredNotifications.slice(0, visibleCount), [filteredNotifications, visibleCount]);
  const hasMoreNotifications = visibleCount < filteredNotifications.length;

  const summary = useMemo(() => {
    const unread = notifications.filter((notification) => !notification.read).length;
    const appointment = notifications.filter((notification) => notification.type === "APPOINTMENT").length;
    const payment = notifications.filter((notification) => notification.type === "PAYMENT").length;

    return {
      total: notifications.length,
      unread,
      appointment,
      payment,
    };
  }, [notifications]);

  const syncNotification = (updatedNotification) => {
    setNotifications((current) =>
      current.map((notification) => (String(notification.id) === String(updatedNotification.id) ? updatedNotification : notification)),
    );

    if (setShellNotifications) {
      setShellNotifications((current) =>
        current.map((notification) => (String(notification.id) === String(updatedNotification.id) ? updatedNotification : notification)),
      );
    }
  };

  const markAsRead = async (notificationId) => {
    if (!notificationId) return;

    setError("");
    setUpdatingIds((current) => (current.includes(notificationId) ? current : [...current, notificationId]));

    try {
      const response = await markPatientNotificationAsRead(notificationId);
      const updatedNotification = response.data;
      if (updatedNotification) {
        syncNotification(updatedNotification);
      }
    } catch (markError) {
      console.error(markError);
      setError("Không thể đánh dấu thông báo đã đọc. Vui lòng thử lại.");
    } finally {
      setUpdatingIds((current) => current.filter((id) => String(id) !== String(notificationId)));
    }
  };

  const markAllAsRead = async () => {
    setError("");
    const unreadNotifications = notifications.filter((notification) => !notification.read);

    if (unreadNotifications.length === 0) {
      return;
    }

    setUpdatingIds((current) => [...new Set([...current, ...unreadNotifications.map((notification) => notification.id)])]);

    try {
      const updatedNotifications = await Promise.all(
        unreadNotifications.map(async (notification) => {
          const response = await markPatientNotificationAsRead(notification.id);
          return response.data || notification;
        }),
      );

      const updatedMap = new Map(updatedNotifications.map((notification) => [String(notification.id), notification]));
      const nextNotifications = notifications.map((notification) => updatedMap.get(String(notification.id)) || notification);

      setNotifications(nextNotifications);
      if (setShellNotifications) {
        setShellNotifications(nextNotifications);
      }
    } catch (markError) {
      console.error(markError);
      setError("Không thể đánh dấu tất cả thông báo đã đọc. Vui lòng thử lại.");
    } finally {
      setUpdatingIds((current) => current.filter((id) => !unreadNotifications.some((notification) => String(notification.id) === String(id))));
    }
  };

  return (
    <div className="patient-page">
      <div className="patient-page-header-row">
        <div>
          <p className="patient-eyebrow">Thông báo</p>
          <h2>Cập nhật lịch hẹn, đơn thuốc, thanh toán và hệ thống</h2>
        </div>
        <Button type="button" className="patient-primary-soft" onClick={markAllAsRead} disabled={notifications.every((notification) => notification.read)}>
          <BsCheck2All /> Đánh dấu tất cả đã đọc
        </Button>
      </div>

      {error && (
        <div className="patient-info-banner alert alert-warning">
          <BsInfoCircle className="me-2" />
          {error}
        </div>
      )}

      <div className="patient-summary-grid notifications">
        <Card className="patient-summary-card light-blue">
          <Card.Body>
            <div className="patient-summary-icon">
              <BsBell />
            </div>
            <div className="patient-summary-copy">
              <span>Tổng thông báo</span>
              <strong>{summary.total}</strong>
            </div>
          </Card.Body>
        </Card>
        <Card className="patient-summary-card light-red">
          <Card.Body>
            <div className="patient-summary-icon">
              <BsInfoCircle />
            </div>
            <div className="patient-summary-copy">
              <span>Chưa đọc</span>
              <strong>{summary.unread}</strong>
            </div>
          </Card.Body>
        </Card>
        <Card className="patient-summary-card light-green">
          <Card.Body>
            <div className="patient-summary-icon">
              <BsCalendar2Check />
            </div>
            <div className="patient-summary-copy">
              <span>Lịch hẹn</span>
              <strong>{summary.appointment}</strong>
            </div>
          </Card.Body>
        </Card>
        <Card className="patient-summary-card light-teal">
          <Card.Body>
            <div className="patient-summary-icon">
              <BsCreditCard2Front />
            </div>
            <div className="patient-summary-copy">
              <span>Thanh toán</span>
              <strong>{summary.payment}</strong>
            </div>
          </Card.Body>
        </Card>
      </div>

      <div className="patient-tab-header notifications">
        <Nav
          variant="tabs"
          activeKey={activeTab}
          onSelect={(eventKey) => {
            setActiveTab(eventKey || "ALL");
            setVisibleCount(NOTIFICATION_PAGE_SIZE);
          }}
          className="patient-tabs"
        >
          {tabs.map((tab) => (
            <Nav.Item key={tab.key}>
              <Nav.Link eventKey={tab.key}>{tab.label}</Nav.Link>
            </Nav.Item>
          ))}
        </Nav>
      </div>

      <section className="patient-notification-list">
        {visibleNotifications.length > 0 ? visibleNotifications.map((notification) => {
          const meta = getPatientStatusMeta(notification.read ? "READ" : "UNREAD");
          const Icon = iconMap[notification.type] || BsBell;

          return (
            <Card key={notification.id} className={`patient-notification-card ${notification.read ? "read" : "unread"}`}>
              <Card.Body>
                <div className="patient-notification-main">
                  <div className="patient-notification-icon">
                    <Icon />
                  </div>
                  <div className="patient-notification-copy">
                    <div className="patient-notification-title-row">
                      <h3>{notification.title}</h3>
                      {!notification.read && <span className="patient-notification-dot-small" />}
                    </div>
                    <p>{notification.content}</p>
                    <div className="patient-notification-meta">
                      <span>{notification.typeLabel || notification.type}</span>
                      <span>{notification.time}</span>
                    </div>
                  </div>
                </div>
                <div className="patient-notification-actions">
                  <Badge bg={meta.variant} className="patient-status-badge">
                    {meta.label}
                  </Badge>
                  {!notification.read && (
                    <Button
                      type="button"
                      variant="link"
                      className="patient-link-button"
                      onClick={() => markAsRead(notification.id)}
                      disabled={updatingIds.some((id) => String(id) === String(notification.id))}
                    >
                      {updatingIds.some((id) => String(id) === String(notification.id)) ? "Đang cập nhật..." : "Đánh dấu đã đọc"}
                    </Button>
                  )}
                </div>
              </Card.Body>
            </Card>
          );
        }) : (
          <div className="patient-empty-state">
            <h4>Chưa có thông báo</h4>
            <p>Hiện tại chưa có thông báo nào cho bộ lọc này.</p>
          </div>
        )}
      </section>

      {filteredNotifications.length > 0 && (
        <div className="patient-notification-footer">
          {hasMoreNotifications ? (
            <Button
              type="button"
              variant="light"
              className="patient-outline-button"
              onClick={() => setVisibleCount((current) => current + NOTIFICATION_PAGE_SIZE)}
            >
              Xem thông báo cũ hơn
            </Button>
          ) : (
            <div className="patient-notification-end">Đã hiển thị tất cả thông báo trong bộ lọc này.</div>
          )}
        </div>
      )}
    </div>
  );
}

export default PatientNotifications;
