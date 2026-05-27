import { Button, Card, Container } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
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

function AdminDashboard() {
  const navigate = useNavigate();
  const user = readSavedUser();

  const handleLogout = () => {
    cookies.remove("token", { path: "/" });
    cookies.remove("user", { path: "/" });
    cookies.remove("role", { path: "/" });
    navigate("/login", { replace: true });
  };

  return (
    <Container fluid className="dashboard-shell">
      <Card className="dashboard-panel">
        <Card.Body>
          <div className="brand-mark">EverCare</div>
          <h1 className="dashboard-title">Dashboard</h1>
          <p className="dashboard-copy">
            {user?.fullName || user?.username
              ? `Xin chào, ${user.fullName || user.username}.`
              : "Bạn đã đăng nhập thành công."}
          </p>
          <Button type="button" variant="outline-danger" onClick={handleLogout}>
            Đăng xuất
          </Button>
        </Card.Body>
      </Card>
    </Container>
  );
}

export default AdminDashboard;
