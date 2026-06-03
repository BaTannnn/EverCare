export const unwrapObject = (value) => {
  if (!value) return {};

  if (value.data && !Array.isArray(value.data) && typeof value.data === "object") {
    return value.data;
  }

  if (value.result && !Array.isArray(value.result) && typeof value.result === "object") {
    return value.result;
  }

  if (value.content && !Array.isArray(value.content) && typeof value.content === "object") {
    return value.content;
  }

  return value;
};

export const unwrapList = (response) => {
  const data = response?.data?.data ?? response?.data?.content ?? response?.data?.result ?? response?.data;

  if (Array.isArray(data)) {
    return data;
  }

  if (Array.isArray(data?.items)) {
    return data.items;
  }

  if (Array.isArray(data?.content)) {
    return data.content;
  }

  if (Array.isArray(data?.data)) {
    return data.data;
  }

  return [];
};

export const unwrapPage = (response) => {
  const items = unwrapList(response);
  const data = response?.data?.data ?? response?.data?.content ?? response?.data?.result ?? response?.data;

  return {
    items,
    pageInfo: data && typeof data === "object" && !Array.isArray(data) ? {
      page: Number(data.page ?? data.number ?? data.currentPage ?? 1),
      size: Number(data.size ?? data.pageSize ?? (items.length || 10)),
      totalPages: Number(data.totalPages ?? data.total_page ?? data.pages ?? (items.length ? 1 : 0)),
      totalElements: Number(data.totalElements ?? data.total ?? items.length),
      hasNext: Boolean(data.hasNext ?? data.hasNextPage ?? false),
      hasPrevious: Boolean(data.hasPrevious ?? data.hasPreviousPage ?? false),
    } : null,
  };
};

export const normalizeText = (value, fallback = "") => {
  if (value === null || value === undefined || value === "") {
    return fallback;
  }

  return value;
};

export const normalizeNumber = (value) => Number(value || 0);
