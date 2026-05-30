import { useEffect, useMemo, useState } from "react";
import { Alert, Button, Card, Form, Modal } from "react-bootstrap";
import { BsCalendar3, BsEnvelope, BsExclamationTriangle, BsFileMedical, BsGenderAmbiguous, BsGeoAlt, BsHeartPulse, BsPencil, BsPhone, BsShieldCheck } from "react-icons/bs";
import { useOutletContext } from "react-router-dom";
import { createPatientProfile, updatePatientProfile } from "../../services/patient/patientProfileApi";
import { getAvatarSource } from "./patientPageUtils";

const profileTemplate = {
  fullName: "",
  dateOfBirth: "",
  gender: "",
  phone: "",
  email: "",
  address: "",
  citizenId: "",
  bloodType: "",
  allergyNote: "",
  medicalHistoryNote: "",
  healthInsuranceNo: "",
  emergencyContactName: "",
  emergencyContactPhone: "",
};

function PatientProfile() {
  const { profile, setProfile: setShellProfile } = useOutletContext() || {};
  const [form, setForm] = useState(profileTemplate);
  const [showEditModal, setShowEditModal] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState("");
  const data = useMemo(() => profile || {}, [profile]);
  const avatarSource = getAvatarSource(data, data.fullName || "Bệnh nhân EverCare");

  useEffect(() => {
    setForm({
      fullName: data.fullName || "",
      dateOfBirth: data.dateOfBirth || "",
      gender: data.gender || "",
      phone: data.phone || "",
      email: data.email || "",
      address: data.address || "",
      citizenId: data.citizenId || "",
      bloodType: data.bloodType || "",
      allergyNote: data.allergyNote || "",
      medicalHistoryNote: data.medicalHistoryNote || "",
      healthInsuranceNo: data.healthInsuranceNo || "",
      emergencyContactName: data.emergencyContactName || "",
      emergencyContactPhone: data.emergencyContactPhone || "",
    });
  }, [
    data.address,
    data.allergyNote,
    data.bloodType,
    data.citizenId,
    data.dateOfBirth,
    data.email,
    data.emergencyContactName,
    data.emergencyContactPhone,
    data.gender,
    data.healthInsuranceNo,
    data.medicalHistoryNote,
    data.phone,
    data.fullName,
  ]);

  const handleOpenModal = () => {
    setMessage("");
    setShowEditModal(true);
  };

  const handleChange = (event) => {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!form.gender || !form.dateOfBirth) {
      setMessage("Vui lòng chọn giới tính và ngày sinh trước khi lưu hồ sơ.");
      return;
    }

    setSubmitting(true);
    setMessage("");

    try {
      const payload = {
        fullName: form.fullName,
        dateOfBirth: form.dateOfBirth || null,
        gender: form.gender,
        phone: form.phone,
        email: form.email,
        address: form.address,
        citizenId: form.citizenId,
        healthInsuranceNo: form.healthInsuranceNo,
        emergencyContactName: form.emergencyContactName,
        emergencyContactPhone: form.emergencyContactPhone,
        bloodType: form.bloodType,
        allergyNote: form.allergyNote,
        medicalHistoryNote: form.medicalHistoryNote,
      };

      const response = data?.id ? await updatePatientProfile(payload) : await createPatientProfile(payload);
      const updatedProfile = response.data || null;

      if (setShellProfile) {
        setShellProfile(updatedProfile);
      }

      setMessage(data?.id ? "Đã cập nhật hồ sơ bệnh nhân." : "Đã tạo hồ sơ bệnh nhân.");
      setShowEditModal(false);
    } catch (error) {
      console.error(error);
      setMessage("Không thể lưu hồ sơ. Vui lòng kiểm tra lại thông tin.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="patient-page">
      <div className="patient-page-header-row">
        <div>
          <p className="patient-eyebrow">Hồ sơ cá nhân</p>
          <h2>Quản lý và cập nhật thông tin y tế cá nhân của bạn</h2>
        </div>
        <Button type="button" className="patient-primary-soft" onClick={handleOpenModal}>
          <BsPencil /> {data?.id ? "Chỉnh sửa hồ sơ" : "Tạo hồ sơ"}
        </Button>
      </div>

      {message && <Alert variant="info">{message}</Alert>}

      {!data?.id && (
        <Alert variant="warning" className="patient-info-banner">
          Bạn chưa có hồ sơ bệnh nhân. Hãy tạo hồ sơ trước khi đặt lịch để backend nhận diện đúng thông tin người dùng.
        </Alert>
      )}

      <section className="patient-profile-grid">
        <Card className="patient-profile-card">
          <Card.Body>
            <div className="patient-profile-top">
              <img src={avatarSource} alt={data.fullName} onError={(event) => {
                event.currentTarget.src = getAvatarSource(null, data.fullName || "Bệnh nhân EverCare");
              }} />
              <div>
                <h3>{data.fullName || "Chưa có hồ sơ"}</h3>
                <p>Mã BN: {data.patientCode || data.id || "Chưa có"}</p>
              </div>
            </div>

            <div className="patient-profile-details">
              <div>
                <span><BsCalendar3 /> Ngày sinh</span>
                <strong>{data.dateOfBirth || "Chưa cập nhật"}</strong>
              </div>
              <div>
                <span><BsGenderAmbiguous /> Giới tính</span>
                <strong>{data.gender || "Chưa cập nhật"}</strong>
              </div>
              <div>
                <span><BsPhone /> Số điện thoại</span>
                <strong>{data.phone || "Chưa cập nhật"}</strong>
              </div>
              <div>
                <span><BsEnvelope /> Email</span>
                <strong>{data.email || "Chưa cập nhật"}</strong>
              </div>
              <div className="patient-profile-wide">
                <span><BsGeoAlt /> Địa chỉ</span>
                <strong>{data.address || "Chưa cập nhật"}</strong>
              </div>
            </div>
          </Card.Body>
        </Card>

        <div className="patient-profile-side">
          <Card className="patient-info-card pastel blue">
            <Card.Body>
              <div className="patient-card-head">
                <div className="patient-card-icon"><BsHeartPulse /></div>
                <h3>Thông tin y tế</h3>
              </div>

              <div className="patient-metric-list">
                <div>
                  <span>Nhóm máu</span>
                  <strong>{data.bloodType || "Chưa cập nhật"}</strong>
                </div>
                <div>
                  <span>Dị ứng</span>
                  <strong>{data.allergyNote || "Không ghi nhận"}</strong>
                </div>
                <div>
                  <span>Tiền sử bệnh</span>
                  <strong>{data.medicalHistoryNote || "Không ghi nhận"}</strong>
                </div>
              </div>
            </Card.Body>
          </Card>

          <Card className="patient-info-card pastel green">
            <Card.Body>
              <div className="patient-card-head">
                <div className="patient-card-icon"><BsShieldCheck /></div>
                <h3>Bảo hiểm y tế</h3>
              </div>

              <div className="patient-insurance-card">
                <p>Bảo hiểm y tế bệnh nhân</p>
                <strong>{data.healthInsuranceNo || "Chưa cập nhật"}</strong>
                <div className="patient-insurance-meta">
                  <span>Trạng thái: {data.active ? "Đang hoạt động" : "Chưa đồng bộ"}</span>
                  <span>Mã hồ sơ: {data.patientCode || data.id || "Chưa có"}</span>
                </div>
                <div className="patient-status-pill success">Đã đồng bộ</div>
              </div>
            </Card.Body>
          </Card>

          <Card className="patient-info-card pastel red">
            <Card.Body>
              <div className="patient-card-head">
                <div className="patient-card-icon"><BsExclamationTriangle /></div>
                <h3>Liên hệ khẩn cấp</h3>
              </div>

              <div className="patient-emergency-card">
                <div>
                  <strong>{data.emergencyContactName || "Chưa cập nhật"}</strong>
                  <span>Liên hệ khẩn cấp</span>
                </div>
                <div>
                  <BsPhone />
                  <strong>{data.emergencyContactPhone || "Chưa cập nhật"}</strong>
                </div>
              </div>
            </Card.Body>
          </Card>

          <Card className="patient-info-card note">
            <Card.Body>
              <div className="patient-card-head">
                <div className="patient-card-icon"><BsFileMedical /></div>
                <h3>Ghi chú</h3>
              </div>
              <p>
                Thông tin này được dùng trong trường hợp khẩn cấp khi bác sĩ không thể liên lạc trực tiếp với bạn.
              </p>
            </Card.Body>
          </Card>
        </div>
      </section>

      <Modal show={showEditModal} onHide={() => setShowEditModal(false)} centered size="lg" animation={false}>
        <Modal.Header closeButton>
          <Modal.Title>{data?.id ? "Chỉnh sửa hồ sơ" : "Tạo hồ sơ bệnh nhân"}</Modal.Title>
        </Modal.Header>
        <Form onSubmit={handleSubmit}>
          <Modal.Body>
            <div className="patient-form-grid">
              <Form.Group className="patient-form-group">
                <Form.Label>Họ tên</Form.Label>
                <Form.Control name="fullName" value={form.fullName} onChange={handleChange} required />
              </Form.Group>
              <Form.Group className="patient-form-group">
                <Form.Label>Ngày sinh</Form.Label>
                <Form.Control name="dateOfBirth" type="date" value={form.dateOfBirth} onChange={handleChange} required />
              </Form.Group>
              <Form.Group className="patient-form-group">
                <Form.Label>Giới tính</Form.Label>
                <Form.Select name="gender" value={form.gender} onChange={handleChange} required>
                  <option value="">Chọn giới tính</option>
                  <option value="MALE">Nam</option>
                  <option value="FEMALE">Nữ</option>
                  <option value="OTHER">Khác</option>
                </Form.Select>
              </Form.Group>
              <Form.Group className="patient-form-group">
                <Form.Label>Số điện thoại</Form.Label>
                <Form.Control name="phone" value={form.phone} onChange={handleChange} required />
              </Form.Group>
              <Form.Group className="patient-form-group">
                <Form.Label>Email</Form.Label>
                <Form.Control name="email" type="email" value={form.email} onChange={handleChange} />
              </Form.Group>
              <Form.Group className="patient-form-group">
                <Form.Label>CCCD / Citizen ID</Form.Label>
                <Form.Control name="citizenId" value={form.citizenId} onChange={handleChange} />
              </Form.Group>
              <Form.Group className="patient-form-group patient-form-wide">
                <Form.Label>Địa chỉ</Form.Label>
                <Form.Control name="address" value={form.address} onChange={handleChange} />
              </Form.Group>
              <Form.Group className="patient-form-group">
                <Form.Label>Nhóm máu</Form.Label>
                <Form.Control name="bloodType" value={form.bloodType} onChange={handleChange} />
              </Form.Group>
              <Form.Group className="patient-form-group">
                <Form.Label>Số BHYT</Form.Label>
                <Form.Control name="healthInsuranceNo" value={form.healthInsuranceNo} onChange={handleChange} />
              </Form.Group>
              <Form.Group className="patient-form-group patient-form-wide">
                <Form.Label>Dị ứng</Form.Label>
                <Form.Control as="textarea" rows={2} name="allergyNote" value={form.allergyNote} onChange={handleChange} />
              </Form.Group>
              <Form.Group className="patient-form-group patient-form-wide">
                <Form.Label>Tiền sử bệnh</Form.Label>
                <Form.Control as="textarea" rows={2} name="medicalHistoryNote" value={form.medicalHistoryNote} onChange={handleChange} />
              </Form.Group>
              <Form.Group className="patient-form-group">
                <Form.Label>Liên hệ khẩn cấp</Form.Label>
                <Form.Control name="emergencyContactName" value={form.emergencyContactName} onChange={handleChange} />
              </Form.Group>
              <Form.Group className="patient-form-group">
                <Form.Label>SĐT khẩn cấp</Form.Label>
                <Form.Control name="emergencyContactPhone" value={form.emergencyContactPhone} onChange={handleChange} />
              </Form.Group>
            </div>
          </Modal.Body>
          <Modal.Footer>
            <Button type="button" variant="outline-secondary" onClick={() => setShowEditModal(false)}>
              Hủy
            </Button>
            <Button type="submit" className="patient-primary-soft" disabled={submitting}>
              {submitting ? "Đang lưu..." : "Lưu hồ sơ"}
            </Button>
          </Modal.Footer>
        </Form>
      </Modal>
    </div>
  );
}

export default PatientProfile;
