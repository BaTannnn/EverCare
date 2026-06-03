import { useMemo } from "react";
import { Alert, Button, Card } from "react-bootstrap";
import { BsArrowLeft, BsCheckCircle, BsInfoCircle, BsXCircle } from "react-icons/bs";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";

const providerLabelMap = {
  momo: "MoMo",
  zalopay: "ZaloPay",
  vnpay: "VNPay",
};

function PaymentResultPage() {
  const navigate = useNavigate();
  const { provider = "" } = useParams();
  const [searchParams] = useSearchParams();

  const rawStatus = searchParams.get("status") || searchParams.get("paymentStatus") || "";
  const status = rawStatus.toLowerCase();
  const invoiceId = searchParams.get("invoiceId") || "";
  const invoiceCode = searchParams.get("invoiceCode") || searchParams.get("invoice") || invoiceId;
  const transactionCode = searchParams.get("transactionCode") || searchParams.get("txnRef") || "";
  const message = searchParams.get("message") || searchParams.get("error") || "";

  const content = useMemo(() => {
    const successStatuses = ["success", "paid"];
    const isSuccess = successStatuses.some((item) => status.includes(item));
    if (!isSuccess) {
      return {
        variant: "danger",
        icon: BsXCircle,
        title: "Thanh toán chưa hoàn tất",
        description: message || "Giao dịch đã bị hủy hoặc không thể hoàn tất. Bạn có thể thử lại từ hóa đơn.",
      };
    }

    return {
      variant: "success",
      icon: BsCheckCircle,
      title: "Thanh toán thành công",
      description: message || "Hệ thống đã nhận được kết quả thanh toán từ backend hoặc cổng thanh toán.",
    };
  }, [message, status]);

  const Icon = content.icon;

  return (
    <div className="payment-result-page">
      <Card className="payment-result-card">
        <Card.Body>
          <div className={`payment-result-icon ${content.variant}`}>
            <Icon />
          </div>
          <p className="payment-result-eyebrow">Kết quả thanh toán {providerLabelMap[provider.toLowerCase()] || provider || "EverCare"}</p>
          <h1>{content.title}</h1>
          <p className="payment-result-copy">{content.description}</p>

          <div className="payment-result-meta">
            <div>
              <span>Hóa đơn</span>
              <strong>{invoiceCode || "Chưa có"}</strong>
            </div>
            <div>
              <span>Mã giao dịch</span>
              <strong>{transactionCode || "Chưa có"}</strong>
            </div>
            <div>
              <span>Trạng thái</span>
              <strong>{status.toUpperCase()}</strong>
            </div>
          </div>

          <Alert variant="info" className="payment-result-note">
            <BsInfoCircle className="me-2" />
            Bạn có thể quay lại danh sách hóa đơn để kiểm tra trạng thái mới nhất.
          </Alert>

          <div className="payment-result-actions">
            <Button
              type="button"
              variant="light"
              className="patient-outline-button"
              onClick={() =>
                navigate("/patient/invoices", {
                  state: invoiceId
                    ? {
                        invoiceId,
                        action: "detail",
                      }
                    : undefined,
                })
              }
            >
              <BsArrowLeft /> Về hóa đơn
            </Button>
            <Button type="button" className="patient-primary-soft" onClick={() => navigate("/patient/dashboard")}>
              Về tổng quan
            </Button>
          </div>
        </Card.Body>
      </Card>
    </div>
  );
}

export default PaymentResultPage;
