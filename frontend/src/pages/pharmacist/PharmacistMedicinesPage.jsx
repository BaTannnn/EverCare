import { useCallback, useEffect, useMemo, useState } from "react";
import { Alert, Button, Card, Form, Table } from "react-bootstrap";
import EmptyState from "../../components/common/EmptyState";
import ErrorState from "../../components/common/ErrorState";
import LoadingState from "../../components/common/LoadingState";
import StatusBadge from "../../components/common/StatusBadge";
import { getLowStockMedicines, getPharmacistMedicines } from "../../services/pharmacist/pharmacistMedicineApi";
import { formatMoney, getErrorMessage, normalizeText } from "./pharmacistPageUtils";

function PharmacistMedicinesPage() {
  const [medicines, setMedicines] = useState([]);
  const [lowStock, setLowStock] = useState([]);
  const [keyword, setKeyword] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadMedicines = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      const [medicineResponse, lowStockResponse] = await Promise.all([
        getPharmacistMedicines(),
        getLowStockMedicines(),
      ]);
      setMedicines(medicineResponse.data || []);
      setLowStock(lowStockResponse.data || []);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadMedicines();
  }, [loadMedicines]);

  const lowStockById = useMemo(() => new Map(lowStock.map((item) => [item.id, item])), [lowStock]);
  const filteredMedicines = useMemo(() => {
    const normalized = normalizeText(keyword);
    return medicines.filter((medicine) =>
      normalizeText([medicine.medicineCode, medicine.name].join(" ")).includes(normalized)
    );
  }, [keyword, medicines]);

  if (loading) {
    return <LoadingState message="Đang tải danh mục thuốc..." />;
  }

  if (error) {
    return <ErrorState message={error} onRetry={loadMedicines} />;
  }

  return (
    <>
      <div className="exam-page-header">
        <div className="doctor-breadcrumb">Dược sĩ <span>/</span> <strong>Thuốc</strong></div>
        <h1>Thuốc</h1>
        <p>Tra cứu danh mục thuốc và cảnh báo tồn kho thấp.</p>
      </div>

      <Alert variant="info">
        API thêm/sửa thuốc đã có ở backend. Giao diện modal thêm/sửa sẽ được nối ở bước quản trị danh mục tiếp theo.
      </Alert>

      <Card className="doctor-card mb-3">
        <Card.Body>
          <Form.Group>
            <Form.Label>Tìm thuốc</Form.Label>
            <Form.Control value={keyword} onChange={(e) => setKeyword(e.target.value)} placeholder="Tên hoặc mã thuốc" />
          </Form.Group>
        </Card.Body>
      </Card>

      <Card className="doctor-card">
        <Card.Header>
          <h2>Danh sách thuốc</h2>
          <Button type="button" variant="link" onClick={loadMedicines}>Tải lại</Button>
        </Card.Header>
        <Card.Body className="p-0">
          {filteredMedicines.length === 0 ? (
            <EmptyState title="Không tìm thấy thuốc" />
          ) : (
            <Table responsive hover className="doctor-table mb-0">
              <thead>
                <tr>
                  <th>Mã thuốc</th>
                  <th>Tên thuốc</th>
                  <th>Đơn vị</th>
                  <th>Giá bán</th>
                  <th>Tồn khả dụng</th>
                  <th>Tồn tối thiểu</th>
                  <th>Trạng thái</th>
                  <th>Cảnh báo</th>
                </tr>
              </thead>
              <tbody>
                {filteredMedicines.map((medicine) => {
                  const lowStockItem = lowStockById.get(medicine.id);
                  return (
                    <tr key={medicine.id}>
                      <td>{medicine.medicineCode}</td>
                      <td>{medicine.name}</td>
                      <td>{medicine.unit}</td>
                      <td>{formatMoney(medicine.unitPrice)}</td>
                      <td>{lowStockItem?.totalRemainingQuantity ?? "--"}</td>
                      <td>{medicine.minStockQuantity ?? lowStockItem?.minStockQuantity ?? "--"}</td>
                      <td><StatusBadge status={medicine.active ? "NORMAL" : "CANCELLED"} label={medicine.active ? "Đang dùng" : "Ngưng dùng"} /></td>
                      <td>{lowStockItem ? <StatusBadge status="LOW" /> : <StatusBadge status="ENOUGH" />}</td>
                    </tr>
                  );
                })}
              </tbody>
            </Table>
          )}
        </Card.Body>
      </Card>
    </>
  );
}

export default PharmacistMedicinesPage;
