package com.evercare.repositories;

import com.evercare.pojo.Invoice;

public interface InvoiceRepository {
    Invoice getInvoiceByMedicalRecordId(Long medicalRecordId);

    void updateInvoice(Invoice invoice);
}
