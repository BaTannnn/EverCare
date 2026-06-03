import { authApis } from "../../configs/Apis";

export const unwrapPatientData = (response) => {
  if (!response) return response;

  if (Array.isArray(response.data)) {
    return response.data;
  }

  return response.data?.data ?? response.data?.content ?? response.data?.result ?? response.data;
};

export const unwrapPatientList = (response) => {
  if (!response) return [];

  const candidate = response.data?.data ?? response.data?.content ?? response.data?.result ?? response.data;
  if (Array.isArray(candidate)) {
    return candidate;
  }

  if (Array.isArray(candidate?.items)) {
    return candidate.items;
  }

  if (Array.isArray(candidate?.content)) {
    return candidate.content;
  }

  if (Array.isArray(candidate?.data)) {
    return candidate.data;
  }

  if (Array.isArray(response.data)) {
    return response.data;
  }

  return [];
};

export const unwrapPatientPage = (response) => {
  if (!response) {
    return { items: [], pageInfo: null };
  }

  const data = response.data?.data ?? response.data?.content ?? response.data?.result ?? response.data;

  if (Array.isArray(data)) {
    return { items: data, pageInfo: null };
  }

  if (data && typeof data === "object") {
    const items = Array.isArray(data.content) ? data.content : Array.isArray(data.items) ? data.items : [];
    return {
      items,
      pageInfo: {
        page: Number(data.page ?? data.number ?? data.currentPage ?? 1),
        size: Number(data.size ?? data.pageSize ?? (items.length || 10)),
        totalPages: Number(data.totalPages ?? data.total_page ?? data.pages ?? 1),
        totalElements: Number(data.totalElements ?? data.total ?? items.length),
        hasNext: Boolean(data.hasNext ?? data.hasNextPage ?? false),
        hasPrevious: Boolean(data.hasPrevious ?? data.hasPreviousPage ?? false),
      },
    };
  }

  return { items: [], pageInfo: null };
};

export const resolvePatientResponse = async (requestPromise, fallbackData, label) => {
  try {
    const response = await requestPromise;
    return { ...response, data: unwrapPatientData(response) };
  } catch (error) {
    if (typeof fallbackData !== "undefined") {
      if (label) {
        console.info(`[patient todo] ${label} API unavailable, using fallback data.`);
      }

      return { data: fallbackData };
    }

    throw error;
  }
};

export const createPatientGet = (endpoint, fallbackData, label) => {
  return resolvePatientResponse(authApis().get(endpoint), fallbackData, label);
};

export const createPatientPost = (endpoint, payload, fallbackData, label) => {
  return resolvePatientResponse(authApis().post(endpoint, payload), fallbackData, label);
};
