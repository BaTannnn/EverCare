import { useEffect, useMemo, useState } from "react";
import { Alert, Button, Card, Col, Form, Row } from "react-bootstrap";
import { BsClock, BsExclamationTriangle, BsInfoCircle } from "react-icons/bs";
import { useNavigate } from "react-router-dom";
import { usePatientShell } from "../../contexts/usePatientShell";
import SearchableSelect from "../../components/common/SearchableSelect";
import { bookPatientAppointment } from "../../services/patient/patientAppointmentApi";
import { getPatientDoctors, getPatientMedicalServices } from "../../services/patient/patientCatalogApi";
import { getPatientDoctorSchedules } from "../../services/patient/patientDoctorScheduleApi";
import { formatCurrency } from "./patientPageUtils";

const toLocalDateInput = (date = new Date()) => {
  const localDate = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
  return localDate.toISOString().slice(0, 10);
};

const normalizeId = (value) => String(value ?? "");

const getApiErrorMessage = (error, fallback) => {
  const responseData = error?.response?.data;

  if (typeof responseData === "string" && responseData.trim()) {
    return responseData;
  }

  if (responseData && typeof responseData === "object") {
    return responseData.message || responseData.error || fallback;
  }

  return error?.message || fallback;
};

const buildBookingConflictMessage = (message) => {
  const normalized = String(message || "").toLowerCase();

  if (normalized.includes("hồ sơ bệnh nhân") || normalized.includes("tạo hồ sơ")) {
    return "Bạn cần tạo hồ sơ bệnh nhân trước khi đặt lịch khám.";
  }

  if (normalized.includes("đã có người đặt") || normalized.includes("đủ số lượng")) {
    return "Khung giờ này đã được đặt hoặc đã đủ số lượng bệnh nhân. Vui lòng chọn khung giờ khác.";
  }

  if (normalized.includes("không có lịch trống")) {
    return "Bác sĩ không có lịch trống trong khung giờ này. Vui lòng chọn thời gian khác.";
  }

  if (normalized.includes("dịch vụ") && normalized.includes("bác sĩ") && normalized.includes("không")) {
    return "Dịch vụ bạn chọn không phù hợp với bác sĩ đã chọn. Vui lòng chọn lại dịch vụ hoặc bác sĩ.";
  }

  return message || "Không thể đặt lịch ở thời điểm này. Vui lòng thử lại.";
};

const isScheduleAvailable = (schedule) => schedule.remainingSlots > 0 && ["AVAILABLE", "OPEN", "ACTIVE"].includes(schedule.status);

const getDepartmentKey = (item) => normalizeId(item?.departmentId);

const matchesServiceDoctorDepartment = (service, doctor) => {
  if (!service || !doctor) return true;

  const serviceDepartmentId = getDepartmentKey(service);
  const doctorDepartmentId = getDepartmentKey(doctor);
  const serviceDepartmentName = service.departmentName || service.specialization || "";
  const doctorDepartmentName = doctor.departmentName || doctor.specialization || "";

  if (serviceDepartmentId) {
    if (doctorDepartmentId) {
      return doctorDepartmentId === serviceDepartmentId;
    }

    if (serviceDepartmentName) {
      return [doctorDepartmentName, doctor.specialization].some((value) => value === serviceDepartmentName);
    }
  }

  if (serviceDepartmentName) {
    return [doctorDepartmentName, doctor.specialization].some((value) => value === serviceDepartmentName);
  }

  return true;
};

