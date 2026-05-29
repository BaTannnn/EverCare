package com.evercare.services.impl;

import com.evercare.dtos.response.InvoiceDetailResponse;
import com.evercare.dtos.response.InvoiceResponse;
import com.evercare.mappers.InvoiceMapper;
import com.evercare.pojo.Invoice;
import com.evercare.pojo.Patient;
import com.evercare.pojo.User;
import com.evercare.repositories.InvoiceRepository;
import com.evercare.repositories.PatientRepository;
import com.evercare.repositories.PaymentRepository;
import com.evercare.services.InvoiceService;
import com.evercare.services.UserService;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private PatientRepository patientRepo;

    @Autowired
    private UserService userService;

    @Override
    public List<InvoiceResponse> getInvoicesByCurrentPatient(Map<String, String> params) {
        Patient currentPatient = getCurrentPatientOrNull();
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
        Patient currentPatient = requireCurrentPatient();
        Invoice invoice = this.invoiceRepo.getInvoiceByPatientIdAndId(currentPatient.getId(), invoiceId);
        if (invoice == null) {
            throw new java.util.NoSuchElementException("Không tìm thấy hóa đơn");
        }

        return InvoiceMapper.toDetailResponse(invoice, this.paymentRepo.getPaymentsByInvoiceId(invoice.getId()));
    }

    @Override
    public List<InvoiceResponse> getInvoicesForReceptionist(Map<String, String> params) {
        requireReceptionistUser();
        return this.invoiceRepo.getInvoicesForReceptionist(params)
                .stream()
                .map(InvoiceMapper::toResponse)
                .toList();
    }

    @Override
    public InvoiceDetailResponse getInvoiceForReceptionist(Long invoiceId) {
        requireReceptionistUser();
        Invoice invoice = this.invoiceRepo.getInvoiceById(invoiceId);
        if (invoice == null) {
            throw new NoSuchElementException("Không tìm thấy hóa đơn");
        }

        return InvoiceMapper.toDetailResponse(invoice, this.paymentRepo.getPaymentsByInvoiceId(invoice.getId()));
    }

    private Patient getCurrentPatientOrNull() {
        try {
            return requireCurrentPatient();
        } catch (java.util.NoSuchElementException ex) {
            return null;
        }
    }

    private Patient requireCurrentPatient() {
        User currentUser = getCurrentUser();
        Patient patient = this.patientRepo.getPatientByUserId(currentUser.getId());
        if (patient == null || Boolean.FALSE.equals(patient.getActive())) {
            throw new java.util.NoSuchElementException("Bạn chưa có hồ sơ bệnh nhân");
        }
        return patient;
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : null;

        if (username == null || username.isBlank()) {
            throw new SecurityException("Vui lòng đăng nhập");
        }

        User user = this.userService.getUserByUsername(username);
        if (user == null || Boolean.FALSE.equals(user.getActive())) {
            throw new SecurityException("Tài khoản không hợp lệ");
        }
        return user;
    }

    private User requireReceptionistUser() {
        User user = getCurrentUser();
        if (!hasAnyRole(user, "ROLE_RECEPTIONIST", "ROLE_ADMIN")) {
            throw new AccessDeniedException("Tài khoản không có quyền lễ tân");
        }
        return user;
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

    private boolean hasAnyRole(User user, String... roles) {
        if (user == null || user.getRoleSet() == null || roles == null) {
            return false;
        }

        for (var role : user.getRoleSet()) {
            if (role == null || role.getCode() == null) {
                continue;
            }
            for (String expectedRole : roles) {
                if (expectedRole != null && expectedRole.equalsIgnoreCase(role.getCode())) {
                    return true;
                }
            }
        }

        return false;
    }
}
