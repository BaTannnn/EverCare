import { Badge } from "react-bootstrap";

const STATUS_META = {
  BOOKED: { label: "Đã đặt lịch", bg: "primary" },
  WAITING: { label: "Đang chờ", bg: "warning" },
  IN_PROGRESS: { label: "Đang khám", bg: "info" },
  COMPLETED: { label: "Đã khám xong", bg: "success" },
  CANCELLED: { label: "Đã hủy", bg: "danger" },
  NO_SHOW: { label: "Không đến", bg: "secondary" },
  PRESCRIBED: { label: "Chờ cấp phát", bg: "primary" },
  DISPENSED: { label: "Đã cấp phát", bg: "success" },
  UNPAID: { label: "Chưa thanh toán", bg: "danger" },
  PAID: { label: "Đã thanh toán", bg: "success" },
  PARTIALLY_PAID: { label: "Thanh toán một phần", bg: "warning" },
  REFUNDED: { label: "Đã hoàn tiền", bg: "secondary" },
};

function StatusBadge({ status, label }) {
  const meta = STATUS_META[status] || { label: label || status || "Không rõ", bg: "secondary" };

  return (
    <Badge bg={meta.bg} className="status-badge">
      {label || meta.label}
    </Badge>
  );
}

export default StatusBadge;
