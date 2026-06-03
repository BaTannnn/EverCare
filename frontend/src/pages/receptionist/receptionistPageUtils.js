export const formatCurrency = (value) => new Intl.NumberFormat("vi-VN", {
  style: "currency",
  currency: "VND",
  maximumFractionDigits: 0,
}).format(Number(value || 0));

export const formatDate = (value) => {
  if (!value) return "--";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat("vi-VN").format(date);
};

export const formatDateTime = (value) => {
  if (!value) return "--";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat("vi-VN", { dateStyle: "short", timeStyle: "short" }).format(date);
};

export const formatTime = (value) => (value ? String(value).slice(0, 5) : "--");

export const todayInputValue = () => new Date().toISOString().slice(0, 10);

export const getErrorMessage = (error) => {
  const status = error.response?.status;
  const data = error.response?.data;

  if (status === 401) return "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.";
  if (status === 403) return "Bạn không có quyền truy cập khu vực lễ tân.";
  if (status === 404) return "Không tìm thấy dữ liệu.";
  if (status === 409) return typeof data === "string" ? data : data?.message || "Dữ liệu đang ở trạng thái không hợp lệ.";
  if (status === 400) return typeof data === "string" ? data : data?.message || "Dữ liệu nhập vào không hợp lệ.";
  if (typeof data === "string") return data;

  return data?.message || error.message || "Đã có lỗi xảy ra.";
};

export const appointmentStatusMeta = (status) => {
  const normalized = String(status || "BOOKED").toUpperCase();
  const map = {
    BOOKED: { label: "Đã đặt lịch", variant: "primary" },
    WAITING: { label: "Đang chờ", variant: "warning" },
    IN_PROGRESS: { label: "Đang khám", variant: "info" },
    COMPLETED: { label: "Đã khám xong", variant: "success" },
    CANCELLED: { label: "Đã hủy", variant: "secondary" },
    NO_SHOW: { label: "Không đến", variant: "danger" },
  };
  return map[normalized] || { label: normalized, variant: "secondary" };
};

export const invoiceStatusMeta = (status) => {
  const normalized = String(status || "UNPAID").toUpperCase();
  const map = {
    UNPAID: { label: "Chưa thanh toán", variant: "danger" },
    PAID: { label: "Đã thanh toán", variant: "success" },
    REFUNDED: { label: "Đã hoàn tiền", variant: "secondary" },
    PENDING: { label: "Đang xử lý", variant: "warning" },
  };
  return map[normalized] || { label: normalized, variant: "secondary" };
};

export const getMethodLabel = (method) => {
  const map = {
    CASH: "Tiền mặt",
    BANK_TRANSFER: "Chuyển khoản",
    VIETQR: "VIETQR",
    MOMO: "MoMo",
    ZALOPAY: "ZaloPay",
    VNPAY: "VNPay",
  };
  return map[String(method || "").toUpperCase()] || method || "--";
};

export const appointmentSearchText = (appointment) =>
  String([
    appointment?.appointmentCode,
    appointment?.patient?.phone,
    appointment?.patient?.fullName,
    appointment?.patient?.citizenId,
  ].join(" ")).toLowerCase();

export const invoiceSearchText = (invoice) =>
  String([
    invoice?.invoiceCode,
    invoice?.patientCode,
    invoice?.patientName,
    invoice?.patientPhone,
    invoice?.medicalRecordCode,
  ].join(" ")).toLowerCase();
