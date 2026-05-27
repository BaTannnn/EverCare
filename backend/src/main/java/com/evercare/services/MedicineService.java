package com.evercare.services;

import com.evercare.dtos.request.MedicineRequest;
import com.evercare.dtos.response.MedicineLowStockResponse;
import com.evercare.dtos.response.MedicineResponse;
import java.util.List;
import java.util.Map;

public interface MedicineService {
    List<MedicineResponse> getMedicines(Map<String, String> params);

    List<MedicineLowStockResponse> getLowStockMedicines();

    MedicineResponse getMedicineById(Long id);

    MedicineResponse createMedicine(MedicineRequest request);

    MedicineResponse updateMedicine(Long id, MedicineRequest request);

    MedicineResponse updateStatus(Long id, Boolean active);

    void softDelete(Long id);
}
