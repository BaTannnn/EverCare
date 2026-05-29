package com.evercare.controllers.api;

import com.evercare.dtos.request.PaymentRequest;
import com.evercare.dtos.response.InvoiceDetailResponse;
import com.evercare.dtos.response.InvoiceResponse;
import com.evercare.dtos.response.PaymentResultResponse;
import com.evercare.services.InvoiceService;
import com.evercare.services.PaymentService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/secure/receptionist/invoices")
@CrossOrigin
public class ApiReceptionistInvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private PaymentService paymentService;

    @GetMapping
    public ResponseEntity<List<InvoiceResponse>> list(@RequestParam Map<String, String> params) {
        return ResponseEntity.ok(this.invoiceService.getInvoicesForReceptionist(params));
    }

    @GetMapping("/{invoiceId}")
    public ResponseEntity<InvoiceDetailResponse> retrieve(@PathVariable("invoiceId") Long invoiceId) {
        return ResponseEntity.ok(this.invoiceService.getInvoiceForReceptionist(invoiceId));
    }

    @PostMapping("/{invoiceId}/payments")
    public ResponseEntity<PaymentResultResponse> create(
            @PathVariable("invoiceId") Long invoiceId,
            @RequestBody PaymentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.paymentService.createReceptionistPayment(invoiceId, request));
    }
}
