package com.evercare.repositories;

import com.evercare.pojo.Medicine;
import java.util.List;
import java.util.Map;

public interface MedicineRepository {
    List<Medicine> getMedicines(Map<String, String> params);

    List<Object[]> getLowStockMedicines();

    Medicine getMedicineById(Long id);
    List<Medicine> getActiveMedicinesByIds(List<Long> ids);

    Medicine getMedicineByCode(String medicineCode);

    boolean existsByCode(String medicineCode, Long excludeId);

    boolean hasPrescriptionItems(Long medicineId);

    void addMedicine(Medicine medicine);

    void updateMedicine(Medicine medicine);
}
