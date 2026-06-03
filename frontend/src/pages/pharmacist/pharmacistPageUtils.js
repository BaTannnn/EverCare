export const DEV_MODE_ALLOW_DISPENSE_WITHOUT_PAYMENT = false;

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

export const formatDateTime = (value) => {
  if (!value) {
    return "--";
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat("vi-VN", {
    dateStyle: "short",
    timeStyle: "short",
  }).format(date);
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

export const getPrescriptionStockStatus = (prescription) => {
  const items = prescription?.items || [];

  if (items.length === 0) {
    return "OUT";
  }

  if (items.some((item) => item.enoughStock === false)) {
    return "OUT";
  }

  if (items.some((item) => {
    const available = Number(item.availableQuantity);
    const quantity = Number(item.quantity || 0);
    return Number.isFinite(available) && available <= quantity;
  })) {
    return "LOW";
  }

  return "ENOUGH";
};

export const getBatchExpiryStatus = (batch, nearDays = 30) => {
  if (!batch?.expiryDate) {
    return "ENOUGH";
  }

  const expiry = new Date(batch.expiryDate);
  if (Number.isNaN(expiry.getTime())) {
    return "ENOUGH";
  }

  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const diffDays = Math.ceil((expiry.getTime() - today.getTime()) / 86400000);

  if (diffDays < 0) {
    return "EXPIRED";
  }

  if (diffDays <= nearDays) {
    return "NEAR_EXPIRY";
  }

  return "ENOUGH";
};
