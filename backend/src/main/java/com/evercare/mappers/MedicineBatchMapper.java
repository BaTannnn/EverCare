package com.evercare.mappers;

import com.evercare.dtos.response.MedicineBatchResponse;
import com.evercare.pojo.Medicine;
import com.evercare.pojo.MedicineBatch;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class MedicineBatchMapper {
    private static final String DATE_PATTERN = "yyyy-MM-dd";

    private MedicineBatchMapper() {
    }

    public static MedicineBatchResponse toResponse(MedicineBatch batch) {
        MedicineBatchResponse res = new MedicineBatchResponse();
        Medicine medicine = batch.getMedicineId();

        res.setId(batch.getId());
        res.setMedicineId(medicine != null ? medicine.getId() : null);
        res.setMedicineCode(medicine != null ? medicine.getMedicineCode() : null);
        res.setMedicineName(medicine != null ? medicine.getName() : null);
        res.setBatchCode(batch.getBatchCode());
        res.setImportDate(format(batch.getImportDate()));
        res.setExpiryDate(format(batch.getExpiryDate()));
        res.setQuantity(batch.getQuantity());
        res.setRemainingQuantity(batch.getRemainingQuantity());
        res.setImportPrice(batch.getImportPrice());
        res.setSupplierName(batch.getSupplierName());
        res.setActive(batch.getActive());

        return res;
    }

    private static String format(Date value) {
        if (value == null) {
            return null;
        }

        return new SimpleDateFormat(DATE_PATTERN).format(value);
    }
}
