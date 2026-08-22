package com.evercare.services;

import com.evercare.dtos.request.MedicineBatchImportRequest;
import com.evercare.dtos.response.MedicineBatchImportResponse;
import com.evercare.dtos.response.MedicineBatchResponse;
import java.util.List;
import java.util.Map;

public interface MedicineBatchService {
    MedicineBatchImportResponse importBatch(String username, MedicineBatchImportRequest request);

    List<MedicineBatchResponse> getBatches(Map<String, String> params);

    List<MedicineBatchResponse> getBatchesByMedicineId(Long medicineId);

    MedicineBatchResponse getBatchById(Long id);

    List<MedicineBatchResponse> getNearExpiryBatches(Integer days);

    List<MedicineBatchResponse> getExpiredBatches();

    MedicineBatchResponse updateBatch(Long id, MedicineBatchImportRequest request);

    void deleteBatch(Long id);
}
