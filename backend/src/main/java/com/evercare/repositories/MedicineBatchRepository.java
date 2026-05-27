package com.evercare.repositories;

import com.evercare.pojo.MedicineBatch;
import java.util.List;
import java.util.Map;

public interface MedicineBatchRepository {
    List<MedicineBatch> getBatches(Map<String, String> params);

    List<MedicineBatch> getBatchesByMedicineId(Long medicineId);

    boolean existsByBatchCode(String batchCode);

    void addBatch(MedicineBatch batch);
}
