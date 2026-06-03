import { Button, Card, Table } from "react-bootstrap";
import EmptyState from "../../common/EmptyState";
import { formatDate } from "../../../pages/doctor/doctorPageUtils";

function TestResultsPanel({ results, openingResultFileId, onOpenResultFile }) {
  return (
    <Card className="doctor-card exam-section-card">
      <Card.Header>
        <h2>Kết quả xét nghiệm / Chẩn đoán hình ảnh</h2>
      </Card.Header>
      <Card.Body className="p-0">
        {results.length === 0 ? (
          <EmptyState title="Chưa có kết quả xét nghiệm" description="Bác sĩ chỉ xem kết quả, nhân viên y tế nhập kết quả." />
        ) : (
          <Table responsive className="doctor-table exam-table mb-0">
            <thead>
              <tr>
                <th>Dịch vụ</th>
                <th>Tên xét nghiệm</th>
                <th>Kết luận</th>
                <th>Ngày trả kết quả</th>
                <th>Thực hiện bởi</th>
                <th>Tệp kết quả</th>
              </tr>
            </thead>
            <tbody>
              {results.map((result) => (
                <tr key={result.id}>
                  <td>{result.serviceName || "--"}</td>
                  <td>{result.resultTitle || "--"}</td>
                  <td>{result.conclusion || "--"}</td>
                  <td>{formatDate(result.resultDate)}</td>
                  <td>{result.performedByName || "--"}</td>
                  <td>
                    {result.fileUrl ? (
                      <Button
                        type="button"
                        onClick={() => onOpenResultFile(result)}
                        disabled={openingResultFileId === result.id}
                        size="sm"
                        variant="outline-primary"
                      >
                        {openingResultFileId === result.id ? "Đang mở..." : "Xem PDF"}
                      </Button>
                    ) : (
                      "--"
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        )}
      </Card.Body>
    </Card>
  );
}

export default TestResultsPanel;
