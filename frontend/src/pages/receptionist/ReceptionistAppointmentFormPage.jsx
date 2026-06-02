import { useEffect, useMemo, useState } from "react";
import { Alert, Button, Card, Col, Form, Row } from "react-bootstrap";
import { BsArrowLeft, BsCalendarCheck } from "react-icons/bs";
import { Link, useNavigate } from "react-router-dom";
import ErrorState from "../../components/common/ErrorState";
import LoadingState from "../../components/common/LoadingState";
import {
  createReceptionistAppointment,
  getReceptionistAppointmentDetail,
  updateReceptionistAppointment,
} from "../../services/receptionist/receptionistAppointmentApi";
import {
  getReceptionistDepartments,
  getReceptionistDoctors,
  getReceptionistMedicalServices,
} from "../../services/receptionist/receptionistReferenceApi";
import { formatTime, getErrorMessage, todayInputValue } from "./receptionistPageUtils";

const emptyPatient = {
  fullName: "",
  phone: "",
  gender: "",
  dateOfBirth: "",
};

const emptyForm = {
  patientId: "",
  patient: emptyPatient,
  departmentId: "",
  doctorId: "",
  serviceId: "",
  appointmentDate: todayInputValue(),
  startTime: "08:00",
  endTime: "",
  reason: "",
  symptomNote: "",
  checkInNow: false,
};

