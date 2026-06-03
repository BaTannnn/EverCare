package com.evercare.repositories;

import com.evercare.pojo.PrescriptionItem;

public interface PrescriptionItemRepository {
    void addItem(PrescriptionItem item);

    void deleteItemsByPrescriptionId(Long prescriptionId);
}
