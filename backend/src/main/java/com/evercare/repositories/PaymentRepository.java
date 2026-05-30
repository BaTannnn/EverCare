package com.evercare.repositories;

import com.evercare.pojo.Payment;
import java.math.BigDecimal;
import java.util.List;

public interface PaymentRepository {
    Payment createPayment(Payment payment);
    Payment updatePayment(Payment payment);
    Payment getPaymentByTransactionCode(String transactionCode);
    List<Payment> getPaymentsByInvoiceId(Long invoiceId);
    List<Payment> getSuccessPaymentsByInvoiceId(Long invoiceId);
    BigDecimal sumSuccessAmountByInvoiceId(Long invoiceId);
}
