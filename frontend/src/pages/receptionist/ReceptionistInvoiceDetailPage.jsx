import { useCallback, useEffect, useState } from "react";
import { Alert, Button, Card, Table } from "react-bootstrap";
import { BsArrowLeft, BsArrowRepeat } from "react-icons/bs";
import { Link, useLocation, useNavigate, useParams } from "react-router-dom";
import ErrorState from "../../components/common/ErrorState";
import LoadingState from "../../components/common/LoadingState";
import PageHeader from "../../components/common/PageHeader";
import StatusBadge from "../../components/common/StatusBadge";
import { createReceptionistInvoicePayment, getReceptionistInvoiceDetail } from "../../services/receptionist/receptionistInvoiceApi";
import PaymentResultPanel from "./PaymentResultPanel";
import ReceptionistPaymentForm from "./ReceptionistPaymentForm";
import { formatCurrency, formatDateTime, getErrorMessage, getMethodLabel, invoiceStatusMeta } from "./receptionistPageUtils";

const counterMethod = "CASH";

const onlineMethods = ["MOMO", "ZALOPAY", "VNPAY"];
const counterMethods = ["CASH", "BANK_TRANSFER", "VIETQR"];

const InfoRow = ({ label, value }) => (
  <div className="receptionist-info-row">
    <span>{label}</span>
    <strong>{value || "--"}</strong>
  </div>
);

