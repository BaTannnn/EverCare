package com.evercare.repositories;

import com.evercare.pojo.Invoice;
import java.time.LocalDate;
import java.util.List;

public interface InvoiceRepository {
    Invoice getInvoiceByMedicalRecordId(Long medicalRecordId);
    List<Invoice> getInvoicesByPatientId(Long patientId, String paymentStatus, LocalDate from, LocalDate to);
    Invoice getInvoiceByPatientIdAndId(Long patientId, Long invoiceId);
    Invoice getInvoiceById(Long invoiceId);
    Invoice getInvoiceByAppointmentId(Long appointmentId);

    void addInvoice(Invoice invoice);
    void updateInvoice(Invoice invoice);
}
