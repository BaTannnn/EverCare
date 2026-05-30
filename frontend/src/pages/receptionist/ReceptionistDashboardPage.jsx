import { useEffect, useMemo, useState } from "react";
import { Button, Card, Col, Row, Table } from "react-bootstrap";
import { BsArrowRight, BsCalendarCheck, BsCashCoin, BsClipboardCheck, BsReceipt, BsSearch } from "react-icons/bs";
import { Link, useNavigate } from "react-router-dom";
import EmptyState from "../../components/common/EmptyState";
import ErrorState from "../../components/common/ErrorState";
import LoadingState from "../../components/common/LoadingState";
import StatusBadge from "../../components/common/StatusBadge";
import { getReceptionistAppointments } from "../../services/receptionist/receptionistAppointmentApi";
import { getReceptionistInvoices } from "../../services/receptionist/receptionistInvoiceApi";
import {
  appointmentStatusMeta,
  formatCurrency,
  getErrorMessage,
  invoiceStatusMeta,
  todayInputValue,
} from "./receptionistPageUtils";

function ReceptionistDashboardPage() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [appointments, setAppointments] = useState([]);
  const [invoices, setInvoices] = useState([]);

  const loadDashboard = async () => {
    setLoading(true);
    setError("");

    try {
      const [appointmentRes, invoiceRes] = await Promise.all([
        getReceptionistAppointments({ date: todayInputValue(), status: "BOOKED", size: 20 }),
        getReceptionistInvoices({ paymentStatus: "UNPAID", size: 20 }),
      ]);
      setAppointments(appointmentRes.data || []);
      setInvoices(invoiceRes.data || []);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDashboard();
  }, []);

  const summary = useMemo(() => ({
    booked: appointments.filter((item) => item.status === "BOOKED").length,
    waiting: appointments.filter((item) => item.status === "WAITING").length,
    unpaidInvoices: invoices.filter((item) => item.paymentStatus === "UNPAID").length,
    totalRevenue: invoices.reduce((sum, invoice) => sum + Number(invoice.totalAmount || 0), 0),
  }), [appointments, invoices]);

  if (loading) {
    return <LoadingState message="Đang tải dashboard lễ tân..." />;
  }

  if (error) {
    return <ErrorState message={error} onRetry={loadDashboard} />;
  }

  return (
    <>
      <div className="page-header receptionist-page-header">
        <div>
          <div className="page-eyebrow">Lễ tân</div>
          <h1>Receptionist Dashboard</h1>
          <p>Màn hình làm việc chính cho điều phối lịch khám, check-in và ghi nhận thanh toán.</p>
        </div>
        <div className="page-header-actions">
          <Button as={Link} to="/receptionist/appointments/new" type="button">
            <BsCalendarCheck /> Tạo lịch hộ
          </Button>
          <Button as={Link} to="/receptionist/invoices" type="button" variant="outline-primary">
            <BsReceipt /> Xem hóa đơn
          </Button>
        </div>
      </div>

      <section className="receptionist-summary-grid">
        <Card className="doctor-card receptionist-summary-card">
          <Card.Body>
            <span>Lịch hẹn hôm nay</span>
            <strong>{summary.booked + summary.waiting}</strong>
          </Card.Body>
        </Card>
        <Card className="doctor-card receptionist-summary-card">
          <Card.Body>
            <span>Đang chờ</span>
            <strong>{summary.waiting}</strong>
          </Card.Body>
        </Card>
        <Card className="doctor-card receptionist-summary-card">
          <Card.Body>
            <span>Hóa đơn chưa thanh toán</span>
            <strong>{summary.unpaidInvoices}</strong>
          </Card.Body>
        </Card>
        <Card className="doctor-card receptionist-summary-card">
          <Card.Body>
            <span>Tổng tiền chờ thu</span>
            <strong>{formatCurrency(summary.totalRevenue)}</strong>
          </Card.Body>
        </Card>
      </section>

      <Row className="g-3">
        <Col lg={6}>
          <Card className="doctor-card">
            <Card.Header>
              <h2>Quản lý lịch hẹn</h2>
              <Button as={Link} to="/receptionist/appointments" type="button" variant="link">
                Xem tất cả <BsArrowRight />
              </Button>
            </Card.Header>
            <Card.Body>
              <div className="receptionist-quick-actions">
                <Button as={Link} to={`/receptionist/appointments?status=BOOKED&date=${todayInputValue()}`} type="button">
                  <BsSearch /> Tìm lịch hẹn
                </Button>
                <Button as={Link} to="/receptionist/appointments/new" type="button" variant="outline-primary">
                  <BsClipboardCheck /> Tạo lịch mới
                </Button>
                <Button as={Link} to={`/receptionist/appointments?status=WAITING&date=${todayInputValue()}`} type="button" variant="outline-primary">
                  <BsCalendarCheck /> Danh sách chờ
                </Button>
              </div>

              <div className="table-responsive mt-3">
                {appointments.length > 0 ? (
                  <Table hover className="doctor-table mb-0">
                    <thead>
                      <tr>
                        <th>Mã lịch</th>
                        <th>Bệnh nhân</th>
                        <th>Giờ khám</th>
                        <th>Trạng thái</th>
                        <th />
                      </tr>
                    </thead>
                    <tbody>
                      {appointments.slice(0, 5).map((item) => {
                        const meta = appointmentStatusMeta(item.status);

                        return (
                          <tr key={item.id}>
                            <td>{item.appointmentCode}</td>
                            <td>{item.patient?.fullName || "--"}</td>
                            <td>{item.startTime?.slice(0, 5)} - {item.endTime?.slice(0, 5)}</td>
                            <td><StatusBadge status={item.status} label={meta.label} /></td>
                            <td>
                              <Button as={Link} to={`/receptionist/appointments/${item.id}`} type="button" size="sm" variant="outline-primary">
                                Chi tiết
                              </Button>
                            </td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </Table>
                ) : (
                  <EmptyState title="Không có lịch hẹn hôm nay" description="Backend chưa trả về lịch hẹn phù hợp với bộ lọc hiện tại." />
                )}
              </div>
            </Card.Body>
          </Card>
        </Col>

        <Col lg={6}>
          <Card className="doctor-card">
            <Card.Header>
              <h2>Quản lý hóa đơn</h2>
              <Button as={Link} to="/receptionist/invoices" type="button" variant="link">
                Xem tất cả <BsArrowRight />
              </Button>
            </Card.Header>
            <Card.Body>
              <div className="receptionist-quick-actions">
                <Button as={Link} to="/receptionist/invoices?paymentStatus=UNPAID" type="button">
                  <BsReceipt /> Hóa đơn chưa thanh toán
                </Button>
                <Button as={Link} to="/receptionist/invoices" type="button" variant="outline-primary">
                  <BsCashCoin /> Ghi nhận thanh toán
                </Button>
              </div>

              <div className="table-responsive mt-3">
                {invoices.length > 0 ? (
                  <Table hover className="doctor-table mb-0">
                    <thead>
                      <tr>
                        <th>Mã HĐ</th>
                        <th>Bệnh nhân</th>
                        <th>Tổng tiền</th>
                        <th>Trạng thái</th>
                        <th />
                      </tr>
                    </thead>
                    <tbody>
                      {invoices.slice(0, 5).map((invoice) => {
                        const meta = invoiceStatusMeta(invoice.paymentStatus);

                        return (
                          <tr key={invoice.id}>
                            <td>{invoice.invoiceCode}</td>
                            <td>{invoice.patientName || "--"}</td>
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
                  <EmptyState title="Chưa có hóa đơn cần xử lý" description="Hóa đơn chưa thanh toán sẽ hiển thị tại đây." />
                )}
              </div>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </>
  );
}

export default ReceptionistDashboardPage;
