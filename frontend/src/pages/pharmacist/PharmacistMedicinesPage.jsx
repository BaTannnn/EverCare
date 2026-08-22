import { useCallback, useEffect, useMemo, useState } from "react";
import { Button, Card, Form, Table } from "react-bootstrap";
import EmptyState from "../../components/common/EmptyState";
import ErrorState from "../../components/common/ErrorState";
import LoadingState from "../../components/common/LoadingState";
import StatusBadge from "../../components/common/StatusBadge";
import { useDebouncedValue } from "../../hooks/useDebouncedValue";
import { getLowStockMedicines, getPharmacistMedicines } from "../../services/pharmacist/pharmacistMedicineApi";
import { formatMoney, getErrorMessage } from "./pharmacistPageUtils";

const PAGE_SIZE = 10;

function PharmacistPagination({ page, pageSize, itemCount, onPageChange }) {
  const canGoPrev = page > 1;
  const canGoNext = itemCount >= pageSize;

  if (!canGoPrev && !canGoNext) {
    return null;
  }

  return (
    <div className="pharmacist-pagination">
      <Button type="button" variant="outline-primary" disabled={!canGoPrev} onClick={() => onPageChange(page - 1)}>
        Trước
      </Button>
      <span>Trang {page}</span>
      <Button type="button" variant="outline-primary" disabled={!canGoNext} onClick={() => onPageChange(page + 1)}>
        Sau
      </Button>
    </div>
  );
}

function PharmacistMedicinesPage() {
  const [medicines, setMedicines] = useState([]);
  const [lowStock, setLowStock] = useState([]);
  const [keyword, setKeyword] = useState("");
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const debouncedKeyword = useDebouncedValue(keyword, 600);

  const loadMedicines = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      const [medicineResponse, lowStockResponse] = await Promise.all([
        getPharmacistMedicines({ keyword: debouncedKeyword.trim(), page, size: PAGE_SIZE }),
        getLowStockMedicines(),
      ]);
      setMedicines(medicineResponse.data || []);
      setLowStock(lowStockResponse.data || []);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [debouncedKeyword, page]);

  useEffect(() => {
    loadMedicines();
  }, [loadMedicines]);

  useEffect(() => {
    setPage(1);
  }, [debouncedKeyword]);

  const lowStockById = useMemo(() => new Map(lowStock.map((item) => [item.id, item])), [lowStock]);

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
          {medicines.length === 0 ? (
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
                {medicines.map((medicine) => {
                  const lowStockItem = lowStockById.get(medicine.id);
                  return (
                    <tr key={medicine.id}>
                      <td>{medicine.medicineCode}</td>
                      <td>{medicine.name}</td>
                      <td>{medicine.unit}</td>
                      <td>{formatMoney(medicine.unitPrice)}</td>
                      <td>{medicine.availableQuantity ?? "--"}</td>
                      <td>{medicine.minStockQuantity ?? lowStockItem?.minStockQuantity ?? "--"}</td>
                      <td><StatusBadge status={medicine.active ? "NORMAL" : "CANCELLED"} label={medicine.active ? "Đang dùng" : "Ngưng dùng"} /></td>
                      <td>{lowStockItem ? <StatusBadge status="LOW" /> : <StatusBadge status="ENOUGH" />}</td>
                    </tr>
                  );
                })}
              </tbody>
            </Table>
          )}
          <PharmacistPagination page={page} pageSize={PAGE_SIZE} itemCount={medicines.length} onPageChange={setPage} />
        </Card.Body>
      </Card>
    </>
  );
}

export default PharmacistMedicinesPage;
