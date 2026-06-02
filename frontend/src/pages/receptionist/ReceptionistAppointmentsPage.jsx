import { useEffect, useMemo, useState } from "react";
import { Alert, Button, Card, Col, Form, Row, Table } from "react-bootstrap";
import { BsClipboardCheck, BsPlusCircle, BsSearch } from "react-icons/bs";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import EmptyState from "../../components/common/EmptyState";
import LoadingState from "../../components/common/LoadingState";
import StatusBadge from "../../components/common/StatusBadge";
import { checkInReceptionistAppointment, getReceptionistAppointments } from "../../services/receptionist/receptionistAppointmentApi";
import { getReceptionistDepartments, getReceptionistDoctors } from "../../services/receptionist/receptionistReferenceApi";
import {
  appointmentStatusMeta,
  formatTime,
  getErrorMessage,
  todayInputValue,
} from "./receptionistPageUtils";

const statusOptions = ["BOOKED", "WAITING", "IN_PROGRESS", "COMPLETED", "CANCELLED", "NO_SHOW"];

const defaultFormState = (params) => ({
  keyword: params.get("keyword") || "",
  date: params.get("date") || todayInputValue(),
  status: params.get("status") || "BOOKED",
  doctorId: params.get("doctorId") || "",
  departmentId: params.get("departmentId") || "",
  page: Number(params.get("page") || 1),
  size: Number(params.get("size") || 10),
});

