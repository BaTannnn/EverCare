import { Button, Card, Container } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/useAuth";

function AdminDashboard() {
  const navigate = useNavigate();
  const { logout, user } = useAuth();

  const handleLogout = () => {
    logout();
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
