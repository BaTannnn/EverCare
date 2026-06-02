package com.evercare.repositories;

import com.evercare.pojo.Invoice;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.List;

public interface InvoiceRepository {
    List<Invoice> getInvoicesForReceptionist(Map<String, String> params);
    long countInvoicesForReceptionist(Map<String, String> params);
    Invoice getInvoiceByMedicalRecordId(Long medicalRecordId);
    List<Invoice> getInvoicesByPatientId(Long patientId, String paymentStatus, LocalDate from, LocalDate to);
    Map<Long, BigDecimal> getTotalTestAmountsByMedicalRecordIds(List<Long> medicalRecordIds);
    Invoice getInvoiceByPatientIdAndId(Long patientId, Long invoiceId);
    Invoice getInvoiceById(Long invoiceId);

    void addInvoice(Invoice invoice);
    void updateInvoice(Invoice invoice);
}
