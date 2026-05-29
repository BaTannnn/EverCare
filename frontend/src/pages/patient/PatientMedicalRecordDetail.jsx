import { useEffect, useMemo, useState } from "react";
import { Badge, Button, Card } from "react-bootstrap";
import { BsArrowLeft, BsDownload, BsFileEarmarkMedical, BsPrinter } from "react-icons/bs";
import { useNavigate, useParams } from "react-router-dom";
import { getPatientMedicalRecordById } from "../../services/patient/patientMedicalRecordApi";
import { formatCurrency, getPatientStatusMeta } from "./patientPageUtils";

function PatientMedicalRecordDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [record, setRecord] = useState(null);

  useEffect(() => {
    let mounted = true;

    const loadRecord = async () => {
      try {
        const response = await getPatientMedicalRecordById(id);
        if (mounted) {
          setRecord(response.data || null);
        }
      } catch (error) {
        console.error(error);
        if (mounted) {
          setRecord(null);
        }
      }
    };

    loadRecord();

    return () => {
      mounted = false;
    };
  }, [id]);

  const statusMeta = useMemo(() => getPatientStatusMeta(record?.paymentStatus || record?.status), [record]);

  if (!record) {
    return <div className="patient-loading-panel">Không tìm thấy bệnh án.</div>;
  }

  return (
    <div className="patient-page">
      <div className="patient-page-header-row align-start">
        <div>
          <p className="patient-eyebrow">{record.recordCode}</p>
          <h2>Chi tiết bệnh án ngày {record.displayVisitDate || record.visitDate}</h2>
        </div>
        <div className="patient-header-actions">
          <Button type="button" variant="light" className="patient-outline-button" onClick={() => navigate("/patient/medical-records")}>
            <BsArrowLeft /> Quay lại
          </Button>
          <Button type="button" variant="light" className="patient-outline-button">
            <BsPrinter /> In bệnh án
          </Button>
          <Button type="button" variant="light" className="patient-outline-button">
            <BsDownload /> Tải PDF
          </Button>
        </div>
      </div>

      <section className="patient-detail-grid">
        <Card className="patient-detail-main">
          <Card.Body>
            <div className="patient-detail-top">
              <div>
                <p>Bác sĩ phụ trách</p>
                <h3>{record.doctorName}</h3>
                <span>{record.departmentName}</span>
              </div>
              <Badge bg={statusMeta.variant} className="patient-status-badge">
                {statusMeta.label}
              </Badge>
            </div>

            <div className="patient-detail-block">
              <h4>Triệu chứng</h4>
              <p>{record.chiefComplaint || "Chưa có dữ liệu"}</p>
            </div>
            <div className="patient-detail-block">
              <h4>Chẩn đoán</h4>
              <p>{record.diagnosis || "Chưa có dữ liệu"}</p>
            </div>
            <div className="patient-detail-block">
              <h4>Kế hoạch điều trị</h4>
              <p>{record.treatmentPlan || "Chưa có dữ liệu"}</p>
            </div>
            <div className="patient-detail-block">
              <h4>Ghi chú / Lời dặn</h4>
              <p>{record.doctorNote || "Chưa có dữ liệu"}</p>
            </div>
          </Card.Body>
        </Card>

        <div className="patient-detail-side">
          <Card className="patient-detail-side-card">
            <Card.Body>
              <div className="patient-card-head">
                <div className="patient-card-icon">
                  <BsFileEarmarkMedical />
                </div>
                <h3>Dịch vụ đã sử dụng</h3>
              </div>
              {(record.services || []).length > 0 ? (
                <ul className="patient-bullet-list">
                  {(record.services || []).map((service) => (
                    <li key={service.id}>
                      <span>{service.name}</span>
                      <strong>{formatCurrency(service.price)}</strong>
                    </li>
                  ))}
                </ul>
              ) : (
                <p>Chưa có dịch vụ liên quan.</p>
              )}
            </Card.Body>
          </Card>

          <Card className="patient-detail-side-card">
            <Card.Body>
              <div className="patient-card-head">
                <div className="patient-card-icon">
                  <BsFileEarmarkMedical />
                </div>
                <h3>Kết quả xét nghiệm liên quan</h3>
              </div>
              {(record.testResults || []).length > 0 ? (
                <ul className="patient-result-list">
                  {(record.testResults || []).map((test) => (
                    <li key={test.id}>
                      <span>{test.resultTitle}</span>
                      <strong>{test.conclusion || "Chưa có kết quả"}</strong>
                    </li>
                  ))}
                </ul>
              ) : (
                <p>Chưa có kết quả xét nghiệm liên quan.</p>
              )}
            </Card.Body>
          </Card>

          <Card className="patient-detail-side-card">
            <Card.Body>
              <div className="patient-card-head">
                <div className="patient-card-icon">
                  <BsFileEarmarkMedical />
                </div>
                <h3>Đơn thuốc liên quan</h3>
              </div>
              <p>{record.prescription?.prescriptionCode || "Chưa có đơn thuốc liên quan"}</p>
            </Card.Body>
          </Card>

          <Card className="patient-detail-side-card">
            <Card.Body>
              <div className="patient-card-head">
                <div className="patient-card-icon">
                  <BsFileEarmarkMedical />
                </div>
                <h3>Hóa đơn liên quan</h3>
              </div>
              <p>TODO: Backend hiện chưa có API hóa đơn cho bệnh nhân.</p>
            </Card.Body>
          </Card>
        </div>
      </section>
    </div>
  );
}

export default PatientMedicalRecordDetail;
