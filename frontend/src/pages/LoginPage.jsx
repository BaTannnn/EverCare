import { useState } from "react";
import { Alert, Button, Form, Spinner } from "react-bootstrap";
import { Link, useNavigate } from "react-router-dom";
import cookies from "react-cookies";
import Apis, { authApis, endpoints } from "../configs/Apis";
import authBackground from "../assets/auth-medical-bg.png";

const getErrorMessage = (error) => {
  const data = error.response?.data;

  if (typeof data === "string") {
    return data;
  }

  return data?.message || "Đăng nhập không thành công. Vui lòng kiểm tra lại thông tin.";
};

function LoginPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    username: "",
    password: "",
    remember: true,
  });
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const updateField = (field, value) => {
    setForm((current) => ({
      ...current,
      [field]: value,
    }));
  };

  const saveAuthCookie = (name, value) => {
    const options = { path: "/" };

    if (form.remember) {
      options.maxAge = 7 * 24 * 60 * 60;
    }

    cookies.save(name, value, options);
  };

  const loadProfile = async () => {
    try {
      const response = await authApis().get(endpoints.profile);
      const profile = response.data;

      if (profile) {
        saveAuthCookie("user", JSON.stringify(profile));

        if (profile.roles?.length) {
          saveAuthCookie("role", profile.roles);
        }
      }

      return profile;
    } catch {
      const fallbackProfile = { username: form.username.trim() };
      saveAuthCookie("user", JSON.stringify(fallbackProfile));
      return fallbackProfile;
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    if (!form.username.trim() || !form.password) {
      setError("Vui lòng nhập username và mật khẩu.");
      return;
    }

    setSubmitting(true);

    try {
      const response = await Apis.post(endpoints.login, {
        username: form.username.trim(),
        password: form.password,
      });
      const token = response.data?.token;

      if (!token) {
        throw new Error("Backend không trả token đăng nhập.");
      }

      saveAuthCookie("token", token);
      const profile = await loadProfile();
      const roles = profile?.roles || [];
      const isDoctor = roles.includes("DOCTOR") || roles.includes("ROLE_DOCTOR");
      const isStaff =
        roles.includes("LAB_TECH")
        || roles.includes("ROLE_LAB_TECH");

      navigate(isDoctor ? "/doctor/dashboard" : isStaff ? "/staff/dashboard" : "/admin/dashboard", { replace: true });
      const isAdmin = roles.includes("ADMIN") || roles.includes("ROLE_ADMIN");
      navigate(isDoctor ? "/doctor/dashboard" : isAdmin ? "/admin/dashboard" : "/patient/dashboard", { replace: true });
    } catch (err) {
      setError(err.response ? getErrorMessage(err) : err.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <main className="auth-page auth-page-login">
      <section className="auth-visual" style={{ backgroundImage: `url(${authBackground})` }} />

      <section className="auth-panel">
        <div className="auth-panel-inner">
          <div className="auth-brand auth-brand-with-icon">
            <span className="brand-icon" aria-hidden="true" />
            <span>EverCare Portal</span>
          </div>
          <h1 className="auth-title">Đăng nhập</h1>
          <p className="auth-subtitle">Chào mừng bạn quay trở lại với hệ thống y tế EverCare.</p>

          {error && (
            <Alert variant="danger" className="text-start">
              {error}
            </Alert>
          )}

          <Form onSubmit={handleSubmit} noValidate className="auth-form">
            <Form.Group className="field-group" controlId="loginUsername">
              <Form.Label>Username</Form.Label>
              <div className="field-control">
                <span className="field-icon" aria-hidden="true">
                  @
                </span>
                <Form.Control
                  autoComplete="username"
                  value={form.username}
                  onChange={(e) => updateField("username", e.target.value)}
                  placeholder="ten_dang_nhap"
                  disabled={submitting}
                />
              </div>
            </Form.Group>

            <Form.Group className="field-group" controlId="loginPassword">
              <Form.Label>Mật khẩu</Form.Label>
              <div className="field-control">
                <span className="field-icon" aria-hidden="true">
                  #
                </span>
                <Form.Control
                  type={showPassword ? "text" : "password"}
                  autoComplete="current-password"
                  value={form.password}
                  onChange={(e) => updateField("password", e.target.value)}
                  placeholder="Mật khẩu"
                  disabled={submitting}
                />
                <button
                  type="button"
                  className="field-action"
                  onClick={() => setShowPassword((current) => !current)}
                  disabled={submitting}
                  aria-label={showPassword ? "Ẩn mật khẩu" : "Hiện mật khẩu"}
                >
                  {showPassword ? "Ẩn" : "Hiện"}
                </button>
              </div>
            </Form.Group>

            <div className="auth-row">
              <Form.Check
                id="rememberLogin"
                type="checkbox"
                label="Ghi nhớ đăng nhập"
                checked={form.remember}
                onChange={(e) => updateField("remember", e.target.checked)}
                disabled={submitting}
              />
            </div>

            <Button type="submit" className="auth-submit" disabled={submitting}>
              {submitting ? (
                <>
                  <Spinner as="span" animation="border" size="sm" className="me-2" />
                  Đang đăng nhập
                </>
              ) : (
                "Đăng nhập"
              )}
            </Button>
          </Form>

          <div className="auth-divider">
            <span />
            <p>Hoặc đăng nhập bằng</p>
            <span />
          </div>

          <div className="social-buttons">
            <Button type="button" variant="outline-secondary">
              Google
            </Button>
            <Button type="button" variant="primary">
              Facebook
            </Button>
          </div>

          <div className="auth-switch">
            Chưa có tài khoản? <Link to="/register">Đăng ký ngay</Link>
          </div>

          <p className="auth-footnote">© 2026 EverCare Health Systems. Bảo mật & Tin cậy.</p>
        </div>
      </section>
    </main>
  );
}

export default LoginPage;
