import { useEffect, useMemo, useState } from "react";
import { Badge, Button, Card, Form, Modal } from "react-bootstrap";
import { BsCheckCircle, BsCreditCard2Front, BsFileEarmarkText, BsWallet2 } from "react-icons/bs";
import { getPatientInvoices } from "../../services/patient/patientInvoiceApi";
import { patientInvoices as fallbackInvoices } from "../../data/patientMockData";
import { countByStatus, formatCurrency, getPatientStatusMeta } from "./patientPageUtils";

function PatientInvoices() {
  const [invoices, setInvoices] = useState(fallbackInvoices);
  const [selectedInvoice, setSelectedInvoice] = useState(null);
  const [paymentMethod, setPaymentMethod] = useState("Thẻ ngân hàng");
  const [showPaymentModal, setShowPaymentModal] = useState(false);

  useEffect(() => {
    let mounted = true;

    const loadInvoices = async () => {
      try {
        const response = await getPatientInvoices();
        if (mounted) {
          setInvoices(response.data || []);
        }
      } catch (error) {
        console.error(error);
        if (mounted) {
          setInvoices(fallbackInvoices);
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

  const paymentOptions = selectedInvoice?.paymentMethods || ["Thẻ ngân hàng", "Ví MoMo", "VNPay"];

  const handlePay = () => {
    setInvoices((current) => current.map((invoice) => (invoice.id === selectedInvoice.id ? { ...invoice, status: "PAID" } : invoice)));
    setShowPaymentModal(false);
    setSelectedInvoice(null);
  };

  return (
    <div className="patient-page">
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
                  <span>Thẻ ngân hàng</span>
                  <strong>VISA •••• 8842</strong>
                </div>
                <div className="patient-payment-method light">
                  <span>Ví MoMo</span>
                  <strong>090 ••• •1234</strong>
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
              <p>TODO: Backend hiện chưa có API hóa đơn/thanh toán cho bệnh nhân, nên màn này đang chạy theo trạng thái local.</p>
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
              <p>Thanh toán hóa đơn đang mở ở chế độ UI, không reload trang.</p>
              <Form.Group className="patient-form-group">
                <Form.Label>Phương thức thanh toán</Form.Label>
                <Form.Select value={paymentMethod} onChange={(event) => setPaymentMethod(event.target.value)}>
                  {paymentOptions.map((method) => (
                    <option key={method} value={method}>
                      {method}
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
