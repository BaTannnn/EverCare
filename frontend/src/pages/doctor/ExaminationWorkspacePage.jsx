/* eslint-disable react-hooks/set-state-in-effect */
import { useCallback, useEffect, useMemo, useState } from "react";
import { Alert, Button, Card, Col, Form, Row, Table } from "react-bootstrap";
import { useNavigate, useParams } from "react-router-dom";
import ConfirmModal from "../../components/common/ConfirmModal";
import EmptyState from "../../components/common/EmptyState";
import ErrorState from "../../components/common/ErrorState";
import LoadingState from "../../components/common/LoadingState";
import StatusBadge from "../../components/common/StatusBadge";
import { getAppointmentDetail } from "../../services/doctor/doctorAppointmentApi";
import {
  addMedicalRecordService,
  getMedicalRecordServices,
  updateMedicalRecord,
} from "../../services/doctor/doctorMedicalRecordApi";
import { searchMedicalServices } from "../../services/doctor/doctorMedicalServiceApi";
import { searchMedicines } from "../../services/doctor/doctorMedicineApi";
import {
  canStartExamination,
  editableStatuses,
  formatDate,
  formatMoney,
  getErrorMessage,
} from "./doctorPageUtils";

const emptyRecordForm = {
  chiefComplaint: "",
  diagnosis: "",
  treatmentPlan: "",
  doctorNote: "",
};

const emptyServiceForm = {
  serviceId: "",
  resultSummary: "",
};

const emptyPrescriptionLine = {
  quantity: 1,
  dosage: "",
  frequency: "",
  duration: "",
  instruction: "",
};

