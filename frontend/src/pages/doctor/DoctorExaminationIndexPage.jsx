import { Button, Card } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import PageHeader from "../../components/common/PageHeader";

function DoctorExaminationIndexPage() {
  const navigate = useNavigate();

  return (
    <>
      <PageHeader
        title="Khám bệnh"
        description="Chọn một lịch hẹn đang chờ hoặc đang khám để vào workspace."
      />
      <Card className="doctor-card">
        <Card.Body className="action-card-body">
          <div>
            <h2>Vào ca khám từ lịch hẹn</h2>
            <p>Workspace cần mã lịch hẹn để tải bệnh nhân, bệnh án, chỉ định và đơn thuốc.</p>
          </div>
          <Button type="button" onClick={() => navigate("/doctor/appointments")}>
            Xem lịch hẹn
          </Button>
        </Card.Body>
      </Card>
    </>
  );
}

export default DoctorExaminationIndexPage;
