// TODO replace derived stock check when backend stock-check API is available.
export const buildStockCheckFromPrescription = (prescription) => {
  const items = prescription?.items || [];

  return {
    prescriptionId: prescription?.id,
    items: items.map((item) => {
      const requiredQuantity = Number(item.quantity || 0);
      const availableQuantity = Number(item.availableQuantity);
      const hasStockData = Number.isFinite(availableQuantity);

      return {
        medicineId: item.medicineId,
        medicineCode: item.medicineCode,
        medicineName: item.medicineName,
        requiredQuantity,
        availableQuantity: hasStockData ? availableQuantity : null,
        enoughStock: hasStockData ? availableQuantity >= requiredQuantity : false,
        batches: [],
      };
    }),
  };
};
