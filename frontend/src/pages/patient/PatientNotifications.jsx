import { useEffect, useMemo, useState } from "react";
import { Badge, Button, Card, Nav } from "react-bootstrap";
import { BsBell, BsCalendar2Check, BsCapsule, BsCheck2All, BsCreditCard2Front, BsFileEarmarkText, BsInfoCircle } from "react-icons/bs";
import { useOutletContext } from "react-router-dom";
import { getPatientNotifications } from "../../services/patient/patientNotificationApi";
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

function PatientNotifications() {
  const { setNotifications: setShellNotifications } = useOutletContext() || {};
  const [notifications, setNotifications] = useState([]);
  const [activeTab, setActiveTab] = useState("ALL");

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

  const markAllAsRead = () => {
    setNotifications((current) => {
      const updated = current.map((notification) => ({ ...notification, read: true }));
      if (setShellNotifications) {
        setShellNotifications(updated);
      }
      return updated;
    });
  };

  return (
    <div className="patient-page">
      <div className="patient-page-header-row">
        <div>
          <p className="patient-eyebrow">Thông báo</p>
          <h2>Cập nhật lịch hẹn, đơn thuốc, thanh toán và hệ thống</h2>
        </div>
        <Button type="button" className="patient-primary-soft" onClick={markAllAsRead}>
          <BsCheck2All /> Đánh dấu tất cả đã đọc
        </Button>
      </div>

      <div className="patient-tab-header notifications">
        <Nav variant="tabs" activeKey={activeTab} onSelect={(eventKey) => setActiveTab(eventKey || "ALL")} className="patient-tabs">
          {tabs.map((tab) => (
            <Nav.Item key={tab.key}>
              <Nav.Link eventKey={tab.key}>{tab.label}</Nav.Link>
            </Nav.Item>
          ))}
        </Nav>
      </div>

      <section className="patient-notification-list">
        {filteredNotifications.length > 0 ? filteredNotifications.map((notification) => {
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
                    <span>{notification.time}</span>
                  </div>
                </div>
                <Badge bg={meta.variant} className="patient-status-badge">
                  {meta.label}
                </Badge>
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

      <div className="patient-notification-footer">
        <Button type="button" variant="light" className="patient-outline-button">
          Xem thông báo cũ hơn
        </Button>
      </div>
    </div>
  );
}

export default PatientNotifications;
