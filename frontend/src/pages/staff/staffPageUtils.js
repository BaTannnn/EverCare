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

export const formatDateTime = (value) => {
  if (!value) {
    return "--";
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  }).format(date);
};
