import { Card } from "react-bootstrap";
import cookies from "react-cookies";
import PageHeader from "../../components/common/PageHeader";

const readSavedUser = () => {
  const savedUser = cookies.load("user");

  if (!savedUser) {
    return null;
  }

  if (typeof savedUser === "object") {
    return savedUser;
  }

  try {
    return JSON.parse(savedUser);
  } catch {
    return null;
  }
};

function DoctorSettingsPage() {
  const user = readSavedUser();

  return (
    <>
      <PageHeader title="Cài đặt" description="Thông tin tài khoản bác sĩ đang đăng nhập." />
      <Card className="doctor-card">
        <Card.Body>
          <div className="info-row">
            <span>Họ tên</span>
            <strong>{user?.fullName || "--"}</strong>
          </div>
          <div className="info-row">
            <span>Username</span>
            <strong>{user?.username || "--"}</strong>
          </div>
          <div className="info-row">
            <span>Email</span>
            <strong>{user?.email || "--"}</strong>
          </div>
          <div className="info-row">
            <span>Số điện thoại</span>
            <strong>{user?.phone || "--"}</strong>
          </div>
        </Card.Body>
      </Card>
    </>
  );
}

export default DoctorSettingsPage;
