import { useEffect, useMemo, useState } from "react";
import { Alert, Badge, Button, Card, Form, Modal } from "react-bootstrap";
import { BsCheckCircle, BsCreditCard2Front, BsFileEarmarkText, BsWallet2 } from "react-icons/bs";
import { getPatientInvoices, payPatientInvoice } from "../../services/patient/patientInvoiceApi";
import { countByStatus, formatCurrency, getPatientStatusMeta } from "./patientPageUtils";

function PatientInvoices() {
  const [invoices, setInvoices] = useState([]);
  const [selectedInvoice, setSelectedInvoice] = useState(null);
  const [paymentMethod, setPaymentMethod] = useState("VNPAY");
  const [paymentChannel, setPaymentChannel] = useState("QR");
  const [showPaymentModal, setShowPaymentModal] = useState(false);
  const [error, setError] = useState("");
  const [paymentNotice, setPaymentNotice] = useState("");

  useEffect(() => {
    let mounted = true;

    const loadInvoices = async () => {
      try {
        const response = await getPatientInvoices();
        if (mounted) {
          setInvoices(response.data || []);
          setError("");
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
  }, []);

  const summary = useMemo(() => {
    const total = invoices.reduce((sum, invoice) => sum + Number(invoice.totalAmount || 0), 0);
    const unpaid = countByStatus(invoices, "UNPAID");
    const paid = countByStatus(invoices, "PAID");

    return { total, unpaid, paid };
  }, [invoices]);

  const paymentOptions = [
    { value: "VNPAY", label: "VNPay", channel: "QR" },
    { value: "MOMO", label: "MoMo", channel: "WALLET" },
    { value: "ZALOPAY", label: "ZaloPay", channel: "WALLET" },
  ];

  const handlePay = async () => {
    if (!selectedInvoice) return;

    try {
      setPaymentNotice("");
      const response = await payPatientInvoice(selectedInvoice.id, {
        paymentMethod,
        paymentChannel,
        returnUrl: `${window.location.origin}/patient/invoices`,
        cancelUrl: `${window.location.origin}/patient/invoices`,
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
          {invoices.map((invoice) => {
            const meta = getPatientStatusMeta(invoice.status);

            return (
              <Card key={invoice.id} className={`patient-invoice-card ${invoice.status === "UNPAID" ? "unpaid" : ""}`}>
                <Card.Body>
                  <div className="patient-invoice-top">
                    <div>
                      <h3>{invoice.invoiceCode}</h3>
                      <p>
                        HĐ: {invoice.invoiceCode} • {invoice.createdAt}
                      </p>
                    </div>
                    <div className="patient-invoice-total">{formatCurrency(invoice.totalAmount)}</div>
                  </div>

                  <div className="patient-invoice-meta">
                    <span>Tiền dịch vụ: {formatCurrency(invoice.serviceAmount)}</span>
                    <span>Tiền thuốc: {formatCurrency(invoice.medicineAmount)}</span>
                    <span>Tiền xét nghiệm: {formatCurrency(invoice.testAmount)}</span>
                    <span>Tổng tiền: {formatCurrency(invoice.totalAmount)}</span>
                  </div>

                  <div className="patient-invoice-actions">
                    <Badge bg={meta.variant} className="patient-status-badge">
                      {meta.label}
                    </Badge>
                    <div className="patient-invoice-buttons">
                      <Button type="button" variant="light" className="patient-outline-button" onClick={() => setSelectedInvoice(invoice)}>
                        <BsFileEarmarkText /> Chi tiết
                      </Button>
                      {invoice.status === "UNPAID" && (
                        <Button
                          type="button"
                          className="patient-primary-soft"
                          onClick={() => {
                            setSelectedInvoice(invoice);
                            setShowPaymentModal(true);
                          }}
                        >
                          Thanh toán
                        </Button>
                      )}
                    </div>
                  </div>
                </Card.Body>
              </Card>
            );
          })}
        </div>

        <aside className="patient-invoice-aside">
          <Card className="patient-payment-card">
            <Card.Body>
              <h3>Phương thức thanh toán</h3>
              <div className="patient-payment-panel">
                <div className="patient-payment-method">
                  <span>VNPay</span>
                  <strong>Thanh toán QR</strong>
                </div>
                <div className="patient-payment-method light">
                  <span>MoMo</span>
                  <strong>Ví điện tử</strong>
                </div>
                <div className="patient-payment-method outline">+ Thêm mới</div>
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

      <Modal show={Boolean(selectedInvoice) && showPaymentModal} onHide={() => setShowPaymentModal(false)} centered>
        <Modal.Header closeButton>
          <Modal.Title>Thanh toán hóa đơn</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {selectedInvoice && (
            <div className="patient-payment-modal">
              <h4>{selectedInvoice.invoiceCode}</h4>
              <p>Hóa đơn này sẽ tạo giao dịch qua backend và mở cổng thanh toán nếu có URL trả về.</p>
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
