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

  if (Array.isArray(response.data)) {
    return response.data;
  }

  return [];
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
