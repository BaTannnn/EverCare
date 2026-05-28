import { useEffect, useMemo, useState } from "react";
import { Badge, Button, Card, Form, Table } from "react-bootstrap";
import { BsArrowRight, BsDownload, BsFileEarmarkMedical, BsFilter, BsPrinter } from "react-icons/bs";
import { Link } from "react-router-dom";
import { getPatientMedicalRecords } from "../../services/patient/patientMedicalRecordApi";
import { formatShortDate, getPatientStatusMeta } from "./patientPageUtils";

function MedicalRecords() {
  const [records, setRecords] = useState([]);
  const [department, setDepartment] = useState("ALL");
  const [year, setYear] = useState("ALL");

  useEffect(() => {
    let mounted = true;

    const loadRecords = async () => {
      try {
        const response = await getPatientMedicalRecords();
        if (mounted) {
          setRecords(response.data || []);
        }
      } catch (error) {
        console.error(error);
        if (mounted) {
          setRecords([]);
        }
      }
    };

    loadRecords();

    return () => {
      mounted = false;
    };
  }, []);

  const departments = useMemo(() => ["ALL", ...new Set(records.map((record) => record.departmentName).filter(Boolean))], [records]);
  const years = useMemo(() => ["ALL", ...new Set(records.map((record) => String(record.visitDate || "").slice(0, 4)).filter(Boolean))], [records]);

  const filteredRecords = useMemo(() => {
    return records.filter((record) => {
      const recordYear = String(record.visitDate || "").slice(0, 4);
      const matchDepartment = department === "ALL" || record.departmentName === department;
      const matchYear = year === "ALL" || recordYear === year;
      return matchDepartment && matchYear;
    });
  }, [department, records, year]);

  return (
    <div className="patient-page">
      <div className="patient-page-header-row align-start">
        <div>
          <p className="patient-eyebrow">Hồ sơ bệnh án</p>
          <h2>Lịch sử khám bệnh và chẩn đoán đã ghi nhận</h2>
          <p className="patient-page-subtitle">Lọc theo chuyên khoa hoặc năm để tìm hồ sơ nhanh hơn.</p>
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
                <p>Chuyên khoa, năm và khoảng thời gian</p>
              </div>
              <Button type="button" variant="link" className="patient-inline-link">
                <BsFilter /> Lọc
              </Button>
            </div>

            <div className="patient-filter-grid">
              <Form.Group className="patient-form-group">
                <Form.Label>Chuyên khoa</Form.Label>
                <Form.Select value={department} onChange={(event) => setDepartment(event.target.value)}>
                  {departments.map((item) => (
                    <option key={item} value={item}>
                      {item === "ALL" ? "Tất cả chuyên khoa" : item}
                    </option>
                  ))}
                </Form.Select>
              </Form.Group>
              <Form.Group className="patient-form-group">
                <Form.Label>Năm</Form.Label>
                <Form.Select value={year} onChange={(event) => setYear(event.target.value)}>
                  {years.map((item) => (
                    <option key={item} value={item}>
                      {item === "ALL" ? "Tất cả năm" : item}
                    </option>
                  ))}
                </Form.Select>
              </Form.Group>
            </div>
          </Card.Body>
        </Card>

        <Card className="patient-record-summary">
          <Card.Body>
            <p>Tổng lượt khám</p>
            <strong>{filteredRecords.length} lần</strong>
            <span>Dữ liệu được đồng bộ từ backend</span>
          </Card.Body>
        </Card>
      </section>

      <Card className="patient-table-card">
        <Card.Body className="p-0">
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
              {filteredRecords.map((record) => {
                const statusMeta = getPatientStatusMeta(record.paymentStatus || record.status);

                return (
                  <tr key={record.id}>
                    <td>
                      <strong>{record.recordCode}</strong>
                    </td>
                    <td>{formatShortDate(record.visitDate)}</td>
                    <td>{record.doctorName}</td>
                    <td>{record.departmentName}</td>
                    <td>{record.diagnosis}</td>
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
        </Card.Body>
      </Card>

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

export default MedicalRecords;
