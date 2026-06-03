import { useEffect, useState } from "react";
import { Badge, Button, Card, Modal } from "react-bootstrap";
import { BsArrowRight, BsCapsule, BsClock, BsEye, BsPerson } from "react-icons/bs";
import { getPatientPrescriptionById, getPatientPrescriptions } from "../../services/patient/patientPrescriptionApi";
import { getPatientStatusMeta } from "./patientPageUtils";

function PatientPrescriptions() {
  const [prescriptions, setPrescriptions] = useState([]);
  const [selectedPrescription, setSelectedPrescription] = useState(null);
  const [detailLoading, setDetailLoading] = useState(false);

  useEffect(() => {
    let mounted = true;

    const loadPrescriptions = async () => {
      try {
        const response = await getPatientPrescriptions();
        if (mounted) {
          setPrescriptions(response.data || []);
        }
      } catch (error) {
        console.error(error);
        if (mounted) {
          setPrescriptions([]);
        }
      }
    };

    loadPrescriptions();

    return () => {
      mounted = false;
    };
  }, []);

  const openDetail = async (prescription) => {
    setSelectedPrescription({ ...prescription, items: prescription.items || [] });
    setDetailLoading(true);

    try {
      const response = await getPatientPrescriptionById(prescription.id);
      setSelectedPrescription(response.data || prescription);
    } catch (error) {
      console.error(error);
      setSelectedPrescription(prescription);
    } finally {
      setDetailLoading(false);
    }
  };

  return (
    <div className="patient-page">
      <div className="patient-page-header-row">
        <div>
          <p className="patient-eyebrow">Đơn thuốc</p>
          <h2>Danh sách đơn thuốc theo từng đợt khám</h2>
        </div>
      </div>

      <section className="patient-prescription-list">
        {prescriptions.length > 0 ? prescriptions.map((prescription) => {
          const statusMeta = getPatientStatusMeta(prescription.status);

          return (
            <Card key={prescription.id} className="patient-prescription-card">
              <Card.Body>
                <div className="patient-prescription-top">
                  <div className="patient-prescription-title">
                    <div className="patient-prescription-icon">
                      <BsCapsule />
                    </div>
                    <div>
                      <h3>{prescription.prescriptionCode}</h3>
                      <span>
                        <BsClock /> {prescription.displayDate || prescription.date}
                      </span>
                    </div>
                  </div>
                  <Badge bg={statusMeta.variant} className="patient-status-badge">
                    {statusMeta.label}
                  </Badge>
                </div>

                <div className="patient-prescription-meta">
                  <span>
                    <BsPerson /> {prescription.doctorName}
                  </span>
                  <span>{prescription.medicineCount} loại thuốc</span>
                </div>

                <div className="patient-prescription-actions">
                  <Button type="button" className="patient-primary-soft" onClick={() => openDetail(prescription)}>
                    <BsEye /> Xem chi tiết
                  </Button>
                </div>
              </Card.Body>
            </Card>
          );
        }) : (
          <div className="patient-empty-state">
            <h4>Chưa có đơn thuốc</h4>
            <p>Backend hiện chưa trả về đơn thuốc cho hồ sơ của bạn.</p>
          </div>
        )}
      </section>

      <Card className="patient-info-note prescription-banner">
        <Card.Body>
          <div>
            <h3>Đơn thuốc điện tử an toàn</h3>
            <p>Mỗi đơn thuốc đều gắn với bệnh án và có thể tra cứu nhanh chóng bất cứ lúc nào.</p>
          </div>
          <Button type="button" className="patient-primary-soft">
            Xem lịch sử <BsArrowRight />
          </Button>
        </Card.Body>
      </Card>

      <Modal show={Boolean(selectedPrescription)} onHide={() => setSelectedPrescription(null)} centered size="lg" animation={false}>
        <Modal.Header closeButton>
          <Modal.Title>Chi tiết đơn thuốc</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {selectedPrescription && (
            <div className="patient-prescription-detail">
              {detailLoading ? (
                <div className="patient-loading-panel">Đang tải chi tiết đơn thuốc...</div>
              ) : selectedPrescription.items?.length ? (
                selectedPrescription.items.map((medicine) => (
                  <div key={medicine.id} className="patient-medicine-row">
                    <strong>{medicine.name}</strong>
                    <span>Số lượng: {medicine.quantity}</span>
                    <span>Liều dùng: {medicine.dosage}</span>
                    <span>Tần suất: {medicine.frequency}</span>
                    <span>Thời gian: {medicine.duration}</span>
                    <p>{medicine.instruction}</p>
                  </div>
                ))
              ) : (
                <div className="patient-loading-panel">Đơn thuốc chưa có danh sách thuốc chi tiết.</div>
              )}
            </div>
          )}
        </Modal.Body>
      </Modal>
    </div>
  );
}

export default PatientPrescriptions;