function ExaminationWorkspacePage() {
  const { appointmentId } = useParams();
  const navigate = useNavigate();
  const [appointment, setAppointment] = useState(null);
  const [recordForm, setRecordForm] = useState(emptyRecordForm);
  const [services, setServices] = useState([]);
  const [serviceForm, setServiceForm] = useState(emptyServiceForm);
  const [serviceKeyword, setServiceKeyword] = useState("");
  const [serviceOptions, setServiceOptions] = useState([]);
  const [serviceSearching, setServiceSearching] = useState(false);
  const [serviceDropdownOpen, setServiceDropdownOpen] = useState(false);
  const [prescription, setPrescription] = useState(null);
  const [medicineKeyword, setMedicineKeyword] = useState("");
  const [medicineResults, setMedicineResults] = useState([]);
  const [loading, setLoading] = useState(true);
  const [savingRecord, setSavingRecord] = useState(false);
  const [savingService, setSavingService] = useState(false);
  const [medicineLoading, setMedicineLoading] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [completeModal, setCompleteModal] = useState(false);

  const medicalRecord = appointment?.medicalRecord;
  const editable = editableStatuses.includes(appointment?.status);

  const loadWorkspace = useCallback(async () => {
    setLoading(true);
    setError("");
    setNotice("");

    try {
      const response = await getAppointmentDetail(appointmentId);
      const detail = response.data;
      setAppointment(detail);
      setRecordForm({
        chiefComplaint: detail?.medicalRecord?.chiefComplaint || "",
        diagnosis: detail?.medicalRecord?.diagnosis || "",
        treatmentPlan: detail?.medicalRecord?.treatmentPlan || "",
        doctorNote: detail?.medicalRecord?.doctorNote || "",
      });
      setPrescription(detail?.prescription || null);

      if (detail?.medicalRecord?.id) {
        const serviceResponse = await getMedicalRecordServices(detail.medicalRecord.id);
        setServices(serviceResponse.data || []);
      } else {
        setServices([]);
      }
    } catch (err) {
      if (err.response?.status === 401) {
        navigate("/login", { replace: true });
        return;
      }

      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [appointmentId, navigate]);

  useEffect(() => {
    loadWorkspace();
  }, [loadWorkspace]);

  useEffect(() => {
    const keyword = serviceKeyword.trim();

    const timer = setTimeout(async () => {
      setServiceSearching(true);
      try {
        const response = await searchMedicalServices(keyword);
        setServiceOptions(response.data || []);
      } catch {
        setServiceOptions([]);
      } finally {
        setServiceSearching(false);
      }
    }, 300);

    return () => clearTimeout(timer);
  }, [serviceKeyword]);

  useEffect(() => {
    const keyword = medicineKeyword.trim();

    if (!keyword) {
      setMedicineResults([]);
      return undefined;
    }

    const timer = setTimeout(async () => {
      setMedicineLoading(true);
      try {
        const response = await searchMedicines(keyword);
        setMedicineResults(response.data || []);
      } catch {
        setMedicineResults([]);
      } finally {
        setMedicineLoading(false);
      }
    }, 300);

    return () => clearTimeout(timer);
  }, [medicineKeyword]);

  const updateRecordField = (field, value) => {
    setRecordForm((current) => ({ ...current, [field]: value }));
  };

  const handleSaveRecord = async (e) => {
    e.preventDefault();

    if (!medicalRecord?.id) {
      setNotice("Vui lòng bắt đầu khám trước khi lưu bệnh án.");
      return;
    }

    setSavingRecord(true);
    setNotice("");

    try {
      const response = await updateMedicalRecord(medicalRecord.id, recordForm);
      setAppointment((current) => ({
        ...current,
        medicalRecord: response.data,
      }));
      setNotice("Đã lưu bệnh án.");
    } catch (err) {
      setNotice(getErrorMessage(err));
      if (err.response?.status === 409) {
        loadWorkspace();
      }
    } finally {
      setSavingRecord(false);
    }
  };

  const handleAddService = async (e) => {
    e.preventDefault();

    if (!medicalRecord?.id) {
      setNotice("Vui lòng bắt đầu khám trước khi thêm chỉ định.");
      return;
    }

    if (!serviceForm.serviceId) {
      setNotice("Vui lòng chọn dịch vụ từ danh sách gợi ý.");
      return;
    }

    const isDuplicateService = services.some((item) => Number(item.serviceId) === Number(serviceForm.serviceId));
    if (isDuplicateService) {
      setNotice("Dịch vụ này đã được chỉ định trong bệnh án.");
      return;
    }

    setSavingService(true);
    setNotice("");

    try {
      const response = await addMedicalRecordService(medicalRecord.id, {
        serviceId: Number(serviceForm.serviceId),
        quantity: 1,
        resultSummary: serviceForm.resultSummary,
      });
      setServices((current) => [response.data, ...current]);
      setServiceForm(emptyServiceForm);
      setServiceKeyword("");
      setNotice("Đã thêm chỉ định.");
    } catch (err) {
      setNotice(getErrorMessage(err));
      if (err.response?.status === 409) {
        loadWorkspace();
      }
    } finally {
      setSavingService(false);
    }
  };

  const selectMedicalService = (service) => {
    setServiceForm((current) => ({
      ...current,
      serviceId: service.id,
    }));
    setServiceKeyword(service.name || "");
    setServiceDropdownOpen(false);
  };

  const addMedicineToPrescription = (medicine) => {
    const newItem = {
      ...emptyPrescriptionLine,
      medicineId: medicine.medicineId,
      medicineName: medicine.medicineName,
      unit: medicine.unit,
      unitPrice: medicine.unitPrice,
      availableQuantity: medicine.availableQuantity,
    };

    setPrescription((current) => ({
      id: current?.id,
      prescriptionCode: current?.prescriptionCode,
      status: current?.status || "PRESCRIBED",
      note: current?.note || "",
      medicalRecordId: medicalRecord?.id,
      patientName: appointment?.patient?.fullName,
      items: [...(current?.items || []), newItem],
    }));
  };

  const updatePrescriptionItem = (index, field, value) => {
    setPrescription((current) => ({
      ...current,
      items: (current?.items || []).map((item, itemIndex) =>
        itemIndex === index ? { ...item, [field]: value } : item
      ),
    }));
  };

  const removePrescriptionItem = (index) => {
    setPrescription((current) => ({
      ...current,
      items: (current?.items || []).filter((_, itemIndex) => itemIndex !== index),
    }));
  };

  const handleSavePrescription = async (e) => {
    e.preventDefault();
    setNotice("Backend chưa có API bác sĩ tạo/cập nhật đơn thuốc.");
  };

  const allResults = useMemo(
    () => services.flatMap((item) => item.testResults || []),
    [services]
  );

  if (loading) {
    return <LoadingState />;
  }

  if (error) {
    return <ErrorState message={error} onRetry={loadWorkspace} />;
  }

  if (!appointment) {
    return <ErrorState message="Không tìm thấy ca khám." />;
  }

  const patient = appointment.patient || {};
  const appointmentService = appointment.service || {};

  return (
    <>
      <div className="exam-page-header">
        <div className="doctor-breadcrumb">
          Bác sĩ <span>/</span> <strong>Khám bệnh</strong>
        </div>
        <h1>Khám bệnh</h1>
        <p>Ghi nhận thông tin khám và tạo bệnh án điện tử.</p>
      </div>

      {notice && <Alert variant={notice.includes("Đã") ? "success" : "warning"}>{notice}</Alert>}

      {canStartExamination(appointment.status) && (
        <Alert variant="warning">
          Vui lòng bắt đầu khám trước từ trang chi tiết lịch hẹn để tạo bệnh án.
        </Alert>
      )}

      <Card className="exam-patient-banner">
        <Card.Body>
          <div className="exam-avatar">◎</div>
          <div className="exam-patient-title">
            <h2>{patient.fullName || "Bệnh nhân"}</h2>
            <span>ID: {appointment.appointmentCode || appointment.id} · Ngày: {formatDate(appointment.appointmentDate)}</span>
          </div>
          <div className="exam-banner-meta">
            <span>Dịch vụ</span>
            <strong>{appointmentService.name || "--"}</strong>
          </div>
          <div className="exam-banner-meta">
            <span>Trạng thái</span>
            <StatusBadge status={appointment.status} label={appointment.statusLabel} />
          </div>
        </Card.Body>
      </Card>

      <div className="exam-layout">
        <div className="exam-main-column">
          <Card className="doctor-card exam-section-card">
            <Card.Header>
              <h2>Nội dung thăm khám</h2>
            </Card.Header>
            <Card.Body>
              <Form id="medical-record-form" onSubmit={handleSaveRecord}>
                <Form.Group className="mb-3" controlId="recordChiefComplaint">
                  <Form.Label>Triệu chứng chính *</Form.Label>
                  <Form.Control
                    value={recordForm.chiefComplaint}
                    onChange={(e) => updateRecordField("chiefComplaint", e.target.value)}
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
                    onChange={(e) => updateRecordField("doctorNote", e.target.value)}
                    placeholder="Ghi nhận diễn biến triệu chứng..."
                    disabled={!editable || savingRecord}
                  />
                </Form.Group>
                <Form.Group className="mb-3" controlId="recordDiagnosis">
                  <Form.Label>Chẩn đoán</Form.Label>
                  <Form.Control
                    value={recordForm.diagnosis}
                    onChange={(e) => updateRecordField("diagnosis", e.target.value)}
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
                    onChange={(e) => updateRecordField("treatmentPlan", e.target.value)}
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

          <Card className="doctor-card exam-section-card">
            <Card.Header>
              <h2>Dịch vụ cần làm sáng đã sử dụng</h2>
            </Card.Header>
            <Card.Body className="p-0">
              <Form className="exam-inline-form" onSubmit={handleAddService}>
                <div className="service-autocomplete">
                  <Form.Control
                    value={serviceKeyword}
                    onFocus={() => setServiceDropdownOpen(true)}
                    onChange={(e) => {
                      setServiceKeyword(e.target.value);
                      setServiceForm((current) => ({ ...current, serviceId: "" }));
                      setServiceDropdownOpen(true);
                    }}
                    placeholder="Tìm dịch vụ..."
                    disabled={!editable || savingService}
                  />
                  {serviceDropdownOpen && editable && (
                    <div className="service-autocomplete-menu">
                      {serviceSearching ? (
                        <div className="service-autocomplete-empty">Đang tìm dịch vụ...</div>
                      ) : serviceOptions.length === 0 ? (
                        <div className="service-autocomplete-empty">Không tìm thấy dịch vụ</div>
                      ) : (
                        serviceOptions.map((service) => (
                          <button type="button" key={service.id} onClick={() => selectMedicalService(service)}>
                            <strong>{service.name}</strong>
                            <span>
                              {service.code || "DV"} · {service.serviceType || "--"} · {formatMoney(service.price)}
                            </span>
                          </button>
                        ))
                      )}
                    </div>
                  )}
                </div>
                <Form.Control
                  value={serviceForm.resultSummary}
                  onChange={(e) => setServiceForm((current) => ({ ...current, resultSummary: e.target.value }))}
                  placeholder="Ghi chú"
                  disabled={!editable || savingService}
                />
                <Button type="submit" disabled={!editable || savingService}>
                  {savingService ? "Đang thêm..." : "Thêm dịch vụ"}
                </Button>
              </Form>
              {services.length === 0 ? (
                <EmptyState title="Chưa có dịch vụ" description="Nhấn thêm dịch vụ để ghi nhận dịch vụ phát sinh." />
              ) : (
                <Table responsive className="doctor-table exam-table mb-0">
                  <thead>
                    <tr>
                      <th>Tên dịch vụ</th>
                      <th>Loại</th>
                      <th>Giá</th>
                      <th>Ghi chú</th>
                      <th>Kết quả</th>
                    </tr>
                  </thead>
                  <tbody>
                    {services.map((item) => (
                      <tr key={item.id}>
                        <td>{item.serviceName}</td>
                        <td>{item.serviceType}</td>
                        <td>{formatMoney(item.unitPrice)}</td>
                        <td>{item.resultSummary || "--"}</td>
                        <td>{(item.testResults || []).length ? "Đã có" : "Chưa có"}</td>
                      </tr>
                    ))}
                  </tbody>
                </Table>
              )}
            </Card.Body>
          </Card>

          <Card className="doctor-card exam-section-card">
            <Card.Header>
              <h2>Chỉ định xét nghiệm / Chẩn đoán hình ảnh</h2>
            </Card.Header>
            <Card.Body className="p-0">
              {allResults.length === 0 ? (
                <EmptyState title="Chưa có kết quả xét nghiệm" description="Bác sĩ chỉ xem kết quả, nhân viên y tế nhập kết quả." />
              ) : (
                <Table responsive className="doctor-table exam-table mb-0">
                  <thead>
                    <tr>
                      <th>Dịch vụ</th>
                      <th>Tên xét nghiệm</th>
                      <th>Kết luận</th>
                      <th>Ngày trả kết quả</th>
                      <th>Thực hiện bởi</th>
                    </tr>
                  </thead>
                  <tbody>
                    {allResults.map((result) => (
                      <tr key={result.id}>
                        <td>{result.serviceName || "--"}</td>
                        <td>{result.resultTitle || "--"}</td>
                        <td>{result.conclusion || "--"}</td>
                        <td>{formatDate(result.resultDate)}</td>
                        <td>{result.performedByName || "--"}</td>
                      </tr>
                    ))}
                  </tbody>
                </Table>
              )}
            </Card.Body>
          </Card>

          <Card className="doctor-card exam-section-card">
            <Card.Header>
              <h2>Đơn thuốc</h2>
              <Alert variant="info" className="small-alert mb-0">
                Backend chưa có API bác sĩ tạo/cập nhật đơn thuốc.
              </Alert>
            </Card.Header>
            <Card.Body>
              <Form onSubmit={handleSavePrescription}>
                <Form.Group className="mb-3" controlId="medicineSearch">
                  <Form.Label>Tìm thuốc</Form.Label>
                  <Form.Control
                    value={medicineKeyword}
                    onChange={(e) => setMedicineKeyword(e.target.value)}
                    placeholder="Nhập tên thuốc"
                    disabled={!editable}
                  />
                </Form.Group>
                {medicineLoading && <LoadingState message="Đang tìm thuốc..." />}
                {medicineResults.length > 0 && (
                  <div className="medicine-results">
                    {medicineResults.map((medicine) => (
                      <div className="medicine-result" key={medicine.medicineId}>
                        <div>
                          <strong>{medicine.medicineName}</strong>
                          <span>
                            {medicine.unit} · {formatMoney(medicine.unitPrice)} · Tồn {medicine.availableQuantity}
                          </span>
                        </div>
                        <Button type="button" size="sm" onClick={() => addMedicineToPrescription(medicine)} disabled={!editable}>
                          Thêm
                        </Button>
                      </div>
                    ))}
                  </div>
                )}
                {(prescription?.items || []).length === 0 ? (
                  <EmptyState title="Chưa có thuốc trong đơn" description="Tìm thuốc và thêm vào đơn kê." />
                ) : (
                  <div className="prescription-list">
                    {(prescription?.items || []).map((item, index) => (
                      <div className="prescription-item" key={`${item.medicineId}-${index}`}>
                        <div className="prescription-item-title">
                          <strong>{item.medicineName}</strong>
                          <Button type="button" size="sm" variant="outline-danger" onClick={() => removePrescriptionItem(index)} disabled={!editable}>
                            Xóa
                          </Button>
                        </div>
                        <Row className="g-2">
                          {[
                            ["quantity", "Số lượng", "number"],
                            ["dosage", "Liều dùng", "text"],
                            ["frequency", "Tần suất", "text"],
                            ["duration", "Thời gian dùng", "text"],
                            ["instruction", "Hướng dẫn", "text"],
                          ].map(([field, label, type]) => (
                            <Col md={field === "instruction" ? 4 : 2} key={field}>
                              <Form.Label>{label}</Form.Label>
                              <Form.Control
                                type={type}
                                min={field === "quantity" ? "1" : undefined}
                                value={item[field] || ""}
                                onChange={(e) => updatePrescriptionItem(index, field, e.target.value)}
                                disabled={!editable}
                              />
                            </Col>
                          ))}
                        </Row>
                      </div>
                    ))}
                  </div>
                )}
                <Button type="submit" disabled={!editable}>
                  Lưu đơn thuốc
                </Button>
              </Form>
            </Card.Body>
          </Card>
        </div>

      </div>

      <div className="exam-action-bar">
        <Button
          type="button"
          variant="outline-secondary"
          onClick={() =>
            setRecordForm({
              chiefComplaint: medicalRecord?.chiefComplaint || "",
              diagnosis: medicalRecord?.diagnosis || "",
              treatmentPlan: medicalRecord?.treatmentPlan || "",
              doctorNote: medicalRecord?.doctorNote || "",
            })
          }
        >
          Hủy thay đổi
        </Button>
        <Button type="submit" form="medical-record-form" variant="outline-primary" disabled={!editable || savingRecord}>
          {savingRecord ? "Đang lưu..." : "Lưu nháp"}
        </Button>
        <Button type="button" variant="outline-primary" disabled={!editable}>
          Kê đơn thuốc
        </Button>
        <Button type="button" disabled={!editable} onClick={() => setCompleteModal(true)}>
          Hoàn tất bệnh án
        </Button>
      </div>

      <ConfirmModal
        show={completeModal}
        title="Hoàn tất khám"
        message="Backend chưa có API hoàn tất khám, nên thao tác này chưa cập nhật trạng thái thật."
        confirmText="Đã hiểu"
        onConfirm={() => setCompleteModal(false)}
        onHide={() => setCompleteModal(false)}
      />
    </>
  );
}

export default ExaminationWorkspacePage;
