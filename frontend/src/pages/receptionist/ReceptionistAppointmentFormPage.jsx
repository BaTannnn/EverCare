import { useCallback, useEffect, useMemo, useState } from "react";
import { Alert, Button, Card, Col, Form, Row } from "react-bootstrap";
import { BsArrowLeft, BsCalendarCheck, BsSearch } from "react-icons/bs";
import { Link, useNavigate } from "react-router-dom";
import ErrorState from "../../components/common/ErrorState";
import LoadingState from "../../components/common/LoadingState";
import {
  createReceptionistAppointment,
  getReceptionistAppointmentDetail,
  updateReceptionistAppointment,
} from "../../services/receptionist/receptionistAppointmentApi";
import { searchReceptionistPatients } from "../../services/receptionist/receptionistPatientApi";
import {
  getReceptionistDepartments,
  getReceptionistDoctors,
  getReceptionistMedicalServices,
} from "../../services/receptionist/receptionistReferenceApi";
import { getReceptionistDoctorSchedules } from "../../services/receptionist/receptionistDoctorScheduleApi";
import SearchableSelect from "../../components/common/SearchableSelect";
import { formatTime, getErrorMessage, todayInputValue } from "./receptionistPageUtils";

const emptyPatient = {
  fullName: "",
  phone: "",
  gender: "",
  dateOfBirth: "",
  email: "",
  citizenId: "",
  healthInsuranceNo: "",
  address: "",
  emergencyContactName: "",
  emergencyContactPhone: "",
  bloodType: "",
  allergyNote: "",
  medicalHistoryNote: "",
};

const emptyForm = {
  patientId: "",
  patient: emptyPatient,
  departmentId: "",
  doctorId: "",
  serviceId: "",
  appointmentDate: todayInputValue(),
  scheduleId: "",
  startTime: "",
  endTime: "",
  reason: "",
  symptomNote: "",
  checkInNow: false,
};

const getEntityId = (value) => {
  if (!value) return "";
  if (typeof value === "object") {
    return value.id || value.departmentId || value.serviceId || value.doctorId || "";
  }
  return value;
};

const getServiceDepartmentId = (service) => getEntityId(service?.departmentId);

