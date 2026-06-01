import { useEffect, useMemo, useState } from "react";
import { Alert, Badge, Button, Card, Modal, Nav } from "react-bootstrap";
import { BsArrowRight, BsCalendar3, BsEyedropper, BsFileEarmarkArrowDown, BsFlask, BsPerson, BsWater } from "react-icons/bs";
import { getPatientTestResultFile, getPatientTestResults } from "../../services/patient/patientTestResultApi";
import { countByStatus, getPatientStatusMeta } from "./patientPageUtils";

const buildResultFilename = (result) => {
  const label = result?.resultCode || result?.name || result?.resultTitle || "ket-qua-xet-nghiem";
  const safeLabel = String(label)
    .trim()
    .replace(/[^\p{L}\p{N}]+/gu, "-")
    .replace(/^-+|-+$/g, "")
    .toLowerCase();

  return `${safeLabel || "ket-qua-xet-nghiem"}.pdf`;
};

function PatientTestResults() {
  const [results, setResults] = useState([]);
  const [activeTab, setActiveTab] = useState("ALL");
  const [selectedResult, setSelectedResult] = useState(null);
  const [downloadingResultId, setDownloadingResultId] = useState(null);
  const [fileError, setFileError] = useState("");

  useEffect(() => {
    let mounted = true;

    const loadResults = async () => {
      try {
        const response = await getPatientTestResults();
        if (mounted) {
          setResults(response.data || []);
        }
      } catch (error) {
        console.error(error);
        if (mounted) {
          setResults([]);
        }
      }
    };

    loadResults();

    return () => {
      mounted = false;
    };
  }, []);

  const summary = useMemo(
    () => ({
      total: results.length,
      normal: countByStatus(results, "NORMAL"),
      attention: countByStatus(results, "ATTENTION"),
    }),
    [results],
  );

  const tabs = useMemo(() => {
    const categories = [...new Set(results.map((item) => item.category).filter(Boolean))];
    return ["ALL", ...categories];
  }, [results]);

  const selectedTab = tabs.includes(activeTab) ? activeTab : "ALL";

  const filtered = useMemo(() => {
    if (selectedTab === "ALL") return results;
    return results.filter((item) => item.category === selectedTab);
  }, [results, selectedTab]);

  const selectedResultMeta = useMemo(() => getPatientStatusMeta(selectedResult?.status), [selectedResult]);

  const downloadResultFile = async (result) => {
    if (!result?.id || downloadingResultId) {
      return;
    }

    setDownloadingResultId(result.id);
    setFileError("");

    try {
      const response = await getPatientTestResultFile(result.id);
      const fileUrl = URL.createObjectURL(new Blob([response.data], { type: "application/pdf" }));
      const link = document.createElement("a");

      link.href = fileUrl;
      link.download = buildResultFilename(result);
      document.body.appendChild(link);
      link.click();
      link.remove();
      URL.revokeObjectURL(fileUrl);
    } catch (error) {
      console.error(error);
      setFileError("Không thể tải file kết quả. Vui lòng thử lại sau.");
    } finally {
      setDownloadingResultId(null);
    }
  };

  return (
    <div className="patient-page">
      {fileError && <Alert variant="warning">{fileError}</Alert>}

      <div className="patient-summary-grid results">
        <Card className="patient-summary-card light-blue">
          <Card.Body>
            <div className="patient-summary-icon">
              <BsFlask />
            </div>
            <div className="patient-summary-copy">
              <span>Tổng xét nghiệm</span>
              <strong>{summary.total}</strong>
            </div>
          </Card.Body>
        </Card>
        <Card className="patient-summary-card light-green">
          <Card.Body>
            <div className="patient-summary-icon">
              <BsWater />
            </div>
            <div className="patient-summary-copy">
              <span>Bình thường</span>
              <strong>{summary.normal}</strong>
            </div>
          </Card.Body>
        </Card>
        <Card className="patient-summary-card light-red">
          <Card.Body>
            <div className="patient-summary-icon">
              <BsEyedropper />
            </div>
            <div className="patient-summary-copy">
              <span>Cần lưu ý</span>
              <strong>{summary.attention}</strong>
            </div>
          </Card.Body>
        </Card>
      </div>

      <div className="patient-tab-header results">
        <Nav variant="pills" activeKey={selectedTab} onSelect={(eventKey) => setActiveTab(eventKey || "ALL")} className="patient-pills">
          {tabs.map((tab) => (
            <Nav.Item key={tab}>
              <Nav.Link eventKey={tab}>{tab === "ALL" ? "Tất cả" : tab}</Nav.Link>
            </Nav.Item>
          ))}
        </Nav>
      </div>

      <section className="patient-result-list">
        {filtered.length > 0 ? filtered.map((result) => {
          const meta = getPatientStatusMeta(result.status);

          return (
            <Card key={result.id} className="patient-result-card">
              <Card.Body>
                <div className="patient-result-main">
                  <div className="patient-result-icon">
                    <BsFlask />
                  </div>
                  <div className="patient-result-copy">
                    <div className="patient-result-title-row">
                      <h3>{result.name}</h3>
                      <Badge bg={meta.variant} className="patient-status-badge">
                        {meta.label}
                      </Badge>
                    </div>
                    <div className="patient-result-meta">
                      <span>{result.date}</span>
                      <span>{result.doctorName}</span>
                      <span>{result.categoryLabel || result.category}</span>
                    </div>
                    <p>{result.conclusion}</p>
                  </div>
                </div>
                <div className="patient-result-actions">
                  <Button type="button" className="patient-primary-soft" onClick={() => setSelectedResult(result)}>
                    Xem chi tiết <BsArrowRight />
                  </Button>
                  <Button
                    type="button"
                    variant="light"
                    className="patient-outline-button"
                    disabled={!result.id || downloadingResultId === result.id}
                    onClick={() => downloadResultFile(result)}
                  >
                    <BsFileEarmarkArrowDown /> {downloadingResultId === result.id ? "Đang tải..." : "Tải kết quả"}
                  </Button>
                </div>
              </Card.Body>
            </Card>
          );
        }) : (
          <div className="patient-empty-state">
            <h4>Chưa có kết quả xét nghiệm</h4>
            <p>Chưa có kết quả nào phù hợp với bộ lọc hiện tại.</p>
          </div>
        )}
      </section>

      <Card className="patient-info-note results-banner">
        <Card.Body>
          <div>
            <h3>Quy trình hiện đại</h3>
            <p>Chúng tôi sử dụng công nghệ xét nghiệm mới nhất đạt chuẩn ISO.</p>
          </div>
          <div className="patient-result-benefits">
            <strong>Tại sao chọn EverCare?</strong>
            <ul>
              <li>Kết quả chính xác</li>
              <li>Nhận kết quả nhanh</li>
              <li>Bảo mật thông tin tuyệt đối</li>
            </ul>
          </div>
        </Card.Body>
      </Card>

      <Modal
        show={Boolean(selectedResult)}
        onHide={() => setSelectedResult(null)}
        centered
        size="lg"
        animation={false}
        dialogClassName="patient-test-result-dialog"
      >
        <Modal.Header closeButton>
          <Modal.Title>Chi tiết xét nghiệm</Modal.Title>
        </Modal.Header>
        <Modal.Body className="patient-test-result-detail-body">
          {selectedResult && (
            <div className="patient-test-result-detail">
              <div className="patient-test-result-hero">
                <div className="patient-test-result-hero-icon">
                  <BsFlask />
                </div>
                <div>
                  <span>{selectedResult.categoryLabel || selectedResult.category || "Xét nghiệm"}</span>
                  <h4>{selectedResult.name}</h4>
                  {selectedResult.resultCode && <p>Mã kết quả: {selectedResult.resultCode}</p>}
                </div>
                <Badge bg={selectedResultMeta.variant} className="patient-status-badge">
                  {selectedResultMeta.label}
                </Badge>
              </div>

              <div className="patient-test-result-info-grid">
                <div>
                  <span>
                    <BsCalendar3 /> Ngày kết quả
                  </span>
                  <strong>{selectedResult.date || "Chưa có ngày"}</strong>
                </div>
                <div>
                  <span>
                    <BsPerson /> Người thực hiện
                  </span>
                  <strong>{selectedResult.doctorName || "Chưa cập nhật"}</strong>
                </div>
                <div>
                  <span>Dịch vụ</span>
                  <strong>{selectedResult.serviceName || selectedResult.resultTitle || "Chưa cập nhật"}</strong>
                </div>
              </div>

              <div className="patient-test-result-section">
                <span>Tên kết quả</span>
                <div className="patient-modal-note">{selectedResult.resultTitle || selectedResult.name}</div>
              </div>

              <div className="patient-test-result-section">
                <span>Nội dung kết quả</span>
                <div className="patient-modal-note">{selectedResult.resultContent || "Chưa có nội dung chi tiết."}</div>
              </div>

              <div className="patient-test-result-section important">
                <span>Kết luận chuyên môn</span>
                <div className="patient-modal-note">{selectedResult.conclusion || "Chưa có kết luận."}</div>
              </div>
            </div>
          )}
        </Modal.Body>
        <Modal.Footer>
          <Button type="button" variant="outline-secondary" onClick={() => setSelectedResult(null)}>
            Đóng
          </Button>
          {selectedResult && (
            <Button
              type="button"
              className="patient-primary-soft"
              disabled={!selectedResult.id || downloadingResultId === selectedResult.id}
              onClick={() => downloadResultFile(selectedResult)}
            >
              <BsFileEarmarkArrowDown /> {downloadingResultId === selectedResult.id ? "Đang tải..." : "Tải kết quả"}
            </Button>
          )}
        </Modal.Footer>
      </Modal>
    </div>
  );
}

export default PatientTestResults;
