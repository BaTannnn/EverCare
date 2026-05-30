/* eslint-disable react-hooks/set-state-in-effect */
import { useCallback, useEffect, useMemo, useState } from "react";
import { Alert, Badge, Button, Card, Form, Spinner, Table } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import EmptyState from "../../components/common/EmptyState";
import { getStaffTestRequests } from "../../services/staff/staffTestResultApi";
import { formatDateTime, getErrorMessage } from "./staffPageUtils";

const serviceTypeLabels = {
  TEST: "Xét nghiệm",
  LAB_TEST: "Xét nghiệm",
  IMAGING: "Chẩn đoán hình ảnh",
};

function StaffTestRequestsPage() {
  const navigate = useNavigate();
  const [recordId, setRecordId] = useState("");
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadRequests = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      const response = await getStaffTestRequests();
      setRequests(response.data || []);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadRequests();
  }, [loadRequests]);

  const totalPendingServices = useMemo(
    () => requests.reduce((total, item) => total + Number(item.pendingServiceCount || 0), 0),
    [requests]
  );

  const handleSubmit = (e) => {
    e.preventDefault();

    if (!recordId.trim()) {
      return;
    }

    navigate(`/staff/test-requests/${recordId.trim()}`);
  };

  return (
    <>
      <div className="exam-page-header">
        <div className="doctor-breadcrumb">
          Nhân viên y tế <span>/</span> <strong>Chỉ định chờ xử lý</strong>
        </div>
        <h1>Chỉ định chờ xử lý</h1>
        <p>Chọn chỉ định dịch vụ hoặc nhập mã bệnh án để tạo kết quả xét nghiệm, chẩn đoán hình ảnh.</p>
      </div>

      <Alert variant="info">
        Có {requests.length} bệnh án đang chờ xử lý, tổng cộng {totalPendingServices} dịch vụ chưa có kết quả.
      </Alert>

      <Card className="doctor-card mb-3">
        <Card.Header>
          <h2>Nhập theo mã bệnh án</h2>
        </Card.Header>
        <Card.Body>
          <Form className="exam-inline-form" onSubmit={handleSubmit}>
            <Form.Control
              value={recordId}
              onChange={(e) => setRecordId(e.target.value)}
              placeholder="Nhập mã bệnh án"
              aria-label="Mã bệnh án"
            />
            <Button type="submit" disabled={!recordId.trim()}>
              Tiếp tục
            </Button>
          </Form>
        </Card.Body>
      </Card>

      <Card className="doctor-card">
        <Card.Header>
          <h2>Danh sách chờ xử lý</h2>
          <Button type="button" variant="link" onClick={loadRequests}>
            Tải lại
          </Button>
        </Card.Header>
        <Card.Body className="p-0">
          {loading ? (
            <div className="text-center py-4">
              <Spinner animation="border" role="status" />
            </div>
          ) : error ? (
            <Alert variant="danger">{error}</Alert>
          ) : requests.length === 0 ? (
            <EmptyState
              title="Không có chỉ định chờ xử lý"
              description="Các dịch vụ xét nghiệm hoặc chẩn đoán hình ảnh chưa có kết quả sẽ xuất hiện tại đây."
            />
          ) : (
            <Table responsive hover className="doctor-table mb-0">
              <thead>
                <tr>
                  <th>Mã bệnh án</th>
                  <th>Bệnh nhân</th>
                  <th>Bác sĩ</th>
                  <th>Dịch vụ chờ</th>
                  <th>Số dịch vụ chờ</th>
                  <th>Thời điểm chỉ định</th>
                  <th>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {requests.map((request) => (
                  <tr key={request.medicalRecordId}>
                    <td>{request.recordCode || `#${request.medicalRecordId}`}</td>
                    <td>
                      <strong>{request.patientName || "--"}</strong>
                      <span className="muted-cell">{request.patientCode || "--"}</span>
                    </td>
                    <td>{request.doctorName || "--"}</td>
                    <td>
                      <div className="staff-pending-service-list">
                        {(request.pendingServices || []).map((service) => (
                          <span className="staff-pending-service" key={service.id || `${request.medicalRecordId}-${service.serviceId}`}>
                            <strong>{service.serviceName || "--"}</strong>
                            <Badge bg={service.serviceType === "IMAGING" ? "info" : "primary"}>
                              {serviceTypeLabels[service.serviceType] || service.serviceType || "Dịch vụ"}
                            </Badge>
                          </span>
                        ))}
                      </div>
                    </td>
                    <td>{request.pendingServiceCount || 0}</td>
                    <td>{formatDateTime(request.requestedAt || request.visitDate)}</td>
                    <td>
                      <Button
                        type="button"
                        size="sm"
                        onClick={() => navigate(`/staff/test-requests/${request.medicalRecordId}`)}
                      >
                        Nhập kết quả
                      </Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </Table>
          )}
        </Card.Body>
      </Card>
    </>
  );
}

export default StaffTestRequestsPage;
