import { makeAvatarDataUri } from "../../data/patientMockData";

export const formatCurrency = (value) => new Intl.NumberFormat("vi-VN").format(value || 0) + "đ";

export const formatShortDate = (value) => {
  if (!value) return "";

  if (typeof value !== "string") return String(value);

  const isoMatch = value.match(/^(\d{4})-(\d{2})-(\d{2})/);
  if (isoMatch) {
    const [, year, month, day] = isoMatch;
    return `${day}/${month}/${year}`;
  }

  return value;
};

export const patientStatusMeta = {
  CONFIRMED: { label: "Đã xác nhận", variant: "primary" },
  PENDING: { label: "Chờ xác nhận", variant: "warning" },
  COMPLETED: { label: "Hoàn thành", variant: "success" },
  CANCELLED: { label: "Đã hủy", variant: "secondary" },
  STABLE: { label: "Ổn định", variant: "success" },
  MONITOR: { label: "Theo dõi", variant: "info" },
  FOLLOW_UP: { label: "Cần tái khám", variant: "warning" },
  COMPLETE: { label: "Hoàn tất", variant: "success" },
  NORMAL: { label: "Bình thường", variant: "success" },
  ATTENTION: { label: "Cần lưu ý", variant: "danger" },
  PRESCRIBED: { label: "Đã kê đơn", variant: "primary" },
  DISPENSED: { label: "Đã cấp phát", variant: "success" },
  PAID: { label: "Đã thanh toán", variant: "success" },
  UNPAID: { label: "Chưa thanh toán", variant: "danger" },
  READ: { label: "Đã đọc", variant: "secondary" },
  UNREAD: { label: "Chưa đọc", variant: "warning" },
  APPOINTMENT: { label: "Lịch hẹn", variant: "primary" },
  PRESCRIPTION: { label: "Đơn thuốc", variant: "success" },
  PAYMENT: { label: "Thanh toán", variant: "danger" },
  TEST: { label: "Xét nghiệm", variant: "info" },
  SYSTEM: { label: "Hệ thống", variant: "secondary" },
};

export const getPatientStatusMeta = (status, fallbackLabel) => {
  return patientStatusMeta[status] || { label: fallbackLabel || status || "Không rõ", variant: "secondary" };
};

export const countByStatus = (items = [], status) => items.filter((item) => item.status === status).length;

export const getAvatarSource = (profile, fallbackName = "EverCare") => {
  const avatarUrl = profile?.avatarUrl || profile?.avatar || profile?.photoUrl || profile?.imageUrl || profile?.profilePicture || profile?.avatarPath;
  return avatarUrl || makeAvatarDataUri(profile?.fullName || fallbackName);
};

export const filterByCategory = (items = [], category) => {
  if (!category || category === "ALL") return items;

  return items.filter((item) => item.category === category || item.type === category);
};
