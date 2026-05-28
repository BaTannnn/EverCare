import { Badge } from "react-bootstrap";

const STATUS_META = {
  PENDING: { label: "Chờ xác nhận", bg: "warning" },
  CONFIRMED: { label: "Đã xác nhận", bg: "primary" },
  NORMAL: { label: "Bình thường", bg: "success" },
  ATTENTION: { label: "Cần lưu ý", bg: "danger" },
  READ: { label: "Đã đọc", bg: "secondary" },
  UNREAD: { label: "Chưa đọc", bg: "warning" },
  PAID: { label: "Đã thanh toán", bg: "success" },
  UNPAID: { label: "Chưa thanh toán", bg: "danger" },
  BOOKED: { label: "Đã đặt lịch", bg: "primary" },
  WAITING: { label: "Đang chờ", bg: "warning" },
  IN_PROGRESS: { label: "Đang khám", bg: "info" },
  COMPLETED: { label: "Đã khám xong", bg: "success" },
  CANCELLED: { label: "Đã hủy", bg: "danger" },
  NO_SHOW: { label: "Không đến", bg: "secondary" },
  PRESCRIBED: { label: "Chờ cấp phát", bg: "primary" },
  DISPENSED: { label: "Đã cấp phát", bg: "success" },
  PARTIALLY_PAID: { label: "Thanh toán một phần", bg: "warning" },
  REFUNDED: { label: "Đã hoàn tiền", bg: "secondary" },
  ENOUGH: { label: "Đủ tồn", bg: "success" },
  LOW: { label: "Sắp hết", bg: "warning" },
  OUT: { label: "Hết hàng", bg: "danger" },
  EXPIRED: { label: "Hết hạn", bg: "danger" },
  NEAR_EXPIRY: { label: "Gần hết hạn", bg: "warning" },
};

function StatusBadge({ status, label }) {
  const meta = STATUS_META[status] || { label: label || status || "Không rõ", bg: "secondary" };

  return (
    <Badge bg={meta.bg} className="status-badge rounded-pill">
      {label || meta.label}
    </Badge>
  );
}

export default StatusBadge;
