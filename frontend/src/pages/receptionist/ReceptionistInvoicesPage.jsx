import { useCallback, useEffect, useMemo, useState } from "react";
import { Button, Card, Col, Form, Row, Table } from "react-bootstrap";
import { BsClipboardCheck, BsReceipt, BsSearch } from "react-icons/bs";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import EmptyState from "../../components/common/EmptyState";
import ErrorState from "../../components/common/ErrorState";
import LoadingState from "../../components/common/LoadingState";
import StatusBadge from "../../components/common/StatusBadge";
import { getReceptionistInvoices } from "../../services/receptionist/receptionistInvoiceApi";
import { formatCurrency, getErrorMessage, invoiceStatusMeta } from "./receptionistPageUtils";

const defaultFormState = (params) => ({
  keyword: params.get("keyword") || "",
  paymentStatus: params.get("paymentStatus") || "UNPAID",
  from: params.get("from") || "",
  to: params.get("to") || "",
  page: Number(params.get("page") || 1),
  size: Number(params.get("size") || 10),
});

function ReceptionistInvoicesPage() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const [form, setForm] = useState(() => defaultFormState(searchParams));
  const [invoices, setInvoices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadInvoices = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      const filters = defaultFormState(searchParams);
      const params = {
        paymentStatus: filters.paymentStatus || "UNPAID",
        page: filters.page,
        size: filters.size,
      };

      if (filters.keyword.trim()) params.keyword = filters.keyword.trim();
      if (filters.from) params.from = filters.from;
      if (filters.to) params.to = filters.to;

      const response = await getReceptionistInvoices(params);
      setInvoices(response.data || []);
    } catch (err) {
      if (err.response?.status === 401) {
        navigate("/login", { replace: true });
        return;
      }
      setError(getErrorMessage(err));
      setInvoices([]);
    } finally {
      setLoading(false);
    }
  }, [navigate, searchParams]);

  useEffect(() => {
    const ensureDefaults = new URLSearchParams(searchParams);
    let changed = false;

    if (!ensureDefaults.get("paymentStatus")) {
      ensureDefaults.set("paymentStatus", "UNPAID");
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
    if (!searchParams.get("paymentStatus")) {
      return;
    }
    loadInvoices();
  }, [loadInvoices, searchParams]);

  const updateField = (field, value) => {
    setForm((current) => ({ ...current, [field]: value }));
  };

  const submitFilters = (e) => {
    e.preventDefault();

    const params = new URLSearchParams();
    if (form.keyword.trim()) params.set("keyword", form.keyword.trim());
    params.set("paymentStatus", form.paymentStatus || "UNPAID");
    if (form.from) params.set("from", form.from);
    if (form.to) params.set("to", form.to);
    params.set("page", "1");
    params.set("size", String(form.size));
    setSearchParams(params);
  };

  const canGoPrev = Number(form.page) > 1;
  const canGoNext = invoices.length >= Number(form.size);

  const gotoPage = (page) => {
    const params = new URLSearchParams(searchParams);
    params.set("page", String(page));
    setSearchParams(params);
  };

  const summary = useMemo(() => ({
    unpaid: invoices.filter((invoice) => invoice.paymentStatus === "UNPAID").length,
    paid: invoices.filter((invoice) => invoice.paymentStatus === "PAID").length,
  }), [invoices]);

  if (loading) {
    return <LoadingState message="Đang tải danh sách hóa đơn..." />;
  }

  if (error) {
    return <ErrorState message={error} onRetry={loadInvoices} />;
  }

  return (
    <>
      <div className="page-header receptionist-page-header">
        <div>
          <div className="page-eyebrow">Lễ tân / Hóa đơn</div>
          <h1>Quản lý hóa đơn và thanh toán</h1>
          <p>Tìm hóa đơn theo mã, tên bệnh nhân hoặc mã bệnh án để ghi nhận thanh toán tại quầy nhanh chóng.</p>
        </div>
        <div className="page-header-actions">
          <Button as={Link} to="/receptionist/appointments" type="button" variant="outline-primary">
            <BsClipboardCheck /> Điều phối lịch hẹn
          </Button>
        </div>
      </div>

      <section className="receptionist-summary-grid">
        <Card className="doctor-card receptionist-summary-card">
          <Card.Body>
            <span>UNPAID</span>
            <strong>{summary.unpaid}</strong>
          </Card.Body>
        </Card>
        <Card className="doctor-card receptionist-summary-card">
          <Card.Body>
            <span>PAID</span>
            <strong>{summary.paid}</strong>
          </Card.Body>
        </Card>
      </section>

      <Card className="doctor-card mb-3">
        <Card.Header>
          <h2>Bộ lọc</h2>
          <Button type="button" variant="link" onClick={loadInvoices}>
            <BsReceipt /> Tải lại
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
                    placeholder="Mã hóa đơn, tên, SĐT, mã bệnh án"
                  />
                </Form.Group>
              </Col>
              <Col md={3}>
                <Form.Group>
                  <Form.Label>Trạng thái thanh toán</Form.Label>
                  <Form.Select value={form.paymentStatus} onChange={(e) => updateField("paymentStatus", e.target.value)}>
                    <option value="">Tất cả</option>
                    <option value="UNPAID">UNPAID</option>
                    <option value="PAID">PAID</option>
                    <option value="REFUNDED">REFUNDED</option>
                  </Form.Select>
                </Form.Group>
              </Col>
              <Col md={2}>
                <Form.Group>
                  <Form.Label>Từ ngày</Form.Label>
                  <Form.Control type="date" value={form.from} onChange={(e) => updateField("from", e.target.value)} />
                </Form.Group>
              </Col>
              <Col md={2}>
                <Form.Group>
                  <Form.Label>Đến ngày</Form.Label>
                  <Form.Control type="date" value={form.to} onChange={(e) => updateField("to", e.target.value)} />
                </Form.Group>
              </Col>
              <Col md={1}>
                <Form.Group>
                  <Form.Label>Size</Form.Label>
                  <Form.Select value={form.size} onChange={(e) => updateField("size", Number(e.target.value))}>
                    <option value={10}>10</option>
                    <option value={20}>20</option>
                    <option value={50}>50</option>
                  </Form.Select>
                </Form.Group>
              </Col>
            </Row>

            <div className="receptionist-form-actions mt-3">
              <Button type="submit">
                <BsSearch /> Lọc hóa đơn
              </Button>
            </div>
          </Form>
        </Card.Body>
      </Card>

      <Card className="doctor-card">
        <Card.Header>
          <h2>Danh sách hóa đơn</h2>
          <div className="receptionist-pagination-summary">
            Trang {form.page}
          </div>
        </Card.Header>
        <Card.Body>
          <div className="table-responsive">
            {invoices.length > 0 ? (
              <Table hover className="doctor-table mb-0">
                <thead>
                  <tr>
                    <th>Mã hóa đơn</th>
                    <th>Bệnh nhân</th>
                    <th>SĐT</th>
                    <th>Mã bệnh án</th>
                    <th>Tổng tiền</th>
                    <th>Trạng thái thanh toán</th>
                    <th>Hành động</th>
                  </tr>
                </thead>
                <tbody>
                  {invoices.map((invoice) => {
                    const meta = invoiceStatusMeta(invoice.paymentStatus);

                    return (
                      <tr key={invoice.id}>
                        <td>{invoice.invoiceCode}</td>
                        <td>{invoice.patientName || "--"}</td>
                        <td>{invoice.patientPhone || "--"}</td>
                        <td>{invoice.medicalRecordCode || "--"}</td>
                        <td>{formatCurrency(invoice.totalAmount)}</td>
                        <td><StatusBadge status={invoice.paymentStatus} label={meta.label} /></td>
                        <td>
                          <Button as={Link} to={`/receptionist/invoices/${invoice.id}`} type="button" size="sm" variant="outline-primary">
                            Chi tiết
                          </Button>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </Table>
            ) : (
              <EmptyState title="Không có hóa đơn phù hợp" description="Hóa đơn cần thanh toán sẽ xuất hiện tại đây." />
            )}
          </div>

          <div className="receptionist-pagination">
            <Button type="button" variant="outline-primary" disabled={!canGoPrev} onClick={() => gotoPage(Number(form.page) - 1)}>
              Trang trước
            </Button>
            <span>Trang {form.page}</span>
            <Button type="button" variant="outline-primary" disabled={!canGoNext} onClick={() => gotoPage(Number(form.page) + 1)}>
              Trang sau
            </Button>
          </div>
        </Card.Body>
      </Card>
    </>
  );
}

export default ReceptionistInvoicesPage;
