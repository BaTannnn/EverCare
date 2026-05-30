import { Alert, Button, Card, Col, Form, Row } from "react-bootstrap";
import { BsCashCoin, BsGlobe2 } from "react-icons/bs";
import { formatCurrency, getMethodLabel } from "./receptionistPageUtils";

const counterMethods = [
  { value: "CASH", label: "Tiền mặt" },
  { value: "BANK_TRANSFER", label: "Chuyển khoản" },
  { value: "VIETQR", label: "VIETQR" },
];

const onlineMethods = [
  { value: "MOMO", label: "MoMo" },
  { value: "ZALOPAY", label: "ZaloPay" },
  { value: "VNPAY", label: "VNPay" },
];

function ReceptionistPaymentForm({
  paymentMode,
  paymentMethod,
  onPaymentModeChange,
  onPaymentMethodChange,
  onSubmit,
  submitting,
  disabled,
  amount,
  paymentStatus,
}) {
  const methods = paymentMode === "online" ? onlineMethods : counterMethods;

  return (
    <Card className="doctor-card receptionist-payment-card">
      <Card.Header>
        <h2>Thanh toán</h2>
      </Card.Header>
      <Card.Body>
        <Form onSubmit={(e) => e.preventDefault()}>
          <div className="receptionist-mode-toggle">
            <button
              type="button"
              className={`receptionist-mode-pill${paymentMode === "counter" ? " active" : ""}`}
              onClick={() => onPaymentModeChange("counter")}
              disabled={submitting || disabled}
            >
              Ghi nhận tại quầy
            </button>
            <button
              type="button"
              className={`receptionist-mode-pill${paymentMode === "online" ? " active" : ""}`}
              onClick={() => onPaymentModeChange("online")}
              disabled={submitting || disabled}
            >
              Tạo payment online
            </button>
          </div>

          <Row className="g-3 mt-2">
            <Col md={12}>
              <Form.Group>
                <Form.Label>Phương thức thanh toán</Form.Label>
                <Form.Select value={paymentMethod} onChange={(e) => onPaymentMethodChange(e.target.value)} disabled={submitting || disabled}>
                  {methods.map((method) => (
                    <option value={method.value} key={method.value}>
                      {method.label}
                    </option>
                  ))}
                </Form.Select>
              </Form.Group>
            </Col>
            <Col md={12}>
              <div className="receptionist-payment-summary">
                <div>
                  <span>Phương thức</span>
                  <strong>{getMethodLabel(paymentMethod)}</strong>
                </div>
                <div>
                  <span>Tổng tiền</span>
                  <strong>{formatCurrency(amount)}</strong>
                </div>
                <div>
                  <span>Trạng thái</span>
                  <strong>{paymentStatus || "--"}</strong>
                </div>
                <div>
                  <span>Luồng</span>
                  <strong>{paymentMode === "online" ? "Online" : "Tại quầy"}</strong>
                </div>
              </div>
            </Col>
          </Row>

          {disabled && (
            <Alert variant="warning" className="mt-3 mb-0">
              Hóa đơn đã ở trạng thái PAID hoặc REFUNDED nên không thể tạo payment mới.
            </Alert>
          )}

          <div className="receptionist-payment-actions">
            <Button type="button" disabled={submitting || disabled} onClick={() => onSubmit(paymentMode)}>
              {paymentMode === "online" ? <BsGlobe2 /> : <BsCashCoin />}
              {paymentMode === "online" ? "Tạo payment online" : "Ghi nhận tại quầy"}
            </Button>
          </div>
        </Form>
      </Card.Body>
    </Card>
  );
}

export default ReceptionistPaymentForm;
