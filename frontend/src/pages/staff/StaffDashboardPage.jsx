import { useCallback, useEffect, useState } from "react";
import { Alert, Button, Card, Col, Row, Spinner } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import { getStaffTestRequests, getStaffTestResults } from "../../services/staff/staffTestResultApi";
import { getErrorMessage } from "./staffPageUtils";

function StaffDashboardPage() {
  const navigate = useNavigate();
  const [pendingRequests, setPendingRequests] = useState([]);
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadDashboard = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      const [requestResponse, resultResponse] = await Promise.all([
        getStaffTestRequests(),
        getStaffTestResults(),
      ]);
      setPendingRequests(requestResponse.data || []);
      setResults(resultResponse.data || []);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadDashboard();
  }, [loadDashboard]);

  return (
    <>
      <div className="exam-page-header">
        <div className="doctor-breadcrumb">
          Nhân viên y tế <span>/</span> <strong>Tổng quan</strong>
        </div>
        <h1>Tổng quan</h1>
        <p>Theo dõi chỉ định xét nghiệm, chẩn đoán hình ảnh và nhập kết quả cho bệnh án.</p>
      </div>

      {error && <Alert variant="danger">{error}</Alert>}

      <Row className="g-3">
        <Col md={4}>
          <Card className="doctor-card h-100">
            <Card.Body>
              <p className="card-kicker">Chờ xử lý</p>
              <h2>{loading ? <Spinner animation="border" size="sm" /> : pendingRequests.length}</h2>
              <p>Bệnh án còn chỉ định xét nghiệm hoặc chẩn đoán hình ảnh chưa có kết quả.</p>
              <Button type="button" onClick={() => navigate("/staff/test-requests")}>
                Mở danh sách
              </Button>
            </Card.Body>
          </Card>
        </Col>
        <Col md={4}>
          <Card className="doctor-card h-100">
            <Card.Body>
              <p className="card-kicker">Nhập kết quả</p>
              <h2>Theo mã bệnh án</h2>
              <p>Chọn bệnh án trong danh sách chờ hoặc nhập mã bệnh án để tạo kết quả.</p>
              <Button type="button" onClick={() => navigate("/staff/test-requests")}>
                Nhập kết quả
              </Button>
            </Card.Body>
          </Card>
        </Col>
        <Col md={4}>
          <Card className="doctor-card h-100">
            <Card.Body>
              <p className="card-kicker">Lịch sử</p>
              <h2>{loading ? <Spinner animation="border" size="sm" /> : results.length}</h2>
              <p>Kết quả xét nghiệm và chẩn đoán hình ảnh đã được ghi nhận.</p>
              <Button type="button" variant="outline-primary" onClick={() => navigate("/staff/test-results")}>
                Xem lịch sử
              </Button>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </>
  );
}

export default StaffDashboardPage;
