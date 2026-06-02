import { Button, Card, Form, Table } from "react-bootstrap";
import EmptyState from "../../common/EmptyState";
import { formatMoney } from "../../../pages/doctor/doctorPageUtils";

function MedicalServicesPanel({
  allPendingSelected,
  editable,
  pendingServiceCount,
  savingService,
  selectedPendingCount,
  selectedServiceIds,
  serviceDropdownOpen,
  serviceForm,
  serviceKeyword,
  serviceOptions,
  serviceSearching,
  services,
  onAddService,
  onConfirmServices,
  onRemovePendingService,
  onRemoveSelectedPendingServices,
  onSelectMedicalService,
  onServiceFormChange,
  onServiceKeywordChange,
  onToggleAllPendingServices,
  onToggleServiceSelection,
}) {
  return (
    <Card className="doctor-card exam-section-card">
      <Card.Header>
        <h2>Chỉ định xét nghiệm / Chẩn đoán hình ảnh</h2>
      </Card.Header>
      <Card.Body className="p-0">
        <Form className="exam-inline-form" onSubmit={onAddService}>
          <div className="service-autocomplete">
            <Form.Control
              value={serviceKeyword}
              onFocus={() => onServiceKeywordChange(serviceKeyword, { keepSelection: true })}
              onChange={(e) => onServiceKeywordChange(e.target.value)}
              placeholder="Tìm xét nghiệm hoặc chẩn đoán hình ảnh..."
              disabled={!editable || savingService}
            />
            {serviceDropdownOpen && editable && (
              <div className="service-autocomplete-menu">
                {serviceSearching ? (
                  <div className="service-autocomplete-empty">Đang tìm dịch vụ...</div>
                ) : serviceOptions.length === 0 ? (
                  <div className="service-autocomplete-empty">Không tìm thấy dịch vụ</div>
                ) : (
                  serviceOptions.map((service) => (
                    <button type="button" key={service.id} onClick={() => onSelectMedicalService(service)}>
                      <strong>{service.name}</strong>
                      <span>
                        {service.code || "DV"} · {service.serviceType || "--"} · {formatMoney(service.price)}
                      </span>
                    </button>
                  ))
                )}
              </div>
            )}
          </div>
          <Form.Control
            value={serviceForm.resultSummary}
            onChange={(e) => onServiceFormChange({ resultSummary: e.target.value })}
            placeholder="Ghi chú"
            disabled={!editable || savingService}
          />
          <Button type="submit" disabled={!editable || savingService}>
            Thêm chỉ định
          </Button>
        </Form>

        {pendingServiceCount > 0 && (
          <div className="exam-service-actions">
            <span>
              {pendingServiceCount} dịch vụ chờ xác nhận
              {selectedPendingCount > 0 ? ` · Đã chọn ${selectedPendingCount}` : ""}
            </span>
            <div className="exam-service-action-buttons">
              <Button
                type="button"
                variant="outline-danger"
                onClick={onRemoveSelectedPendingServices}
                disabled={!editable || savingService || selectedPendingCount === 0}
              >
                Xóa đã chọn
              </Button>
              <Button
                type="button"
                onClick={onConfirmServices}
                disabled={!editable || savingService || pendingServiceCount === 0}
              >
                {savingService ? "Đang xác nhận..." : "Xác nhận dịch vụ"}
              </Button>
            </div>
          </div>
        )}

        {services.length === 0 ? (
          <EmptyState title="Chưa có chỉ định" description="Tìm xét nghiệm hoặc chẩn đoán hình ảnh để thêm chỉ định." />
        ) : (
          <Table responsive className="doctor-table exam-table mb-0">
            <thead>
              <tr>
                <th>
                  <Form.Check
                    aria-label="Chọn tất cả dịch vụ chưa xác nhận"
                    checked={allPendingSelected}
                    disabled={!editable || savingService || pendingServiceCount === 0}
                    onChange={(e) => onToggleAllPendingServices(e.target.checked)}
                  />
                </th>
                <th>Trạng thái</th>
                <th>Tên dịch vụ</th>
                <th>Loại</th>
                <th>Giá</th>
                <th>Ghi chú</th>
                <th>Kết quả</th>
                <th>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {services.map((item) => (
                <tr key={item.id}>
                  <td>
                    {item.pending ? (
                      <Form.Check
                        aria-label={`Chọn ${item.serviceName}`}
                        checked={selectedServiceIds.includes(item.id)}
                        disabled={savingService}
                        onChange={(e) => onToggleServiceSelection(item.id, e.target.checked)}
                      />
                    ) : (
                      <span className="service-table-placeholder">--</span>
                    )}
                  </td>
                  <td>
                    <span className={`service-state ${item.pending ? "is-pending" : "is-confirmed"}`}>
                      {item.pending ? "Chưa xác nhận" : "Đã xác nhận"}
                    </span>
                  </td>
                  <td>{item.serviceName}</td>
                  <td>{item.serviceType}</td>
                  <td>{formatMoney(item.unitPrice)}</td>
                  <td>{item.resultSummary || "--"}</td>
                  <td>{(item.testResults || []).length ? "Đã có" : "Chưa có"}</td>
                  <td>
                    {item.pending ? (
                      <Button
                        type="button"
                        variant="outline-danger"
                        size="sm"
                        onClick={() => onRemovePendingService(item.id)}
                        disabled={savingService}
                      >
                        Xóa
                      </Button>
                    ) : (
                      <span className="service-locked-text">Đã khóa</span>
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

export default MedicalServicesPanel;
