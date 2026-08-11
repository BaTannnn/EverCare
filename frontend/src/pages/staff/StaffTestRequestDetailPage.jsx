import { useEffect, useState } from "react";
import { Alert, Button, Card, Col, Form, Row, Spinner, Table } from "react-bootstrap";
import { useNavigate, useParams } from "react-router-dom";
import { createStaffTestResult, getStaffTestRequestDetail, getStaffTestRequests } from "../../services/staff/staffTestResultApi";
import { formatDateTime, getErrorMessage } from "./staffPageUtils";

const emptyForm = {
  serviceId: "",
  resultTitle: "",
  resultContent: "",
  file: null,
  conclusion: "",
};

const MAX_RESULT_FILE_SIZE = 10 * 1024 * 1024;

const normalizeRecordKey = (value) => String(value || "").trim().toLowerCase();
const isNumericId = (value) => /^\d+$/.test(String(value || "").trim());

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

function StaffTestRequestDetailPage() {
  const { recordId } = useParams();
  const navigate = useNavigate();
  const [form, setForm] = useState(emptyForm);
  const [detail, setDetail] = useState(null);
  const [services, setServices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [fileInputKey, setFileInputKey] = useState(0);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");

  useEffect(() => {
    let active = true;

    const loadDetail = async () => {
      setLoading(true);
      setError("");

      try {
        let resolvedRecordId = recordId;

        if (!isNumericId(resolvedRecordId)) {
          const requestResponse = await getStaffTestRequests({ keyword: resolvedRecordId });
          const normalizedKey = normalizeRecordKey(resolvedRecordId);
          const matchedRequest = (requestResponse.data || []).find((request) => (
            normalizeRecordKey(request.recordCode) === normalizedKey
            || normalizeRecordKey(request.medicalRecordId) === normalizedKey
            || normalizeRecordKey(`#${request.medicalRecordId}`) === normalizedKey
          ));

          if (!matchedRequest?.medicalRecordId) {
            throw new Error("Không tìm thấy bệnh án đang chờ nhập kết quả với mã đã nhập.");
          }

          resolvedRecordId = matchedRequest.medicalRecordId;
          navigate(`/staff/test-requests/${resolvedRecordId}`, { replace: true });
        }

        const response = await getStaffTestRequestDetail(resolvedRecordId);
        if (active) {
          const nextDetail = response.data;
          const pendingServices = (nextDetail?.services || []).filter((service) => !(service.testResults || []).length);
          setDetail(nextDetail);
          setServices(pendingServices);
          setForm((current) => ({
            ...current,
            serviceId: pendingServices[0]?.serviceId || "",
            resultTitle: current.resultTitle || pendingServices[0]?.serviceName || "",
          }));
        }
      } catch (err) {
        if (active) {
          setError(getErrorMessage(err));
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    };

    loadDetail();

    return () => {
      active = false;
    };
  }, [recordId, navigate]);

  const updateField = (field, value) => {
    setForm((current) => ({ ...current, [field]: value }));
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

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!form.serviceId) {
      setNotice("Vui lòng chọn dịch vụ.");
      return;
    }

    setSaving(true);
    setNotice("");
    setError("");

    try {
      const resolvedRecordId = detail?.medicalRecord?.id;
      if (!resolvedRecordId) {
        throw new Error("Không xác định được bệnh án để lưu kết quả.");
      }

      await createStaffTestResult(resolvedRecordId, {
        serviceId: Number(form.serviceId),
        resultTitle: form.resultTitle,
        resultContent: form.resultContent,
        file: form.file,
        conclusion: form.conclusion,
      });
      const selectedServiceId = Number(form.serviceId);
      setServices((current) => current.filter((service) => Number(service.serviceId) !== selectedServiceId));
      setForm(emptyForm);
      setFileInputKey((current) => current + 1);
      setNotice("Đã lưu kết quả. Bác sĩ có thể xem kết quả trong khu vực khám bệnh.");
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
          Nhân viên y tế <span>/</span> <strong>Nhập kết quả</strong>
        </div>
        <h1>Nhập kết quả</h1>
        <p>
          Bệnh án {detail?.medicalRecord?.recordCode || `#${detail?.medicalRecord?.id || recordId}`}. Chọn dịch vụ đã được bác sĩ chỉ định và nhập kết quả.
        </p>
      </div>

      {notice && <Alert variant={notice.startsWith("Đã") ? "success" : "warning"}>{notice}</Alert>}
      {error && <Alert variant="danger">{error}</Alert>}

      {detail && (
        <Card className="doctor-card mb-3">
          <Card.Header>
            <h2>Thông tin bệnh án</h2>
          </Card.Header>
          <Card.Body>
            <Row className="g-3">
              <Col md={3}>
                <span className="muted-cell">Mã bệnh án</span>
                <strong>{detail.medicalRecord?.recordCode || `#${recordId}`}</strong>
              </Col>
              <Col md={3}>
                <span className="muted-cell">Bệnh nhân</span>
                <strong>{detail.patient?.fullName || "--"}</strong>
              </Col>
              <Col md={3}>
                <span className="muted-cell">Bác sĩ chỉ định</span>
                <strong>{detail.doctorName || "--"}</strong>
              </Col>
              <Col md={3}>
                <span className="muted-cell">Ngày khám</span>
                <strong>{formatDateTime(detail.medicalRecord?.visitDate)}</strong>
              </Col>
            </Row>
          </Card.Body>
        </Card>
      )}

      <Card className="doctor-card mb-3">
        <Card.Header>
          <h2>Dịch vụ chờ nhập kết quả</h2>
        </Card.Header>
        <Card.Body className="p-0">
          {loading ? (
            <div className="text-center py-4">
              <Spinner animation="border" role="status" />
            </div>
          ) : services.length === 0 ? (
            <Alert variant="info" className="m-3">
              Bệnh án này không còn dịch vụ nào đang chờ nhập kết quả.
            </Alert>
          ) : (
            <Table responsive hover className="doctor-table mb-0">
              <thead>
                <tr>
                  <th>Dịch vụ</th>
                  <th>Loại</th>
                  <th>Ghi chú</th>
                  <th>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {services.map((service) => (
                  <tr key={service.id || service.serviceId}>
                    <td>{service.serviceName || "--"}</td>
                    <td>{service.serviceType || "--"}</td>
                    <td>{service.resultSummary || "--"}</td>
                    <td>
                      <Button
                        type="button"
                        size="sm"
                        variant="outline-primary"
                        onClick={() =>
                          setForm((current) => ({
                            ...current,
                            serviceId: service.serviceId,
                            resultTitle: current.resultTitle || service.serviceName || "",
                          }))
                        }
                      >
                        Chọn
                      </Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </Table>
          )}
        </Card.Body>
      </Card>

      <Card className="doctor-card">
        <Card.Header>
          <h2>Phiếu kết quả</h2>
        </Card.Header>
        <Card.Body>
          {loading ? (
            <div className="text-center py-4">
              <Spinner animation="border" role="status" />
              <div className="mt-2">Đang tải chỉ định...</div>
            </div>
          ) : (
            <Form onSubmit={handleSubmit}>
              <Row className="g-3">
                <Col md={6}>
                  <Form.Group controlId="staffServiceId">
                    <Form.Label>Dịch vụ</Form.Label>
                    <Form.Select
                      value={form.serviceId}
                      onChange={(e) => updateField("serviceId", e.target.value)}
                      disabled={saving}
                    >
                      <option value="">Chọn dịch vụ</option>
                      {services.map((service) => (
                        <option value={service.serviceId} key={service.id || service.serviceId}>
                          {service.serviceName} - {service.serviceType || "Dịch vụ"}
                        </option>
                      ))}
                    </Form.Select>
                  </Form.Group>
                </Col>
                <Col md={6}>
                  <Form.Group controlId="staffResultTitle">
                    <Form.Label>Tên kết quả</Form.Label>
                    <Form.Control
                      value={form.resultTitle}
                      onChange={(e) => updateField("resultTitle", e.target.value)}
                      placeholder="Ví dụ: Kết quả xét nghiệm máu"
                      disabled={saving}
                    />
                  </Form.Group>
                </Col>
                <Col xs={12}>
                  <Form.Group controlId="staffResultContent">
                    <Form.Label>Nội dung kết quả</Form.Label>
                    <Form.Control
                      as="textarea"
                      rows={5}
                      value={form.resultContent}
                      onChange={(e) => updateField("resultContent", e.target.value)}
                      placeholder="Nhập chỉ số, mô tả hình ảnh hoặc nội dung chuyên môn..."
                      disabled={saving}
                    />
                  </Form.Group>
                </Col>
                <Col md={6}>
                  <Form.Group controlId="staffResultFile">
                    <Form.Label>Tệp kết quả PDF</Form.Label>
                    <Form.Control
                      key={fileInputKey}
                      type="file"
                      accept="application/pdf,.pdf"
                      onChange={handleFileChange}
                      disabled={saving}
                    />
                    <Form.Text className="text-muted">
                      Tệp sẽ được upload lên Cloudinary khi lưu kết quả. Tối đa 10MB.
                    </Form.Text>
                  </Form.Group>
                </Col>
                <Col md={6}>
                  <Form.Group controlId="staffConclusion">
                    <Form.Label>Kết luận</Form.Label>
                    <Form.Control
                      value={form.conclusion}
                      onChange={(e) => updateField("conclusion", e.target.value)}
                      placeholder="Kết luận của nhân viên y tế"
                      disabled={saving}
                    />
                  </Form.Group>
                </Col>
              </Row>

              <div className="mt-3 d-flex gap-2">
                <Button type="submit" disabled={saving || !form.serviceId}>
                  {saving ? "Đang lưu..." : "Lưu kết quả"}
                </Button>
                <Button type="button" variant="outline-secondary" onClick={() => navigate("/staff/test-requests")} disabled={saving}>
                  Quay lại
                </Button>
              </div>
            </Form>
          )}
        </Card.Body>
      </Card>
    </>
  );
}

export default StaffTestRequestDetailPage;
