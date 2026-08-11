import { useCallback, useEffect, useState } from "react";
import { Alert, Button, Card, Col, Form, Modal, Row, Spinner, Table } from "react-bootstrap";
import EmptyState from "../../components/common/EmptyState";
import {
  getStaffTestResultDetail,
  getStaffTestResults,
  updateStaffTestResult,
} from "../../services/staff/staffTestResultApi";
import { formatDateTime, getErrorMessage } from "./staffPageUtils";

const emptyForm = {
  resultId: "",
  resultCode: "",
  serviceId: "",
  serviceName: "",
  resultTitle: "",
  resultContent: "",
  fileUrl: "",
  file: null,
  conclusion: "",
};

const MAX_RESULT_FILE_SIZE = 10 * 1024 * 1024;
const PAGE_SIZE = 10;

const validatePdfFile = (file) => {
  if (!file) {
    return "";
  }

  if (file.type && file.type !== "application/pdf") {
    return "File kết quả phải là PDF.";
  }

  if (!file.name.toLowerCase().endsWith(".pdf")) {
    return "File kết quả phải có đuôi .pdf.";
  }

  if (file.size > MAX_RESULT_FILE_SIZE) {
    return "File kết quả không được vượt quá 10MB.";
  }

  return "";
};

function StaffTestResultsPage() {
  const [form, setForm] = useState(emptyForm);
  const [filters, setFilters] = useState({ recordId: "", serviceId: "", fromDate: "", toDate: "" });
  const [results, setResults] = useState([]);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [showUpdateModal, setShowUpdateModal] = useState(false);
  const [fileInputKey, setFileInputKey] = useState(0);
  const [notice, setNotice] = useState("");
  const [error, setError] = useState("");

  const loadResults = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      const params = Object.fromEntries(
        Object.entries(filters).filter(([, value]) => String(value || "").trim())
      );
      const response = await getStaffTestResults({ ...params, page, size: PAGE_SIZE });
      setResults(response.data || []);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [filters, page]);

  useEffect(() => {
    loadResults();
  }, [loadResults]);

  useEffect(() => {
    setPage(1);
  }, [filters]);

  const canGoPrev = page > 1;
  const canGoNext = results.length >= PAGE_SIZE;

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
        resultCode: result.resultCode || "",
        serviceId: result.serviceId ? String(result.serviceId) : "",
        serviceName: result.serviceName || "",
        resultTitle: result.resultTitle || "",
        resultContent: result.resultContent || "",
        fileUrl: result.fileUrl || "",
        file: null,
        conclusion: result.conclusion || "",
      });
      setFileInputKey((current) => current + 1);
      setShowUpdateModal(true);
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
        resultTitle: form.resultTitle,
        resultContent: form.resultContent,
        file: form.file,
        conclusion: form.conclusion,
      });
      setNotice("Đã cập nhật kết quả.");
      loadResults();
      setShowUpdateModal(false);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  const handleFileChange = (e) => {
    const file = e.target.files?.[0] || null;
    const validationMessage = validatePdfFile(file);
    setNotice(validationMessage);
    updateField("file", validationMessage ? null : file);
    if (validationMessage) {
      e.target.value = "";
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
            <>
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

      <Modal show={showUpdateModal} onHide={() => setShowUpdateModal(false)} size="lg" centered>
        <Form onSubmit={handleUpdate}>
          <Modal.Header closeButton>
            <Modal.Title>Cập nhật kết quả</Modal.Title>
          </Modal.Header>
          <Modal.Body>
            <Row className="g-3">
              <Col md={4}>
                <Form.Group controlId="staffResultId">
                  <Form.Label>Mã kết quả</Form.Label>
                  <Form.Control
                    value={form.resultCode || (form.resultId ? `#${form.resultId}` : "")}
                    disabled
                  />
                </Form.Group>
              </Col>
              <Col md={4}>
                <Form.Group controlId="staffHistoryServiceId">
                  <Form.Label>Dịch vụ</Form.Label>
                  <Form.Control
                    value={form.serviceName || (form.serviceId ? `#${form.serviceId}` : "")}
                    disabled
                  />
                </Form.Group>
              </Col>
              <Col md={4}>
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
                <Form.Group controlId="staffHistoryResultFile">
                  <Form.Label>Tệp kết quả PDF</Form.Label>
                  <Form.Control
                    key={fileInputKey}
                    type="file"
                    accept="application/pdf,.pdf"
                    onChange={handleFileChange}
                    disabled={saving}
                  />
                  {form.fileUrl && (
                    <Form.Text className="text-muted d-block">
                      Đang có tệp kết quả. Chọn PDF mới nếu cần thay thế.
                    </Form.Text>
                  )}
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
          </Modal.Body>
          <Modal.Footer>
            <Button type="button" variant="outline-secondary" onClick={() => setShowUpdateModal(false)} disabled={saving}>
              Hủy
            </Button>
            <Button type="submit" disabled={saving || !form.resultId.trim()}>
              {saving ? "Đang cập nhật..." : "Cập nhật kết quả"}
            </Button>
          </Modal.Footer>
        </Form>
      </Modal>
    </>
  );
}

export default StaffTestResultsPage;
