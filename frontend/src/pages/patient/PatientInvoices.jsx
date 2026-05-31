import { useEffect, useMemo, useState } from "react";
import { Alert, Badge, Button, Card, Form, Modal } from "react-bootstrap";
import {
  BsCalendar3,
  BsCapsule,
  BsCashCoin,
  BsCheckCircle,
  BsCreditCard2Front,
  BsFileEarmarkText,
  BsFlask,
  BsReceipt,
  BsWallet2,
} from "react-icons/bs";
import { useLocation } from "react-router-dom";
import { getPatientInvoices, payPatientInvoice } from "../../services/patient/patientInvoiceApi";
import { countByStatus, formatCurrency, getPatientStatusMeta } from "./patientPageUtils";

function PatientInvoices() {
  const location = useLocation();
  const [invoices, setInvoices] = useState([]);
  const [selectedInvoice, setSelectedInvoice] = useState(null);
  const [paymentMethod, setPaymentMethod] = useState("VNPAY");
  const [paymentChannel, setPaymentChannel] = useState("QR");
  const [showPaymentModal, setShowPaymentModal] = useState(false);
  const [showInvoiceDetailModal, setShowInvoiceDetailModal] = useState(false);
  const [error, setError] = useState("");
  const [paymentNotice, setPaymentNotice] = useState("");

  const invoiceFocus = useMemo(() => {
    const params = new URLSearchParams(location.search);

    return {
      invoiceId: location.state?.invoiceId || params.get("invoiceId"),
      action: location.state?.action || params.get("action") || "detail",
    };
  }, [location.search, location.state]);

  useEffect(() => {
    let mounted = true;

    const loadInvoices = async () => {
      try {
        const response = await getPatientInvoices();
        const nextInvoices = response.data || [];
        const focusedInvoice = invoiceFocus.invoiceId
          ? nextInvoices.find((invoice) => String(invoice.id) === String(invoiceFocus.invoiceId))
          : null;

        if (mounted) {
          setInvoices(nextInvoices);
          setError("");
          if (focusedInvoice) {
            setSelectedInvoice(focusedInvoice);
            setShowPaymentModal(invoiceFocus.action === "pay" && focusedInvoice.status === "UNPAID");
            setShowInvoiceDetailModal(invoiceFocus.action !== "pay");
          }
        }
      } catch (error) {
        console.error(error);
        if (mounted) {
          setInvoices([]);
          setError("Không tải được danh sách hóa đơn từ backend.");
        }
      }
    };

    loadInvoices();

    return () => {
      mounted = false;
    };
  }, [invoiceFocus.action, invoiceFocus.invoiceId]);

  const summary = useMemo(() => {
    const total = invoices.reduce((sum, invoice) => sum + Number(invoice.totalAmount || 0), 0);
    const unpaid = countByStatus(invoices, "UNPAID");
    const paid = countByStatus(invoices, "PAID");

    return { total, unpaid, paid };
  }, [invoices]);

  const paymentOptions = [
    { value: "VNPAY", label: "VNPay - Ví kiểm thử", channel: "WALLET" },
    { value: "MOMO", label: "MoMo - Ví điện tử", channel: "WALLET" },
    { value: "ZALOPAY", label: "ZaloPay QR", channel: "QR" },
  ];

  const buildPaymentResultUrl = (method) => {
    const provider = String(method || "vnpay").trim().toLowerCase();
    return `${window.location.origin}/payment/${provider}/result`;
  };

  const openInvoiceDetail = (invoice) => {
    setSelectedInvoice(invoice);
    setShowPaymentModal(false);
    setShowInvoiceDetailModal(true);
  };

  const openPaymentModal = (invoice) => {
    setSelectedInvoice(invoice);
    setShowInvoiceDetailModal(false);
    setShowPaymentModal(true);
  };

  const handlePay = async () => {
    if (!selectedInvoice) return;

    try {
      setPaymentNotice("");
      const response = await payPatientInvoice(selectedInvoice.id, {
        paymentMethod,
        paymentChannel,
        returnUrl: buildPaymentResultUrl(paymentMethod),
        cancelUrl: buildPaymentResultUrl(paymentMethod),
      });

      const payment = response.data || {};
      if (payment.paymentUrl) {
        window.open(payment.paymentUrl, "_blank", "noopener,noreferrer");
        setPaymentNotice(`Đã khởi tạo thanh toán cho ${selectedInvoice.invoiceCode}. Vui lòng hoàn tất trên cổng thanh toán.`);
      } else {
        setPaymentNotice(`Đã tạo giao dịch thanh toán cho ${selectedInvoice.invoiceCode}.`);
      }

      setInvoices((current) =>
        current.map((invoice) =>
          invoice.id === selectedInvoice.id
            ? {
                ...invoice,
                paymentMethod: payment.paymentMethod || paymentMethod,
                paymentStatus: payment.paymentStatus || invoice.paymentStatus,
                status: payment.paymentStatus || invoice.status,
              }
            : invoice,
        ),
      );

      setShowPaymentModal(false);
      setSelectedInvoice(null);
    } catch (payError) {
      console.error(payError);
      setPaymentNotice("");
    }
  };

  return (
    <div className="patient-page">
      {error && <Alert variant="warning">{error}</Alert>}
      {paymentNotice && <Alert variant="success">{paymentNotice}</Alert>}

      <div className="patient-summary-grid invoices">
        <Card className="patient-summary-card light-blue">
          <Card.Body>
            <div className="patient-summary-icon">
              <BsCreditCard2Front />
            </div>
            <div className="patient-summary-copy">
              <span>Tổng chi tiêu</span>
              <strong>{formatCurrency(summary.total)}</strong>
            </div>
          </Card.Body>
        </Card>
        <Card className="patient-summary-card light-red">
          <Card.Body>
            <div className="patient-summary-icon">
              <BsWallet2 />
            </div>
            <div className="patient-summary-copy">
              <span>Chưa thanh toán</span>
              <strong>{summary.unpaid}</strong>
            </div>
          </Card.Body>
        </Card>
        <Card className="patient-summary-card light-green">
          <Card.Body>
            <div className="patient-summary-icon">
              <BsCheckCircle />
            </div>
            <div className="patient-summary-copy">
              <span>Đã thanh toán</span>
              <strong>{summary.paid}</strong>
            </div>
          </Card.Body>
        </Card>
      </div>

      <section className="patient-invoice-layout">
        <div className="patient-invoice-list">
          {invoices.length > 0 ? invoices.map((invoice) => {
            const meta = getPatientStatusMeta(invoice.status);

            return (
              <Card key={invoice.id} className={`patient-invoice-card ${invoice.status === "UNPAID" ? "unpaid" : ""}`}>
                <Card.Body>
                  <div className="patient-invoice-top">
                    <div className="patient-invoice-title">
                      <div className="patient-invoice-icon">
                        <BsReceipt />
                      </div>
                      <div>
                        <h3>{invoice.invoiceCode}</h3>
                        <p>
                          <BsCalendar3 /> {invoice.createdAt || "Chưa có ngày tạo"}
                        </p>
                      </div>
                    </div>
                    <div className="patient-invoice-total">{formatCurrency(invoice.totalAmount)}</div>
                  </div>

                  <div className="patient-invoice-breakdown">
                    <div>
                      <span>
                        <BsCreditCard2Front /> Khám/dịch vụ
                      </span>
                      <strong>{formatCurrency(invoice.serviceAmount)}</strong>
                    </div>
                    <div>
                      <span>
                        <BsCapsule /> Tiền thuốc
                      </span>
                      <strong>{formatCurrency(invoice.medicineAmount)}</strong>
                    </div>
                    <div>
                      <span>
                        <BsFlask /> Xét nghiệm
                      </span>
                      <strong>{formatCurrency(invoice.testAmount)}</strong>
                    </div>
                    <div className="total">
                      <span>
                        <BsCashCoin /> Tổng thanh toán
                      </span>
                      <strong>{formatCurrency(invoice.totalAmount)}</strong>
                    </div>
                  </div>

                  <div className="patient-invoice-actions">
                    <Badge bg={meta.variant} className="patient-status-badge">
                      {meta.label}
                    </Badge>
                    <div className="patient-invoice-buttons">
                      <Button type="button" variant="light" className="patient-outline-button" onClick={() => openInvoiceDetail(invoice)}>
                        <BsFileEarmarkText /> Chi tiết
                      </Button>
                      {invoice.status === "UNPAID" && (
                        <Button type="button" className="patient-primary-soft" onClick={() => openPaymentModal(invoice)}>
                          Thanh toán
                        </Button>
                      )}
                    </div>
                  </div>
                </Card.Body>
              </Card>
            );
          }) : (
            <div className="patient-empty-state">
              <h4>Chưa có hóa đơn</h4>
              <p>Backend hiện chưa trả về hóa đơn cho bệnh nhân của bạn.</p>
            </div>
          )}
        </div>

        <aside className="patient-invoice-aside">
          <Card className="patient-payment-card">
            <Card.Body>
              <h3>Phương thức thanh toán</h3>
              <div className="patient-payment-panel">
                <div className="patient-payment-method">
                  <span>ZaloPay</span>
                  <strong>Thanh toán QR</strong>
                </div>
                <div className="patient-payment-method light">
                  <span>VNPay</span>
                  <strong>Ví kiểm thử</strong>
                </div>
                <div className="patient-payment-method outline">
                  <span>MoMo</span>
                  <strong>Ví điện tử</strong>
                </div>
              </div>
            </Card.Body>
          </Card>

          <Card className="patient-info-note payment-note">
            <Card.Body>
              <div className="patient-card-head">
                <div className="patient-card-icon">
                  <BsWallet2 />
                </div>
                <h3>Thanh toán an toàn</h3>
              </div>
              <p>Thanh toán hiện đã gọi API backend thật. Nếu cổng trả về `paymentUrl`, hệ thống sẽ mở cổng thanh toán trong tab mới.</p>
            </Card.Body>
          </Card>
        </aside>
      </section>

      <Modal
        show={Boolean(selectedInvoice) && showInvoiceDetailModal}
        onHide={() => setShowInvoiceDetailModal(false)}
        centered
        size="lg"
        animation={false}
        dialogClassName="patient-invoice-detail-dialog"
      >
        <Modal.Header closeButton>
          <Modal.Title>Chi tiết hóa đơn</Modal.Title>
        </Modal.Header>
        <Modal.Body className="patient-payment-modal-body">
          {selectedInvoice && (
            <div className="patient-invoice-detail-modal">
              <div className="patient-payment-modal-head">
                <div className="patient-invoice-icon">
                  <BsReceipt />
                </div>
                <div>
                  <h4>{selectedInvoice.invoiceCode}</h4>
                  <p>
                    <BsCalendar3 /> {selectedInvoice.createdAt || "Chưa có ngày tạo"}
                  </p>
                </div>
                <Badge bg={getPatientStatusMeta(selectedInvoice.status).variant} className="patient-status-badge">
                  {getPatientStatusMeta(selectedInvoice.status).label}
                </Badge>
              </div>

              <div className="patient-invoice-breakdown detail">
                <div>
                  <span>
                    <BsCreditCard2Front /> Khám/dịch vụ
                  </span>
                  <strong>{formatCurrency(selectedInvoice.serviceAmount)}</strong>
                </div>
                <div>
                  <span>
                    <BsCapsule /> Tiền thuốc
                  </span>
                  <strong>{formatCurrency(selectedInvoice.medicineAmount)}</strong>
                </div>
                <div>
                  <span>
                    <BsFlask /> Xét nghiệm
                  </span>
                  <strong>{formatCurrency(selectedInvoice.testAmount)}</strong>
                </div>
                <div>
                  <span>Giảm trừ</span>
                  <strong>{formatCurrency(selectedInvoice.discountAmount)}</strong>
                </div>
              </div>

              <div className="patient-payment-summary">
                <span>Tổng thanh toán</span>
                <strong>{formatCurrency(selectedInvoice.totalAmount)}</strong>
              </div>

              <div className="patient-invoice-detail-grid">
                <div>
                  <span>Mã bệnh án</span>
                  <strong>{selectedInvoice.medicalRecordId || "Chưa cập nhật"}</strong>
                </div>
                <div>
                  <span>Phương thức</span>
                  <strong>{selectedInvoice.paymentMethod || "Chưa thanh toán"}</strong>
                </div>
                <div>
                  <span>Ngày thanh toán</span>
                  <strong>{selectedInvoice.paidAt || "Chưa thanh toán"}</strong>
                </div>
              </div>
            </div>
          )}
        </Modal.Body>
        <Modal.Footer>
          <Button type="button" variant="outline-secondary" onClick={() => setShowInvoiceDetailModal(false)}>
            Đóng
          </Button>
          {selectedInvoice?.status === "UNPAID" && (
            <Button type="button" className="patient-primary-soft" onClick={() => openPaymentModal(selectedInvoice)}>
              Thanh toán ngay
            </Button>
          )}
        </Modal.Footer>
      </Modal>

      <Modal show={Boolean(selectedInvoice) && showPaymentModal} onHide={() => setShowPaymentModal(false)} centered animation={false}>
        <Modal.Header closeButton>
          <Modal.Title>Thanh toán hóa đơn</Modal.Title>
        </Modal.Header>
        <Modal.Body className="patient-payment-modal-body">
          {selectedInvoice && (
            <div className="patient-payment-modal">
              <div className="patient-payment-modal-head">
                <div className="patient-invoice-icon">
                  <BsReceipt />
                </div>
                <div>
                  <h4>{selectedInvoice.invoiceCode}</h4>
                  <p>Hệ thống sẽ tạo giao dịch qua backend và mở cổng thanh toán nếu có URL trả về.</p>
                </div>
              </div>
              <Form.Group className="patient-form-group">
                <Form.Label>Phương thức thanh toán</Form.Label>
                <Form.Select
                  value={paymentMethod}
                  onChange={(event) => {
                    const nextMethod = event.target.value;
                    const option = paymentOptions.find((item) => item.value === nextMethod);
                    setPaymentMethod(nextMethod);
                    setPaymentChannel(option?.channel || "QR");
                  }}
                >
                  {paymentOptions.map((method) => (
                    <option key={method.value} value={method.value}>
                      {method.label}
                    </option>
                  ))}
                </Form.Select>
              </Form.Group>
              <div className="patient-payment-summary">
                <span>Tổng thanh toán</span>
                <strong>{formatCurrency(selectedInvoice.totalAmount)}</strong>
              </div>
            </div>
          )}
        </Modal.Body>
        <Modal.Footer>
          <Button type="button" variant="outline-secondary" onClick={() => setShowPaymentModal(false)}>
            Hủy
          </Button>
          <Button type="button" className="patient-primary-soft" onClick={handlePay}>
            Xác nhận thanh toán
          </Button>
        </Modal.Footer>
      </Modal>
    </div>
  );
}

export default PatientInvoices;
