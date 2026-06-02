export const emptyRecordForm = {
  chiefComplaint: "",
  diagnosis: "",
  treatmentPlan: "",
  doctorNote: "",
};

export const emptyServiceForm = {
  serviceId: "",
  serviceCode: "",
  serviceName: "",
  serviceType: "",
  unitPrice: null,
  resultSummary: "",
};

export const ORDERABLE_SERVICE_TYPES = new Set(["TEST", "LAB_TEST", "IMAGING"]);

export const isOrderableMedicalService = (service) => {
  const serviceType = String(service?.serviceType || "").trim().toUpperCase();
  return ORDERABLE_SERVICE_TYPES.has(serviceType);
};

export const unwrapList = (data) => {
  if (Array.isArray(data)) return data;
  if (Array.isArray(data?.content)) return data.content;
  if (Array.isArray(data?.data)) return data.data;
  if (Array.isArray(data?.items)) return data.items;
  if (Array.isArray(data?.results)) return data.results;
  return [];
};

export const hasKnownAvailableQuantity = (item) =>
  item?.availableQuantity !== null
  && item?.availableQuantity !== undefined
  && item?.availableQuantity !== "";

export const emptyPrescriptionLine = {
  quantity: 1,
  dosage: "",
  frequency: "",
  duration: "",
  instruction: "",
};

export const prescriptionPresets = {
  dosage: ["1 viên/lần", "2 viên/lần", "5ml/lần", "10ml/lần", "1 gói/lần"],
  frequency: ["Ngày 1 lần", "Ngày 2 lần", "Ngày 3 lần", "Mỗi 8 giờ", "Khi đau/sốt"],
  duration: ["3 ngày", "5 ngày", "7 ngày", "10 ngày", "14 ngày"],
  instruction: ["Uống sau ăn", "Uống trước ăn", "Uống nhiều nước", "Không tự ý ngưng thuốc", "Tái khám nếu không đỡ"],
};

export const createPrescriptionDraft = ({
  current,
  medicalRecord,
  patientName,
  overrides = {},
}) => ({
  id: current?.id,
  prescriptionCode: current?.prescriptionCode,
  prescribedAt: current?.prescribedAt,
  status: current?.status || "PRESCRIBED",
  note: current?.note || "",
  medicalRecordId: medicalRecord?.id,
  patientName,
  items: current?.items || [],
  ...overrides,
});

export const getPrescriptionStockWarnings = (items = []) =>
  items.filter((item) => {
    if (!hasKnownAvailableQuantity(item)) {
      return false;
    }

    const availableQuantity = Number(item.availableQuantity);
    return Number.isFinite(availableQuantity) && Number(item.quantity || 0) > availableQuantity;
  });
