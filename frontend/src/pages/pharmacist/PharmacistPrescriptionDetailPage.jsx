import { useCallback, useEffect, useMemo, useState } from "react";
import { Alert, Button, Card, Col, Row, Table } from "react-bootstrap";
import { useNavigate, useParams } from "react-router-dom";
import ConfirmModal from "../../components/common/ConfirmModal";
import ErrorState from "../../components/common/ErrorState";
import LoadingState from "../../components/common/LoadingState";
import StatusBadge from "../../components/common/StatusBadge";
import { buildStockCheckFromPrescription } from "../../data/pharmacistMockData";
import { dispensePrescription, getPharmacistPrescriptionDetail } from "../../services/pharmacist/pharmacistPrescriptionApi";
import {
  DEV_MODE_ALLOW_DISPENSE_WITHOUT_PAYMENT,
  formatDate,
  formatMoney,
  getErrorMessage,
} from "./pharmacistPageUtils";

function PharmacistPrescriptionDetailPage() {
  const { prescriptionId } = useParams();
  const navigate = useNavigate();
  const [prescription, setPrescription] = useState(null);
  const [stockCheck, setStockCheck] = useState(null);
  const [loading, setLoading] = useState(true);
  const [checking, setChecking] = useState(false);
  const [dispensing, setDispensing] = useState(false);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");

  const loadDetail = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      const response = await getPharmacistPrescriptionDetail(prescriptionId);
      setPrescription(response.data);
      setStockCheck(buildStockCheckFromPrescription(response.data));
    } catch (err) {
      if (err.response?.status === 401) {
        navigate("/login", { replace: true });
        return;
      }

      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [navigate, prescriptionId]);

  useEffect(() => {
    loadDetail();
  }, [loadDetail]);

  const canDispense = useMemo(() => {
    const prescribed = prescription?.status === "PRESCRIBED";
    const paid = prescription?.paymentStatus === "PAID" || DEV_MODE_ALLOW_DISPENSE_WITHOUT_PAYMENT;
    const enoughStock = (stockCheck?.items || []).length > 0
      && stockCheck.items.every((item) => item.enoughStock);

    return prescribed && paid && enoughStock;
  }, [prescription, stockCheck]);

  const handleStockCheck = () => {
    setChecking(true);
    setNotice("");
    setStockCheck(buildStockCheckFromPrescription(prescription));
    window.setTimeout(() => setChecking(false), 150);
  };

  const handleDispense = async () => {
    setDispensing(true);
    setNotice("");

    try {
      await dispensePrescription(prescription.id);
      setConfirmOpen(false);
      setNotice("Đã cấp phát thuốc thành công.");
      await loadDetail();
    } catch (err) {
      setNotice(getErrorMessage(err));
      setStockCheck(buildStockCheckFromPrescription(prescription));
      if (err.response?.status === 401) {
        navigate("/login", { replace: true });
      }
    } finally {
      setDispensing(false);
    }
  };

  if (loading) {
    return <LoadingState message="Đang tải chi tiết đơn thuốc..." />;
  }

  if (error) {
    return <ErrorState message={error} onRetry={loadDetail} />;
  }

  if (!prescription) {
    return <ErrorState message="Không tìm thấy đơn thuốc." onRetry={loadDetail} />;
  }

  const stockItems = stockCheck?.items || [];
  const paymentKnown = Boolean(prescription.paymentStatus);

  return (
    <>
      <div className="exam-page-header">
        <div className="doctor-breadcrumb">Dược sĩ <span>/</span> <strong>Chi tiết đơn thuốc</strong></div>
        <h1>{prescription.prescriptionCode || "Đơn thuốc"}</h1>
        <p>Cấp phát thuốc theo FEFO sau khi bệnh nhân đã thanh toán.</p>
      </div>

      {notice && <Alert variant={notice.startsWith("Đã") ? "success" : "warning"}>{notice}</Alert>}
      {!paymentKnown && (
        <Alert variant="warning">
          Chưa có API/field kiểm tra thanh toán. Theo nghiệp vụ Cách A, cần thanh toán trước khi cấp phát.
        </Alert>
      )}

      <Card className="doctor-card mb-3">
        <Card.Header>
          <h2>Thông tin đơn thuốc</h2>
          <div className="d-flex gap-2">
            <StatusBadge status={prescription.status} />
            <StatusBadge status={prescription.paymentStatus || "UNPAID"} />
          </div>
        </Card.Header>
        <Card.Body>
          <Row className="g-3">
            <Col md={4}><strong>Bệnh nhân</strong><div>{prescription.patientName || "--"}</div></Col>
            <Col md={4}><strong>Mã bệnh nhân</strong><div>{prescription.patientCode || "--"}</div></Col>
            <Col md={4}><strong>Ngày kê</strong><div>{formatDate(prescription.prescribedAt)}</div></Col>
            <Col md={4}><strong>Bác sĩ kê đơn</strong><div>{prescription.doctorName || "--"}</div></Col>
            <Col md={4}><strong>Chẩn đoán</strong><div>{prescription.diagnosis || "--"}</div></Col>
            <Col md={4}><strong>Ghi chú</strong><div>{prescription.note || "--"}</div></Col>
          </Row>
        </Card.Body>
      </Card>

      <Card className="doctor-card mb-3">
        <Card.Header><h2>Danh sách thuốc trong đơn</h2></Card.Header>
        <Card.Body className="p-0">
          <Table responsive className="doctor-table mb-0">
            <thead>
              <tr>
                <th>Tên thuốc</th>
                <th>Mã thuốc</th>
                <th>Đơn vị</th>
                <th>Số lượng cần cấp</th>
                <th>Tồn khả dụng</th>
                <th>Đủ kho?</th>
                <th>Liều dùng</th>
                <th>Tần suất</th>
                <th>Thời gian</th>
                <th>Hướng dẫn</th>
              </tr>
            </thead>
            <tbody>
              {(prescription.items || []).map((item) => (
                <tr key={item.id || item.medicineId}>
                  <td>{item.medicineName}</td>
                  <td>{item.medicineCode || "--"}</td>
                  <td>{item.unit || "--"}</td>
                  <td>{item.quantity}</td>
                  <td>{item.availableQuantity ?? "--"}</td>
                  <td><StatusBadge status={item.enoughStock ? "ENOUGH" : "OUT"} /></td>
                  <td>{item.dosage || "--"}</td>
                  <td>{item.frequency || "--"}</td>
                  <td>{item.duration || "--"}</td>
                  <td>{item.instruction || "--"}</td>
                </tr>
              ))}
            </tbody>
          </Table>
        </Card.Body>
      </Card>

      <Card className="doctor-card mb-3">
        <Card.Header>
          <h2>Kết quả kiểm tra tồn kho</h2>
          <Button type="button" variant="outline-primary" onClick={handleStockCheck} disabled={checking}>
            {checking ? "Đang kiểm tra..." : "Kiểm tra tồn kho"}
          </Button>
        </Card.Header>
        <Card.Body className="p-0">
          <Table responsive className="doctor-table mb-0">
            <thead>
              <tr>
                <th>Thuốc</th>
                <th>Cần cấp</th>
                <th>Có thể cấp</th>
                <th>Trạng thái</th>
                <th>Lô FEFO</th>
              </tr>
            </thead>
            <tbody>
              {stockItems.map((item) => (
                <tr key={item.medicineId}>
                  <td>{item.medicineName}</td>
                  <td>{item.requiredQuantity}</td>
                  <td>{item.availableQuantity ?? "--"}</td>
                  <td><StatusBadge status={item.enoughStock ? "ENOUGH" : "OUT"} /></td>
                  <td>{item.batches?.length ? item.batches.map((batch) => batch.batchCode).join(", ") : "Backend chưa trả chi tiết lô"}</td>
                </tr>
              ))}
            </tbody>
          </Table>
        </Card.Body>
      </Card>

      <Card className="doctor-card">
        <Card.Header><h2>Điều kiện cấp phát</h2></Card.Header>
        <Card.Body>
          <ul className="mb-3">
            <li>Đơn thuốc đang PRESCRIBED: {prescription.status === "PRESCRIBED" ? "Đạt" : "Không đạt"}</li>
            <li>Bệnh nhân đã thanh toán PAID: {prescription.paymentStatus === "PAID" ? "Đạt" : "Không đạt"}</li>
            <li>Tất cả thuốc đủ tồn kho: {stockItems.every((item) => item.enoughStock) ? "Đạt" : "Không đạt"}</li>
            <li>Không dùng lô hết hạn: backend dispense lọc lô còn hạn</li>
            <li>Backend sẽ trừ kho theo FEFO</li>
          </ul>
          <Button type="button" disabled={!canDispense || dispensing} onClick={() => setConfirmOpen(true)}>
            {dispensing ? "Đang cấp phát..." : "Xác nhận cấp phát"}
          </Button>
        </Card.Body>
      </Card>

      <ConfirmModal
        show={confirmOpen}
        title="Xác nhận cấp phát"
        message={`Xác nhận cấp phát thuốc cho đơn này? Hệ thống sẽ trừ kho theo FEFO và chuyển đơn sang Đã cấp phát. Tổng tiền thuốc: ${formatMoney((prescription.items || []).reduce((sum, item) => sum + Number(item.unitPrice || 0) * Number(item.quantity || 0), 0))}`}
        confirmText="Xác nhận cấp phát"
        loading={dispensing}
        onConfirm={handleDispense}
        onHide={() => setConfirmOpen(false)}
      />
    </>
  );
}

export default PharmacistPrescriptionDetailPage;
