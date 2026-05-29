import { useEffect, useMemo, useState } from "react";
import { Alert, Button, Card, Form } from "react-bootstrap";
import {
  BsArrowLeft,
  BsArrowRight,
  BsCheckCircle,
  BsClock,
  BsExclamationTriangle,
  BsStarFill,
} from "react-icons/bs";
import { useNavigate, useOutletContext } from "react-router-dom";
import { bookPatientAppointment } from "../../services/patient/patientAppointmentApi";
import { getPatientDoctors, getPatientMedicalServices } from "../../services/patient/patientCatalogApi";
import { getPatientDoctorSchedules } from "../../services/patient/patientDoctorScheduleApi";
import { payPatientInvoice } from "../../services/patient/patientInvoiceApi";
import { getAvatarSource, formatCurrency } from "./patientPageUtils";

const steps = ["Chuyên khoa", "Dịch vụ", "Bác sĩ", "Thời gian", "Xác nhận"];

const toLocalDateInput = (date = new Date()) => {
  const localDate = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
  return localDate.toISOString().slice(0, 10);
};

const normalizeId = (value) => String(value ?? "");

const matchesSpecialty = (item, specialty) => {
  if (!item || !specialty || specialty === "ALL") return true;
  return [item.specialization, item.departmentName, item.specialty].some((value) => value === specialty);
};

const isScheduleAvailable = (schedule) => schedule.remainingSlots > 0 && ["AVAILABLE", "OPEN", "ACTIVE"].includes(schedule.status);

