import { Button, Card } from "react-bootstrap";
import { BsCalendar3, BsEnvelope, BsExclamationTriangle, BsFileMedical, BsGenderAmbiguous, BsGeoAlt, BsHeartPulse, BsPencil, BsPerson, BsPhone, BsShieldCheck } from "react-icons/bs";
import { useOutletContext } from "react-router-dom";

function PatientProfile() {
  const { profile } = useOutletContext() || {};
  const data = profile || {};

  return (
    <div className="patient-page">
      <div className="patient-page-header-row">
        <div>
          <p className="patient-eyebrow">Hồ sơ cá nhân</p>
          <h2>Quản lý và cập nhật thông tin y tế cá nhân của bạn</h2>
        </div>
        <Button type="button" className="patient-primary-soft">
          <BsPencil /> Chỉnh sửa hồ sơ
        </Button>
      </div>

      <section className="patient-profile-grid">
        <Card className="patient-profile-card">
          <Card.Body>
            <div className="patient-profile-top">
              <img src={data.avatar} alt={data.fullName} />
              <div>
                <h3>{data.fullName}</h3>
                <p>Mã BN: {data.patientCode || data.id}</p>
              </div>
            </div>

            <div className="patient-profile-details">
              <div>
                <span><BsCalendar3 /> Ngày sinh</span>
                <strong>{data.dateOfBirth || "Chưa cập nhật"}</strong>
              </div>
              <div>
                <span><BsGenderAmbiguous /> Giới tính</span>
                <strong>{data.gender}</strong>
              </div>
              <div>
                <span><BsPhone /> Số điện thoại</span>
                <strong>{data.phone}</strong>
              </div>
              <div>
                <span><BsEnvelope /> Email</span>
                <strong>{data.email}</strong>
              </div>
              <div className="patient-profile-wide">
                <span><BsGeoAlt /> Địa chỉ</span>
                <strong>{data.address}</strong>
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
                  <strong>{data.bloodType}</strong>
                </div>
                <div>
                  <span>Dị ứng</span>
                  <strong>{data.allergyNote}</strong>
                </div>
                <div>
                  <span>Tiền sử bệnh</span>
                  <strong>{data.medicalHistoryNote}</strong>
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
                <strong>{data.healthInsuranceNo}</strong>
                <div className="patient-insurance-meta">
                  <span>Trạng thái: {data.active ? "Đang hoạt động" : "Không hoạt động"}</span>
                  <span>Mã hồ sơ: {data.patientCode || data.id}</span>
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
                  <strong>{data.emergencyContactName}</strong>
                  <span>Liên hệ khẩn cấp</span>
                </div>
                <div>
                  <BsPhone />
                  <strong>{data.emergencyContactPhone}</strong>
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
    </div>
  );
}

export default PatientProfile;
