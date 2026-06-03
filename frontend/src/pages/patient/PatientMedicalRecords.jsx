import { useEffect, useMemo, useState } from "react";
import { Badge, Button, Card, Form, Table } from "react-bootstrap";
import { BsArrowLeft, BsArrowRight, BsDownload, BsFileEarmarkMedical, BsFilter, BsPrinter } from "react-icons/bs";
import { Link } from "react-router-dom";
import { getPatientMedicalRecords } from "../../services/patient/patientMedicalRecordApi";
import { formatShortDate, getPatientStatusMeta } from "./patientPageUtils";

function PatientMedicalRecords() {
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(true);
  const [from, setFrom] = useState("");
  const [to, setTo] = useState("");
  const [page, setPage] = useState(1);
  const [pageInfo, setPageInfo] = useState(null);

  useEffect(() => {
    let mounted = true;

    const loadRecords = async () => {
      setLoading(true);
      try {
        const response = await getPatientMedicalRecords({
          from: from || undefined,
          to: to || undefined,
          page,
        });

        if (!mounted) return;

        setRecords(response.data?.items || []);
        setPageInfo(response.data?.pageInfo || null);
      } catch (error) {
        console.error(error);
        if (mounted) {
          setRecords([]);
          setPageInfo(null);
        }
      } finally {
        if (mounted) {
          setLoading(false);
        }
      }
    };

    loadRecords();

    return () => {
      mounted = false;
    };
  }, [from, page, to]);

  const summary = useMemo(() => ({ total: pageInfo?.totalElements ?? records.length }), [pageInfo, records.length]);

  const totalPages = pageInfo?.totalPages || 1;
  const currentPage = pageInfo?.page || page;

  return (
    <div className="patient-page">
      <div className="patient-page-header-row align-start">
        <div>
          <p className="patient-eyebrow">Hồ sơ bệnh án</p>
          <h2>Lịch sử khám bệnh và chẩn đoán đã ghi nhận</h2>
          <p className="patient-page-subtitle">Lọc theo khoảng thời gian để tìm hồ sơ nhanh hơn.</p>
        </div>
        <div className="patient-header-actions">
          <Button type="button" variant="light" className="patient-outline-button">
            <BsDownload /> Xuất file PDF
          </Button>
          <Button type="button" variant="light" className="patient-outline-button">
            <BsPrinter /> In hồ sơ
          </Button>
        </div>
      </div>

      <section className="patient-record-layout">
        <Card className="patient-filter-card records">
          <Card.Body>
            <div className="patient-section-head compact">
              <div>
                <h3>Bộ lọc</h3>
                <p>Khoảng thời gian, trang dữ liệu và hồ sơ liên quan</p>
              </div>
              <Button type="button" variant="link" className="patient-inline-link" onClick={() => setPage(1)}>
                <BsFilter /> Lọc
              </Button>
            </div>

            <div className="patient-filter-grid">
              <Form.Group className="patient-form-group">
                <Form.Label>Từ ngày</Form.Label>
                <Form.Control type="date" value={from} onChange={(event) => {
                  setFrom(event.target.value);
                  setPage(1);
                }} />
              </Form.Group>
              <Form.Group className="patient-form-group">
                <Form.Label>Đến ngày</Form.Label>
                <Form.Control type="date" value={to} onChange={(event) => {
                  setTo(event.target.value);
                  setPage(1);
                }} />
              </Form.Group>
            </div>
          </Card.Body>
        </Card>

        <Card className="patient-record-summary">
          <Card.Body>
            <p>Tổng lượt khám</p>
            <strong>{summary.total} lần</strong>
            <span>Dữ liệu đồng bộ từ backend</span>
          </Card.Body>
        </Card>
      </section>

      <Card className="patient-table-card">
        <Card.Body className="p-0">
          {loading ? (
            <div className="patient-loading-panel inline">Đang tải hồ sơ bệnh án...</div>
          ) : records.length > 0 ? (
            <Table responsive className="patient-table">
              <thead>
                <tr>
                  <th>Mã BA</th>
                  <th>Ngày khám</th>
                  <th>Bác sĩ</th>
                  <th>Chuyên khoa</th>
                  <th>Chẩn đoán</th>
                  <th>Trạng thái</th>
                  <th>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {records.map((record) => {
                  const statusMeta = getPatientStatusMeta(record.paymentStatus || record.status);

                  return (
                    <tr key={record.id}>
                      <td>
                        <strong>{record.recordCode}</strong>
                      </td>
                      <td>{formatShortDate(record.visitDate)}</td>
                      <td>{record.doctorName}</td>
                      <td>{record.departmentName}</td>
                      <td>{record.diagnosis || "Chưa có chẩn đoán"}</td>
                      <td>
                        <Badge bg={statusMeta.variant} className="patient-status-badge">
                          {statusMeta.label}
                        </Badge>
                      </td>
                      <td>
                        <Link to={`/patient/medical-records/${record.id}`} className="patient-inline-link">
                          Xem chi tiết <BsArrowRight />
                        </Link>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </Table>
          ) : (
            <div className="patient-empty-state table-empty">
              <h4>Chưa có hồ sơ bệnh án</h4>
              <p>Backend hiện chưa trả về lịch sử khám trong khoảng thời gian đã chọn.</p>
            </div>
          )}
        </Card.Body>
      </Card>

      {totalPages > 1 && (
        <div className="patient-pagination">
          <Button type="button" variant="light" className="patient-outline-button" onClick={() => setPage((current) => Math.max(current - 1, 1))} disabled={currentPage <= 1}>
            <BsArrowLeft /> Trước
          </Button>
          <span>
            Trang {currentPage} / {totalPages}
          </span>
          <Button
            type="button"
            variant="light"
            className="patient-outline-button"
            onClick={() => setPage((current) => Math.min(current + 1, totalPages))}
            disabled={currentPage >= totalPages}
          >
            Sau <BsArrowRight />
          </Button>
        </div>
      )}

      <section className="patient-info-grid">
        <Card className="patient-info-note">
          <Card.Body>
            <div className="patient-card-head">
              <div className="patient-card-icon">
                <BsFileEarmarkMedical />
              </div>
              <h3>Cần hỗ trợ tra cứu?</h3>
            </div>
            <p>Nếu bạn không tìm thấy hồ sơ mong muốn, vui lòng liên hệ bộ phận chăm sóc khách hàng EverCare.</p>
          </Card.Body>
        </Card>
        <Card className="patient-info-note">
          <Card.Body>
            <div className="patient-card-head">
              <div className="patient-card-icon">
                <BsFileEarmarkMedical />
              </div>
              <h3>Bảo mật thông tin</h3>
            </div>
            <p>Hồ sơ được mã hóa và chỉ bạn cùng bác sĩ điều trị mới có quyền truy cập.</p>
          </Card.Body>
        </Card>
      </section>
    </div>
  );
}

export default PatientMedicalRecords;
