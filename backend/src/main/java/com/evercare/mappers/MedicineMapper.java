package com.evercare.mappers;

import com.evercare.dtos.request.MedicineRequest;
import com.evercare.dtos.response.MedicineResponse;
import com.evercare.pojo.Medicine;

public final class MedicineMapper {
    private MedicineMapper() {
    }

    public static MedicineResponse toResponse(Medicine medicine) {
        MedicineResponse res = new MedicineResponse();
        res.setId(medicine.getId());
        res.setMedicineCode(medicine.getMedicineCode());
        res.setName(medicine.getName());
        res.setUnit(medicine.getUnit());
        res.setDescription(medicine.getDescription());
        res.setUsageNote(medicine.getUsageNote());
        res.setUnitPrice(medicine.getUnitPrice());
        res.setMinStockQuantity(medicine.getMinStockQuantity());
        res.setActive(medicine.getActive());

        return res;
    }

    public static void updateEntity(Medicine medicine, MedicineRequest req) {
        medicine.setMedicineCode(req.getMedicineCode());
        medicine.setName(req.getName());
        medicine.setUnit(req.getUnit());
        medicine.setDescription(req.getDescription());
        medicine.setUsageNote(req.getUsageNote());
        medicine.setUnitPrice(req.getUnitPrice());
        medicine.setMinStockQuantity(req.getMinStockQuantity());
    }
}
