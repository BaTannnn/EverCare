import { useMemo } from "react";
import { Outlet, matchPath, useLocation } from "react-router-dom";
import PatientShellProvider from "../../../contexts/PatientShellContext";
import { usePatientShell } from "../../../contexts/usePatientShell";
import PatientSidebar from "./PatientSidebar";
import PatientTopBar from "./PatientTopBar";

const routeMeta = [
  {
    pattern: "/patient/dashboard",
    title: "Tổng quan",
    subtitle: "Theo dõi lịch hẹn, hồ sơ và thanh toán của bạn trong một nơi.",
    searchPlaceholder: "Tìm kiếm dịch vụ, bác sĩ...",
  },
  {
    pattern: "/patient/profile",
    title: "Hồ sơ cá nhân",
    subtitle: "Quản lý thông tin cá nhân và dữ liệu y tế quan trọng.",
    searchPlaceholder: "Tìm kiếm thông tin hồ sơ...",
  },
  {
    pattern: "/patient/book-appointment",
    title: "Đặt lịch khám",
    subtitle: "Chọn dịch vụ, bác sĩ, thời gian và xác nhận nhanh chóng.",
    searchPlaceholder: "Tìm kiếm bác sĩ, chuyên khoa...",
  },
  {
    pattern: "/patient/appointments",
    title: "Lịch hẹn của tôi",
    subtitle: "Xem và quản lý toàn bộ lịch hẹn khám chữa bệnh.",
    searchPlaceholder: "Tìm kiếm lịch hẹn...",
  },
  {
    pattern: "/patient/medical-records",
    title: "Hồ sơ bệnh án",
    subtitle: "Tra cứu lịch sử khám bệnh và các chỉ định liên quan.",
    searchPlaceholder: "Tìm kiếm bệnh án...",
  },
  {
    pattern: "/patient/medical-records/:id",
    title: "Chi tiết bệnh án",
    subtitle: "Xem lại triệu chứng, chẩn đoán, điều trị và các liên quan.",
    searchPlaceholder: "Tìm kiếm trong hồ sơ...",
  },
  {
    pattern: "/patient/test-results",
    title: "Kết quả xét nghiệm",
    subtitle: "Theo dõi kết quả xét nghiệm, kết luận và tải file.",
    searchPlaceholder: "Tìm kiếm kết quả...",
  },
  {
    pattern: "/patient/prescriptions",
    title: "Đơn thuốc",
    subtitle: "Xem đơn thuốc đã kê và hướng dẫn sử dụng chi tiết.",
    searchPlaceholder: "Tìm kiếm đơn thuốc...",
  },
  {
    pattern: "/patient/invoices",
    title: "Hóa đơn & Thanh toán",
    subtitle: "Theo dõi hóa đơn, phương thức thanh toán và lịch sử chi tiêu.",
    searchPlaceholder: "Tìm kiếm hóa đơn...",
  },
  {
    pattern: "/patient/notifications",
    title: "Thông báo",
    subtitle: "Nhận cập nhật về lịch hẹn, đơn thuốc, thanh toán và hệ thống.",
    searchPlaceholder: "Tìm kiếm thông báo...",
  },
  {
    pattern: "/patient/support",
    title: "Tư vấn trực tuyến",
    subtitle: "Nhắn tin với lễ tân để được hỗ trợ đặt lịch và tư vấn từ xa.",
    searchPlaceholder: "Tìm kiếm cuộc trò chuyện...",
  },
];

function PatientLayoutContent() {
  const location = useLocation();
  const { profile, unreadNotifications } = usePatientShell();

  const meta = useMemo(() => {
    return (
      routeMeta.find((item) => matchPath({ path: item.pattern, end: item.pattern === "/patient/dashboard" }, location.pathname)) ||
      routeMeta[0]
    );
  }, [location.pathname]);

  return (
    <div className="patient-shell">
      <PatientSidebar profile={profile} />

      <div className="patient-main">
        <PatientTopBar
          title={meta.title}
          subtitle={meta.subtitle}
          searchPlaceholder={meta.searchPlaceholder}
          profile={profile}
          unreadCount={unreadNotifications}
        />

        <main className="patient-content">
          <Outlet />
        </main>

        <footer className="patient-footer">
          <span>© 2026 EverCare Health System. All rights reserved.</span>
          <div>
            <span>Điều khoản</span>
            <span>Quyền riêng tư</span>
            <span>Hỗ trợ 24/7</span>
          </div>
        </footer>
      </div>
    </div>
  );
}

function PatientLayout() {
  return (
    <PatientShellProvider>
      <PatientLayoutContent />
    </PatientShellProvider>
  );
}

export default PatientLayout;
