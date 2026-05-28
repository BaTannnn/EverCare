/* eslint-disable react-hooks/set-state-in-effect */
import { useCallback, useEffect, useState } from "react";
import { Alert, Button, Card, Col, Form, Row, Spinner, Table } from "react-bootstrap";
import EmptyState from "../../components/common/EmptyState";
import {
  getStaffTestResultDetail,
  getStaffTestResults,
  updateStaffTestResult,
} from "../../services/staff/staffTestResultApi";
import { formatDateTime, getErrorMessage } from "./staffPageUtils";

const emptyForm = {
  resultId: "",
  serviceId: "",
  resultTitle: "",
  resultContent: "",
  fileUrl: "",
  conclusion: "",
};

function StaffTestResultsPage() {
  const [form, setForm] = useState(emptyForm);
  const [filters, setFilters] = useState({ recordId: "", serviceId: "", fromDate: "", toDate: "" });
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [notice, setNotice] = useState("");
  const [error, setError] = useState("");

  const loadResults = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      const params = Object.fromEntries(
        Object.entries(filters).filter(([, value]) => String(value || "").trim())
      );
      const response = await getStaffTestResults(params);
      setResults(response.data || []);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [filters]);

  useEffect(() => {
    loadResults();
  }, [loadResults]);

  const updateField = (field, value) => {
    setForm((current) => ({ ...current, [field]: value }));
  };

  const updateFilter = (field, value) => {
    setFilters((current) => ({ ...current, [field]: value }));
  };

  const loadResultForEdit = async (resultId) => {
    setSaving(true);
    setNotice("");
    setError("");

    try {
      const response = await getStaffTestResultDetail(resultId);
      const result = response.data;
      setForm({
        resultId: result.id ? String(result.id) : "",
        serviceId: result.serviceId ? String(result.serviceId) : "",
        resultTitle: result.resultTitle || "",
        resultContent: result.resultContent || "",
        fileUrl: result.fileUrl || "",
        conclusion: result.conclusion || "",
      });
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  const handleUpdate = async (e) => {
    e.preventDefault();

    if (!form.resultId.trim()) {
      setNotice("Vui lòng nhập mã kết quả cần cập nhật.");
      return;
    }

    setSaving(true);
    setNotice("");
    setError("");

    try {
      await updateStaffTestResult(form.resultId.trim(), {
        serviceId: form.serviceId ? Number(form.serviceId) : null,
        resultTitle: form.resultTitle,
        resultContent: form.resultContent,
        fileUrl: form.fileUrl,
        conclusion: form.conclusion,
      });
      setNotice("Đã cập nhật kết quả.");
      loadResults();
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  return (
    <>
      <div className="exam-page-header">
        <div className="doctor-breadcrumb">
          Nhân viên y tế <span>/</span> <strong>Lịch sử kết quả</strong>
        </div>
        <h1>Lịch sử kết quả</h1>
      <p>Theo dõi các kết quả xét nghiệm và chẩn đoán hình ảnh đã nhập.</p>
      </div>

      {notice && <Alert variant={notice.startsWith("Đã") ? "success" : "warning"}>{notice}</Alert>}
      {error && <Alert variant="danger">{error}</Alert>}

      <Card className="doctor-card mb-3">
        <Card.Header>
          <h2>Bộ lọc lịch sử</h2>
        </Card.Header>
        <Card.Body>
          <Form
            onSubmit={(e) => {
              e.preventDefault();
              loadResults();
            }}
          >
            <Row className="g-3">
              <Col md={3}>
                <Form.Group controlId="resultFilterRecordId">
                  <Form.Label>Mã bệnh án</Form.Label>
                  <Form.Control
                    value={filters.recordId}
                    onChange={(e) => updateFilter("recordId", e.target.value)}
                    placeholder="Bỏ trống để xem tất cả"
                  />
                </Form.Group>
              </Col>
              <Col md={3}>
                <Form.Group controlId="resultFilterServiceId">
                  <Form.Label>Mã dịch vụ</Form.Label>
                  <Form.Control
                    value={filters.serviceId}
                    onChange={(e) => updateFilter("serviceId", e.target.value)}
                    placeholder="Bỏ trống để xem tất cả"
                  />
                </Form.Group>
              </Col>
              <Col md={3}>
                <Form.Group controlId="resultFilterFromDate">
                  <Form.Label>Từ ngày</Form.Label>
                  <Form.Control type="date" value={filters.fromDate} onChange={(e) => updateFilter("fromDate", e.target.value)} />
                </Form.Group>
              </Col>
              <Col md={3}>
                <Form.Group controlId="resultFilterToDate">
                  <Form.Label>Đến ngày</Form.Label>
                  <Form.Control type="date" value={filters.toDate} onChange={(e) => updateFilter("toDate", e.target.value)} />
                </Form.Group>
              </Col>
            </Row>
            <div className="mt-3 d-flex gap-2">
              <Button type="submit">Lọc kết quả</Button>
              <Button
                type="button"
                variant="outline-secondary"
                onClick={() => setFilters({ recordId: "", serviceId: "", fromDate: "", toDate: "" })}
              >
                Xóa lọc
              </Button>
            </div>
          </Form>
        </Card.Body>
      </Card>

      <Card className="doctor-card mb-3">
        <Card.Header>
          <h2>Cập nhật kết quả theo mã</h2>
        </Card.Header>
        <Card.Body>
          <Form onSubmit={handleUpdate}>
            <Row className="g-3">
              <Col md={3}>
                <Form.Group controlId="staffResultId">
                  <Form.Label>Mã kết quả</Form.Label>
                  <Form.Control
                    value={form.resultId}
                    onChange={(e) => updateField("resultId", e.target.value)}
                    placeholder="Nhập mã kết quả"
                    disabled={saving}
                  />
                </Form.Group>
              </Col>
              <Col md={3}>
                <Form.Group controlId="staffHistoryServiceId">
                  <Form.Label>Mã dịch vụ</Form.Label>
                  <Form.Control
                    type="number"
                    min="1"
                    value={form.serviceId}
                    onChange={(e) => updateField("serviceId", e.target.value)}
                    placeholder="Có thể bỏ trống"
                    disabled={saving}
                  />
                </Form.Group>
              </Col>
              <Col md={6}>
                <Form.Group controlId="staffHistoryResultTitle">
                  <Form.Label>Tên kết quả</Form.Label>
                  <Form.Control
                    value={form.resultTitle}
                    onChange={(e) => updateField("resultTitle", e.target.value)}
                    disabled={saving}
                  />
                </Form.Group>
              </Col>
              <Col xs={12}>
                <Form.Group controlId="staffHistoryResultContent">
                  <Form.Label>Nội dung kết quả</Form.Label>
                  <Form.Control
                    as="textarea"
                    rows={4}
                    value={form.resultContent}
                    onChange={(e) => updateField("resultContent", e.target.value)}
                    disabled={saving}
                  />
                </Form.Group>
              </Col>
              <Col md={6}>
                <Form.Group controlId="staffHistoryFileUrl">
                  <Form.Label>Đường dẫn tệp kết quả</Form.Label>
                  <Form.Control
                    value={form.fileUrl}
                    onChange={(e) => updateField("fileUrl", e.target.value)}
                    disabled={saving}
                  />
                </Form.Group>
              </Col>
              <Col md={6}>
                <Form.Group controlId="staffHistoryConclusion">
                  <Form.Label>Kết luận</Form.Label>
                  <Form.Control
                    value={form.conclusion}
                    onChange={(e) => updateField("conclusion", e.target.value)}
                    disabled={saving}
                  />
                </Form.Group>
              </Col>
            </Row>
            <div className="mt-3">
              <Button type="submit" disabled={saving || !form.resultId.trim()}>
                {saving ? "Đang cập nhật..." : "Cập nhật kết quả"}
              </Button>
            </div>
          </Form>
        </Card.Body>
      </Card>

      <Card className="doctor-card">
        <Card.Header>
          <h2>Danh sách kết quả</h2>
          <Button type="button" variant="link" onClick={loadResults}>
            Tải lại
          </Button>
        </Card.Header>
        <Card.Body className="p-0">
          {loading ? (
            <div className="text-center py-4">
              <Spinner animation="border" role="status" />
            </div>
          ) : results.length === 0 ? (
            <EmptyState
              title="Chưa có dữ liệu lịch sử"
              description="Các kết quả đã nhập sẽ xuất hiện tại đây."
            />
          ) : (
            <Table responsive hover className="doctor-table mb-0">
              <thead>
                <tr>
                  <th>Mã kết quả</th>
                  <th>Bệnh án</th>
                  <th>Dịch vụ</th>
                  <th>Tên kết quả</th>
                  <th>Kết luận</th>
                  <th>Ngày trả kết quả</th>
                  <th>Thực hiện bởi</th>
                  <th>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {results.map((result) => (
                  <tr key={result.id}>
                    <td>{result.resultCode || `#${result.id}`}</td>
                    <td>#{result.medicalRecordId || "--"}</td>
                    <td>{result.serviceName || "--"}</td>
                    <td>{result.resultTitle || "--"}</td>
                    <td>{result.conclusion || "--"}</td>
                    <td>{formatDateTime(result.resultDate)}</td>
                    <td>{result.performedByName || "--"}</td>
                    <td>
                      <Button type="button" size="sm" variant="outline-primary" onClick={() => loadResultForEdit(result.id)}>
                        Sửa
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

export default StaffTestResultsPage;