function ReceptionistAppointmentFormPage({ mode = "create", appointmentId }) {
  const navigate = useNavigate();
  const isEdit = mode === "edit";
  const [loading, setLoading] = useState(isEdit);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [appointment, setAppointment] = useState(null);
  const [departments, setDepartments] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [services, setServices] = useState([]);
  const [form, setForm] = useState(emptyForm);

  const loadReferences = async () => {
    const [departmentRes, doctorRes, serviceRes] = await Promise.all([
      getReceptionistDepartments(),
      getReceptionistDoctors(),
      getReceptionistMedicalServices(),
    ]);

    setDepartments(departmentRes.data || []);
    setDoctors(doctorRes.data || []);
    setServices(serviceRes.data || []);
  };

  const loadAppointment = async () => {
    if (!isEdit) {
      return;
    }

    setLoading(true);

    try {
      const response = await getReceptionistAppointmentDetail(appointmentId);
      const detail = response.data;
      setAppointment(detail);
      setForm({
        patientId: "",
        patient: emptyPatient,
        departmentId: detail.departmentId || detail.doctor?.departmentId || "",
        doctorId: detail.doctorId || detail.doctor?.id || "",
        serviceId: detail.serviceId || detail.service?.id || "",
        appointmentDate: detail.appointmentDate || todayInputValue(),
        startTime: formatTime(detail.startTime) !== "--" ? formatTime(detail.startTime) : "08:00",
        endTime: formatTime(detail.endTime) !== "--" ? formatTime(detail.endTime) : "",
        reason: detail.reason || "",
        symptomNote: detail.symptomNote || "",
        checkInNow: detail.status === "WAITING",
      });
    } catch (err) {
      if (err.response?.status === 401) {
        navigate("/login", { replace: true });
        return;
      }
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    let mounted = true;

    const init = async () => {
      try {
        await loadReferences();
        if (mounted) {
          await loadAppointment();
        }
      } catch (err) {
        if (err.response?.status === 401) {
          navigate("/login", { replace: true });
          return;
        }
        setError(getErrorMessage(err));
      }
    };

    init();

    return () => {
      mounted = false;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const filteredDoctors = useMemo(() => {
    if (!form.departmentId) return doctors;
    return doctors.filter((doctor) => String(doctor.departmentId) === String(form.departmentId));
  }, [doctors, form.departmentId]);

  const filteredServices = useMemo(() => {
    if (!form.departmentId) return services;
    return services.filter((service) => String(service.departmentId) === String(form.departmentId));
  }, [form.departmentId, services]);

  useEffect(() => {
    if (form.doctorId && !filteredDoctors.some((doctor) => String(doctor.id) === String(form.doctorId))) {
      setForm((current) => ({ ...current, doctorId: "" }));
    }
  }, [filteredDoctors, form.doctorId]);

  useEffect(() => {
    if (form.serviceId && !filteredServices.some((service) => String(service.id) === String(form.serviceId))) {
      setForm((current) => ({ ...current, serviceId: "" }));
    }
  }, [filteredServices, form.serviceId]);

  const updateField = (field, value) => {
    setForm((current) => ({ ...current, [field]: value }));
  };

  const updatePatientField = (field, value) => {
    setForm((current) => ({
      ...current,
      patient: { ...current.patient, [field]: value },
    }));
  };

  const validate = () => {
    if (!form.departmentId) return "Vui lòng chọn chuyên khoa.";
    if (!form.doctorId) return "Vui lòng chọn bác sĩ.";
    if (!form.serviceId) return "Vui lòng chọn dịch vụ.";
    if (!form.appointmentDate) return "Vui lòng chọn ngày khám.";
    if (!form.startTime) return "Vui lòng chọn giờ khám.";

    if (!isEdit && !String(form.patientId || "").trim()) {
      if (!form.patient.fullName.trim()) return "Vui lòng nhập tên bệnh nhân.";
      if (!form.patient.phone.trim()) return "Vui lòng nhập số điện thoại.";
      if (!form.patient.gender.trim()) return "Vui lòng nhập giới tính.";
      if (!form.patient.dateOfBirth.trim()) return "Vui lòng nhập ngày sinh.";
    }

    return "";
  };

  const buildPayload = () => {
    const payload = {
      departmentId: Number(form.departmentId),
      doctorId: Number(form.doctorId),
      serviceId: Number(form.serviceId),
      appointmentDate: form.appointmentDate,
      startTime: form.startTime,
      endTime: form.endTime || undefined,
      reason: form.reason.trim() || undefined,
      symptomNote: form.symptomNote.trim() || undefined,
    };

    if (!isEdit) {
      payload.checkInNow = Boolean(form.checkInNow);
      if (String(form.patientId || "").trim()) {
        payload.patientId = Number(form.patientId);
      } else {
        payload.patient = {
          fullName: form.patient.fullName.trim(),
          phone: form.patient.phone.trim(),
          gender: form.patient.gender.trim(),
          dateOfBirth: form.patient.dateOfBirth.trim(),
        };
      }
    }

    return payload;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setNotice("");

    const validationError = validate();
    if (validationError) {
      setError(validationError);
      return;
    }

    setSubmitting(true);

    try {
      const payload = buildPayload();
      const response = isEdit
        ? await updateReceptionistAppointment(appointmentId, payload)
        : await createReceptionistAppointment(payload);

      const saved = response.data;
      setNotice(isEdit ? "Đã cập nhật lịch hẹn." : "Đã tạo lịch hẹn thành công.");
      navigate(`/receptionist/appointments/${saved.id}`, {
        replace: true,
        state: { notice: isEdit ? "Đã cập nhật lịch hẹn." : "Đã tạo lịch hẹn thành công." },
      });
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
    return <LoadingState message={isEdit ? "Đang tải form sửa lịch..." : "Đang tải form tạo lịch..."} />;
  }

  if (error && !appointment && isEdit) {
    return <ErrorState message={error} onRetry={loadAppointment} />;
  }

  return (
    <>
      <div className="page-header receptionist-page-header">
        <div>
          <div className="page-eyebrow">{isEdit ? "Lễ tân / Sửa lịch hẹn" : "Lễ tân / Tạo lịch hẹn"}</div>
          <h1>{isEdit ? appointment?.appointmentCode || "Sửa lịch hẹn" : "Tạo lịch hẹn hộ bệnh nhân"}</h1>
          <p>
            {isEdit
              ? "Chỉ sửa các trường cho phép khi lịch còn BOOKED hoặc WAITING."
              : "Cho phép nhập patientId hoặc tạo bệnh nhân mới ngay trong quầy."}
          </p>
        </div>
        <div className="page-header-actions">
          <Button as={Link} to="/receptionist/appointments" type="button" variant="outline-primary">
            <BsArrowLeft /> Về danh sách
          </Button>
        </div>
      </div>

      {notice && <Alert variant="success">{notice}</Alert>}
      {error && <Alert variant="danger">{error}</Alert>}

      <Card className="doctor-card">
        <Card.Body>
          <Form onSubmit={handleSubmit}>
            {!isEdit && (
              <section className="receptionist-form-section">
                <h2>Thông tin bệnh nhân</h2>
                <Row className="g-3">
                  <Col md={4}>
                    <Form.Group>
                      <Form.Label>patientId nếu đã có</Form.Label>
                      <Form.Control
                        value={form.patientId}
                        onChange={(e) => updateField("patientId", e.target.value)}
                        placeholder="ID bệnh nhân"
                      />
                    </Form.Group>
                  </Col>
                  <Col md={8} />
                  <Col md={6}>
                    <Form.Group>
                      <Form.Label>Họ và tên</Form.Label>
                      <Form.Control value={form.patient.fullName} onChange={(e) => updatePatientField("fullName", e.target.value)} />
                    </Form.Group>
                  </Col>
                  <Col md={6}>
                    <Form.Group>
                      <Form.Label>Số điện thoại</Form.Label>
                      <Form.Control value={form.patient.phone} onChange={(e) => updatePatientField("phone", e.target.value)} />
                    </Form.Group>
                  </Col>
                  <Col md={6}>
                    <Form.Group>
                      <Form.Label>Giới tính</Form.Label>
                      <Form.Select value={form.patient.gender} onChange={(e) => updatePatientField("gender", e.target.value)}>
                        <option value="">Chọn giới tính</option>
                        <option value="MALE">Nam</option>
                        <option value="FEMALE">Nữ</option>
                        <option value="OTHER">Khác</option>
                      </Form.Select>
                    </Form.Group>
                  </Col>
                  <Col md={6}>
                    <Form.Group>
                      <Form.Label>Ngày sinh</Form.Label>
                      <Form.Control type="date" value={form.patient.dateOfBirth} onChange={(e) => updatePatientField("dateOfBirth", e.target.value)} />
                    </Form.Group>
                  </Col>
                </Row>
              </section>
            )}

            <section className="receptionist-form-section">
              <h2>Thông tin lịch hẹn</h2>
              <Row className="g-3">
                <Col md={4}>
                  <Form.Group>
                    <Form.Label>Chuyên khoa</Form.Label>
                    <Form.Select value={form.departmentId} onChange={(e) => updateField("departmentId", e.target.value)}>
                      <option value="">Chọn chuyên khoa</option>
                      {departments.map((department) => (
                        <option key={department.id} value={department.id}>
                          {department.name}
                        </option>
                      ))}
                    </Form.Select>
                  </Form.Group>
                </Col>
                <Col md={4}>
                  <Form.Group>
                    <Form.Label>Bác sĩ</Form.Label>
                    <Form.Select value={form.doctorId} onChange={(e) => updateField("doctorId", e.target.value)}>
                      <option value="">Chọn bác sĩ</option>
                      {filteredDoctors.map((doctor) => (
                        <option key={doctor.id} value={doctor.id}>
                          {doctor.fullName} {doctor.departmentName ? `- ${doctor.departmentName}` : ""}
                        </option>
                      ))}
                    </Form.Select>
                  </Form.Group>
                </Col>
                <Col md={4}>
                  <Form.Group>
                    <Form.Label>Dịch vụ</Form.Label>
                    <Form.Select value={form.serviceId} onChange={(e) => updateField("serviceId", e.target.value)}>
                      <option value="">Chọn dịch vụ</option>
                      {filteredServices.map((service) => (
                        <option key={service.id} value={service.id}>
                          {service.name}
                        </option>
                      ))}
                    </Form.Select>
                  </Form.Group>
                </Col>
                <Col md={4}>
                  <Form.Group>
                    <Form.Label>Ngày khám</Form.Label>
                    <Form.Control type="date" value={form.appointmentDate} onChange={(e) => updateField("appointmentDate", e.target.value)} />
                  </Form.Group>
                </Col>
                <Col md={4}>
                  <Form.Group>
                    <Form.Label>Giờ bắt đầu</Form.Label>
                    <Form.Control type="time" value={form.startTime} onChange={(e) => updateField("startTime", e.target.value)} />
                  </Form.Group>
                </Col>
                <Col md={4}>
                  <Form.Group>
                    <Form.Label>Giờ kết thúc</Form.Label>
                    <Form.Control type="time" value={form.endTime} onChange={(e) => updateField("endTime", e.target.value)} />
                  </Form.Group>
                </Col>
                <Col md={6}>
                  <Form.Group>
                    <Form.Label>Lý do khám</Form.Label>
                    <Form.Control as="textarea" rows={3} value={form.reason} onChange={(e) => updateField("reason", e.target.value)} />
                  </Form.Group>
                </Col>
                <Col md={6}>
                  <Form.Group>
                    <Form.Label>Ghi chú triệu chứng</Form.Label>
                    <Form.Control as="textarea" rows={3} value={form.symptomNote} onChange={(e) => updateField("symptomNote", e.target.value)} />
                  </Form.Group>
                </Col>
                <Col md={12}>
                  <Form.Check
                    type="switch"
                    id="checkInNow"
                    label="Check-in ngay sau khi tạo"
                    checked={form.checkInNow}
                    onChange={(e) => updateField("checkInNow", e.target.checked)}
                  />
                </Col>
              </Row>
            </section>

            <div className="receptionist-form-actions">
              <Button type="submit" disabled={submitting}>
                <BsCalendarCheck />
                {submitting ? "Đang lưu..." : isEdit ? "Cập nhật lịch hẹn" : "Tạo lịch hẹn"}
              </Button>
              <Button as={Link} to="/receptionist/appointments" type="button" variant="outline-primary" disabled={submitting}>
                Hủy
              </Button>
            </div>
          </Form>
        </Card.Body>
      </Card>
    </>
  );
}

export default ReceptionistAppointmentFormPage;