function PatientBookAppointment() {
  const navigate = useNavigate();
  const { profile } = useOutletContext() || {};
  const [step, setStep] = useState(0);
  const [doctors, setDoctors] = useState([]);
  const [services, setServices] = useState([]);
  const [doctorSchedules, setDoctorSchedules] = useState([]);
  const [selectedSpecialty, setSelectedSpecialty] = useState("ALL");
  const [selectedServiceId, setSelectedServiceId] = useState("");
  const [selectedDoctorId, setSelectedDoctorId] = useState("");
  const [selectedScheduleId, setSelectedScheduleId] = useState("");
  const [scheduleDate, setScheduleDate] = useState(toLocalDateInput());
  const [appointmentDate, setAppointmentDate] = useState("");
  const [startTime, setStartTime] = useState("");
  const [endTime, setEndTime] = useState("");
  const [reason, setReason] = useState("");
  const [symptomNote, setSymptomNote] = useState("");
  const [paymentMethod, setPaymentMethod] = useState("VNPAY");
  const [paymentChannel, setPaymentChannel] = useState("QR");
  const [loading, setLoading] = useState(true);
  const [loadingSchedules, setLoadingSchedules] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const [scheduleError, setScheduleError] = useState("");

  useEffect(() => {
    let mounted = true;

    const loadCatalog = async () => {
      try {
        const [doctorResponse, serviceResponse] = await Promise.all([getPatientDoctors(), getPatientMedicalServices()]);

        if (!mounted) return;

        const doctorList = doctorResponse.data || [];
        const serviceList = serviceResponse.data || [];

        setDoctors(doctorList);
        setServices(serviceList);

        const firstSpecialty = doctorList.find((doctor) => doctor.specialization)?.specialization || serviceList.find((service) => service.departmentName)?.departmentName || "ALL";
        setSelectedSpecialty(firstSpecialty);
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

  const specialties = useMemo(() => {
    const fromDoctors = doctors.map((doctor) => doctor.specialization).filter(Boolean);
    const fromServices = services.map((service) => service.departmentName || service.specialization).filter(Boolean);
    return ["ALL", ...new Set([...fromDoctors, ...fromServices])];
  }, [doctors, services]);

  const availableServices = useMemo(
    () => services.filter((service) => matchesSpecialty(service, selectedSpecialty)),
    [selectedSpecialty, services],
  );

  const availableDoctors = useMemo(
    () => doctors.filter((doctor) => matchesSpecialty(doctor, selectedSpecialty)),
    [doctors, selectedSpecialty],
  );

  const selectedService = useMemo(
    () => availableServices.find((service) => normalizeId(service.id) === normalizeId(selectedServiceId)) || availableServices[0] || null,
    [availableServices, selectedServiceId],
  );

  const selectedDoctor = useMemo(
    () => availableDoctors.find((doctor) => normalizeId(doctor.id) === normalizeId(selectedDoctorId)) || availableDoctors[0] || null,
    [availableDoctors, selectedDoctorId],
  );

  const selectedSchedule = useMemo(
    () => doctorSchedules.find((schedule) => normalizeId(schedule.id) === normalizeId(selectedScheduleId)) || null,
    [doctorSchedules, selectedScheduleId],
  );

  useEffect(() => {
    if (availableServices.length === 0) {
      setSelectedServiceId("");
      return;
    }

    const nextService = availableServices.find((service) => normalizeId(service.id) === normalizeId(selectedServiceId)) || availableServices[0];
    setSelectedServiceId(normalizeId(nextService.id));
  }, [availableServices, selectedServiceId]);

  useEffect(() => {
    if (availableDoctors.length === 0) {
      setSelectedDoctorId("");
      return;
    }

    const nextDoctor = availableDoctors.find((doctor) => normalizeId(doctor.id) === normalizeId(selectedDoctorId)) || availableDoctors[0];
    setSelectedDoctorId(normalizeId(nextDoctor.id));
  }, [availableDoctors, selectedDoctorId]);

  useEffect(() => {
    let mounted = true;

    const loadSchedules = async () => {
      if (!selectedDoctor?.id) {
        setDoctorSchedules([]);
        setSelectedScheduleId("");
        return;
      }

      setLoadingSchedules(true);
      setScheduleError("");

      try {
        const response = await getPatientDoctorSchedules(selectedDoctor.id, {
          from: scheduleDate,
          to: scheduleDate,
        });

        if (!mounted) return;

        const scheduleList = response.data || [];
        setDoctorSchedules(scheduleList);

        const firstAvailableSchedule = scheduleList.find(isScheduleAvailable) || scheduleList[0] || null;
        if (firstAvailableSchedule) {
          setSelectedScheduleId(normalizeId(firstAvailableSchedule.id));
          setAppointmentDate(firstAvailableSchedule.workDate || scheduleDate);
          setStartTime(firstAvailableSchedule.startTime || "");
          setEndTime(firstAvailableSchedule.endTime || "");
        } else {
          setSelectedScheduleId("");
          setAppointmentDate(scheduleDate);
          setStartTime("");
          setEndTime("");
        }
      } catch (loadError) {
        if (!mounted) return;
        setDoctorSchedules([]);
        setSelectedScheduleId("");
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

  const goNext = () => {
    if (step === 1 && !selectedService) {
      setError("Vui lòng chọn dịch vụ trước khi tiếp tục.");
      return;
    }

    if (step === 2 && !selectedDoctor) {
      setError("Vui lòng chọn bác sĩ trước khi tiếp tục.");
      return;
    }

    if (step === 3 && !selectedSchedule) {
      setError("Bác sĩ này hiện chưa có lịch trống trong ngày bạn chọn.");
      return;
    }

    setError("");
    setStep((current) => Math.min(current + 1, steps.length - 1));
  };

  const goBack = () => {
    setError("");
    setStep((current) => Math.max(current - 1, 0));
  };

  const handleConfirm = async (event) => {
    event.preventDefault();

    if (!profile?.id) {
      setError("Bạn cần tạo hồ sơ bệnh nhân trước khi đặt lịch.");
      return;
    }

    if (!selectedSchedule || !selectedDoctor || !selectedService) {
      setError("Vui lòng chọn đầy đủ chuyên khoa, bác sĩ, dịch vụ và khung giờ.");
      return;
    }

    setSubmitting(true);
    setError("");

    try {
      const response = await bookPatientAppointment({
        doctorId: selectedDoctor.id,
        serviceId: selectedService.id,
        appointmentDate,
        startTime,
        endTime: endTime || startTime,
        reason,
        symptomNote,
      });

      const bookedAppointment = response?.data || {};
      const invoiceId = bookedAppointment.invoiceId || bookedAppointment.invoice?.id || null;
      const invoicePaymentUrl = bookedAppointment.paymentUrl || bookedAppointment.invoice?.paymentUrl || "";

      if (invoiceId) {
        const paymentResponse = await payPatientInvoice(invoiceId, {
          paymentMethod,
          paymentChannel,
          returnUrl: `${window.location.origin}/patient/invoices`,
          cancelUrl: `${window.location.origin}/patient/book-appointment`,
        });

        const payment = paymentResponse?.data || {};
        const nextPaymentUrl = payment.paymentUrl || invoicePaymentUrl;
        if (nextPaymentUrl) {
          window.open(nextPaymentUrl, "_blank", "noopener,noreferrer");
        }
      }

      navigate("/patient/appointments");
    } catch (bookError) {
      setError("Đặt lịch chưa thành công. Vui lòng thử lại.");
      console.error(bookError);
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
          <h2>Đặt lịch trực tuyến theo từng bước</h2>
          <p className="patient-page-subtitle">Chọn chuyên khoa, dịch vụ, bác sĩ, lịch trống và xác nhận mà không cần tải lại trang.</p>
        </div>
      </div>

      {error && (
        <Alert variant="warning" className="patient-info-banner">
          <BsExclamationTriangle className="me-2" />
          {error}
          {!profile?.id && (
            <div className="mt-3">
              <Button type="button" className="patient-primary-soft" onClick={() => navigate("/patient/profile")}>
                Tạo hồ sơ ngay
              </Button>
            </div>
          )}
        </Alert>
      )}

      <section className="patient-stepper">
        {steps.map((label, index) => (
          <div key={label} className={`patient-step ${index <= step ? "active" : ""}`}>
            <div className="patient-step-circle">{index < step ? <BsCheckCircle /> : index + 1}</div>
            <span>{label}</span>
          </div>
        ))}
      </section>

      <section className="patient-booking-layout single-column">
        <div className="patient-booking-main">
          {step === 0 && (
            <Card className="patient-booking-card">
              <Card.Body>
                <div className="patient-section-head compact">
                  <div>
                    <h3>Chọn chuyên khoa</h3>
                    <p>Danh sách chuyên khoa được lấy từ danh mục bác sĩ và dịch vụ thật.</p>
                  </div>
                </div>
                <div className="patient-pill-grid">
                  {specialties.map((specialty) => (
                    <button
                      key={specialty}
                      type="button"
                      className={`patient-pill ${selectedSpecialty === specialty ? "selected" : ""}`}
                      onClick={() => {
                        setSelectedSpecialty(specialty);
                        setSelectedServiceId("");
                        setSelectedDoctorId("");
                        setSelectedScheduleId("");
                      }}
                    >
                      {specialty === "ALL" ? "Tất cả" : specialty}
                    </button>
                  ))}
                </div>
              </Card.Body>
            </Card>
          )}

          {step === 1 && (
            <Card className="patient-booking-card">
              <Card.Body>
                <div className="patient-section-head compact">
                  <div>
                    <h3>Chọn dịch vụ</h3>
                    <p>{selectedSpecialty === "ALL" ? "Tất cả dịch vụ đang khả dụng." : `Dịch vụ thuộc chuyên khoa ${selectedSpecialty}.`}</p>
                  </div>
                </div>

                {availableServices.length > 0 ? (
                  <div className="patient-service-grid">
                    {availableServices.map((service) => (
                      <button
                        key={service.id}
                        type="button"
                        className={`patient-service-card ${normalizeId(selectedServiceId) === normalizeId(service.id) ? "selected" : ""}`}
                        onClick={() => setSelectedServiceId(normalizeId(service.id))}
                      >
                        <strong>{service.name}</strong>
                        <span>{service.description || service.serviceType || "Dịch vụ khám"}</span>
                        <div>
                          <em>{service.departmentName || service.specialization || "EverCare"}</em>
                          <b>{formatCurrency(service.price)}</b>
                        </div>
                      </button>
                    ))}
                  </div>
                ) : (
                  <div className="patient-empty-state">
                    <h4>Chưa có dịch vụ phù hợp</h4>
                    <p>Backend hiện chưa trả về dịch vụ cho chuyên khoa này.</p>
                  </div>
                )}
              </Card.Body>
            </Card>
          )}

          {step === 2 && (
            <Card className="patient-booking-card">
              <Card.Body>
                <div className="patient-section-head compact">
                  <div>
                    <h3>Chọn bác sĩ</h3>
                    <p>{availableDoctors.length} bác sĩ phù hợp với chuyên khoa hiện tại.</p>
                  </div>
                </div>

                {availableDoctors.length > 0 ? (
                  <div className="patient-doctor-grid">
                    {availableDoctors.map((doctor) => (
                      <button
                        key={doctor.id}
                        type="button"
                        className={`patient-doctor-card ${normalizeId(selectedDoctorId) === normalizeId(doctor.id) ? "selected" : ""}`}
                        onClick={() => {
                          setSelectedDoctorId(normalizeId(doctor.id));
                          setSelectedScheduleId("");
                        }}
                      >
                        <div className="patient-doctor-top">
                          <img src={getAvatarSource(doctor, doctor.fullName)} alt={doctor.fullName} />
                          <div>
                            <strong>{doctor.fullName}</strong>
                            <span>{doctor.specialization}</span>
                          </div>
                          <div className="patient-doctor-rating">
                            <BsStarFill /> {Number(doctor.rating || 4.5).toFixed(1)}
                          </div>
                        </div>
                        <p>{doctor.qualification || doctor.departmentName || "Bác sĩ EverCare"}</p>
                        <div className="patient-doctor-meta">
                          <span>{doctor.workStatus}</span>
                          <span>{doctor.departmentName || doctor.specialization}</span>
                        </div>
                      </button>
                    ))}
                  </div>
                ) : (
                  <div className="patient-empty-state">
                    <h4>Chưa có bác sĩ phù hợp</h4>
                    <p>Backend hiện chưa trả về bác sĩ thuộc chuyên khoa này.</p>
                  </div>
                )}
              </Card.Body>
            </Card>
          )}

          {step === 3 && (
            <Card className="patient-booking-card">
              <Card.Body>
                <div className="patient-section-head compact">
                  <div>
                    <h3>Chọn thời gian</h3>
                    <p>Chỉ các khung giờ còn trống và còn hiệu lực mới có thể đặt.</p>
                  </div>
                </div>

                <div className="patient-booking-schedule-toolbar">
                  <Form.Group className="patient-form-group">
                    <Form.Label>Ngày khám</Form.Label>
                    <Form.Control type="date" value={scheduleDate} onChange={(event) => setScheduleDate(event.target.value)} />
                  </Form.Group>
                  <div className="patient-booking-summary mini">
                    <div>
                      <span>Bác sĩ</span>
                      <strong>{selectedDoctor?.fullName || "Chưa chọn"}</strong>
                    </div>
                    <div>
                      <span>Dịch vụ</span>
                      <strong>{selectedService?.name || "Chưa chọn"}</strong>
                    </div>
                  </div>
                </div>

                {scheduleError && <Alert variant="warning">{scheduleError}</Alert>}

                {loadingSchedules ? (
                  <div className="patient-loading-panel inline">Đang tải lịch làm việc của bác sĩ...</div>
                ) : doctorSchedules.length > 0 ? (
                  <div className="patient-schedule-grid">
                    {doctorSchedules.map((schedule) => {
                      const scheduleId = normalizeId(schedule.id);
                      const isSelected = normalizeId(selectedScheduleId) === scheduleId;
                      const isDisabled = !isScheduleAvailable(schedule);

                      return (
                        <button
                          key={scheduleId}
                          type="button"
                          className={`patient-schedule-card ${isSelected ? "selected" : ""} ${isDisabled ? "disabled" : ""}`}
                          onClick={() => {
                            if (isDisabled) return;
                            setSelectedScheduleId(scheduleId);
                            setAppointmentDate(schedule.workDate);
                            setStartTime(schedule.startTime);
                            setEndTime(schedule.endTime);
                          }}
                          disabled={isDisabled}
                        >
                          <div className="patient-schedule-head">
                            <strong>{schedule.displayDate}</strong>
                            <span className={`patient-schedule-badge ${isDisabled ? "danger" : "success"}`}>{schedule.statusLabel}</span>
                          </div>
                          <div className="patient-schedule-time">
                            <BsClock />
                            {schedule.displayRange}
                          </div>
                          <div className="patient-schedule-meta">
                            <span>Còn trống: {schedule.remainingSlots}</span>
                            <span>Tối đa: {schedule.maxPatients}</span>
                          </div>
                          {schedule.note && <p>{schedule.note}</p>}
                        </button>
                      );
                    })}
                  </div>
                ) : (
                  <div className="patient-empty-state">
                    <h4>Chưa có lịch trống</h4>
                    <p>Không có khung giờ khả dụng trong ngày bạn chọn.</p>
                  </div>
                )}

                <Form.Group className="patient-form-group mt-4">
                  <Form.Label>Lý do khám</Form.Label>
                  <Form.Control as="textarea" rows={3} value={reason} onChange={(event) => setReason(event.target.value)} placeholder="Mô tả ngắn triệu chứng hoặc nhu cầu khám" />
                </Form.Group>

                <Form.Group className="patient-form-group">
                  <Form.Label>Triệu chứng chính</Form.Label>
                  <Form.Control as="textarea" rows={3} value={symptomNote} onChange={(event) => setSymptomNote(event.target.value)} placeholder="Ví dụ: sốt, đau đầu, ho khan..." />
                </Form.Group>
              </Card.Body>
            </Card>
          )}

          {step === 4 && (
            <Card className="patient-booking-card">
              <Card.Body>
                <div className="patient-section-head compact">
                  <div>
                    <h3>Xác nhận đặt lịch</h3>
                    <p>Kiểm tra lại thông tin trước khi gửi yêu cầu.</p>
                  </div>
                </div>

                <div className="patient-booking-summary">
                  <div>
                    <span>Chuyên khoa</span>
                    <strong>{selectedSpecialty === "ALL" ? "Tất cả" : selectedSpecialty}</strong>
                  </div>
                  <div>
                    <span>Dịch vụ</span>
                    <strong>{selectedService?.name || "Chưa chọn"}</strong>
                  </div>
                  <div>
                    <span>Bác sĩ</span>
                    <strong>{selectedDoctor?.fullName || "Chưa chọn"}</strong>
                  </div>
                  <div>
                    <span>Ngày giờ</span>
                    <strong>
                      {appointmentDate} {startTime}
                    </strong>
                  </div>
                </div>

                <Form.Group className="patient-form-group mt-3">
                  <Form.Label>Phương thức thanh toán</Form.Label>
                  <Form.Select
                    value={paymentMethod}
                    onChange={(event) => {
                      const nextMethod = event.target.value;
                      setPaymentMethod(nextMethod);
                      setPaymentChannel(nextMethod === "VNPAY" ? "QR" : "WALLET");
                    }}
                  >
                    <option value="VNPAY">VNPay - QR</option>
                    <option value="MOMO">MoMo - Ví điện tử</option>
                    <option value="ZALOPAY">ZaloPay - Ví điện tử</option>
                  </Form.Select>
                </Form.Group>

                {!profile?.id && (
                  <Alert variant="warning" className="mb-0">
                    Bạn chưa có hồ sơ bệnh nhân. Hãy tạo hồ sơ trước khi xác nhận để backend xử lý đặt lịch chính xác.
                  </Alert>
                )}

                <Alert variant="info" className="mt-3 mb-0">
                  Hệ thống sẽ gọi API thật để tạo lịch hẹn. Nếu backend trả về hóa đơn, EverCare sẽ khởi tạo thanh toán ngay mà không reload trang.
                </Alert>
              </Card.Body>
            </Card>
          )}

          <div className="patient-booking-actions">
            <Button type="button" variant="light" className="patient-outline-button" onClick={goBack} disabled={step === 0 || submitting}>
              <BsArrowLeft /> Quay lại
            </Button>

            {step < steps.length - 1 ? (
              <Button
                type="button"
                className="patient-primary-soft"
                onClick={goNext}
                disabled={
                  (step === 1 && !selectedService) ||
                  (step === 2 && !selectedDoctor) ||
                  (step === 3 && !selectedSchedule) ||
                  submitting
                }
              >
                Tiếp tục <BsArrowRight />
              </Button>
            ) : (
              <Button
                type="button"
                className="patient-primary-soft"
                onClick={handleConfirm}
                disabled={submitting || !appointmentDate || !selectedDoctor || !selectedService || !selectedSchedule || !profile?.id}
              >
                {submitting ? "Đang đặt..." : "Xác nhận đặt lịch"}
              </Button>
            )}
          </div>
        </div>
      </section>
    </div>
  );
}

export default PatientBookAppointment;
