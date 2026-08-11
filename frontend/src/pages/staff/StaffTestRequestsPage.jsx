import { useCallback, useEffect, useMemo, useState } from "react";
import { Alert, Badge, Button, Card, Form, Spinner, Table } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import EmptyState from "../../components/common/EmptyState";
import { useDebouncedValue } from "../../hooks/useDebouncedValue";
import { getStaffTestRequests } from "../../services/staff/staffTestResultApi";
import { formatDateTime, getErrorMessage } from "./staffPageUtils";

const serviceTypeLabels = {
  TEST: "Xét nghiệm",
  LAB_TEST: "Xét nghiệm",
  IMAGING: "Chẩn đoán hình ảnh",
};

const normalizeRecordKey = (value) => String(value || "").trim().toLowerCase();
const PAGE_SIZE = 10;

function StaffTestRequestsPage() {
  const navigate = useNavigate();
  const [recordId, setRecordId] = useState("");
  const [requests, setRequests] = useState([]);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const debouncedRecordId = useDebouncedValue(recordId, 500);

  const loadRequests = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      const response = await getStaffTestRequests({
        keyword: debouncedRecordId.trim(),
        page,
        size: PAGE_SIZE,
      });
      setRequests(response.data || []);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [debouncedRecordId, page]);

  useEffect(() => {
    loadRequests();
  }, [loadRequests]);

  const totalPendingServices = useMemo(
    () => requests.reduce((total, item) => total + Number(item.pendingServiceCount || 0), 0),
    [requests]
  );

  useEffect(() => {
    setPage(1);
  }, [debouncedRecordId]);

  const canGoPrev = page > 1;
  const canGoNext = requests.length >= PAGE_SIZE;

  const handleSearchChange = (e) => {
    setRecordId(e.target.value);
    if (error) {
      setError("");
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();

    const keyword = recordId.trim();
    if (!keyword) {
      return;
    }

    const normalizedKeyword = normalizeRecordKey(keyword);
    const matchedRequest = requests.find((request) => (
      normalizeRecordKey(request.recordCode) === normalizedKeyword
      || normalizeRecordKey(request.medicalRecordId) === normalizedKeyword
      || normalizeRecordKey(`#${request.medicalRecordId}`) === normalizedKeyword
      || normalizeRecordKey(request.patientName) === normalizedKeyword
      || normalizeRecordKey(request.patientCode) === normalizedKeyword
    )) || requests[0];

    if (!matchedRequest) {
      setError("Không tìm thấy bệnh án đang chờ nhập kết quả với thông tin đã nhập.");
      return;
    }

    navigate(`/staff/test-requests/${matchedRequest.medicalRecordId}`);
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
        Đang hiển thị {requests.length} bệnh án chờ xử lý, {totalPendingServices} dịch vụ chưa có kết quả trên trang này.
      </Alert>

      <Card className="doctor-card mb-3">
        <Card.Header>
          <h2>Nhập theo mã bệnh án hoặc tên</h2>
        </Card.Header>
        <Card.Body>
          <Form className="exam-inline-form" onSubmit={handleSubmit}>
            <Form.Control
              value={recordId}
              onChange={handleSearchChange}
              placeholder="Nhập mã bệnh án hoặc tên bệnh nhân"
              aria-label="Mã bệnh án hoặc tên bệnh nhân"
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
              title={debouncedRecordId.trim() ? "Không tìm thấy bệnh án phù hợp" : "Không có chỉ định chờ xử lý"}
              description={debouncedRecordId.trim()
                ? "Thử nhập mã bệnh án, mã bệnh nhân hoặc tên bệnh nhân khác."
                : "Các dịch vụ xét nghiệm hoặc chẩn đoán hình ảnh chưa có kết quả sẽ xuất hiện tại đây."}
            />
          ) : (
            <>
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
              {(canGoPrev || canGoNext) && (
                <div className="pharmacist-pagination">
                  <Button type="button" variant="outline-primary" disabled={!canGoPrev} onClick={() => setPage(page - 1)}>
                    Trước
                  </Button>
                  <span>Trang {page}</span>
                  <Button type="button" variant="outline-primary" disabled={!canGoNext} onClick={() => setPage(page + 1)}>
                    Sau
                  </Button>
                </div>
              )}
            </>
          )}
        </Card.Body>
      </Card>
    </>
  );
}

export default StaffTestRequestsPage;
