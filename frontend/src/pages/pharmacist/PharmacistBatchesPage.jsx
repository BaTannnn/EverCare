import { useCallback, useEffect, useState } from "react";
import { Alert, Button, Card, Col, Form, Modal, Row, Table } from "react-bootstrap";
import EmptyState from "../../components/common/EmptyState";
import ErrorState from "../../components/common/ErrorState";
import LoadingState from "../../components/common/LoadingState";
import StatusBadge from "../../components/common/StatusBadge";
import { useDebouncedValue } from "../../hooks/useDebouncedValue";
import {
  deleteMedicineBatch,
  getMedicineBatches,
  importMedicineBatch,
  updateMedicineBatch,
} from "../../services/pharmacist/pharmacistBatchApi";
import { getPharmacistMedicines } from "../../services/pharmacist/pharmacistMedicineApi";
import { formatDate, formatMoney, getBatchExpiryStatus, getErrorMessage } from "./pharmacistPageUtils";

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

const emptyImportForm = {
  medicineId: "",
  batchCode: "",
  importDate: new Date().toISOString().slice(0, 10),
  expiryDate: "",
  quantity: "",
  importPrice: "",
  supplierName: "",
};

function PharmacistBatchesPage() {
  const [batches, setBatches] = useState([]);
  const [medicines, setMedicines] = useState([]);
  const [keyword, setKeyword] = useState("");
  const [filter, setFilter] = useState("ALL");
  const [page, setPage] = useState(1);
  const [showImport, setShowImport] = useState(false);
  const [editingBatch, setEditingBatch] = useState(null);
  const [form, setForm] = useState(emptyImportForm);
  const [saving, setSaving] = useState(false);
  const [actionId, setActionId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const debouncedKeyword = useDebouncedValue(keyword, 600);

  const loadBatches = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      const [batchResponse, medicineResponse] = await Promise.all([
        getMedicineBatches({ keyword: debouncedKeyword.trim(), status: filter, page, size: PAGE_SIZE }),
        getPharmacistMedicines({ page: 1, size: 1000 }),
      ]);
      setBatches(batchResponse.data || []);
      setMedicines(medicineResponse.data || []);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [debouncedKeyword, filter, page]);

  useEffect(() => {
    loadBatches();
  }, [loadBatches]);

  useEffect(() => {
    setPage(1);
  }, [debouncedKeyword, filter]);

  const updateForm = (field, value) => {
    setForm((current) => ({ ...current, [field]: value }));
  };

  const openCreateForm = () => {
    setEditingBatch(null);
    setForm(emptyImportForm);
    setNotice("");
    setShowImport(true);
  };

  const openEditForm = (batch) => {
    setEditingBatch(batch);
    setForm({
      medicineId: String(batch.medicineId || ""),
      batchCode: batch.batchCode || "",
      importDate: batch.importDate || new Date().toISOString().slice(0, 10),
      expiryDate: batch.expiryDate || "",
      quantity: String(batch.quantity || ""),
      importPrice: String(batch.importPrice || ""),
      supplierName: batch.supplierName || "",
    });
    setNotice("");
    setShowImport(true);
  };

  const closeBatchForm = () => {
    setShowImport(false);
    setEditingBatch(null);
    setForm(emptyImportForm);
  };

  const handleSaveBatch = async (e) => {
    e.preventDefault();
    setNotice("");

    if (!form.medicineId || !form.batchCode.trim() || !form.expiryDate || Number(form.quantity) <= 0) {
      setNotice("Vui lòng nhập đủ thông tin lô thuốc hợp lệ.");
      return;
    }

    if (new Date(form.expiryDate) <= new Date(form.importDate)) {
      setNotice("Hạn sử dụng phải lớn hơn ngày nhập.");
      return;
    }

    setSaving(true);
    try {
      const payload = {
        medicineId: Number(form.medicineId),
        batchCode: form.batchCode,
        importDate: form.importDate,
        expiryDate: form.expiryDate,
        quantity: Number(form.quantity),
        importPrice: Number(form.importPrice || 0),
        supplierName: form.supplierName,
      };

      if (editingBatch) {
        await updateMedicineBatch(editingBatch.id, payload);
        setNotice("Đã cập nhật lô thuốc.");
      } else {
        await importMedicineBatch(payload);
        setNotice("Đã nhập lô thuốc.");
      }

      closeBatchForm();
      loadBatches();
    } catch (err) {
      setNotice(getErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  const handleDeleteBatch = async (batch) => {
    if (!window.confirm(`Xóa lô ${batch.batchCode}?`)) {
      return;
    }

    setActionId(batch.id);
    setNotice("");
    try {
      await deleteMedicineBatch(batch.id);
      setNotice("Đã xóa lô thuốc.");
      loadBatches();
    } catch (err) {
      setNotice(getErrorMessage(err));
    } finally {
      setActionId(null);
    }
  };

  if (loading) {
    return <LoadingState message="Đang tải lô thuốc..." />;
  }

  if (error) {
    return <ErrorState message={error} onRetry={loadBatches} />;
  }

  return (
    <>
      <div className="exam-page-header">
        <div className="doctor-breadcrumb">Dược sĩ <span>/</span> <strong>Lô thuốc</strong></div>
        <h1>Lô thuốc</h1>
        <p>Quản lý hạn dùng, số lượng còn lại và nhập kho thuốc.</p>
      </div>

      {notice && <Alert variant={notice.startsWith("Đã") ? "success" : "warning"}>{notice}</Alert>}

      <Card className="doctor-card mb-3">
        <Card.Body>
          <Row className="g-3">
            <Col md={4}>
              <Form.Group>
                <Form.Label>Tìm kiếm</Form.Label>
                <Form.Control value={keyword} onChange={(e) => setKeyword(e.target.value)} placeholder="Mã lô hoặc tên thuốc" />
              </Form.Group>
            </Col>
            <Col md={4}>
              <Form.Group>
                <Form.Label>Trạng thái hạn dùng</Form.Label>
                <Form.Select value={filter} onChange={(e) => setFilter(e.target.value)}>
                  <option value="ALL">Tất cả</option>
                  <option value="VALID">Còn hạn</option>
                  <option value="NEAR_EXPIRY">Gần hết hạn</option>
                  <option value="EXPIRED">Đã hết hạn</option>
                </Form.Select>
              </Form.Group>
            </Col>
            <Col md={4} className="d-flex align-items-end justify-content-end">
              <Button type="button" onClick={openCreateForm}>Nhập lô thuốc</Button>
            </Col>
          </Row>
        </Card.Body>
      </Card>

      <Card className="doctor-card">
        <Card.Header>
          <h2>Danh sách lô thuốc</h2>
          <div className="d-flex gap-2">
            <Button type="button" variant="link" onClick={() => setFilter("NEAR_EXPIRY")}>Gần hết hạn</Button>
            <Button type="button" variant="link" onClick={() => setFilter("EXPIRED")}>Đã hết hạn</Button>
            <Button type="button" variant="link" onClick={loadBatches}>Tải lại</Button>
          </div>
        </Card.Header>
        <Card.Body className="p-0">
          {batches.length === 0 ? (
            <EmptyState title="Không có lô thuốc phù hợp" />
          ) : (
            <Table responsive hover className="doctor-table mb-0">
              <thead>
                <tr>
                  <th>Mã lô</th>
                  <th>Thuốc</th>
                  <th>Ngày nhập</th>
                  <th>Hạn sử dụng</th>
                  <th>SL ban đầu</th>
                  <th>SL còn lại</th>
                  <th>Giá nhập</th>
                  <th>Nhà cung cấp</th>
                  <th>Trạng thái</th>
                  <th>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {batches.map((batch) => (
                  <tr key={batch.id}>
                    <td>{batch.batchCode}</td>
                    <td>{batch.medicineName}</td>
                    <td>{formatDate(batch.importDate)}</td>
                    <td>{formatDate(batch.expiryDate)}</td>
                    <td>{batch.quantity}</td>
                    <td>{batch.remainingQuantity}</td>
                    <td>{formatMoney(batch.importPrice)}</td>
                    <td>{batch.supplierName || "--"}</td>
                    <td><StatusBadge status={getBatchExpiryStatus(batch)} /></td>
                    <td>
                      <div className="d-flex gap-2">
                        <Button type="button" size="sm" variant="outline-primary" onClick={() => openEditForm(batch)}>
                          Sửa
                        </Button>
                        <Button
                          type="button"
                          size="sm"
                          variant="outline-danger"
                          disabled={actionId === batch.id}
                          onClick={() => handleDeleteBatch(batch)}
                        >
                          Xóa
                        </Button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </Table>
          )}
          <PharmacistPagination page={page} pageSize={PAGE_SIZE} itemCount={batches.length} onPageChange={setPage} />
        </Card.Body>
      </Card>

      <Modal show={showImport} onHide={closeBatchForm} size="lg" centered>
        <Form onSubmit={handleSaveBatch}>
          <Modal.Header closeButton={!saving}><Modal.Title>{editingBatch ? "Cập nhật lô thuốc" : "Nhập lô thuốc"}</Modal.Title></Modal.Header>
          <Modal.Body>
            <Row className="g-3">
              <Col md={6}>
                <Form.Group>
                  <Form.Label>Thuốc</Form.Label>
                  <Form.Select value={form.medicineId} onChange={(e) => updateForm("medicineId", e.target.value)} disabled={saving}>
                    <option value="">Chọn thuốc</option>
                    {medicines.map((medicine) => (
                      <option value={medicine.id} key={medicine.id}>{medicine.name}</option>
                    ))}
                  </Form.Select>
                </Form.Group>
              </Col>
              <Col md={6}><Form.Group><Form.Label>Mã lô</Form.Label><Form.Control value={form.batchCode} onChange={(e) => updateForm("batchCode", e.target.value)} disabled={saving} /></Form.Group></Col>
              <Col md={6}><Form.Group><Form.Label>Ngày nhập</Form.Label><Form.Control type="date" value={form.importDate} onChange={(e) => updateForm("importDate", e.target.value)} disabled={saving} /></Form.Group></Col>
              <Col md={6}><Form.Group><Form.Label>Hạn sử dụng</Form.Label><Form.Control type="date" value={form.expiryDate} onChange={(e) => updateForm("expiryDate", e.target.value)} disabled={saving} /></Form.Group></Col>
              <Col md={6}><Form.Group><Form.Label>Số lượng</Form.Label><Form.Control type="number" min="1" value={form.quantity} onChange={(e) => updateForm("quantity", e.target.value)} disabled={saving} /></Form.Group></Col>
              <Col md={6}><Form.Group><Form.Label>Giá nhập</Form.Label><Form.Control type="number" min="0" value={form.importPrice} onChange={(e) => updateForm("importPrice", e.target.value)} disabled={saving} /></Form.Group></Col>
              <Col xs={12}><Form.Group><Form.Label>Nhà cung cấp</Form.Label><Form.Control value={form.supplierName} onChange={(e) => updateForm("supplierName", e.target.value)} disabled={saving} /></Form.Group></Col>
            </Row>
          </Modal.Body>
          <Modal.Footer>
            <Button type="button" variant="outline-secondary" onClick={closeBatchForm} disabled={saving}>Hủy</Button>
            <Button type="submit" disabled={saving}>{saving ? "Đang lưu..." : editingBatch ? "Cập nhật" : "Nhập kho"}</Button>
          </Modal.Footer>
        </Form>
      </Modal>
    </>
  );
}

export default PharmacistBatchesPage;
