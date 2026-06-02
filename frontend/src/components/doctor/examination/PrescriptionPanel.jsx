import { Alert, Badge, Button, Card, Form, Table } from "react-bootstrap";
import EmptyState from "../../common/EmptyState";
import { formatDate, formatMoney } from "../../../pages/doctor/doctorPageUtils";
import { hasKnownAvailableQuantity, prescriptionPresets } from "../../../pages/doctor/examinationWorkspaceUtils";

function PrescriptionPanel({
  activePresetField,
  editable,
  medicineDropdownOpen,
  medicineKeyword,
  medicineLoading,
  medicineResults,
  medicineSearchError,
  prescription,
  prescriptionDispensed,
  prescriptionEditable,
  prescriptionStatusLabel,
  prescriptionStatusVariant,
  prescriptionStockWarnings,
  savingPrescription,
  onAddMedicine,
  onApplyPreset,
  onMedicineKeywordChange,
  onMedicineSearchBlur,
  onMedicineSearchFocus,
  onRemoveItem,
  onSavePrescription,
  onSetActivePresetField,
  onUpdateItem,
  onUpdateNote,
}) {
  return (
    <Card className="doctor-card exam-section-card">
      <Card.Header>
        <div>
          <h2>Đơn thuốc</h2>
          <div className="prescription-header-meta">
            {prescription?.prescriptionCode && <span>{prescription.prescriptionCode}</span>}
            {prescription?.prescribedAt && <span>{formatDate(prescription.prescribedAt)}</span>}
          </div>
        </div>
        <Badge bg={prescriptionStatusVariant}>{prescriptionStatusLabel}</Badge>
      </Card.Header>
      <Card.Body>
        <Form onSubmit={onSavePrescription}>
          {!editable && (
            <Alert variant="info">
              Lịch khám không ở trạng thái đang khám, đơn thuốc chỉ được xem nếu đã có.
            </Alert>
          )}
          {prescriptionDispensed && (
            <Alert variant="warning">
              Đơn thuốc đã được dược sĩ cấp phát, không thể chỉnh sửa.
            </Alert>
          )}
          {prescriptionStockWarnings.length > 0 && (
            <Alert variant="warning">
              Số lượng kê vượt tồn khả dụng.
            </Alert>
          )}

          {prescriptionEditable && (
            <Card className="prescription-subcard">
              <Card.Header>
                <h3>Tìm thuốc</h3>
              </Card.Header>
              <Card.Body>
                <Form.Group controlId="medicineSearch" className="mb-0">
                  <Form.Label>Tìm thuốc</Form.Label>
                  <div className="medicine-autocomplete">
                    <Form.Control
                      value={medicineKeyword}
                      onFocus={onMedicineSearchFocus}
                      onBlur={onMedicineSearchBlur}
                      onChange={(e) => onMedicineKeywordChange(e.target.value)}
                      placeholder="Nhập tên thuốc"
                      disabled={savingPrescription}
                    />
                    {medicineDropdownOpen && !savingPrescription && medicineKeyword.trim() && (
                      <div className="medicine-autocomplete-menu">
                        {medicineLoading ? (
                          <div className="medicine-autocomplete-empty">Đang tìm thuốc...</div>
                        ) : medicineSearchError ? (
                          <div className="medicine-autocomplete-empty is-error">{medicineSearchError}</div>
                        ) : medicineResults.length === 0 ? (
                          <div className="medicine-autocomplete-empty">Không tìm thấy thuốc</div>
                        ) : (
                          medicineResults.map((medicine) => {
                            const medicineId = medicine.medicineId ?? medicine.id;
                            const medicineName = medicine.medicineName ?? medicine.name;
                            const unitPrice = medicine.unitPrice ?? medicine.price;

                            return (
                              <button
                                type="button"
                                className="medicine-autocomplete-item"
                                key={medicineId}
                                onMouseDown={(e) => e.preventDefault()}
                                onClick={() => onAddMedicine(medicine)}
                              >
                                <strong>{medicineName}</strong>
                                <span>
                                  {medicine.unit || "--"} · {formatMoney(unitPrice)} · Tồn {medicine.availableQuantity ?? "--"}
                                </span>
                              </button>
                            );
                          })
                        )}
                      </div>
                    )}
                  </div>
                </Form.Group>
              </Card.Body>
            </Card>
          )}

          <Card className="prescription-subcard">
            <Card.Header>
              <h3>Danh sách thuốc trong đơn</h3>
            </Card.Header>
            <Card.Body className="p-0 prescription-table-body">
              {(prescription?.items || []).length === 0 ? (
                <EmptyState title="Chưa có thuốc trong đơn" description="Tìm thuốc và thêm vào đơn kê." />
              ) : (
                <Table responsive className="doctor-table mb-0">
                  <thead>
                    <tr>
                      <th>Thuốc</th>
                      <th>Số lượng</th>
                      <th>Liều dùng</th>
                      <th>Tần suất</th>
                      <th>Thời gian dùng</th>
                      <th>Hướng dẫn</th>
                      <th>Tồn khả dụng</th>
                      {prescriptionEditable && <th>Thao tác</th>}
                    </tr>
                  </thead>
                  <tbody>
                    {(prescription?.items || []).map((item, index) => {
                      const overStock = hasKnownAvailableQuantity(item)
                        && Number.isFinite(Number(item.availableQuantity))
                        && Number(item.quantity || 0) > Number(item.availableQuantity);

                      return (
                        <tr key={`${item.medicineId}-${index}`}>
                          <td>
                            <strong>{item.medicineName}</strong>
                            <span className="muted-cell">{item.unit || "--"} · {formatMoney(item.unitPrice)}</span>
                          </td>
                          {[
                            ["quantity", "Số lượng", "number"],
                            ["dosage", "Liều dùng", "text"],
                            ["frequency", "Tần suất", "text"],
                            ["duration", "Thời gian dùng", "text"],
                            ["instruction", "Hướng dẫn", "text"],
                          ].map(([field, label, type]) => (
                            <td key={field}>
                              <div
                                className="prescription-preset-field"
                                onBlur={(e) => {
                                  if (!e.currentTarget.contains(e.relatedTarget)) {
                                    onSetActivePresetField(null);
                                  }
                                }}
                              >
                                <Form.Control
                                  type={type}
                                  min={field === "quantity" ? "1" : undefined}
                                  value={item[field] || ""}
                                  onFocus={() => onSetActivePresetField(`${index}-${field}`)}
                                  onChange={(e) => onUpdateItem(index, field, e.target.value)}
                                  disabled={!prescriptionEditable || savingPrescription}
                                  aria-label={label}
                                  isInvalid={field === "quantity" && overStock}
                                />
                                {prescriptionPresets[field] && activePresetField === `${index}-${field}` && prescriptionEditable && (
                                  <div className="prescription-preset-menu">
                                    {prescriptionPresets[field].map((preset) => (
                                      <button
                                        type="button"
                                        key={preset}
                                        onMouseDown={(e) => {
                                          e.preventDefault();
                                          onApplyPreset(index, field, preset);
                                        }}
                                        disabled={savingPrescription}
                                      >
                                        {preset}
                                      </button>
                                    ))}
                                  </div>
                                )}
                              </div>
                            </td>
                          ))}
                          <td className={overStock ? "text-danger fw-bold" : ""}>
                            {item.availableQuantity ?? "--"}
                          </td>
                          {prescriptionEditable && (
                            <td>
                              <Button
                                type="button"
                                size="sm"
                                variant="outline-danger"
                                onClick={() => onRemoveItem(index)}
                                disabled={savingPrescription}
                              >
                                Xóa
                              </Button>
                            </td>
                          )}
                        </tr>
                      );
                    })}
                  </tbody>
                </Table>
              )}
            </Card.Body>
          </Card>

          <Form.Group controlId="prescriptionNote" className="prescription-note-field">
            <Form.Label>Ghi chú đơn thuốc</Form.Label>
            <Form.Control
              as="textarea"
              rows={2}
              value={prescription?.note || ""}
              onChange={(e) => onUpdateNote(e.target.value)}
              placeholder="Dặn dò chung cho cả đơn thuốc nếu cần..."
              disabled={!prescriptionEditable || savingPrescription}
            />
          </Form.Group>

          {prescriptionEditable && (
            <Button type="submit" disabled={savingPrescription}>
              {savingPrescription ? "Đang lưu..." : prescription?.id ? "Cập nhật đơn thuốc" : "Lưu đơn thuốc"}
            </Button>
          )}
        </Form>
      </Card.Body>
    </Card>
  );
}

export default PrescriptionPanel;
