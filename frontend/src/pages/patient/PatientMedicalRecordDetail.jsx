import { useEffect, useMemo, useState } from "react";
import { Alert, Badge, Button, Card, Modal } from "react-bootstrap";
import {
  BsArrowLeft,
  BsArrowRight,
  BsCalendar3,
  BsCapsule,
  BsCreditCard2Front,
  BsDownload,
  BsFileEarmarkArrowDown,
  BsFileEarmarkMedical,
  BsFlask,
  BsPerson,
  BsPrinter,
} from "react-icons/bs";
import { useNavigate, useParams } from "react-router-dom";
import { getPatientMedicalRecordById } from "../../services/patient/patientMedicalRecordApi";
import { getPatientTestResultFile } from "../../services/patient/patientTestResultApi";
import { formatCurrency, getPatientStatusMeta } from "./patientPageUtils";

const buildResultFilename = (result) => {
  const label = result?.resultCode || result?.name || result?.resultTitle || "ket-qua-xet-nghiem";
  const safeLabel = String(label)
    .trim()
    .replace(/[^\p{L}\p{N}]+/gu, "-")
    .replace(/^-+|-+$/g, "")
    .toLowerCase();

  return `${safeLabel || "ket-qua-xet-nghiem"}.pdf`;
};

function PatientMedicalRecordDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [record, setRecord] = useState(null);
  const [prescription, setPrescription] = useState(null);
  const [invoice, setInvoice] = useState(null);
  const [showTestResults, setShowTestResults] = useState(false);
  const [downloadingResultId, setDownloadingResultId] = useState(null);
  const [fileError, setFileError] = useState("");

  useEffect(() => {
    let mounted = true;

    const loadRecord = async () => {
      try {
        const response = await getPatientMedicalRecordById(id);
        const nextRecord = response.data || null;

        if (mounted) {
          setRecord(nextRecord);
          setPrescription(nextRecord?.prescription || null);
          setInvoice(nextRecord?.invoice || null);
        }
      } catch (error) {
        console.error(error);
        if (mounted) {
          setRecord(null);
          setPrescription(null);
          setInvoice(null);
        }
      }
    };

    loadRecord();

    return () => {
      mounted = false;
    };
  }, [id]);

  const statusMeta = useMemo(() => getPatientStatusMeta(record?.paymentStatus || record?.status), [record]);
  const testResults = useMemo(() => record?.testResults || [], [record]);
  const prescriptionStatusMeta = useMemo(() => getPatientStatusMeta(prescription?.status), [prescription]);
  const invoiceStatusMeta = useMemo(() => getPatientStatusMeta(invoice?.status || invoice?.paymentStatus), [invoice]);
  const prescriptionItems = useMemo(() => prescription?.items || [], [prescription]);

  const goToInvoice = () => {
    if (!invoice?.id) {
      navigate("/patient/invoices");
      return;
    }

    navigate("/patient/invoices", {
      state: {
        invoiceId: invoice.id,
        action: invoice.status === "UNPAID" ? "pay" : "detail",
      },
    });
  };

  const downloadTestResultFile = async (result) => {
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
      setFileError("Không thể tải file kết quả xét nghiệm. Vui lòng thử lại sau.");
    } finally {
      setDownloadingResultId(null);
    }
  };

  if (!record) {
    return <div className="patient-loading-panel">Không tìm thấy bệnh án.</div>;
  }

  return (
    <div className="patient-page">
      <div className="patient-page-header-row align-start">
        <div>
          <p className="patient-eyebrow">{record.recordCode}</p>
          <h2>Chi tiết bệnh án ngày {record.displayVisitDate || record.visitDate}</h2>
        </div>
        <div className="patient-header-actions">
          <Button type="button" variant="light" className="patient-outline-button" onClick={() => navigate("/patient/medical-records")}>
            <BsArrowLeft /> Quay lại
          </Button>
          <Button type="button" variant="light" className="patient-outline-button">
            <BsPrinter /> In bệnh án
          </Button>
          <Button type="button" variant="light" className="patient-outline-button">
            <BsDownload /> Tải PDF
          </Button>
        </div>
      </div>

      {fileError && <Alert variant="warning">{fileError}</Alert>}

      <section className="patient-detail-grid">
        <Card className="patient-detail-main">
          <Card.Body>
            <div className="patient-detail-top">
              <div>
                <p>Bác sĩ phụ trách</p>
                <h3>{record.doctorName}</h3>
                <span>{record.departmentName}</span>
              </div>
              <Badge bg={statusMeta.variant} className="patient-status-badge">
                {statusMeta.label}
              </Badge>
            </div>

            <div className="patient-detail-block">
              <h4>Triệu chứng</h4>
              <p>{record.chiefComplaint || "Chưa có dữ liệu"}</p>
            </div>
            <div className="patient-detail-block">
              <h4>Chẩn đoán</h4>
              <p>{record.diagnosis || "Chưa có dữ liệu"}</p>
            </div>
            <div className="patient-detail-block">
              <h4>Kế hoạch điều trị</h4>
              <p>{record.treatmentPlan || "Chưa có dữ liệu"}</p>
            </div>
            <div className="patient-detail-block">
              <h4>Ghi chú / Lời dặn</h4>
              <p>{record.doctorNote || "Chưa có dữ liệu"}</p>
            </div>
          </Card.Body>
        </Card>

        <div className="patient-detail-side">
          <Card className="patient-detail-side-card">
            <Card.Body>
              <div className="patient-card-head">
                <div className="patient-card-icon">
                  <BsFileEarmarkMedical />
                </div>
                <h3>Dịch vụ đã sử dụng</h3>
              </div>
              {(record.services || []).length > 0 ? (
                <ul className="patient-bullet-list">
                  {(record.services || []).map((service) => (
                    <li key={service.id}>
                      <span>{service.name}</span>
                      <strong>{formatCurrency(service.price)}</strong>
                    </li>
                  ))}
                </ul>
              ) : (
                <p>Chưa có dịch vụ liên quan.</p>
              )}
            </Card.Body>
          </Card>

          <Card className="patient-detail-side-card">
            <Card.Body>
              <div className="patient-card-head">
                <div className="patient-card-icon">
                  <BsFileEarmarkMedical />
                </div>
                <h3>Kết quả xét nghiệm liên quan</h3>
              </div>
              {testResults.length > 0 ? (
                <>
                  <ul className="patient-result-list">
                    {testResults.map((test) => (
                      <li key={test.id}>
                        <span>{test.resultTitle}</span>
                        <strong>{test.conclusion || "Chưa có kết quả"}</strong>
                      </li>
                    ))}
                  </ul>
                  <Button type="button" variant="link" className="patient-inline-link p-0 mt-2" onClick={() => setShowTestResults(true)}>
                    Chi tiết xét nghiệm <BsArrowRight />
                  </Button>
                </>
              ) : (
                <p>Chưa có kết quả xét nghiệm liên quan.</p>
              )}
            </Card.Body>
          </Card>

          <Card className="patient-detail-side-card">
            <Card.Body>
              <div className="patient-card-head">
                <div className="patient-card-icon">
                  <BsCapsule />
                </div>
                <h3>Đơn thuốc liên quan</h3>
              </div>
              {prescription ? (
                <div className="patient-linked-detail">
                  <div className="patient-linked-detail-head">
                    <div>
                      <strong>{prescription.prescriptionCode}</strong>
                      <span>{prescription.displayDate || prescription.date || "Chưa có ngày kê"}</span>
                    </div>
                    <Badge bg={prescriptionStatusMeta.variant} className="patient-status-badge">
                      {prescriptionStatusMeta.label}
                    </Badge>
                  </div>
                  <div className="patient-linked-detail-meta">
                    <span>Bác sĩ: {prescription.doctorName || record.doctorName}</span>
                    <span>{prescriptionItems.length} loại thuốc</span>
                    {prescription.note && <span>Ghi chú: {prescription.note}</span>}
                  </div>
                  {prescriptionItems.length > 0 ? (
                    <div className="patient-linked-medicine-list">
                      {prescriptionItems.map((medicine) => (
                        <div key={medicine.id} className="patient-medicine-row compact">
                          <strong>{medicine.name}</strong>
                          <span>
                            Số lượng: {medicine.quantity} {medicine.unit || ""}
                          </span>
                          <span>Liều dùng: {medicine.dosage || "Theo chỉ định"}</span>
                          <span>Tần suất: {medicine.frequency || "Chưa ghi"}</span>
                          {medicine.duration && <span>Thời gian: {medicine.duration}</span>}
                          {medicine.instruction && <p>{medicine.instruction}</p>}
                        </div>
                      ))}
                    </div>
                  ) : (
                    <p>Đơn thuốc chưa có danh sách thuốc chi tiết.</p>
                  )}
                </div>
              ) : (
                <p>Chưa có đơn thuốc liên quan.</p>
              )}
            </Card.Body>
          </Card>

          <Card className="patient-detail-side-card">
            <Card.Body>
              <div className="patient-card-head">
                <div className="patient-card-icon">
                  <BsCreditCard2Front />
                </div>
                <h3>Hóa đơn liên quan</h3>
              </div>
              {invoice ? (
                <div className="patient-linked-detail">
                  <div className="patient-linked-detail-head">
                    <div>
                      <strong>{invoice.invoiceCode}</strong>
                      <span>{invoice.createdAt || "Chưa có ngày tạo"}</span>
                    </div>
                    <Badge bg={invoiceStatusMeta.variant} className="patient-status-badge">
                      {invoiceStatusMeta.label}
                    </Badge>
                  </div>
                  <div className="patient-invoice-total compact">{formatCurrency(invoice.totalAmount)}</div>
                  <div className="patient-linked-detail-meta">
                    <span>Khám/dịch vụ: {formatCurrency(invoice.serviceAmount)}</span>
                    <span>Xét nghiệm: {formatCurrency(invoice.testAmount)}</span>
                    <span>Thuốc: {formatCurrency(invoice.medicineAmount)}</span>
                    {Number(invoice.discountAmount || 0) > 0 && <span>Giảm trừ: {formatCurrency(invoice.discountAmount)}</span>}
                  </div>
                  <Button type="button" className="patient-primary-soft w-100" onClick={goToInvoice}>
                    {invoice.status === "UNPAID" ? "Đi tới thanh toán" : "Xem hóa đơn"} <BsArrowRight />
                  </Button>
                </div>
              ) : (
                <>
                  <p>Chưa có hóa đơn liên quan.</p>
                  <Button type="button" variant="link" className="patient-inline-link p-0" onClick={goToInvoice}>
                    Mở trang hóa đơn <BsArrowRight />
                  </Button>
                </>
              )}
            </Card.Body>
          </Card>
        </div>
      </section>

      <Modal
        show={showTestResults}
        onHide={() => setShowTestResults(false)}
        size="lg"
        centered
        animation={false}
        dialogClassName="patient-related-test-dialog patient-test-result-dialog"
      >
        <Modal.Header closeButton>
          <Modal.Title>Kết quả xét nghiệm của phiếu khám</Modal.Title>
        </Modal.Header>
        <Modal.Body className="patient-related-test-body patient-test-result-detail-body">
          {testResults.length > 0 ? (
            <div className="patient-related-test-list">
              {testResults.map((test) => (
                <Card key={test.id} className="patient-result-card patient-related-test-card">
                  <Card.Body>
                    <div className="patient-test-result-detail">
                      <div className="patient-test-result-hero">
                        <div className="patient-test-result-hero-icon">
                          <BsFlask />
                        </div>
                        <div>
                          <span>{test.serviceName || "Xét nghiệm"}</span>
                          <h4>{test.resultTitle}</h4>
                          {test.resultCode && <p>Mã kết quả: {test.resultCode}</p>}
                        </div>
                        {test.resultCode && (
                          <Badge bg="light" text="dark" className="patient-result-code-badge">
                            {test.resultCode}
                          </Badge>
                        )}
                      </div>

                      <div className="patient-test-result-info-grid">
                        <div>
                          <span>
                            <BsCalendar3 /> Ngày kết quả
                          </span>
                          <strong>{test.resultDate || test.date || "Chưa có ngày"}</strong>
                        </div>
                        <div>
                          <span>
                            <BsPerson /> Người thực hiện
                          </span>
                          <strong>{test.performedByName || test.doctorName || "Chưa cập nhật"}</strong>
                        </div>
                        <div>
                          <span>Dịch vụ</span>
                          <strong>{test.serviceName || test.resultTitle || "Chưa cập nhật"}</strong>
                        </div>
                      </div>

                      <div className="patient-test-result-section">
                        <span>Nội dung kết quả</span>
                        <div className="patient-modal-note">{test.resultContent || "Chưa có nội dung chi tiết."}</div>
                      </div>

                      <div className="patient-test-result-section important">
                        <span>Kết luận chuyên môn</span>
                        <div className="patient-modal-note">{test.conclusion || "Chưa có kết luận."}</div>
                      </div>
                    </div>
                    <div className="patient-result-actions">
                      <Button
                        type="button"
                        variant="light"
                        className="patient-outline-button"
                        disabled={!test.id || downloadingResultId === test.id}
                        onClick={() => downloadTestResultFile(test)}
                      >
                        <BsFileEarmarkArrowDown /> {downloadingResultId === test.id ? "Đang tải..." : "Tải kết quả"}
                      </Button>
                    </div>
                  </Card.Body>
                </Card>
              ))}
            </div>
          ) : (
            <p>Chưa có kết quả xét nghiệm liên quan.</p>
          )}
        </Modal.Body>
      </Modal>
    </div>
  );
}

export default PatientMedicalRecordDetail;
