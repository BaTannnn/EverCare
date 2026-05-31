import { useEffect, useMemo, useState } from "react";
import { Badge, Button, Card, Modal, Nav } from "react-bootstrap";
import { BsArrowRight, BsDownload, BsEyedropper, BsFileEarmarkArrowDown, BsFlask, BsWater } from "react-icons/bs";
import { getPatientTestResults } from "../../services/patient/patientTestResultApi";
import { countByStatus, getPatientStatusMeta } from "./patientPageUtils";

function PatientTestResults() {
  const [results, setResults] = useState([]);
  const [activeTab, setActiveTab] = useState("ALL");
  const [selectedResult, setSelectedResult] = useState(null);

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

  useEffect(() => {
    if (activeTab !== "ALL" && !tabs.includes(activeTab)) {
      setActiveTab("ALL");
    }
  }, [activeTab, tabs]);

  const filtered = useMemo(() => {
    if (activeTab === "ALL") return results;
    return results.filter((item) => item.category === activeTab);
  }, [activeTab, results]);

  return (
    <div className="patient-page">
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
        <Nav variant="pills" activeKey={activeTab} onSelect={(eventKey) => setActiveTab(eventKey || "ALL")} className="patient-pills">
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
                  <Button type="button" variant="light" className="patient-outline-button">
                    <BsFileEarmarkArrowDown /> Tải kết quả
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

      <Modal show={Boolean(selectedResult)} onHide={() => setSelectedResult(null)} centered animation={false}>
        <Modal.Header closeButton>
          <Modal.Title>Chi tiết xét nghiệm</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {selectedResult && (
            <div className="patient-modal-details">
              <h4>{selectedResult.name}</h4>
              <p>{selectedResult.categoryLabel || selectedResult.category}</p>
              <div className="patient-modal-note">{selectedResult.resultTitle}</div>
              {selectedResult.serviceName && <div className="patient-modal-note mt-3">{selectedResult.serviceName}</div>}
              {selectedResult.resultContent && <div className="patient-modal-note mt-3">{selectedResult.resultContent}</div>}
              <p className="mt-3">{selectedResult.conclusion}</p>
              <strong>Kết luận chuyên môn:</strong>
              <div className="patient-modal-note">{selectedResult.conclusion}</div>
              {selectedResult.fileUrl && (
                <div className="mt-3">
                  <Button type="button" variant="outline-primary" as="a" href={selectedResult.fileUrl} target="_blank" rel="noreferrer">
                    Mở file đính kèm
                  </Button>
                </div>
              )}
            </div>
          )}
        </Modal.Body>
      </Modal>
    </div>
  );
}

export default PatientTestResults;
