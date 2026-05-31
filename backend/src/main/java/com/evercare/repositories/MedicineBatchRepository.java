package com.evercare.repositories;

import com.evercare.pojo.MedicineBatch;
import java.util.List;
import java.util.Map;

public interface MedicineBatchRepository {
    List<MedicineBatch> getBatches(Map<String, String> params);

    List<MedicineBatch> getBatchesByMedicineId(Long medicineId);

    Long getAvailableQuantityByMedicineId(Long medicineId);

    Long getAvailableNonExpiredQuantityByMedicineId(Long medicineId, java.util.Date today);

    Map<Long, Long> getAvailableNonExpiredQuantitiesByMedicineIds(List<Long> medicineIds, java.util.Date today);

    List<MedicineBatch> getDispensableBatchesByMedicineId(Long medicineId, java.util.Date today);

    List<MedicineBatch> getNearExpiryBatches(java.util.Date toDate);

    List<MedicineBatch> getExpiredBatches(java.util.Date today);

    boolean existsByBatchCode(String batchCode);

    void addBatch(MedicineBatch batch);

    void updateBatch(MedicineBatch batch);
}
