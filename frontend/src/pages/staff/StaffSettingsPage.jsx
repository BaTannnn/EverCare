import { Card } from "react-bootstrap";
import EmptyState from "../../components/common/EmptyState";

function StaffSettingsPage() {
  return (
    <>
      <div className="exam-page-header">
        <div className="doctor-breadcrumb">
          Nhân viên y tế <span>/</span> <strong>Cài đặt</strong>
        </div>
        <h1>Cài đặt</h1>
        <p>Quản lý thông tin cá nhân và tùy chọn tài khoản.</p>
      </div>

      <Card className="doctor-card">
        <Card.Body>
          <EmptyState title="Chưa có cài đặt riêng" description="Thông tin tài khoản sẽ được bổ sung ở các phiên bản sau." />
        </Card.Body>
      </Card>
    </>
  );
}

export default StaffSettingsPage;
