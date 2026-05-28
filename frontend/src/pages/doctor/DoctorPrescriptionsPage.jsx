/* eslint-disable react-hooks/set-state-in-effect */
import { useCallback, useEffect, useState } from "react";
import { Alert, Button, Card, Table } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import EmptyState from "../../components/common/EmptyState";
import ErrorState from "../../components/common/ErrorState";
import LoadingState from "../../components/common/LoadingState";
import PageHeader from "../../components/common/PageHeader";
import StatusBadge from "../../components/common/StatusBadge";
import { getDoctorPrescriptions } from "../../services/doctor/doctorPrescriptionApi";
import { formatDate, getErrorMessage } from "./doctorPageUtils";

function DoctorPrescriptionsPage() {
  const navigate = useNavigate();
  const [prescriptions, setPrescriptions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadPrescriptions = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      const response = await getDoctorPrescriptions();
      setPrescriptions(response.data || []);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadPrescriptions();
  }, [loadPrescriptions]);

  return (
    <>
      <PageHeader
        title="Đơn thuốc"
        description="Bác sĩ xem lại đơn đã kê; kê đơn mới trong màn khám của từng bệnh án."
      />

      <Alert variant="info" className="small-alert">
        Bác sĩ chỉ kê đơn và xem lại đơn đã kê. Việc cấp phát thuốc và trừ kho do dược sĩ thực hiện.
      </Alert>

      <Card className="doctor-card">
        <Card.Header>
          <h2>Danh sách đơn thuốc</h2>
          <Button type="button" variant="outline-primary" onClick={() => navigate("/doctor/examination")}>
            Vào màn khám bệnh
          </Button>
        </Card.Header>
        <Card.Body className="p-0">
          {loading ? (
            <LoadingState />
          ) : error ? (
            <ErrorState message={error} onRetry={loadPrescriptions} />
          ) : prescriptions.length === 0 ? (
            <EmptyState
              title="Chưa có đơn thuốc"
              description="Để kê đơn, vào Khám bệnh, chọn lịch đang khám, rồi dùng tab Đơn thuốc trong bệnh án."
            />
          ) : (
            <Table responsive hover className="doctor-table mb-0">
              <thead>
                <tr>
                  <th>Mã đơn</th>
                  <th>Bệnh nhân</th>
                  <th>Ngày kê</th>
                  <th>Trạng thái</th>
                  <th>Số loại thuốc</th>
                  <th>Ghi chú</th>
                  <th>Hành động</th>
                </tr>
              </thead>
              <tbody>
                {prescriptions.map((prescription) => (
                  <tr key={prescription.id}>
                    <td>{prescription.prescriptionCode}</td>
                    <td>{prescription.patientName || "--"}</td>
                    <td>{formatDate(prescription.prescribedAt)}</td>
                    <td>
                      <StatusBadge status={prescription.status} />
                    </td>
                    <td>{(prescription.items || []).length}</td>
                    <td>{prescription.note || "--"}</td>
                    <td>
                      <Button
                        type="button"
                        size="sm"
                        variant="outline-primary"
                        disabled={!prescription.appointmentId}
                        onClick={() => navigate(`/doctor/examination/${prescription.appointmentId}`)}
                      >
                        Xem chi tiết
                      </Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </Table>
          )}
        </Card.Body>
      </Card>
    </>
  );
}

export default DoctorPrescriptionsPage;
