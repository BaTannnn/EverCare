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

        return this.invoiceRepo.getInvoicesByPatientId(currentPatient.getId(), paymentStatus, from, to)
                .stream()
                .map(InvoiceMapper::toResponse)
                .toList();
    }

    @Override
    public InvoiceDetailResponse getInvoiceByCurrentPatient(Long invoiceId) {
        Patient currentPatient = this.authSupport.requireCurrentPatient("Bạn chưa có hồ sơ bệnh nhân");
        Invoice invoice = this.invoiceRepo.getInvoiceByPatientIdAndId(currentPatient.getId(), invoiceId);
        if (invoice == null) {
            throw new java.util.NoSuchElementException("Không tìm thấy hóa đơn");
        }

        return InvoiceMapper.toDetailResponse(invoice, this.paymentRepo.getPaymentsByInvoiceId(invoice.getId()));
    }

    @Override
    public List<InvoiceResponse> getInvoicesForReceptionist(Map<String, String> params) {
        this.authSupport.requireReceptionistUser();
        return this.invoiceRepo.getInvoicesForReceptionist(params)
                .stream()
                .map(InvoiceMapper::toResponse)
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

}
