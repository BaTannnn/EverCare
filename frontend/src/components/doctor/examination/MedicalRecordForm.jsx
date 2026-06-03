import { Card, Col, Form, Row } from "react-bootstrap";

function MedicalRecordForm({
  editable,
  recordForm,
  savingRecord,
  onSave,
  onUpdateField,
}) {
  return (
    <Card className="doctor-card exam-section-card">
      <Card.Header>
        <h2>Nội dung thăm khám</h2>
      </Card.Header>
      <Card.Body>
        <Form id="medical-record-form" onSubmit={onSave}>
          <Form.Group className="mb-3" controlId="recordChiefComplaint">
            <Form.Label>Triệu chứng chính *</Form.Label>
            <Form.Control
              value={recordForm.chiefComplaint}
              onChange={(e) => onUpdateField("chiefComplaint", e.target.value)}
              placeholder="Nhập triệu chứng chính của bệnh nhân..."
              disabled={!editable || savingRecord}
            />
          </Form.Group>
          <Form.Group className="mb-3" controlId="recordDoctorNote">
            <Form.Label>Mô tả triệu chứng chi tiết</Form.Label>
            <Form.Control
              as="textarea"
              rows={4}
              value={recordForm.doctorNote}
              onChange={(e) => onUpdateField("doctorNote", e.target.value)}
              placeholder="Ghi nhận diễn biến triệu chứng..."
              disabled={!editable || savingRecord}
            />
          </Form.Group>
          <Form.Group className="mb-3" controlId="recordDiagnosis">
            <Form.Label>Chẩn đoán</Form.Label>
            <Form.Control
              value={recordForm.diagnosis}
              onChange={(e) => onUpdateField("diagnosis", e.target.value)}
              placeholder="Chẩn đoán lâm sàng..."
              disabled={!editable || savingRecord}
            />
          </Form.Group>
          <Form.Group className="mb-3" controlId="recordTreatmentPlan">
            <Form.Label>Kế hoạch điều trị</Form.Label>
            <Form.Control
              as="textarea"
              rows={4}
              value={recordForm.treatmentPlan}
              onChange={(e) => onUpdateField("treatmentPlan", e.target.value)}
              placeholder="Các bước điều trị tiếp theo..."
              disabled={!editable || savingRecord}
            />
          </Form.Group>
          <Row className="g-3">
            <Col md={6}>
              <Form.Group controlId="followUpDate">
                <Form.Label>Hẹn tái khám</Form.Label>
                <Form.Control type="date" disabled={!editable} />
              </Form.Group>
            </Col>
            <Col md={6}>
              <Form.Group controlId="followPriority">
                <Form.Label>Mức độ ưu tiên theo dõi</Form.Label>
                <Form.Select disabled={!editable}>
                  <option>Bình thường</option>
                  <option>Cần theo dõi</option>
                  <option>Ưu tiên cao</option>
                </Form.Select>
              </Form.Group>
            </Col>
          </Row>
        </Form>
      </Card.Body>
    </Card>
  );
}

export default MedicalRecordForm;
