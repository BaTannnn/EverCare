import { useCallback, useEffect, useMemo, useState } from "react";
import { Alert, Button, Card, Form, Row, Col, Table } from "react-bootstrap";
import { useLocation, useNavigate } from "react-router-dom";
import EmptyState from "../../components/common/EmptyState";
import ErrorState from "../../components/common/ErrorState";
import LoadingState from "../../components/common/LoadingState";
import StatusBadge from "../../components/common/StatusBadge";
import { dispensePrescription, getPharmacistPrescriptions } from "../../services/pharmacist/pharmacistPrescriptionApi";
import { formatDate, getErrorMessage, getPrescriptionStockStatus, normalizeText } from "./pharmacistPageUtils";

const statusOptions = ["PRESCRIBED", "DISPENSED", "CANCELLED"];
const paymentOptions = ["ALL", "PAID", "UNPAID"];

function PharmacistPrescriptionsPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const [status, setStatus] = useState("PRESCRIBED");
  const [payment, setPayment] = useState("ALL");
  const [keyword, setKeyword] = useState(location.state?.search || "");
  const [prescriptions, setPrescriptions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionId, setActionId] = useState(null);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");

  const loadPrescriptions = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      const response = await getPharmacistPrescriptions({ status });
      setPrescriptions(response.data || []);
    } catch (err) {
      if (err.response?.status === 401) {
        navigate("/login", { replace: true });
        return;
      }

      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [navigate, status]);

  useEffect(() => {
    loadPrescriptions();
  }, [loadPrescriptions]);

  const filteredPrescriptions = useMemo(() => {
    const normalizedKeyword = normalizeText(keyword);

    return prescriptions.filter((prescription) => {
      const paymentMatch = payment === "ALL" || prescription.paymentStatus === payment;
      const text = normalizeText([
        prescription.prescriptionCode,
        prescription.patientName,
        prescription.doctorName,
        prescription.patientPhone,
      ].join(" "));

      return paymentMatch && (!normalizedKeyword || text.includes(normalizedKeyword));
    });
  }, [keyword, payment, prescriptions]);

  const handleQuickDispense = async (prescription) => {
    setActionId(prescription.id);
    setNotice("");

    try {
      await dispensePrescription(prescription.id);
      setNotice("Đã cấp phát thuốc.");
      loadPrescriptions();
    } catch (err) {
      setNotice(getErrorMessage(err));
      if (err.response?.status === 401) {
        navigate("/login", { replace: true });
      }
    } finally {
      setActionId(null);
    }
  };

  if (loading) {
    return <LoadingState message="Đang tải danh sách đơn thuốc..." />;
  }

  if (error) {
    return <ErrorState message={error} onRetry={loadPrescriptions} />;
  }

  return (
    <>
      <div className="exam-page-header">
        <div className="doctor-breadcrumb">Dược sĩ <span>/</span> <strong>Đơn chờ cấp phát</strong></div>
        <h1>Đơn thuốc</h1>
        <p>Chỉ cấp phát đơn đã thanh toán và đủ tồn kho.</p>
      </div>

      {notice && <Alert variant={notice.startsWith("Đã") ? "success" : "warning"}>{notice}</Alert>}

      <Card className="doctor-card mb-3">
        <Card.Body>
          <Row className="g-3">
            <Col md={3}>
              <Form.Group>
                <Form.Label>Trạng thái đơn</Form.Label>
                <Form.Select value={status} onChange={(e) => setStatus(e.target.value)}>
                  {statusOptions.map((option) => (
                    <option value={option} key={option}>{option}</option>
                  ))}
                </Form.Select>
              </Form.Group>
            </Col>
            <Col md={3}>
              <Form.Group>
                <Form.Label>Thanh toán</Form.Label>
                <Form.Select value={payment} onChange={(e) => setPayment(e.target.value)}>
                  {paymentOptions.map((option) => (
                    <option value={option} key={option}>{option}</option>
                  ))}
                </Form.Select>
              </Form.Group>
            </Col>
            <Col md={6}>
              <Form.Group>
                <Form.Label>Tìm kiếm</Form.Label>
                <Form.Control
                  value={keyword}
                  onChange={(e) => setKeyword(e.target.value)}
                  placeholder="Mã đơn, bệnh nhân, bác sĩ..."
                />
              </Form.Group>
            </Col>
          </Row>
        </Card.Body>
      </Card>

      <Card className="doctor-card">
        <Card.Header>
          <h2>Danh sách đơn thuốc</h2>
          <Button type="button" variant="link" onClick={loadPrescriptions}>Tải lại</Button>
        </Card.Header>
        <Card.Body className="p-0">
          {filteredPrescriptions.length === 0 ? (
            <EmptyState title="Không có đơn thuốc phù hợp" />
          ) : (
            <Table responsive hover className="doctor-table mb-0">
              <thead>
                <tr>
                  <th>Mã đơn</th>
                  <th>Bệnh nhân</th>
                  <th>Bác sĩ kê đơn</th>
                  <th>Ngày kê</th>
                  <th>Số loại thuốc</th>
                  <th>Trạng thái đơn</th>
                  <th>Thanh toán</th>
                  <th>Tồn kho</th>
                  <th>Hành động</th>
                </tr>
              </thead>
              <tbody>
                {filteredPrescriptions.map((prescription) => {
                  const stockStatus = getPrescriptionStockStatus(prescription);
                  const canDispense = prescription.status === "PRESCRIBED"
                    && prescription.paymentStatus === "PAID"
                    && stockStatus !== "OUT";

                  return (
                    <tr key={prescription.id}>
                      <td>{prescription.prescriptionCode}</td>
                      <td>{prescription.patientName || "--"}</td>
                      <td>{prescription.doctorName || "--"}</td>
                      <td>{formatDate(prescription.prescribedAt)}</td>
                      <td>{(prescription.items || []).length}</td>
                      <td><StatusBadge status={prescription.status} /></td>
                      <td><StatusBadge status={prescription.paymentStatus || "UNPAID"} /></td>
                      <td><StatusBadge status={stockStatus} /></td>
                      <td>
                        <div className="d-flex gap-2">
                          <Button
                            type="button"
                            size="sm"
                            variant="outline-primary"
                            onClick={() => navigate(`/pharmacist/prescriptions/${prescription.id}`)}
                          >
                            Xem chi tiết
                          </Button>
                          <Button
                            type="button"
                            size="sm"
                            disabled={!canDispense || actionId === prescription.id}
                            onClick={() => handleQuickDispense(prescription)}
                            title={prescription.paymentStatus !== "PAID" ? "Bệnh nhân chưa thanh toán, chưa thể cấp phát thuốc." : undefined}
                          >
                            Cấp phát nhanh
                          </Button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </Table>
          )}
        </Card.Body>
      </Card>
    </>
  );
}

export default PharmacistPrescriptionsPage;
