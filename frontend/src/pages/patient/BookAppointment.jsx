import { useEffect, useMemo, useState } from "react";
import { Alert, Button, Card, Form } from "react-bootstrap";
import { BsArrowLeft, BsArrowRight, BsCalendar2Plus, BsCheckCircle, BsClock, BsSearch, BsStarFill } from "react-icons/bs";
import { useNavigate } from "react-router-dom";
import { bookPatientAppointment } from "../../services/patient/patientAppointmentApi";
import { getPatientDoctors, getPatientMedicalServices } from "../../services/patient/patientCatalogApi";
import { formatCurrency } from "./patientPageUtils";

const steps = ["Chuyên khoa", "Dịch vụ", "Bác sĩ", "Thời gian", "Xác nhận"];

const timeSlots = ["08:30", "09:30", "10:30", "13:30", "14:30", "16:00"];

const deriveRating = (doctor) => doctor.rating || 4.5;

function BookAppointment() {
  const navigate = useNavigate();
  const [step, setStep] = useState(0);
  const [doctors, setDoctors] = useState([]);
  const [services, setServices] = useState([]);
  const [selectedSpecialty, setSelectedSpecialty] = useState("ALL");
  const [selectedServiceId, setSelectedServiceId] = useState("");
  const [selectedDoctorId, setSelectedDoctorId] = useState("");
  const [appointmentDate, setAppointmentDate] = useState("");
  const [startTime, setStartTime] = useState(timeSlots[1]);
  const [reason, setReason] = useState("");
  const [symptomNote, setSymptomNote] = useState("");
  const [minimumRating, setMinimumRating] = useState(4.5);
  const [maximumServicePrice, setMaximumServicePrice] = useState(1000000);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

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
        setSelectedDoctorId(doctorList[0]?.id || "");
        setSelectedServiceId(serviceList[0]?.id || "");
        setSelectedSpecialty(doctorList[0]?.specialization || "ALL");
        setMaximumServicePrice(Math.max(...serviceList.map((service) => service.price || 0), 1000000));
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
    const values = doctors.map((doctor) => doctor.specialization).filter(Boolean);
    return ["ALL", ...new Set(values)];
  }, [doctors]);

  const filteredServices = useMemo(() => {
    return services.filter((service) => service.price <= maximumServicePrice);
  }, [maximumServicePrice, services]);

  const filteredDoctors = useMemo(() => {
    return doctors.filter((doctor) => {
      const matchSpecialty = selectedSpecialty === "ALL" || doctor.specialization === selectedSpecialty;
      const matchRating = deriveRating(doctor) >= minimumRating;
      return matchSpecialty && matchRating;
    });
  }, [doctors, minimumRating, selectedSpecialty]);

  const selectedDoctor = filteredDoctors.find((doctor) => doctor.id === selectedDoctorId) || filteredDoctors[0] || null;
  const selectedService = filteredServices.find((service) => service.id === selectedServiceId) || filteredServices[0] || null;

  useEffect(() => {
    if (!selectedDoctor && filteredDoctors[0]) {
      setSelectedDoctorId(filteredDoctors[0].id);
    }
  }, [filteredDoctors, selectedDoctor]);

  useEffect(() => {
    if (!selectedService && filteredServices[0]) {
      setSelectedServiceId(filteredServices[0].id);
    }
  }, [filteredServices, selectedService]);

  const goNext = () => setStep((current) => Math.min(current + 1, steps.length - 1));
  const goBack = () => setStep((current) => Math.max(current - 1, 0));

  const handleConfirm = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError("");

    try {
      await bookPatientAppointment({
        doctorId: selectedDoctor?.id,
        serviceId: selectedService?.id,
        appointmentDate,
        startTime,
        endTime: startTime,
        reason,
        symptomNote,
      });

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
          <p className="patient-page-subtitle">Chọn chuyên khoa, dịch vụ, bác sĩ và thời gian khám mà không cần tải lại trang.</p>
        </div>
      </div>

      {error && <Alert variant="warning">{error}</Alert>}

      <section className="patient-stepper">
        {steps.map((label, index) => (
          <div key={label} className={`patient-step ${index <= step ? "active" : ""}`}>
            <div className="patient-step-circle">{index < step ? <BsCheckCircle /> : index + 1}</div>
            <span>{label}</span>
          </div>
        ))}
      </section>

      <section className="patient-booking-layout">
        <aside className="patient-booking-filters">
          <Card className="patient-filter-card">
            <Card.Body>
              <div className="patient-section-head compact">
                <div>
                  <h3>Bộ lọc</h3>
                  <p>Chuyên khoa, đánh giá và giá dịch vụ</p>
                </div>
              </div>

              <Form.Group className="patient-form-group">
                <Form.Label>Tìm nhanh</Form.Label>
                <div className="patient-input-with-icon">
                  <BsSearch />
                  <Form.Control type="search" placeholder="Bác sĩ, dịch vụ..." />
                </div>
              </Form.Group>

              <Form.Group className="patient-form-group">
                <Form.Label>Chuyên khoa</Form.Label>
                <Form.Select value={selectedSpecialty} onChange={(e) => setSelectedSpecialty(e.target.value)}>
                  {specialties.map((specialty) => (
                    <option key={specialty} value={specialty}>
                      {specialty === "ALL" ? "Tất cả" : specialty}
                    </option>
                  ))}
                </Form.Select>
              </Form.Group>

              <Form.Group className="patient-form-group">
                <Form.Label>Đánh giá tối thiểu</Form.Label>
                <Form.Range min="4" max="5" step="0.1" value={minimumRating} onChange={(e) => setMinimumRating(Number(e.target.value))} />
                <div className="patient-range-labels">
                  <span>4.0</span>
                  <strong>{minimumRating.toFixed(1)}</strong>
                  <span>5.0</span>
                </div>
              </Form.Group>

              <Form.Group className="patient-form-group">
                <Form.Label>Giá dịch vụ tối đa</Form.Label>
                <Form.Range
                  min="0"
                  max={Math.max(...services.map((service) => service.price || 0), 1000000)}
                  step="50000"
                  value={maximumServicePrice}
                  onChange={(e) => setMaximumServicePrice(Number(e.target.value))}
                />
                <div className="patient-range-labels">
                  <span>0đ</span>
                  <strong>{formatCurrency(maximumServicePrice)}</strong>
                </div>
              </Form.Group>

              <div className="patient-support-panel">
                <strong>Cần hỗ trợ?</strong>
                <p>Nếu bạn chưa chắc chắn chuyên khoa phù hợp, EverCare sẽ hỗ trợ bạn chọn đúng dịch vụ.</p>
                <Button type="button" className="patient-primary-soft w-100">
                  Liên hệ tư vấn
                </Button>
              </div>
            </Card.Body>
          </Card>
        </aside>

        <div className="patient-booking-main">
          {step === 0 && (
            <Card className="patient-booking-card">
              <Card.Body>
                <div className="patient-section-head compact">
                  <div>
                    <h3>Chọn chuyên khoa</h3>
                    <p>Danh sách chuyên khoa được lấy từ danh mục bác sĩ thật.</p>
                  </div>
                </div>
                <div className="patient-pill-grid">
                  {specialties.map((specialty) => (
                    <button
                      key={specialty}
                      type="button"
                      className={`patient-pill ${selectedSpecialty === specialty ? "selected" : ""}`}
                      onClick={() => setSelectedSpecialty(specialty)}
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
                    <p>Dịch vụ và giá đều lấy từ backend medical-services.</p>
                  </div>
                </div>
                <div className="patient-service-grid">
                  {filteredServices.map((service) => (
                    <button
                      key={service.id}
                      type="button"
                      className={`patient-service-card ${selectedServiceId === service.id ? "selected" : ""}`}
                      onClick={() => setSelectedServiceId(service.id)}
                    >
                      <strong>{service.name}</strong>
                      <span>{service.description || service.serviceType || "Dịch vụ khám"}</span>
                      <div>
                        <em>{service.departmentName || "EverCare"}</em>
                        <b>{formatCurrency(service.price)}</b>
                      </div>
                    </button>
                  ))}
                </div>
              </Card.Body>
            </Card>
          )}

          {step === 2 && (
            <Card className="patient-booking-card">
              <Card.Body>
                <div className="patient-section-head compact">
                  <div>
                    <h3>Chọn bác sĩ</h3>
                    <p>{filteredDoctors.length} bác sĩ phù hợp với bộ lọc hiện tại.</p>
                  </div>
                </div>
                <div className="patient-doctor-grid">
                  {filteredDoctors.map((doctor) => (
                    <button
                      key={doctor.id}
                      type="button"
                      className={`patient-doctor-card ${selectedDoctorId === doctor.id ? "selected" : ""}`}
                      onClick={() => setSelectedDoctorId(doctor.id)}
                    >
                      <div className="patient-doctor-top">
                        <img src={doctor.avatar} alt={doctor.fullName} />
                        <div>
                          <strong>{doctor.fullName}</strong>
                          <span>{doctor.specialization}</span>
                        </div>
                        <div className="patient-doctor-rating">
                          <BsStarFill /> {deriveRating(doctor).toFixed(1)}
                        </div>
                      </div>
                      <p>{doctor.qualification || doctor.departmentName || "Bác sĩ EverCare"}</p>
                      <div className="patient-doctor-meta">
                        <span>{doctor.workStatus}</span>
                        <span>{doctor.departmentName || doctor.specialization}</span>
                      </div>
                      <div className="patient-doctor-fee">
                        <strong>Phí khám theo dịch vụ đã chọn</strong>
                        <span className="patient-doctor-featured">{selectedService ? formatCurrency(selectedService.price) : "0đ"}</span>
                      </div>
                    </button>
                  ))}
                </div>
              </Card.Body>
            </Card>
          )}

          {step === 3 && (
            <Card className="patient-booking-card">
              <Card.Body>
                <div className="patient-section-head compact">
                  <div>
                    <h3>Chọn thời gian</h3>
                    <p>Chọn ngày khám và khung giờ phù hợp.</p>
                  </div>
                </div>

                <Form.Group className="patient-form-group">
                  <Form.Label>Ngày khám</Form.Label>
                  <Form.Control type="date" value={appointmentDate} onChange={(e) => setAppointmentDate(e.target.value)} />
                </Form.Group>

                <div className="patient-time-grid">
                  {timeSlots.map((slot) => (
                    <button key={slot} type="button" className={`patient-time-slot ${startTime === slot ? "selected" : ""}`} onClick={() => setStartTime(slot)}>
                      <BsClock />
                      {slot}
                    </button>
                  ))}
                </div>

                <Form.Group className="patient-form-group mt-4">
                  <Form.Label>Lý do khám</Form.Label>
                  <Form.Control as="textarea" rows={3} value={reason} onChange={(e) => setReason(e.target.value)} placeholder="Mô tả ngắn triệu chứng hoặc nhu cầu khám" />
                </Form.Group>

                <Form.Group className="patient-form-group">
                  <Form.Label>Triệu chứng chính</Form.Label>
                  <Form.Control as="textarea" rows={3} value={symptomNote} onChange={(e) => setSymptomNote(e.target.value)} placeholder="Ví dụ: sốt, đau đầu, ho khan..." />
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
                    <strong>{selectedService?.name}</strong>
                  </div>
                  <div>
                    <span>Bác sĩ</span>
                    <strong>{selectedDoctor?.fullName}</strong>
                  </div>
                  <div>
                    <span>Ngày giờ</span>
                    <strong>{appointmentDate} {startTime}</strong>
                  </div>
                </div>

                <Alert variant="info" className="mt-3 mb-0">
                  Hệ thống sẽ gọi API thật để tạo lịch hẹn. Nếu backend trả lỗi, bạn sẽ không bị reload trang.
                </Alert>
              </Card.Body>
            </Card>
          )}

          <div className="patient-booking-actions">
            <Button type="button" variant="light" className="patient-outline-button" onClick={goBack} disabled={step === 0 || submitting}>
              <BsArrowLeft /> Quay lại
            </Button>

            {step < steps.length - 1 ? (
              <Button type="button" className="patient-primary-soft" onClick={goNext} disabled={!selectedService || !selectedDoctor}>
                Tiếp tục <BsArrowRight />
              </Button>
            ) : (
              <Button type="button" className="patient-primary-soft" onClick={handleConfirm} disabled={submitting || !appointmentDate || !selectedDoctor || !selectedService}>
                {submitting ? "Đang đặt..." : "Xác nhận đặt lịch"}
              </Button>
            )}
          </div>
        </div>
      </section>
    </div>
  );
}

export default BookAppointment;
