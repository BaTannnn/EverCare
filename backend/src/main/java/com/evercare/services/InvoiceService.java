package com.evercare.services;

import com.evercare.dtos.response.InvoiceDetailResponse;
import com.evercare.dtos.response.InvoiceResponse;
import java.util.List;
import java.util.Map;

public interface InvoiceService {
    List<InvoiceResponse> getInvoicesByCurrentPatient(Map<String, String> params);
    InvoiceDetailResponse getInvoiceByCurrentPatient(Long invoiceId);
    List<InvoiceResponse> getInvoicesForReceptionist(Map<String, String> params);
    InvoiceDetailResponse getInvoiceForReceptionist(Long invoiceId);
}