function PatientBookAppointment() {
  const navigate = useNavigate();
  const { profile } = usePatientShell();
  const [doctors, setDoctors] = useState([]);
  const [services, setServices] = useState([]);
  const [doctorSchedules, setDoctorSchedules] = useState([]);
  const [selectedServiceId, setSelectedServiceId] = useState("");
  const [selectedDoctorId, setSelectedDoctorId] = useState("");
  const [selectedScheduleId, setSelectedScheduleId] = useState("");
  const [scheduleDate, setScheduleDate] = useState(toLocalDateInput());
  const [appointmentDate, setAppointmentDate] = useState("");
  const [startTime, setStartTime] = useState("");
  const [endTime, setEndTime] = useState("");
  const [reason, setReason] = useState("");
  const [symptomNote, setSymptomNote] = useState("");
  const [loading, setLoading] = useState(true);
  const [loadingSchedules, setLoadingSchedules] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const [scheduleError, setScheduleError] = useState("");
  const [needsProfileFix, setNeedsProfileFix] = useState(false);

  useEffect(() => {
    let mounted = true;

    const loadCatalog = async () => {
      try {
        const [doctorResponse, serviceResponse] = await Promise.all([getPatientDoctors(), getPatientMedicalServices()]);

        if (!mounted) return;

        setDoctors(doctorResponse.data || []);
        setServices(serviceResponse.data || []);
      } catch (loadError) {
        if (!mounted) return;
        setError("Không tải được danh sách bác sĩ hoặc dịch vụ.");
        console.error(loadError);
      } finally {
        if (mounted) {
          setLoading(false);
        }
      }
    };

    loadCatalog();

    return () => {
      mounted = false;
    };
  }, []);

  const availableServices = useMemo(() => services.filter((service) => service.active !== false), [services]);

  const selectedService = useMemo(
    () => availableServices.find((service) => normalizeId(service.id) === normalizeId(selectedServiceId)) || null,
    [availableServices, selectedServiceId],
  );

  const availableDoctors = useMemo(
    () =>
      doctors.filter((doctor) => {
        if (doctor.active === false) return false;
        if (!selectedService) return true;
        return matchesServiceDoctorDepartment(selectedService, doctor);
      }),
    [doctors, selectedService],
  );

  const selectedDoctor = useMemo(
    () => availableDoctors.find((doctor) => normalizeId(doctor.id) === normalizeId(selectedDoctorId)) || null,
    [availableDoctors, selectedDoctorId],
  );

  const availableSchedules = useMemo(
    () => doctorSchedules.filter((schedule) => schedule.workDate === scheduleDate || !schedule.workDate),
    [doctorSchedules, scheduleDate],
  );

  const selectedSchedule = useMemo(
    () => availableSchedules.find((schedule) => normalizeId(schedule.id) === normalizeId(selectedScheduleId)) || null,
    [availableSchedules, selectedScheduleId],
  );

  useEffect(() => {
    if (!selectedServiceId) {
      setSelectedDoctorId("");
      setSelectedScheduleId("");
      setDoctorSchedules([]);
      setAppointmentDate("");
      setStartTime("");
      setEndTime("");
      return;
    }

    if (selectedDoctorId && !availableDoctors.some((doctor) => normalizeId(doctor.id) === normalizeId(selectedDoctorId))) {
      setSelectedDoctorId("");
      setSelectedScheduleId("");
      setDoctorSchedules([]);
      setAppointmentDate("");
      setStartTime("");
      setEndTime("");
    }
  }, [availableDoctors, selectedDoctorId, selectedServiceId]);

  useEffect(() => {
    let mounted = true;

    const loadSchedules = async () => {
      if (!selectedDoctor?.id) {
        setDoctorSchedules([]);
        setSelectedScheduleId("");
        setScheduleError("");
        setAppointmentDate("");
        setStartTime("");
        setEndTime("");
        return;
      }

      setLoadingSchedules(true);
      setScheduleError("");
      setSelectedScheduleId("");
      setAppointmentDate("");
      setStartTime("");
      setEndTime("");

      try {
        const response = await getPatientDoctorSchedules(selectedDoctor.id, {
          from: scheduleDate,
          to: scheduleDate,
        });

        if (!mounted) return;

        setDoctorSchedules(response.data || []);
      } catch (loadError) {
        if (!mounted) return;
        setDoctorSchedules([]);
        setScheduleError("Không tải được lịch làm việc của bác sĩ.");
        console.error(loadError);
      } finally {
        if (mounted) {
          setLoadingSchedules(false);
        }
      }
    };

    loadSchedules();

    return () => {
      mounted = false;
    };
  }, [scheduleDate, selectedDoctor?.id]);

  useEffect(() => {
    if (!selectedSchedule) return;

    setAppointmentDate(selectedSchedule.workDate || scheduleDate);
    setStartTime(selectedSchedule.startTime || "");
    setEndTime(selectedSchedule.endTime || "");
  }, [scheduleDate, selectedSchedule]);

  const handleServiceChange = (event) => {
    setSelectedServiceId(event.target.value);
    setSelectedDoctorId("");
    setSelectedScheduleId("");
    setDoctorSchedules([]);
    setAppointmentDate("");
    setStartTime("");
    setEndTime("");
    setError("");
    setScheduleError("");
  };

  const handleDoctorChange = (event) => {
    setSelectedDoctorId(event.target.value);
    setSelectedScheduleId("");
    setAppointmentDate("");
    setStartTime("");
    setEndTime("");
    setError("");
    setScheduleError("");
  };

  const handleScheduleChange = (event) => {
    const scheduleId = event.target.value;
    setSelectedScheduleId(scheduleId);
    setError("");

    const nextSchedule = availableSchedules.find((schedule) => normalizeId(schedule.id) === normalizeId(scheduleId));
    if (nextSchedule) {
      setAppointmentDate(nextSchedule.workDate || scheduleDate);
      setStartTime(nextSchedule.startTime || "");
      setEndTime(nextSchedule.endTime || "");
    }
  };

  const handleConfirm = async (event) => {
    event.preventDefault();

    if (!profile?.id) {
      setError("Bạn cần tạo hồ sơ bệnh nhân trước khi đặt lịch.");
      return;
    }

    if (!selectedSchedule || !selectedDoctor || !selectedService) {
      setError("Vui lòng chọn đầy đủ dịch vụ, bác sĩ và khung giờ.");
      return;
    }

    setSubmitting(true);
    setError("");
    setNeedsProfileFix(false);

    try {
      await bookPatientAppointment({
        doctorId: selectedDoctor.id,
        serviceId: selectedService.id,
        appointmentDate,
        startTime,
        endTime: endTime || startTime,
        reason,
        symptomNote,
      });

      navigate("/patient/appointments");
    } catch (bookError) {
      const backendMessage = getApiErrorMessage(bookError, "Đặt lịch chưa thành công. Vui lòng thử lại.");
      const status = bookError?.response?.status;

      if (status === 409) {
        const conflictMessage = buildBookingConflictMessage(backendMessage);
        setError(conflictMessage);
        setNeedsProfileFix(conflictMessage.includes("hồ sơ bệnh nhân"));
      } else if (status === 400) {
        setError(backendMessage || "Dữ liệu đặt lịch chưa hợp lệ.");
      } else if (status === 401) {
        setError("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
      } else {
        setError(backendMessage || "Đặt lịch chưa thành công. Vui lòng thử lại.");
      }

      console.warn("Failed to book appointment", bookError?.response?.data || bookError?.message || bookError);
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return <div className="patient-loading-panel">Đang tải danh sách bác sĩ và dịch vụ...</div>;
  }

  return (
    <div className="patient-page">
      <div className="patient-page-header-row align-start">
        <div>
          <p className="patient-eyebrow">Đặt lịch khám</p>
          <h2>Đặt lịch trong một màn hình</h2>
          <p className="patient-page-subtitle">Chọn dịch vụ, bác sĩ, khung giờ và xem lại toàn bộ thông tin ngay bên dưới trước khi xác nhận.</p>
        </div>
      </div>

      {error && (
        <Alert variant="warning" className="patient-info-banner">
          <BsExclamationTriangle className="me-2" />
          {error}
          {(needsProfileFix || !profile?.id) && (
            <div className="mt-3">
              <Button type="button" className="patient-primary-soft" onClick={() => navigate("/patient/profile")}>
                Tạo hồ sơ ngay
              </Button>
            </div>
          )}
        </Alert>
      )}

      <section className="patient-booking-layout single-column">
        <div className="patient-booking-main">
          <Card className="patient-booking-card">
            <Card.Body>
              <div className="patient-section-head compact">
                <div>
                  <h3>Thông tin đặt lịch</h3>
                  <p>Điền từng thông tin trong cùng một form, hệ thống sẽ tự động cập nhật phần xem trước phía dưới.</p>
                </div>
              </div>

              <Form onSubmit={handleConfirm}>
                <Row className="g-3">
                  <Col md={6}>
                    <SearchableSelect
                      label="Dịch vụ"
                      value={selectedServiceId}
                      options={availableServices}
                      onChange={(nextValue) => handleServiceChange({ target: { value: nextValue } })}
                      placeholder="Chọn dịch vụ"
                      searchPlaceholder="Tìm dịch vụ"
                      emptyMessage="Không tìm thấy dịch vụ"
                      getOptionValue={(service) => normalizeId(service.id)}
                      getOptionLabel={(service) => service.name || ""}
                      getOptionDescription={(service) => service.departmentName || service.code || ""}
                    />
                  </Col>

                  <Col md={6}>
                    <SearchableSelect
                      label="Bác sĩ"
                      value={selectedDoctorId}
                      options={availableDoctors}
                      onChange={(nextValue) => handleDoctorChange({ target: { value: nextValue } })}
                      placeholder={selectedServiceId ? "Chọn bác sĩ" : "Chọn dịch vụ trước"}
                      searchPlaceholder="Tìm bác sĩ"
                      emptyMessage="Không tìm thấy bác sĩ"
                      disabled={!selectedServiceId}
                      getOptionValue={(doctor) => normalizeId(doctor.id)}
                      getOptionLabel={(doctor) => doctor.fullName || ""}
                      getOptionDescription={(doctor) => doctor.departmentName || doctor.doctorCode || ""}
                    />
                  </Col>

                  <Col md={4}>
                    <Form.Group className="patient-form-group">
                      <Form.Label>Ngày khám</Form.Label>
                      <Form.Control type="date" value={scheduleDate} onChange={(event) => setScheduleDate(event.target.value)} disabled={!selectedDoctorId} />
                    </Form.Group>
                  </Col>

                  <Col md={8}>
                    <Form.Group className="patient-form-group">
                      <Form.Label>Khung giờ khám</Form.Label>
                      <Form.Select value={selectedScheduleId} onChange={handleScheduleChange} disabled={!selectedDoctorId || loadingSchedules}>
                        <option value="">{selectedDoctorId ? "Chọn khung giờ" : "Chọn bác sĩ trước"}</option>
                        {availableSchedules.map((schedule) => {
                          const isDisabled = !isScheduleAvailable(schedule);
                          return (
                            <option key={schedule.id} value={normalizeId(schedule.id)} disabled={isDisabled}>
                              {schedule.displayDate} | {schedule.displayRange} {isDisabled ? "(Hết chỗ)" : ""}
                            </option>
                          );
                        })}
                      </Form.Select>
                    </Form.Group>
                  </Col>

                  <Col md={6}>
                    <Form.Group className="patient-form-group">
                      <Form.Label>Lý do khám</Form.Label>
                      <Form.Control
                        as="textarea"
                        rows={3}
                        value={reason}
                        onChange={(event) => setReason(event.target.value)}
                        placeholder="Mô tả ngắn triệu chứng hoặc nhu cầu khám"
                      />
                    </Form.Group>
                  </Col>

                  <Col md={6}>
                    <Form.Group className="patient-form-group">
                      <Form.Label>Triệu chứng chính</Form.Label>
                      <Form.Control
                        as="textarea"
                        rows={3}
                        value={symptomNote}
                        onChange={(event) => setSymptomNote(event.target.value)}
                        placeholder="Ví dụ: sốt, đau đầu, ho khan..."
                      />
                    </Form.Group>
                  </Col>
                </Row>

                {scheduleError && <Alert variant="warning" className="mt-3 mb-0">{scheduleError}</Alert>}

                {loadingSchedules && selectedDoctorId && (
                  <div className="patient-loading-panel inline mt-3">Đang tải lịch làm việc của bác sĩ...</div>
                )}

                <Card className="mt-4">
                  <Card.Body>
                    <div className="patient-section-head compact mb-3">
                      <div>
                        <h3>Xem trước thông tin</h3>
                        <p>Thông tin bên dưới sẽ được gửi lên hệ thống khi bạn xác nhận đặt lịch.</p>
                      </div>
                    </div>

                    <div className="patient-booking-summary">
                      <div>
                        <span>Dịch vụ</span>
                        <strong>{selectedService?.name || "Chưa chọn"}</strong>
                      </div>
                      <div>
                        <span>Bác sĩ</span>
                        <strong>{selectedDoctor?.fullName || "Chưa chọn"}</strong>
                      </div>
                      <div>
                        <span>Chuyên khoa</span>
                        <strong>{selectedDoctor?.departmentName || selectedService?.departmentName || "Chưa xác định"}</strong>
                      </div>
                      <div>
                        <span>Ngày khám</span>
                        <strong>{appointmentDate || scheduleDate || "Chưa chọn"}</strong>
                      </div>
                      <div>
                        <span>Giờ khám</span>
                        <strong>{startTime ? `${startTime}${endTime ? ` - ${endTime}` : ""}` : "Chưa chọn"}</strong>
                      </div>
                      <div>
                        <span>Khung giờ</span>
                        <strong>{selectedSchedule?.displayRange || "Chưa chọn"}</strong>
                      </div>
                    </div>

                    <div className="patient-booking-preview-list mt-3">
                      <div className="patient-booking-preview-item">
                        <span>Mã bác sĩ</span>
                        <strong>{selectedDoctor?.doctorCode || "Chưa chọn"}</strong>
                      </div>
                      <div className="patient-booking-preview-item">
                        <span>Mã dịch vụ</span>
                        <strong>{selectedService?.code || "Chưa chọn"}</strong>
                      </div>
                      <div className="patient-booking-preview-item">
                        <span>Giá dịch vụ</span>
                        <strong>{selectedService ? formatCurrency(selectedService.price) : "Chưa chọn"}</strong>
                      </div>
                      <div className="patient-booking-preview-item">
                        <span>Trạng thái lịch</span>
                        <strong>{selectedSchedule?.statusLabel || "Chưa chọn"}</strong>
                      </div>
                    </div>
                  </Card.Body>
                </Card>

                <Alert variant="info" className="mt-3 mb-0">
                  <BsInfoCircle className="me-2" />
                  Sau khi xác nhận, hệ thống sẽ gửi yêu cầu đặt lịch ngay mà không tải lại trang.
                </Alert>

                <div className="patient-booking-actions mt-4">
                  <Button
                    type="button"
                    variant="light"
                    className="patient-outline-button"
                    onClick={() => navigate("/patient/appointments")}
                    disabled={submitting}
                  >
                    <BsClock /> Xem lịch hẹn
                  </Button>

                  <Button
                    type="submit"
                    className="patient-primary-soft"
                    disabled={submitting || !appointmentDate || !selectedDoctor || !selectedService || !selectedSchedule || !profile?.id}
                  >
                    {submitting ? "Đang đặt..." : "Xác nhận đặt lịch"}
                  </Button>
                </div>
              </Form>
            </Card.Body>
          </Card>
        </div>
      </section>
    </div>
  );
}

export default PatientBookAppointment;
