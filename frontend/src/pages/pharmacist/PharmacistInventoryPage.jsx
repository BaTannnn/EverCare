import { useCallback, useEffect, useMemo, useState } from "react";
import { Alert, Card, Col, Form, Row, Table } from "react-bootstrap";
import EmptyState from "../../components/common/EmptyState";
import LoadingState from "../../components/common/LoadingState";
import StatusBadge from "../../components/common/StatusBadge";
import { getInventoryTransactions } from "../../services/pharmacist/pharmacistInventoryApi";
import { formatDateTime, normalizeText } from "./pharmacistPageUtils";

function PharmacistInventoryPage() {
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [type, setType] = useState("ALL");
  const [keyword, setKeyword] = useState("");

  const loadTransactions = useCallback(async () => {
    setLoading(true);
    const response = await getInventoryTransactions();
    setTransactions(response.data || []);
    setLoading(false);
  }, []);

  useEffect(() => {
    loadTransactions();
  }, [loadTransactions]);

  const filteredTransactions = useMemo(() => {
    const normalized = normalizeText(keyword);
    return transactions.filter((transaction) => {
      const typeMatch = type === "ALL" || transaction.transactionType === type;
      const text = normalizeText([transaction.transactionCode, transaction.medicineName, transaction.batchCode].join(" "));
      return typeMatch && (!normalized || text.includes(normalized));
    });
  }, [keyword, transactions, type]);

  if (loading) {
    return <LoadingState message="Đang tải lịch sử nhập xuất kho..." />;
  }

  return (
    <>
      <div className="exam-page-header">
        <div className="doctor-breadcrumb">Dược sĩ <span>/</span> <strong>Nhập / xuất kho</strong></div>
        <h1>Nhập / xuất kho</h1>
        <p>Theo dõi lịch sử giao dịch kho thuốc.</p>
      </div>

      <Alert variant="warning">
        Backend chưa có API danh sách inventory transaction. Dữ liệu bên dưới là mock tạm. TODO replace mock when backend API is available.
      </Alert>

      <Card className="doctor-card mb-3">
        <Card.Body>
          <Row className="g-3">
            <Col md={4}>
              <Form.Group>
                <Form.Label>Loại giao dịch</Form.Label>
                <Form.Select value={type} onChange={(e) => setType(e.target.value)}>
                  <option value="ALL">Tất cả</option>
                  <option value="IMPORT">IMPORT</option>
                  <option value="PRESCRIPTION_EXPORT">PRESCRIPTION_EXPORT</option>
                  <option value="ADJUSTMENT">ADJUSTMENT</option>
                </Form.Select>
              </Form.Group>
            </Col>
            <Col md={8}>
              <Form.Group>
                <Form.Label>Tìm kiếm</Form.Label>
                <Form.Control value={keyword} onChange={(e) => setKeyword(e.target.value)} placeholder="Mã giao dịch, thuốc, lô thuốc" />
              </Form.Group>
            </Col>
          </Row>
        </Card.Body>
      </Card>

      <Card className="doctor-card">
        <Card.Header><h2>Lịch sử giao dịch kho</h2></Card.Header>
        <Card.Body className="p-0">
          {filteredTransactions.length === 0 ? (
            <EmptyState title="Chưa có dữ liệu giao dịch kho" />
          ) : (
            <Table responsive hover className="doctor-table mb-0">
              <thead>
                <tr>
                  <th>Mã giao dịch</th>
                  <th>Loại</th>
                  <th>Thuốc</th>
                  <th>Lô thuốc</th>
                  <th>Số lượng</th>
                  <th>Ngày giao dịch</th>
                  <th>Người thực hiện</th>
                  <th>Đơn thuốc</th>
                </tr>
              </thead>
              <tbody>
                {filteredTransactions.map((transaction) => (
                  <tr key={transaction.id}>
                    <td>{transaction.transactionCode || transaction.id}</td>
                    <td><StatusBadge status={transaction.transactionType} label={transaction.transactionType} /></td>
                    <td>{transaction.medicineName || "--"}</td>
                    <td>{transaction.batchCode || "--"}</td>
                    <td>{transaction.quantity}</td>
                    <td>{formatDateTime(transaction.transactionDate)}</td>
                    <td>{transaction.performedByName || "--"}</td>
                    <td>{transaction.prescriptionId ? `#${transaction.prescriptionId}` : "--"}</td>
                  </tr>
                ))}
              </tbody>
            </Table>
          )}
        </Card.Body>
      </Card>
    </>
  );
}

export default PharmacistInventoryPage;
