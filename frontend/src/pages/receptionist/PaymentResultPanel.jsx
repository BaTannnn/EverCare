import { Alert, Button, Card, Form } from "react-bootstrap";
import { BsBoxArrowUpRight, BsQrCode } from "react-icons/bs";
import { formatCurrency, formatDateTime, getMethodLabel } from "./receptionistPageUtils";

function PaymentResultPanel({ result, onOpenPaymentUrl, onRefresh }) {
  if (!result) return null;

  const payment = result.payment || {};
  const invoice = result.invoice || {};
  const paymentUrl = result.paymentUrl || payment.paymentUrl || result.qrCodeUrl || payment.qrCodeUrl || "";
  const isOnline = Boolean(paymentUrl);

  return (
    <Card className="doctor-card receptionist-result-card">
      <Card.Header>
        <h2>Kết quả thanh toán</h2>
        <div className="d-flex gap-2 flex-wrap">
          <Button type="button" variant="outline-primary" size="sm" onClick={onRefresh}>
            Làm mới
          </Button>
          {paymentUrl && (
            <Button type="button" size="sm" onClick={onOpenPaymentUrl}>
              <BsBoxArrowUpRight /> Mở link
            </Button>
          )}
        </div>
      </Card.Header>
      <Card.Body>
        <div className="receptionist-result-grid">
          <div className="receptionist-result-item"><span>paymentId</span><strong>{payment.id || "--"}</strong></div>
          <div className="receptionist-result-item"><span>invoiceId</span><strong>{invoice.id || "--"}</strong></div>
          <div className="receptionist-result-item"><span>invoiceCode</span><strong>{invoice.invoiceCode || "--"}</strong></div>
          <div className="receptionist-result-item"><span>amount</span><strong>{formatCurrency(payment.amount || 0)}</strong></div>
          <div className="receptionist-result-item"><span>paymentMethod</span><strong>{getMethodLabel(payment.paymentMethod)}</strong></div>
          <div className="receptionist-result-item"><span>paymentProvider</span><strong>{payment.paymentProvider || "--"}</strong></div>
          <div className="receptionist-result-item"><span>transactionCode</span><strong>{payment.transactionCode || "--"}</strong></div>
          <div className="receptionist-result-item"><span>paymentStatus</span><strong>{payment.paymentStatus || invoice.paymentStatus || "--"}</strong></div>
          <div className="receptionist-result-item"><span>paidAt</span><strong>{formatDateTime(payment.paidAt || invoice.paidAt)}</strong></div>
        </div>

        {paymentUrl ? (
          <Alert variant="info" className="mt-3 mb-0">
            <BsQrCode className="me-2" />
            Payment URL/QR từ backend:
            <div className="receptionist-payment-url mt-2">
              <Form.Control value={paymentUrl} readOnly />
              <Button type="button" variant="outline-primary" onClick={onOpenPaymentUrl}>
                Mở paymentUrl
              </Button>
            </div>
          </Alert>
        ) : (
          <Alert variant={isOnline ? "info" : "success"} className="mt-3 mb-0">
            Giao dịch tại quầy đã được backend ghi nhận.
          </Alert>
        )}
      </Card.Body>
    </Card>
  );
}

export default PaymentResultPanel;