function ReceptionistInvoiceDetailPage() {
  const { invoiceId } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const [invoice, setInvoice] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState(location.state?.notice || "");
  const [result, setResult] = useState(null);
  const [paymentMode, setPaymentMode] = useState("counter");
  const [paymentMethod, setPaymentMethod] = useState(counterMethod);
  const [submitting, setSubmitting] = useState(false);

  const loadInvoice = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      const response = await getReceptionistInvoiceDetail(invoiceId);
      setInvoice(response.data);
    } catch (err) {
      if (err.response?.status === 401) {
        navigate("/login", { replace: true });
        return;
      }
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [invoiceId, navigate]);

  useEffect(() => {
    loadInvoice();
  }, [loadInvoice]);

  useEffect(() => {
    if (!invoice) return;
    if (["PAID", "REFUNDED"].includes(invoice.paymentStatus)) return;
    if (!result?.paymentUrl) return;

    const timer = window.setInterval(() => {
      loadInvoice();
    }, 10000);

    return () => window.clearInterval(timer);
  }, [invoice?.paymentStatus, result?.paymentUrl, loadInvoice]);

  useEffect(() => {
    if (paymentMode === "counter") {
      if (!counterMethods.includes(paymentMethod)) {
        setPaymentMethod(counterMethod);
      }
      return;
    }

    if (!onlineMethods.includes(paymentMethod)) {
      setPaymentMethod(onlineMethods[0]);
    }
  }, [paymentMode, paymentMethod]);

  const handleModeChange = (mode) => {
    setPaymentMode(mode);
    setPaymentMethod(mode === "online" ? onlineMethods[0] : counterMethod);
  };

  const handleMethodChange = (method) => {
    setPaymentMethod(method);
  };

  const handlePayment = async (mode) => {
    if (!invoice) return;

    setSubmitting(true);
    setError("");
    setNotice("");

    try {
      const payload = mode === "online"
        ? { paymentMethod }
        : { paymentMethod, note: "Thanh toán tại quầy" };

      const response = await createReceptionistInvoicePayment(invoice.id, payload);
      const paymentResult = response.data;
      setResult(paymentResult);

      if (paymentResult.paymentUrl) {
        window.open(paymentResult.paymentUrl, "_blank", "noopener,noreferrer");
      }

      setNotice(mode === "online" ? "Đã tạo yêu cầu thanh toán online." : "Đã ghi nhận thanh toán tại quầy.");
      await loadInvoice();
    } catch (err) {
      if (err.response?.status === 401) {
        navigate("/login", { replace: true });
        return;
      }
      setError(getErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return <LoadingState message="Đang tải chi tiết hóa đơn..." />;
  }

  if (error && !invoice) {
    return <ErrorState message={error} onRetry={loadInvoice} />;
  }

  if (!invoice) {
    return <ErrorState message="Không tìm thấy hóa đơn." onRetry={loadInvoice} />;
  }

  const patient = invoice.patient || {};
  const medicalRecord = invoice.medicalRecord || {};
  const meta = invoiceStatusMeta(invoice.paymentStatus);
  const disabled = ["PAID", "REFUNDED"].includes(invoice.paymentStatus);

  return (
    <>
      <PageHeader
        eyebrow="Lễ tân / Chi tiết hóa đơn"
        title={invoice.invoiceCode}
        description="Ghi nhận thanh toán tại quầy hoặc tạo payment online mà không rời khỏi SPA."
        actions={(
          <>
            <Button as={Link} to="/receptionist/invoices" type="button" variant="outline-primary">
              <BsArrowLeft /> Về danh sách
            </Button>
            <Button type="button" variant="outline-primary" onClick={loadInvoice}>
              <BsArrowRepeat /> Làm mới
            </Button>
          </>
        )}
      />

      {notice && <Alert variant="success">{notice}</Alert>}
      {error && <Alert variant="danger">{error}</Alert>}
      {disabled && (
        <Alert variant="warning">
          Hóa đơn đã ở trạng thái {invoice.paymentStatus}. Không thể tạo payment mới.
        </Alert>
      )}

      <section className="receptionist-detail-grid">
        <Card className="doctor-card receptionist-detail-card">
          <Card.Header>
            <h2>Thông tin bệnh nhân</h2>
          </Card.Header>
          <Card.Body>
            <InfoRow label="patientCode" value={patient.patientCode} />
            <InfoRow label="fullName" value={patient.fullName} />
            <InfoRow label="phone" value={patient.phone} />
          </Card.Body>
        </Card>

        <Card className="doctor-card receptionist-detail-card">
          <Card.Header>
            <h2>Thông tin bệnh án</h2>
            <StatusBadge status={invoice.paymentStatus} label={meta.label} />
          </Card.Header>
          <Card.Body>
            <InfoRow label="medicalRecordCode" value={medicalRecord.recordCode} />
            <InfoRow label="visitDate" value={formatDateTime(medicalRecord.visitDate)} />
            <InfoRow label="totalServiceAmount" value={formatCurrency(invoice.totalServiceAmount)} />
            <InfoRow label="totalMedicineAmount" value={formatCurrency(invoice.totalMedicineAmount)} />
            <InfoRow label="discountAmount" value={formatCurrency(invoice.discountAmount)} />
            <InfoRow label="totalAmount" value={formatCurrency(invoice.totalAmount)} />
            <InfoRow label="paymentMethod" value={getMethodLabel(invoice.paymentMethod)} />
            <InfoRow label="paidAt" value={formatDateTime(invoice.paidAt)} />
          </Card.Body>
        </Card>
      </section>

      <section className="receptionist-payment-grid">
        <ReceptionistPaymentForm
          paymentMode={paymentMode}
          paymentMethod={paymentMethod}
          onPaymentModeChange={handleModeChange}
          onPaymentMethodChange={handleMethodChange}
          onSubmit={handlePayment}
          submitting={submitting}
          disabled={disabled}
          amount={invoice.totalAmount}
          paymentStatus={invoice.paymentStatus}
        />

        <PaymentResultPanel
          result={result}
          onOpenPaymentUrl={() => {
            const paymentUrl = result?.paymentUrl || result?.qrCodeUrl || result?.payment?.paymentUrl || result?.payment?.qrCodeUrl;
            if (paymentUrl) {
              window.open(paymentUrl, "_blank", "noopener,noreferrer");
            }
          }}
          onRefresh={loadInvoice}
        />
      </section>

      <Card className="doctor-card receptionist-detail-card receptionist-payment-history-card mt-3">
        <Card.Header>
          <h2>Lịch sử thanh toán</h2>
        </Card.Header>
        <Card.Body>
          <div className="table-responsive">
            {Array.isArray(invoice.payments) && invoice.payments.length > 0 ? (
              <Table hover className="doctor-table mb-0">
                <thead>
                  <tr>
                    <th>Payment ID</th>
                    <th>Phương thức</th>
                    <th>Provider</th>
                    <th>Transaction Code</th>
                    <th>Số tiền</th>
                    <th>Trạng thái</th>
                    <th>Paid At</th>
                  </tr>
                </thead>
                <tbody>
                  {invoice.payments.map((payment) => (
                    <tr key={payment.id}>
                      <td>{payment.id}</td>
                      <td>{getMethodLabel(payment.paymentMethod)}</td>
                      <td>{payment.paymentProvider || "--"}</td>
                      <td>{payment.transactionCode || "--"}</td>
                      <td>{formatCurrency(payment.amount)}</td>
                      <td>{payment.paymentStatus || "--"}</td>
                      <td>{formatDateTime(payment.paidAt)}</td>
                    </tr>
                  ))}
                </tbody>
              </Table>
            ) : (
              <div className="state-box state-box-muted">Chưa có payment nào cho hóa đơn này.</div>
            )}
          </div>
        </Card.Body>
      </Card>
    </>
  );
}

export default ReceptionistInvoiceDetailPage;
