import { Card } from "react-bootstrap";
import cookies from "react-cookies";

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

function PharmacistSettingsPage() {
  const user = readSavedUser();

  return (
    <>
      <div className="exam-page-header">
        <div className="doctor-breadcrumb">Dược sĩ <span>/</span> <strong>Cài đặt</strong></div>
        <h1>Cài đặt</h1>
        <p>Thông tin tài khoản dược sĩ.</p>
      </div>

      <Card className="doctor-card">
        <Card.Body>
          <p><strong>Họ tên:</strong> {user?.fullName || "--"}</p>
          <p><strong>Tên đăng nhập:</strong> {user?.username || "--"}</p>
          <p><strong>Email:</strong> {user?.email || "--"}</p>
        </Card.Body>
      </Card>
    </>
  );
}

export default PharmacistSettingsPage;
