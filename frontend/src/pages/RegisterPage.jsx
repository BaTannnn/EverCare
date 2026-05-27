import { useRef, useState } from "react";
import { Alert, Button, Form, Spinner } from "react-bootstrap";
import { Link, useNavigate } from "react-router-dom";
import Apis, { endpoints } from "../configs/Apis";
import authBackground from "../assets/auth-medical-bg.png";

const initialForm = {
  fullName: "",
  username: "",
  email: "",
  phone: "",
  password: "",
  confirmPassword: "",
  acceptedTerms: false,
  avatar: null,
};

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const phonePattern = /^\d{10}$/;
const maxAvatarSize = 5 * 1024 * 1024;

const getErrorMessage = (error) => {
  const data = error.response?.data;

  if (typeof data === "string") {
    return data;
  }

  return data?.message || "Không thể tạo tài khoản. Vui lòng thử lại.";
};

function RegisterPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState(initialForm);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [avatarPreview, setAvatarPreview] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const avatarInputRef = useRef(null);

  const updateField = (field, value) => {
    setForm((current) => ({
      ...current,
      [field]: value,
    }));
  };

  const handleAvatarChange = (e) => {
    const file = e.target.files?.[0] || null;

    updateField("avatar", file);

    if (avatarPreview) {
      URL.revokeObjectURL(avatarPreview);
    }

    setAvatarPreview(file ? URL.createObjectURL(file) : "");
  };

  const validateForm = () => {
    if (!form.fullName.trim()) return "Vui lòng nhập họ tên.";
    if (!form.username.trim()) return "Vui lòng nhập username.";
    if (form.username.trim().length < 4) return "Username phải có ít nhất 4 ký tự.";
    if (!form.email.trim()) return "Vui lòng nhập email.";
    if (!emailPattern.test(form.email.trim())) return "Email không hợp lệ.";
    if (!form.phone.trim()) return "Vui lòng nhập số điện thoại.";
    if (!phonePattern.test(form.phone.trim())) return "Số điện thoại phải gồm đúng 10 chữ số.";
    if (!form.password) return "Vui lòng nhập mật khẩu.";
    if (form.password.length < 8) return "Mật khẩu phải có ít nhất 8 ký tự.";
    if (form.password !== form.confirmPassword) return "Mật khẩu xác nhận không khớp.";
    if (!form.acceptedTerms) return "Vui lòng đồng ý điều khoản để tiếp tục.";
    if (form.avatar && !form.avatar.type.startsWith("image/")) return "Avatar phải là file ảnh.";
    if (form.avatar && form.avatar.size > maxAvatarSize) return "Avatar không được vượt quá 5MB.";

    return "";
  };

  const buildRegisterPayload = () => {
    const data = new FormData();
    data.append("fullName", form.fullName.trim());
    data.append("username", form.username.trim());
    data.append("email", form.email.trim());
    data.append("phone", form.phone.trim());
    data.append("password", form.password);
    data.append("confirmPassword", form.confirmPassword);

    if (form.avatar) {
      data.append("avatar", form.avatar);
    }

    return data;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");

    const validationError = validateForm();

    if (validationError) {
      setError(validationError);
      return;
    }

    setSubmitting(true);

    try {
      await Apis.post(endpoints.register, buildRegisterPayload());
      setSuccess("Tạo tài khoản bệnh nhân thành công. Đang chuyển về trang đăng nhập.");
      setForm(initialForm);
      setAvatarPreview("");
      if (avatarInputRef.current) {
        avatarInputRef.current.value = "";
      }
      setTimeout(() => navigate("/login", { replace: true }), 900);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <main className="auth-page auth-page-register">
      <header className="register-nav">
        <Link to="/" className="nav-brand">
          EverCare Portal
        </Link>
        <nav>
          <Link to="/">Trang chủ</Link>
          <Link to="/">Bác sĩ</Link>
          <Link to="/">Dịch vụ</Link>
          <Link to="/login" className="nav-pill">
            Trợ giúp
          </Link>
        </nav>
      </header>

      <section className="auth-visual" style={{ backgroundImage: `url(${authBackground})` }} />

      <section className="auth-panel">
        <div className="auth-panel-inner">
          <h1 className="auth-title register-title">Đăng ký tài khoản</h1>

          <div className="avatar-upload">
            <button
              type="button"
              className="avatar-preview"
              onClick={() => avatarInputRef.current?.click()}
              aria-label="Chọn ảnh đại diện"
            >
              {avatarPreview ? <img src={avatarPreview} alt="Avatar preview" /> : <span aria-hidden="true">+</span>}
            </button>
            <button
              type="button"
              className="avatar-edit"
              onClick={() => avatarInputRef.current?.click()}
              aria-label="Tải ảnh đại diện"
            >
              Sửa
            </button>
            <p>Tải lên ảnh đại diện của bạn</p>
            <input
              ref={avatarInputRef}
              type="file"
              accept="image/*"
              className="visually-hidden"
              onChange={handleAvatarChange}
              disabled={submitting}
            />
          </div>

          {error && (
            <Alert variant="danger" className="text-start">
              {error}
            </Alert>
          )}
          {success && (
            <Alert variant="success" className="text-start">
              {success}
            </Alert>
          )}

          <Form onSubmit={handleSubmit} noValidate className="auth-form">
            <Form.Group className="field-group" controlId="registerFullName">
              <Form.Label>Họ và tên</Form.Label>
              <div className="field-control">
                <span className="field-icon" aria-hidden="true">
                  U
                </span>
                <Form.Control
                  value={form.fullName}
                  onChange={(e) => updateField("fullName", e.target.value)}
                  placeholder="Nguyễn Văn A"
                  autoComplete="name"
                  disabled={submitting}
                />
              </div>
            </Form.Group>

            <div className="field-grid">
              <Form.Group className="field-group" controlId="registerUsername">
                <Form.Label>Username</Form.Label>
                <div className="field-control">
                  <span className="field-icon" aria-hidden="true">
                    @
                  </span>
                  <Form.Control
                    value={form.username}
                    onChange={(e) => updateField("username", e.target.value)}
                    placeholder="patient_001"
                    autoComplete="username"
                    disabled={submitting}
                  />
                </div>
              </Form.Group>

              <Form.Group className="field-group" controlId="registerPhone">
                <Form.Label>Số điện thoại</Form.Label>
                <div className="field-control">
                  <span className="field-icon" aria-hidden="true">
                    T
                  </span>
                  <Form.Control
                    value={form.phone}
                    onChange={(e) => updateField("phone", e.target.value)}
                    placeholder="09xx xxx xxx"
                    autoComplete="tel"
                    disabled={submitting}
                  />
                </div>
              </Form.Group>
            </div>

            <Form.Group className="field-group" controlId="registerEmail">
              <Form.Label>Email</Form.Label>
              <div className="field-control">
                <span className="field-icon" aria-hidden="true">
                  M
                </span>
                <Form.Control
                  type="email"
                  value={form.email}
                  onChange={(e) => updateField("email", e.target.value)}
                  placeholder="example@email.com"
                  autoComplete="email"
                  disabled={submitting}
                />
              </div>
            </Form.Group>

            <div className="field-grid">
              <Form.Group className="field-group" controlId="registerPassword">
                <Form.Label>Mật khẩu</Form.Label>
                <div className="field-control">
                  <span className="field-icon" aria-hidden="true">
                    #
                  </span>
                  <Form.Control
                    type={showPassword ? "text" : "password"}
                    value={form.password}
                    onChange={(e) => updateField("password", e.target.value)}
                    autoComplete="new-password"
                    disabled={submitting}
                  />
                  <button
                    type="button"
                    className="field-action compact"
                    onClick={() => setShowPassword((current) => !current)}
                    disabled={submitting}
                    aria-label={showPassword ? "Ẩn mật khẩu" : "Hiện mật khẩu"}
                  >
                    {showPassword ? "Ẩn" : "Hiện"}
                  </button>
                </div>
              </Form.Group>

              <Form.Group className="field-group" controlId="registerConfirmPassword">
                <Form.Label>Xác nhận mật khẩu</Form.Label>
                <div className="field-control">
                  <span className="field-icon" aria-hidden="true">
                    #
                  </span>
                  <Form.Control
                    type={showConfirmPassword ? "text" : "password"}
                    value={form.confirmPassword}
                    onChange={(e) => updateField("confirmPassword", e.target.value)}
                    autoComplete="new-password"
                    disabled={submitting}
                  />
                  <button
                    type="button"
                    className="field-action compact"
                    onClick={() => setShowConfirmPassword((current) => !current)}
                    disabled={submitting}
                    aria-label={showConfirmPassword ? "Ẩn mật khẩu xác nhận" : "Hiện mật khẩu xác nhận"}
                  >
                    {showConfirmPassword ? "Ẩn" : "Hiện"}
                  </button>
                </div>
              </Form.Group>
            </div>

            <Form.Check
              className="terms-check"
              id="acceptedTerms"
              type="checkbox"
              label="Tôi đồng ý với điều khoản và Chính sách bảo mật của EverCare Portal."
              checked={form.acceptedTerms}
              onChange={(e) => updateField("acceptedTerms", e.target.checked)}
              disabled={submitting}
            />

            <Button type="submit" className="auth-submit" disabled={submitting}>
              {submitting ? (
                <>
                  <Spinner as="span" animation="border" size="sm" className="me-2" />
                  Đang tạo tài khoản
                </>
              ) : (
                "Đăng ký"
              )}
            </Button>
          </Form>

          <div className="auth-switch register-switch">
            Đã có tài khoản? <Link to="/login">Đăng nhập ngay</Link>
          </div>
        </div>
      </section>
    </main>
  );
}

export default RegisterPage;
