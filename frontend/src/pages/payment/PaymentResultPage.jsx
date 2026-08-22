import { useEffect, useMemo, useState } from "react";
import { Alert, Button, Card, Spinner } from "react-bootstrap";
import { BsArrowLeft, BsCheckCircle, BsInfoCircle, BsXCircle } from "react-icons/bs";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import { getPatientInvoiceDetail } from "../../services/patient/patientInvoiceApi";

const providerLabelMap = {
  momo: "MoMo",
  zalopay: "ZaloPay",
  vnpay: "VNPay",
};

function PaymentResultPage() {
  const navigate = useNavigate();
  const { provider = "" } = useParams();
  const [searchParams] = useSearchParams();
  const [verification, setVerification] = useState({ state: "loading", invoice: null });

  const invoiceId = searchParams.get("invoiceId") || "";
  const invoiceCodeFromUrl = searchParams.get("invoiceCode") || searchParams.get("invoice") || invoiceId;
  const transactionCode = searchParams.get("transactionCode") || searchParams.get("txnRef") || "";
  const gatewayMessage = searchParams.get("message") || searchParams.get("error") || "";

  useEffect(() => {
    let active = true;

    if (!invoiceId) {
      setVerification({ state: "unverified", invoice: null });
      return () => {
        active = false;
      };
    }

    getPatientInvoiceDetail(invoiceId)
      .then((response) => {
        if (active) {
          setVerification({ state: "verified", invoice: response.data });
        }
      })
      .catch(() => {
        if (active) {
          setVerification({ state: "unverified", invoice: null });
        }
      });

    return () => {
      active = false;
    };
  }, [invoiceId]);

  const content = useMemo(() => {
    if (verification.state === "loading") {
      return {
        variant: "info",
        icon: BsInfoCircle,
        title: "Đang xác minh thanh toán",
        description: "EverCare đang đọc trạng thái hóa đơn đã lưu trên hệ thống.",
      };
    }

    if (verification.state !== "verified") {
      return {
        variant: "info",
        icon: BsInfoCircle,
        title: "Cần kiểm tra trạng thái hóa đơn",
        description: gatewayMessage || "Không thể xác minh kết quả chỉ từ URL trả về. Hãy đăng nhập và kiểm tra hóa đơn trên EverCare.",
      };
    }

    const paid = String(verification.invoice?.paymentStatus || "").toUpperCase() === "PAID";
    if (paid) {
      return {
        variant: "success",
        icon: BsCheckCircle,
        title: "Thanh toán thành công",
        description: "Trạng thái PAID đã được xác minh từ hóa đơn lưu trên EverCare.",
      };
    }

    return {
      variant: "danger",
      icon: BsXCircle,
      title: "Thanh toán chưa hoàn tất",
      description: gatewayMessage || "Hóa đơn trên EverCare hiện chưa ở trạng thái PAID. Bạn có thể thử thanh toán lại.",
    };
  }, [gatewayMessage, verification]);

  const Icon = content.icon;
  const invoiceCode = verification.invoice?.invoiceCode || invoiceCodeFromUrl;
  const persistedStatus = verification.invoice?.paymentStatus || "UNVERIFIED";

  return (
    <div className="payment-result-page">
      <Card className="payment-result-card">
        <Card.Body>
          <div className={`payment-result-icon ${content.variant}`}>
            {verification.state === "loading" ? <Spinner animation="border" size="sm" /> : <Icon />}
          </div>
          <p className="payment-result-eyebrow">
            Kết quả thanh toán {providerLabelMap[provider.toLowerCase()] || provider || "EverCare"}
          </p>
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
              <span>Trạng thái hệ thống</span>
              <strong>{persistedStatus}</strong>
            </div>
          </div>

          <Alert variant="info" className="payment-result-note">
            <BsInfoCircle className="me-2" />
            Trang này không tin vào tham số <code>status</code> trên URL; kết quả chỉ được coi là thành công khi hóa đơn trên backend là PAID.
          </Alert>

          <div className="payment-result-actions">
            <Button
              type="button"
              variant="light"
              className="patient-outline-button"
              onClick={() =>
                navigate("/patient/invoices", {
                  state: invoiceId ? { invoiceId, action: "detail" } : undefined,
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