function ReceptionistAppointmentsPage() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const [form, setForm] = useState(() => defaultFormState(searchParams));
  const [appointments, setAppointments] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [actionId, setActionId] = useState(null);

  const loadReferences = async () => {
    const [departmentRes, doctorRes] = await Promise.all([
      getReceptionistDepartments(),
      getReceptionistDoctors(),
    ]);

    setDepartments(departmentRes.data || []);
    setDoctors(doctorRes.data || []);
  };

  const loadAppointments = async () => {
    setLoading(true);
    setError("");

    try {
      const filters = defaultFormState(searchParams);
      const params = {
        date: filters.date || todayInputValue(),
        status: filters.status || "BOOKED",
        page: filters.page,
        size: filters.size,
      };

      if (filters.keyword.trim()) params.keyword = filters.keyword.trim();
      if (filters.doctorId) params.doctorId = filters.doctorId;
      if (filters.departmentId) params.departmentId = filters.departmentId;

      const response = await getReceptionistAppointments(params);
      setAppointments(response.data || []);
    } catch (err) {
      if (err.response?.status === 401) {
        navigate("/login", { replace: true });
        return;
      }
      setError(getErrorMessage(err));
      setAppointments([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const ensureDefaults = new URLSearchParams(searchParams);
    let changed = false;

    if (!ensureDefaults.get("date")) {
      ensureDefaults.set("date", todayInputValue());
      changed = true;
    }
    if (!ensureDefaults.get("status")) {
      ensureDefaults.set("status", "BOOKED");
      changed = true;
    }
    if (!ensureDefaults.get("page")) {
      ensureDefaults.set("page", "1");
      changed = true;
    }
    if (!ensureDefaults.get("size")) {
      ensureDefaults.set("size", "10");
      changed = true;
    }

    if (changed) {
      setSearchParams(ensureDefaults, { replace: true });
      return;
    }

    setForm(defaultFormState(searchParams));
  }, [searchParams, setSearchParams]);

  useEffect(() => {
    const init = async () => {
      try {
        await loadReferences();
      } catch (err) {
        if (err.response?.status === 401) {
          navigate("/login", { replace: true });
          return;
        }
        setError(getErrorMessage(err));
      }
    };

    init();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (!searchParams.get("date")) {
      return;
    }
    loadAppointments();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [searchParams]);

  const filteredDoctors = useMemo(() => {
    if (!form.departmentId) return doctors;
    return doctors.filter((doctor) => String(doctor.departmentId) === String(form.departmentId));
  }, [doctors, form.departmentId]);

  const updateField = (field, value) => {
    setForm((current) => ({ ...current, [field]: value }));
  };

  const submitFilters = (e) => {
    e.preventDefault();

    const params = new URLSearchParams();
    if (form.keyword.trim()) params.set("keyword", form.keyword.trim());
    params.set("date", form.date || todayInputValue());
    params.set("status", form.status || "BOOKED");
    if (form.departmentId) params.set("departmentId", form.departmentId);
    if (form.doctorId) params.set("doctorId", form.doctorId);
    params.set("page", "1");
    params.set("size", String(form.size));
    setSearchParams(params);
  };

  const canGoPrev = Number(form.page) > 1;
  const canGoNext = appointments.length >= Number(form.size);

  const gotoPage = (page) => {
    const params = new URLSearchParams(searchParams);
    params.set("page", String(page));
    setSearchParams(params);
  };

  const handleCheckIn = async (appointment) => {
    setActionId(appointment.id);
    setNotice("");

    try {
      await checkInReceptionistAppointment(appointment.id, { note: "Tiếp nhận tại quầy" });
      setNotice(`Đã check-in ${appointment.appointmentCode}.`);
      await loadAppointments();
    } catch (err) {
      setError(getErrorMessage(err));
      if (err.response?.status === 401) {
        navigate("/login", { replace: true });
      }
    } finally {
      setActionId(null);
    }
  };

  const summary = useMemo(() => ({
    booked: appointments.filter((item) => item.status === "BOOKED").length,
    waiting: appointments.filter((item) => item.status === "WAITING").length,
    completed: appointments.filter((item) => item.status === "COMPLETED").length,
  }), [appointments]);

  return (
    <>
      <div className="page-header receptionist-page-header">
        <div>
          <div className="page-eyebrow">Lễ tân / Lịch hẹn</div>
          <h1>Quản lý lịch khám và check-in</h1>
          <p>Tìm kiếm theo mã lịch, tên bệnh nhân, số điện thoại hoặc CCCD và thao tác ngay tại quầy.</p>
        </div>
        <div className="page-header-actions">
          <Button as={Link} to="/receptionist/appointments/new" type="button">
            <BsPlusCircle /> Tạo lịch hộ
          </Button>
        </div>
      </div>

      {notice && <Alert variant="success">{notice}</Alert>}
      {error && <Alert variant="danger">{error}</Alert>}

      <section className="receptionist-summary-grid">
        <Card className="doctor-card receptionist-summary-card">
          <Card.Body>
            <span>BOOKED</span>
            <strong>{summary.booked}</strong>
          </Card.Body>
        </Card>
        <Card className="doctor-card receptionist-summary-card">
          <Card.Body>
            <span>WAITING</span>
            <strong>{summary.waiting}</strong>
          </Card.Body>
        </Card>
        <Card className="doctor-card receptionist-summary-card">
          <Card.Body>
            <span>COMPLETED</span>
            <strong>{summary.completed}</strong>
          </Card.Body>
        </Card>
      </section>

      <Card className="doctor-card mb-3">
        <Card.Header>
          <h2>Bộ lọc</h2>
          <Button type="button" variant="link" onClick={loadAppointments}>
            <BsClipboardCheck /> Tải lại
          </Button>
        </Card.Header>
        <Card.Body>
          <Form onSubmit={submitFilters}>
            <Row className="g-3">
              <Col md={4}>
                <Form.Group>
                  <Form.Label>Từ khóa</Form.Label>
                  <Form.Control
                    value={form.keyword}
                    onChange={(e) => updateField("keyword", e.target.value)}
                    placeholder="Mã lịch hẹn, tên, SĐT, CCCD"
                  />
                </Form.Group>
              </Col>
              <Col md={2}>
                <Form.Group>
                  <Form.Label>Ngày</Form.Label>
                  <Form.Control type="date" value={form.date} onChange={(e) => updateField("date", e.target.value)} />
                </Form.Group>
              </Col>
              <Col md={2}>
                <Form.Group>
                  <Form.Label>Trạng thái</Form.Label>
                  <Form.Select value={form.status} onChange={(e) => updateField("status", e.target.value)}>
                    {statusOptions.map((status) => (
                      <option key={status} value={status}>
                        {appointmentStatusMeta(status).label}
                      </option>
                    ))}
                  </Form.Select>
                </Form.Group>
              </Col>
              <Col md={2}>
                <Form.Group>
                  <Form.Label>Chuyên khoa</Form.Label>
                  <Form.Select value={form.departmentId} onChange={(e) => updateField("departmentId", e.target.value)}>
                    <option value="">Tất cả</option>
                    {departments.map((department) => (
                      <option key={department.id} value={department.id}>
                        {department.name}
                      </option>
                    ))}
                  </Form.Select>
                </Form.Group>
              </Col>
              <Col md={2}>
                <Form.Group>
                  <Form.Label>Bác sĩ</Form.Label>
                  <Form.Select value={form.doctorId} onChange={(e) => updateField("doctorId", e.target.value)}>
                    <option value="">Tất cả</option>
                    {filteredDoctors.map((doctor) => (
                      <option key={doctor.id} value={doctor.id}>
                        {doctor.fullName}
                      </option>
                    ))}
                  </Form.Select>
                </Form.Group>
              </Col>
              <Col md={2}>
                <Form.Group>
                  <Form.Label>Size</Form.Label>
                  <Form.Select value={form.size} onChange={(e) => updateField("size", Number(e.target.value))}>
                    {[10, 20, 50].map((size) => <option key={size} value={size}>{size}</option>)}
                  </Form.Select>
                </Form.Group>
              </Col>
              <Col md={12} className="d-flex justify-content-end gap-2">
                <Button type="submit">
                  <BsSearch /> Áp dụng
                </Button>
              </Col>
            </Row>
          </Form>
        </Card.Body>
      </Card>

      {loading ? (
        <LoadingState message="Đang tải danh sách lịch hẹn..." />
      ) : appointments.length === 0 ? (
        <EmptyState title="Không có lịch hẹn phù hợp" description="Hãy thay đổi bộ lọc để tìm lịch khác." />
      ) : (
        <Card className="doctor-card">
          <Card.Body className="p-0">
            <Table responsive hover className="doctor-table mb-0">
              <thead>
                <tr>
                  <th>Mã lịch hẹn</th>
                  <th>Bệnh nhân</th>
                  <th>SĐT</th>
                  <th>Bác sĩ</th>
                  <th>Chuyên khoa</th>
                  <th>Dịch vụ</th>
                  <th>Giờ khám</th>
                  <th>Trạng thái</th>
                  <th>Hành động</th>
                </tr>
              </thead>
              <tbody>
                {appointments.map((appointment) => {
                  const meta = appointmentStatusMeta(appointment.status);
                  const canCheckIn = appointment.status === "BOOKED" && appointment.appointmentDate === form.date;
                  const canEdit = ["BOOKED", "WAITING"].includes(appointment.status);

                  return (
                    <tr key={appointment.id}>
                      <td>{appointment.appointmentCode}</td>
                      <td>{appointment.patient?.fullName || "--"}</td>
                      <td>{appointment.patient?.phone || "--"}</td>
                      <td>{appointment.doctor?.fullName || "--"}</td>
                      <td>{appointment.doctor?.departmentName || "--"}</td>
                      <td>{appointment.service?.name || "--"}</td>
                      <td>{formatTime(appointment.startTime)} - {formatTime(appointment.endTime)}</td>
                      <td><StatusBadge status={appointment.status} label={meta.label} /></td>
                      <td>
                        <div className="d-flex gap-2 flex-wrap">
                          <Button as={Link} type="button" size="sm" variant="outline-primary" to={`/receptionist/appointments/${appointment.id}`}>
                            Chi tiết
                          </Button>
                          <Button
                            type="button"
                            size="sm"
                            disabled={!canCheckIn || actionId === appointment.id}
                            onClick={() => handleCheckIn(appointment)}
                          >
                            Check-in
                          </Button>
                          <Button as={Link} type="button" size="sm" variant="outline-secondary" to={`/receptionist/appointments/${appointment.id}/edit`} disabled={!canEdit}>
                            Sửa lịch
                          </Button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </Table>
          </Card.Body>
        </Card>
      )}

      <div className="receptionist-pagination">
        <Button type="button" variant="outline-primary" disabled={!canGoPrev} onClick={() => gotoPage(Math.max(1, form.page - 1))}>
          Trước
        </Button>
        <span>Trang {form.page}</span>
        <Button type="button" variant="outline-primary" disabled={!canGoNext} onClick={() => gotoPage(form.page + 1)}>
          Sau
        </Button>
      </div>
    </>
  );
}

export default ReceptionistAppointmentsPage;
