import { useCallback, useEffect, useMemo, useState } from "react";
import { Button, Card, Col, Row, Table } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import EmptyState from "../../components/common/EmptyState";
import ErrorState from "../../components/common/ErrorState";
import LoadingState from "../../components/common/LoadingState";
import StatusBadge from "../../components/common/StatusBadge";
import { getMedicineBatches } from "../../services/pharmacist/pharmacistBatchApi";
import { getLowStockMedicines } from "../../services/pharmacist/pharmacistMedicineApi";
import { getPharmacistPrescriptions } from "../../services/pharmacist/pharmacistPrescriptionApi";
import { formatDate, getErrorMessage, getPrescriptionStockStatus } from "./pharmacistPageUtils";

const DASHBOARD_PAGE_SIZE = 5;

function PharmacistDashboardPage() {
  const navigate = useNavigate();
  const [prescriptions, setPrescriptions] = useState([]);
  const [dispensedPrescriptions, setDispensedPrescriptions] = useState([]);
  const [lowStock, setLowStock] = useState([]);
  const [nearExpiry, setNearExpiry] = useState([]);
  const [expired, setExpired] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadDashboard = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      const [
        prescriptionResponse,
        dispensedResponse,
        lowStockResponse,
        nearExpiryResponse,
        expiredResponse,
      ] = await Promise.all([
        getPharmacistPrescriptions({ status: "PRESCRIBED", page: 1, size: DASHBOARD_PAGE_SIZE }),
        getPharmacistPrescriptions({ status: "DISPENSED", page: 1, size: 1000 }),
        getLowStockMedicines(),
        getMedicineBatches({ status: "NEAR_EXPIRY", page: 1, size: DASHBOARD_PAGE_SIZE }),
        getMedicineBatches({ status: "EXPIRED", page: 1, size: DASHBOARD_PAGE_SIZE }),
      ]);

      setPrescriptions(prescriptionResponse.data || []);
      setDispensedPrescriptions(dispensedResponse.data || []);
      setLowStock(lowStockResponse.data || []);
      setNearExpiry(nearExpiryResponse.data || []);
      setExpired(expiredResponse.data || []);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadDashboard();
  }, [loadDashboard]);

  const dispensedTodayCount = useMemo(() => {
    const today = new Date().toISOString().slice(0, 10);
    return dispensedPrescriptions.filter((item) => String(item.prescribedAt || "").startsWith(today)).length;
  }, [dispensedPrescriptions]);

  if (loading) {
    return <LoadingState message="Đang tải tổng quan dược sĩ..." />;
  }

  if (error) {
    return <ErrorState message={error} onRetry={loadDashboard} />;
  }

  const summaryCards = [
    { label: "Đơn chờ cấp phát", value: prescriptions.length },
    { label: "Đơn đã cấp phát hôm nay", value: dispensedTodayCount },
    { label: "Thuốc sắp hết", value: lowStock.length },
    { label: "Lô gần hết hạn", value: nearExpiry.length },
    { label: "Lô đã hết hạn", value: expired.length },
  ];

  return (
    <>
      <div className="exam-page-header">
        <div className="doctor-breadcrumb">Dược sĩ <span>/</span> <strong>Tổng quan</strong></div>
        <h1>Tổng quan dược sĩ</h1>
        <p>Theo dõi đơn thuốc chờ cấp phát, tồn kho và hạn dùng thuốc.</p>
      </div>

      <Row className="g-3 mb-3">
        {summaryCards.map((card) => (
          <Col md={card.label.length > 16 ? 3 : 2} key={card.label}>
            <Card className="doctor-card h-100">
              <Card.Body>
                <span className="muted-cell">{card.label}</span>
                <h2 className="mb-0">{card.value}</h2>
              </Card.Body>
            </Card>
          </Col>
        ))}
      </Row>

      <Row className="g-3">
        <Col xl={8}>
          <Card className="doctor-card">
            <Card.Header>
              <h2>Đơn chờ cấp phát gần nhất</h2>
              <Button type="button" variant="link" onClick={() => navigate("/pharmacist/prescriptions")}>Xem tất cả</Button>
            </Card.Header>
            <Card.Body className="p-0">
              {prescriptions.length === 0 ? (
                <EmptyState title="Không có đơn chờ cấp phát" />
              ) : (
                <Table responsive hover className="doctor-table pharmacist-dashboard-table mb-0">
                  <thead>
                    <tr>
                      <th>Mã đơn</th>
                      <th>Bệnh nhân</th>
                      <th>Thanh toán</th>
                      <th>Tồn kho</th>
                      <th className="text-end">Thao tác</th>
                    </tr>
                  </thead>
                  <tbody>
                    {prescriptions.map((prescription) => (
                      <tr key={prescription.id}>
                        <td>{prescription.prescriptionCode}</td>
                        <td>{prescription.patientName || "--"}</td>
                        <td><StatusBadge status={prescription.paymentStatus || "UNPAID"} /></td>
                        <td><StatusBadge status={getPrescriptionStockStatus(prescription)} /></td>
                        <td className="text-end">
                          <Button size="sm" variant="outline-primary" onClick={() => navigate(`/pharmacist/prescriptions/${prescription.id}`)}>
                            Chi tiết
                          </Button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </Table>
              )}
            </Card.Body>
          </Card>
        </Col>

        <Col xl={4}>
          <Card className="doctor-card mb-3">
            <Card.Header><h2>Thuốc sắp hết</h2></Card.Header>
            <Card.Body className="p-0">
              {lowStock.length === 0 ? (
                <EmptyState title="Không có cảnh báo tồn thấp" />
              ) : (
                <Table responsive className="doctor-table mb-0">
                  <tbody>
                    {lowStock.slice(0, DASHBOARD_PAGE_SIZE).map((medicine) => (
                      <tr key={medicine.id}>
                        <td>{medicine.name}</td>
                        <td>{medicine.totalRemainingQuantity}/{medicine.minStockQuantity}</td>
                      </tr>
                    ))}
                  </tbody>
                </Table>
              )}
            </Card.Body>
          </Card>

          <Card className="doctor-card">
            <Card.Header><h2>Lô gần hết hạn</h2></Card.Header>
            <Card.Body className="p-0">
              {nearExpiry.length === 0 ? (
                <EmptyState title="Không có lô gần hết hạn" />
              ) : (
                <Table responsive className="doctor-table mb-0">
                  <tbody>
                    {nearExpiry.map((batch) => (
                      <tr key={batch.id}>
                        <td>{batch.batchCode}</td>
                        <td>{batch.medicineName}</td>
                        <td>{formatDate(batch.expiryDate)}</td>
                      </tr>
                    ))}
                  </tbody>
                </Table>
              )}
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </>
  );
}

export default PharmacistDashboardPage;
