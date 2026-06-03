package com.evercare.services.impl;

import com.evercare.dtos.response.InvoiceDetailResponse;
import com.evercare.dtos.response.InvoiceResponse;
import com.evercare.mappers.InvoiceMapper;
import com.evercare.pojo.Invoice;
import com.evercare.pojo.Patient;
import com.evercare.pojo.User;
import com.evercare.repositories.InvoiceRepository;
import com.evercare.repositories.PaymentRepository;
import com.evercare.services.InvoiceService;
import com.evercare.utils.AuthSupport;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InvoiceServiceImpl implements InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepo;

    @Autowired
    private PaymentRepository paymentRepo;

    @Autowired
    private AuthSupport authSupport;

    @Override
    public List<InvoiceResponse> getInvoicesByCurrentPatient(Map<String, String> params) {
        Patient currentPatient = this.authSupport.getCurrentPatientOrNull();
        if (currentPatient == null) {
            return new ArrayList<>();
        }

        String paymentStatus = params != null ? params.get("paymentStatus") : null;
        LocalDate from = parseDate(params != null ? params.get("from") : null, "from");
        LocalDate to = parseDate(params != null ? params.get("to") : null, "to");

        List<Invoice> invoices = this.invoiceRepo.getInvoicesByPatientId(currentPatient.getId(), paymentStatus, from, to);
        Map<Long, BigDecimal> testAmounts = getTestAmountsByMedicalRecordId(invoices);

        return invoices
                .stream()
                .map(invoice -> InvoiceMapper.toResponse(invoice, getTestAmount(invoice, testAmounts)))
                .toList();
    }

    @Override
    public InvoiceDetailResponse getInvoiceByCurrentPatient(Long invoiceId) {
        Patient currentPatient = this.authSupport.requireCurrentPatient("Bạn chưa có hồ sơ bệnh nhân");
        Invoice invoice = this.invoiceRepo.getInvoiceByPatientIdAndId(currentPatient.getId(), invoiceId);
        if (invoice == null) {
            throw new java.util.NoSuchElementException("Không tìm thấy hóa đơn");
        }

        return InvoiceMapper.toDetailResponse(
                invoice,
                this.paymentRepo.getPaymentsByInvoiceId(invoice.getId()),
                getTestAmount(invoice, getTestAmountsByMedicalRecordId(List.of(invoice)))
        );
    }

    @Override
    public List<InvoiceResponse> getInvoicesForReceptionist(Map<String, String> params) {
        this.authSupport.requireReceptionistUser();
        return this.invoiceRepo.getInvoicesForReceptionist(params)
                .stream()
                .map(InvoiceMapper::toReceptionistResponse)
                .toList();
    }

    @Override
    public InvoiceDetailResponse getInvoiceForReceptionist(Long invoiceId) {
        this.authSupport.requireReceptionistUser();
        Invoice invoice = this.invoiceRepo.getInvoiceById(invoiceId);
        if (invoice == null) {
            throw new NoSuchElementException("Không tìm thấy hóa đơn");
        }

        return InvoiceMapper.toDetailResponse(invoice, this.paymentRepo.getPaymentsByInvoiceId(invoice.getId()));
    }

    private LocalDate parseDate(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(fieldName + " không hợp lệ");
        }
    }

    private Map<Long, BigDecimal> getTestAmountsByMedicalRecordId(List<Invoice> invoices) {
        if (invoices == null || invoices.isEmpty()) {
            return Map.of();
        }

        List<Long> medicalRecordIds = invoices.stream()
                .map(this::getMedicalRecordId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();

        if (medicalRecordIds.isEmpty()) {
            return Map.of();
        }

        return this.invoiceRepo.getTotalTestAmountsByMedicalRecordIds(medicalRecordIds);
    }

    private BigDecimal getTestAmount(Invoice invoice, Map<Long, BigDecimal> testAmounts) {
        Long medicalRecordId = getMedicalRecordId(invoice);
        if (medicalRecordId == null || testAmounts == null) {
            return BigDecimal.ZERO;
        }
        return testAmounts.getOrDefault(medicalRecordId, BigDecimal.ZERO);
    }

    private Long getMedicalRecordId(Invoice invoice) {
        return invoice != null && invoice.getMedicalRecordId() != null
                ? invoice.getMedicalRecordId().getId()
                : null;
    }

}
