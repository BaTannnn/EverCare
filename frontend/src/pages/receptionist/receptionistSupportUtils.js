export const supportStatusMeta = {
  OPEN: { label: "Chờ tiếp nhận", variant: "warning" },
  ASSIGNED: { label: "Đã tiếp nhận", variant: "primary" },
  IN_PROGRESS: { label: "Đang hỗ trợ", variant: "info" },
  CLOSED: { label: "Đã đóng", variant: "secondary" },
};

export const CONVERSATIONS_PAGE_SIZE = 6;
export const MESSAGE_INITIAL_LIMIT = 50;
export const MESSAGE_POLL_LIMIT = 50;
export const MESSAGE_POLL_VISIBLE_MS = 3000;
export const MESSAGE_POLL_HIDDEN_MS = 30000;
export const CONVERSATION_POLL_VISIBLE_MS = 12000;
export const CONVERSATION_POLL_HIDDEN_MS = 60000;

export const defaultSupportSchedule = () => ({
  doctorId: "",
  consultationDate: "",
  startTime: "",
  endTime: "",
  meetLink: "",
  note: "",
});

export const formatSupportDateTime = (value) => {
  if (!value) return "--";
  const date = new Date(String(value).replace(" ", "T"));
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat("vi-VN", { dateStyle: "short", timeStyle: "short" }).format(date);
};

export const pickConversationId = (items, preferredId, fallbackId) => {
  const hasId = (id) => id && items.some((item) => String(item.id) === String(id));
  if (hasId(preferredId)) return preferredId;
  if (hasId(fallbackId)) return fallbackId;
  return items[0]?.id || null;
};

export const getPollDelay = (visibleMs, hiddenMs) =>
  document.visibilityState === "hidden" ? hiddenMs : visibleMs;

export const getLatestMessageId = (items) => items.reduce((maxId, item) => {
  const id = Number(item?.id);
  return Number.isFinite(id) && id > maxId ? id : maxId;
}, 0);

export const mergeMessagesById = (current, incoming) => {
  const byId = new Map(current.map((item) => [String(item.id), item]));
  incoming.forEach((item) => {
    if (item?.id) byId.set(String(item.id), item);
  });
  return Array.from(byId.values()).sort((first, second) => Number(first.id) - Number(second.id));
};
