export const APPOINTMENT_STATUSES = [
  "ALL",
  "BOOKED",
  "WAITING",
  "IN_PROGRESS",
  "COMPLETED",
  "CANCELLED",
  "NO_SHOW",
];

export const statusFilterLabels = {
  ALL: "Tất cả",
  BOOKED: "Đã đặt lịch",
  WAITING: "Đang chờ",
  IN_PROGRESS: "Đang khám",
  COMPLETED: "Đã khám xong",
  CANCELLED: "Đã hủy",
  NO_SHOW: "Không đến",
};

export const editableStatuses = ["IN_PROGRESS"];

export const canStartExamination = (status) => ["BOOKED", "WAITING"].includes(status);

export const canEnterExamination = (status) => ["IN_PROGRESS", "COMPLETED"].includes(status);

export const getErrorMessage = (error) => {
  const data = error.response?.data;

  if (error.response?.status === 401) {
    return "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.";
  }

  if (typeof data === "string") {
    return data;
  }

  return data?.message || error.message || "Đã có lỗi xảy ra.";
};

export const todayInputValue = () => new Date().toISOString().slice(0, 10);

export const formatTime = (value) => {
  if (!value) {
    return "--";
  }

  return String(value).slice(0, 5);
};

export const formatDate = (value) => {
  if (!value) {
    return "--";
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat("vi-VN").format(date);
};

export const formatMoney = (value) => {
  const amount = Number(value || 0);

  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0,
  }).format(amount);
};

export const normalizeText = (value) => String(value || "").trim().toLowerCase();

export const appointmentSearchText = (appointment) =>
  normalizeText(
    [
      appointment?.appointmentCode,
      appointment?.patient?.fullName,
      appointment?.patient?.phone,
      appointment?.reason,
      appointment?.service?.name,
    ].join(" ")
  );
