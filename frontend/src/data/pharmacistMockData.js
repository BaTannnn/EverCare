// TODO replace mock when backend inventory transaction API is available.
export const mockInventoryTransactions = [
  {
    id: "MOCK-INV-001",
    transactionCode: "INV-MOCK-001",
    transactionType: "IMPORT",
    medicineName: "Paracetamol 500mg",
    batchCode: "BATCH-MOCK-01",
    quantity: 100,
    transactionDate: "2026-05-28 08:00:00",
    performedByName: "Dược sĩ",
    prescriptionId: null,
  },
];

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