const getServiceDepartmentName = (service, departments) => {
  if (!service) return "";

  if (typeof service.departmentName === "string" && service.departmentName.trim()) {
    return service.departmentName;
  }

  const departmentId = getServiceDepartmentId(service);
  if (!departmentId) return "";

  return departments.find((department) => String(department.id) === String(departmentId))?.name || "";
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
  const [doctorSchedules, setDoctorSchedules] = useState([]);
  const [scheduleLoading, setScheduleLoading] = useState(false);
  const [patientKeyword, setPatientKeyword] = useState("");
  const [patientSearchResults, setPatientSearchResults] = useState([]);
  const [patientSearchLoading, setPatientSearchLoading] = useState(false);
  const [patientSearchNotice, setPatientSearchNotice] = useState("");
  const [form, setForm] = useState(emptyForm);

  const loadReferences = useCallback(async () => {
    const [departmentRes, doctorRes, serviceRes] = await Promise.all([
      getReceptionistDepartments(),
      getReceptionistDoctors(),
      getReceptionistMedicalServices({ serviceTypes: "EXAMINATION" }),
    ]);

    setDepartments(departmentRes.data || []);
    setDoctors(doctorRes.data || []);
    setServices(serviceRes.data || []);
  }, []);

  const loadAppointment = useCallback(async () => {
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
        departmentId: detail.service?.departmentId || detail.departmentId || detail.doctor?.departmentId || "",
        doctorId: detail.doctorId || detail.doctor?.id || "",
        serviceId: detail.serviceId || detail.service?.id || "",
        appointmentDate: detail.appointmentDate || todayInputValue(),
        scheduleId: "",
        startTime: formatTime(detail.startTime) !== "--" ? formatTime(detail.startTime) : "",
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
  }, [appointmentId, isEdit, navigate]);

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
  }, [loadAppointment, loadReferences, navigate]);

  const selectedService = useMemo(
    () => services.find((service) => String(service.id) === String(form.serviceId)) || null,
    [form.serviceId, services],
  );

  const selectedDepartmentId = getServiceDepartmentId(selectedService) || form.departmentId || "";
  const selectedDepartmentName = getServiceDepartmentName(selectedService, departments);

  const selectedService = useMemo(
    () => services.find((service) => String(service.id) === String(form.serviceId)) || null,
    [form.serviceId, services],
  );

  const selectedDepartmentId = getServiceDepartmentId(selectedService) || form.departmentId || "";
  const selectedDepartmentName = getServiceDepartmentName(selectedService, departments);

  const filteredDoctors = useMemo(() => {
    if (!selectedDepartmentId) return doctors;
    return doctors.filter((doctor) => String(doctor.departmentId) === String(selectedDepartmentId));
  }, [doctors, selectedDepartmentId]);

  const filteredServices = useMemo(
    () => services.filter((service) => String(service.serviceType).toUpperCase() === "EXAMINATION"),
    [services],
  );

  const selectedSchedule = useMemo(
    () => doctorSchedules.find((schedule) => String(schedule.id) === String(form.scheduleId)) || null,
    [doctorSchedules, form.scheduleId],
  );

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

  useEffect(() => {
    if (!selectedService) return;

    setForm((current) => ({
      ...current,
      departmentId: getServiceDepartmentId(selectedService) ? String(getServiceDepartmentId(selectedService)) : "",
    }));
  }, [selectedService]);

  useEffect(() => {
    if (!form.doctorId || !form.appointmentDate) {
      setDoctorSchedules([]);
      return;
    }

    setForm((current) => ({
      ...current,
      departmentId: getServiceDepartmentId(selectedService) ? String(getServiceDepartmentId(selectedService)) : "",
    }));
  }, [selectedService]);

  const updateField = (field, value) => {
    setForm((current) => {
      const next = { ...current, [field]: value };

      if (field === "serviceId") {
        const service = services.find((item) => String(item.id) === String(value)) || null;
        next.departmentId = getServiceDepartmentId(service) ? String(getServiceDepartmentId(service)) : "";
        next.doctorId = "";
        next.scheduleId = "";
        next.startTime = "";
        next.endTime = "";
      }

      if (field === "doctorId") {
        next.scheduleId = "";
        next.startTime = "";
        next.endTime = "";
      }

      if (field === "appointmentDate") {
        next.scheduleId = "";
        next.startTime = "";
        next.endTime = "";
      }

      return next;
    });
  };

  const updatePatientField = (field, value) => {
    setForm((current) => ({
      ...current,
      patient: { ...current.patient, [field]: value },
    }));
  };

  const applyPatientCandidate = (patient) => {
    if (!patient) {
      return;
    }

    setForm((current) => ({
      ...current,
      patientId: String(patient.id || ""),
      patient: {
        fullName: patient.fullName || "",
        phone: patient.phone || "",
        gender: patient.gender || "",
        dateOfBirth: patient.dateOfBirthRaw || patient.dateOfBirth || "",
        email: patient.email || "",
        citizenId: patient.citizenId || "",
        healthInsuranceNo: patient.healthInsuranceNo || "",
        address: patient.address || "",
        emergencyContactName: patient.emergencyContactName || "",
        emergencyContactPhone: patient.emergencyContactPhone || "",
        bloodType: patient.bloodType || "",
        allergyNote: patient.allergyNote || "",
        medicalHistoryNote: patient.medicalHistoryNote || "",
      },
    }));
  };

  const loadPatientCandidates = async () => {
    const keyword = patientKeyword.trim();
    if (!keyword) {
      setPatientSearchNotice("Vui lòng nhập tên, số điện thoại hoặc CCCD của bệnh nhân.");
      setPatientSearchResults([]);
      return;
    }

    setPatientSearchLoading(true);
    setPatientSearchNotice("");

    try {
      const response = await searchReceptionistPatients(keyword, 10);
      const candidates = response.data || [];
      setPatientSearchResults(candidates);

      if (!candidates.length) {
        setForm((current) => ({ ...current, patientId: "", patient: emptyPatient }));
        setPatientSearchNotice("Không tìm thấy bệnh nhân phù hợp.");
        return;
      }

      if (candidates.length === 1) {
        applyPatientCandidate(candidates[0]);
        setPatientSearchNotice(`Đã nạp bệnh nhân ${candidates[0].fullName || candidates[0].patientCode || ""}.`);
      } else {
        setPatientSearchNotice("Có nhiều kết quả, hãy chọn một bệnh nhân để nạp thông tin.");
      }
    } catch (err) {
      setPatientSearchNotice(getErrorMessage(err));
    } finally {
      setPatientSearchLoading(false);
    }
  };

  const loadDoctorSchedules = useCallback(async (doctorId, appointmentDate, preferredScheduleId = "") => {
    if (!doctorId || !appointmentDate) {
      setDoctorSchedules([]);
      return;
    }

    setScheduleLoading(true);
    try {
      const response = await getReceptionistDoctorSchedules(doctorId, {
        from: appointmentDate,
        to: appointmentDate,
      });
      const schedules = (response.data || []).filter((schedule) => schedule.status === "AVAILABLE");
      setDoctorSchedules(schedules);

      setForm((current) => {
        if (preferredScheduleId) {
          const preferred = schedules.find((schedule) => String(schedule.id) === String(preferredScheduleId));
          if (preferred) {
            return {
              ...current,
              scheduleId: String(preferred.id),
              appointmentDate: preferred.workDate || current.appointmentDate,
              startTime: preferred.startTime || current.startTime,
              endTime: preferred.endTime || current.endTime,
            };
          }
        }

        if (current.scheduleId) {
          const matched = schedules.find((schedule) => String(schedule.id) === String(current.scheduleId));
          if (matched) {
            return {
              ...current,
              startTime: matched.startTime || current.startTime,
              endTime: matched.endTime || current.endTime,
            };
          }
        }

        const matchedByTime = schedules.find((schedule) =>
          String(schedule.workDate || "") === String(current.appointmentDate || appointmentDate)
          && String(schedule.startTime || "") === String(current.startTime || "")
          && String(schedule.endTime || "") === String(current.endTime || ""),
        );
        if (matchedByTime) {
          return {
            ...current,
            scheduleId: String(matchedByTime.id),
          };
        }

        return current;
      });
    } catch (err) {
      setDoctorSchedules([]);
      setError(getErrorMessage(err));
    } finally {
      setScheduleLoading(false);
    }
  }, []);

  useEffect(() => {
    if (!form.doctorId || !form.appointmentDate) {
      setDoctorSchedules([]);
      return;
    }

    loadDoctorSchedules(form.doctorId, form.appointmentDate, form.scheduleId);
  }, [form.appointmentDate, form.doctorId, form.scheduleId, loadDoctorSchedules]);

  const validate = () => {
    if (!form.serviceId) return "Vui lòng chọn dịch vụ.";
    if (!selectedDepartmentId) return "Dịch vụ đã chọn chưa xác định được chuyên khoa.";
    if (!form.doctorId) return "Vui lòng chọn bác sĩ.";
    if (!form.appointmentDate) return "Vui lòng chọn ngày khám.";
    if (!form.scheduleId) return "Vui lòng chọn ca khám của bác sĩ.";

    if (!isEdit && !String(form.patientId || "").trim()) {
      if (!form.patient.fullName.trim()) return "Vui lòng nhập tên bệnh nhân.";
      if (!form.patient.phone.trim()) return "Vui lòng nhập số điện thoại.";
      if (!form.patient.gender.trim()) return "Vui lòng nhập giới tính.";
      if (!form.patient.dateOfBirth.trim()) return "Vui lòng nhập ngày sinh.";
    }

    return "";
  };

  const buildPayload = () => {
    const schedule = selectedSchedule;
    const departmentId = Number(selectedDepartmentId || 0);
    const payload = {
      departmentId,
      doctorId: Number(form.doctorId),
      serviceId: Number(form.serviceId),
      appointmentDate: schedule?.workDate || form.appointmentDate,
      startTime: schedule?.startTime || form.startTime,
      endTime: schedule?.endTime || form.endTime || undefined,
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
                  <Col md={12}>
                    <Form.Group>
                      <Form.Label>Tìm bệnh nhân theo CCCD, số điện thoại hoặc tên</Form.Label>
                      <div className="d-flex gap-2 align-items-stretch">
                        <Form.Control
                          className="flex-grow-1"
                          value={patientKeyword}
                          onChange={(e) => setPatientKeyword(e.target.value)}
                          placeholder="Nhập CCCD, số điện thoại hoặc tên bệnh nhân"
                        />
                        <Button type="button" variant="outline-primary" className="flex-shrink-0" onClick={loadPatientCandidates} disabled={patientSearchLoading}>
                          <BsSearch />
                          {patientSearchLoading ? "Đang tìm..." : "Tìm và nạp"}
                        </Button>
                      </div>
                    </Form.Group>
                  </Col>
                  {patientSearchNotice && (
                    <Col md={12}>
                      <Alert variant={patientSearchResults.length ? "info" : "warning"} className="mb-0">
                        {patientSearchNotice}
                      </Alert>
                    </Col>
                  )}
                  {patientSearchResults.length > 1 && (
                    <Col md={12}>
                      <Form.Group>
                        <Form.Label>Chọn bệnh nhân</Form.Label>
                        <Form.Select
                          value={form.patientId}
                          onChange={(e) => {
                            const picked = patientSearchResults.find((patient) => String(patient.id) === String(e.target.value));
                            applyPatientCandidate(picked);
                            if (picked) {
                              setPatientSearchNotice(`Đã nạp bệnh nhân ${picked.fullName || picked.patientCode || ""}.`);
                            }
                          }}
                        >
                          <option value="">Chọn một bệnh nhân</option>
                          {patientSearchResults.map((patient) => (
                            <option key={patient.id} value={patient.id}>
                              {patient.fullName} {patient.phone ? `- ${patient.phone}` : ""} {patient.citizenId ? `- ${patient.citizenId}` : ""}
                            </option>
                          ))}
                        </Form.Select>
                      </Form.Group>
                    </Col>
                  )}
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
                  <Col md={6}>
                    <Form.Group>
                      <Form.Label>CCCD / Citizen ID</Form.Label>
                      <Form.Control value={form.patient.citizenId} onChange={(e) => updatePatientField("citizenId", e.target.value)} />
                    </Form.Group>
                  </Col>
                  <Col md={6}>
                    <Form.Group>
                      <Form.Label>Số BHYT</Form.Label>
                      <Form.Control value={form.patient.healthInsuranceNo} onChange={(e) => updatePatientField("healthInsuranceNo", e.target.value)} />
                    </Form.Group>
                  </Col>
                  <Col md={12}>
                    <Form.Group>
                      <Form.Label>Địa chỉ</Form.Label>
                      <Form.Control value={form.patient.address} onChange={(e) => updatePatientField("address", e.target.value)} />
                    </Form.Group>
                  </Col>
                  <Col md={6}>
                    <Form.Group>
                      <Form.Label>Liên hệ khẩn cấp</Form.Label>
                      <Form.Control value={form.patient.emergencyContactName} onChange={(e) => updatePatientField("emergencyContactName", e.target.value)} />
                    </Form.Group>
                  </Col>
                  <Col md={6}>
                    <Form.Group>
                      <Form.Label>SĐT khẩn cấp</Form.Label>
                      <Form.Control value={form.patient.emergencyContactPhone} onChange={(e) => updatePatientField("emergencyContactPhone", e.target.value)} />
                    </Form.Group>
                  </Col>
                  <Col md={6}>
                    <Form.Group>
                      <Form.Label>Nhóm máu</Form.Label>
                      <Form.Select value={form.patient.bloodType} onChange={(e) => updatePatientField("bloodType", e.target.value)}>
                        <option value="">Chọn nhóm máu</option>
                        <option value="A">A</option>
                        <option value="A+">A+</option>
                        <option value="A-">A-</option>
                        <option value="B">B</option>
                        <option value="B+">B+</option>
                        <option value="B-">B-</option>
                        <option value="AB">AB</option>
                        <option value="AB+">AB+</option>
                        <option value="AB-">AB-</option>
                        <option value="O">O</option>
                        <option value="O+">O+</option>
                        <option value="O-">O-</option>
                      </Form.Select>
                    </Form.Group>
                  </Col>
                  <Col md={12}>
                    <Form.Group>
                      <Form.Label>Dị ứng</Form.Label>
                      <Form.Control as="textarea" rows={2} value={form.patient.allergyNote} onChange={(e) => updatePatientField("allergyNote", e.target.value)} />
                    </Form.Group>
                  </Col>
                  <Col md={12}>
                    <Form.Group>
                      <Form.Label>Tiền sử bệnh</Form.Label>
                      <Form.Control as="textarea" rows={2} value={form.patient.medicalHistoryNote} onChange={(e) => updatePatientField("medicalHistoryNote", e.target.value)} />
                    </Form.Group>
                  </Col>
                </Row>
              </section>
            )}

            <section className="receptionist-form-section">
              <h2>Thông tin lịch hẹn</h2>
              <Row className="g-3">
                <Col md={4}>
                  <SearchableSelect
                    label="Dịch vụ"
                    value={form.serviceId}
                    options={filteredServices}
                    onChange={(nextValue) => updateField("serviceId", nextValue)}
                    placeholder="Chọn dịch vụ"
                    searchPlaceholder="Tìm dịch vụ"
                    emptyMessage="Không tìm thấy dịch vụ"
                    getOptionValue={(service) => service.id}
                    getOptionLabel={(service) => service.name || ""}
                    getOptionDescription={(service) => service.departmentName || service.code || ""}
                  />
                </Col>
                <Col md={4}>
                  <SearchableSelect
                    label="Bác sĩ"
                    value={form.doctorId}
                    options={filteredDoctors}
                    onChange={(nextValue) => updateField("doctorId", nextValue)}
                    placeholder="Chọn bác sĩ"
                    searchPlaceholder="Tìm bác sĩ"
                    emptyMessage="Không tìm thấy bác sĩ"
                    disabled={!form.serviceId}
                    getOptionValue={(doctor) => doctor.id}
                    getOptionLabel={(doctor) => doctor.fullName || ""}
                    getOptionDescription={(doctor) => doctor.departmentName || doctor.doctorCode || ""}
                  />
                </Col>
                <Col md={4}>
                  <Form.Group>
                    <Form.Label>Ngày khám</Form.Label>
                    <Form.Control type="date" value={form.appointmentDate} onChange={(e) => updateField("appointmentDate", e.target.value)} />
                  </Form.Group>
                </Col>
                <Col md={4}>
                  <Form.Group>
                    <Form.Label>Chuyên khoa</Form.Label>
                    <Form.Control
                      type="text"
                      value={selectedDepartmentName || "Sẽ tự động xác định từ dịch vụ EXAMINATION"}
                      readOnly
                    />
                  </Form.Group>
                </Col>
                <Col md={4}>
                  <Form.Group>
                    <Form.Label>Ca khám của bác sĩ</Form.Label>
                    <Form.Select
                      value={form.scheduleId}
                      onChange={(e) => {
                        const schedule = doctorSchedules.find((item) => String(item.id) === String(e.target.value));
                        if (!schedule) {
                          updateField("scheduleId", "");
                          return;
                        }

                        setForm((current) => ({
                          ...current,
                          scheduleId: String(schedule.id),
                          appointmentDate: schedule.workDate || current.appointmentDate,
                          startTime: schedule.startTime || current.startTime,
                          endTime: schedule.endTime || current.endTime,
                        }));
                      }}
                      disabled={!form.doctorId || !form.appointmentDate || scheduleLoading}
                    >
                      <option value="">
                        {scheduleLoading ? "Đang tải ca khám..." : !form.doctorId || !form.appointmentDate ? "Chọn bác sĩ và ngày khám trước" : "Chọn ca khám"}
                      </option>
                      {doctorSchedules.map((schedule) => (
                        <option key={schedule.id} value={schedule.id}>
                          {schedule.workDate} {schedule.startTime} - {schedule.endTime}
                          {typeof schedule.remainingSlots === "number" ? ` | còn ${schedule.remainingSlots} chỗ` : ""}
                        </option>
                      ))}
                    </Form.Select>
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
