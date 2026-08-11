package com.evercare.services.impl;

import com.evercare.dtos.request.PaymentRequest;
import com.evercare.dtos.response.PaymentResultResponse;
import com.evercare.dtos.response.PaymentResponse;
import com.evercare.enums.InvoiceStatus;
import com.evercare.enums.PaymentStatus;
import com.evercare.mappers.PaymentMapper;
import com.evercare.pojo.Invoice;
import com.evercare.pojo.MedicalRecord;
import com.evercare.pojo.Notification;
import com.evercare.pojo.Patient;
import com.evercare.pojo.Payment;
import com.evercare.pojo.User;
import com.evercare.repositories.AppointmentRepository;
import com.evercare.repositories.InvoiceRepository;
import com.evercare.repositories.MedicalRecordRepository;
import com.evercare.repositories.NotificationRepository;
import com.evercare.repositories.PaymentRepository;
import com.evercare.dtos.response.PaymentGatewayResultResponse;
import com.evercare.services.PaymentGatewayService;
import com.evercare.services.PaymentService;
import com.evercare.utils.AuthSupport;
import com.evercare.utils.PaymentGatewaySupport;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@PropertySource("classpath:payments.properties")
@Transactional
public class PaymentServiceImpl implements PaymentService {
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);
    private static final String PAYMENT_TYPE = "PAYMENT";
    private static final String INSURANCE_DISCOUNT_RATE_PROPERTY = "payment.insurance.discountRate";
    private static final BigDecimal DEFAULT_INSURANCE_DISCOUNT_RATE = new BigDecimal("0.8");
    @Autowired
    private InvoiceRepository invoiceRepo;
    @Autowired
    private PaymentRepository paymentRepo;
    @Autowired
    private NotificationRepository notificationRepo;
    @Autowired
    private MedicalRecordRepository medicalRecordRepo;
    @Autowired
    private AppointmentRepository appointmentRepo;
    @Autowired
    private Environment env;
    @Autowired
    private List<PaymentGatewayService> gatewayServices;
    @Autowired
    private AuthSupport authSupport;
    @Override
    @Transactional(noRollbackFor = IllegalStateException.class)
    public PaymentResultResponse createPayment(Long invoiceId, PaymentRequest request) {
        Patient currentPatient = this.authSupport.requireCurrentPatient(new IllegalStateException("Bạn cần tạo hồ sơ bệnh nhân trước khi thanh toán hóa đơn."));
        Invoice invoice = this.invoiceRepo.getInvoiceByPatientIdAndId(currentPatient.getId(), invoiceId);
        if (invoice == null) {
            throw new NoSuchElementException("Không tìm thấy hóa đơn");
        }
        return createPaymentForInvoice(invoice, request);
    }

    @Override
    @Transactional(noRollbackFor = IllegalStateException.class)
    public PaymentResultResponse createReceptionistPayment(Long invoiceId, PaymentRequest request) {
        this.authSupport.requireReceptionistUser();
        Invoice invoice = this.invoiceRepo.getInvoiceById(invoiceId);
        if (invoice == null) {
            throw new NoSuchElementException("Không tìm thấy hóa đơn");
        }

        return createPaymentForInvoice(invoice, request);
    }

    @Override
    public PaymentResponse handleGatewayCallback(String provider, Map<String, String> params) {
        PaymentGatewayService gateway = resolveGatewayByProvider(provider);
        if (!gateway.verifyCallback(params)) {
            throw new IllegalArgumentException("Chữ ký callback không hợp lệ");
        }

        PaymentGatewayResultResponse gatewayResult = gateway.parseCallback(params);
        if (gatewayResult.getTransactionCode() == null || gatewayResult.getTransactionCode().isBlank()) {
            throw new IllegalArgumentException("Callback thiếu mã giao dịch");
        }

        Payment payment = this.paymentRepo.getPaymentByTransactionCode(gatewayResult.getTransactionCode());
        if (payment == null) {
            throw new NoSuchElementException("Không tìm thấy giao dịch thanh toán");
        }

        if (gatewayResult.getAmount() != null && payment.getAmount() != null
                && payment.getAmount().compareTo(gatewayResult.getAmount()) != 0) {
            throw new IllegalArgumentException("Số tiền callback không khớp");
        }

        if (PaymentStatus.SUCCESS.getCode().equalsIgnoreCase(payment.getPaymentStatus())
                && gatewayResult.isSuccess()) {
            return PaymentMapper.toResponse(payment);
        }

        if (gatewayResult.isSuccess()) {
            markPaymentSuccess(payment);
            updateInvoiceAndMedicalRecordAfterPayment(payment, InvoiceStatus.PAID.getCode().equalsIgnoreCase(payment.getInvoiceId().getPaymentStatus()));
            return PaymentMapper.toResponse(payment);
        }

        if (!PaymentStatus.SUCCESS.getCode().equalsIgnoreCase(payment.getPaymentStatus())
                && !PaymentStatus.REFUNDED.getCode().equalsIgnoreCase(payment.getPaymentStatus())) {
            payment.setPaymentStatus(PaymentStatus.FAILED.getCode());
            payment.setUpdatedAt(new Date());
            this.paymentRepo.updatePayment(payment);
        }

        return PaymentMapper.toResponse(payment);
    }

    @Override
    public PaymentResponse handleGatewayResult(String provider, Map<String, String> params) {
        String normalizedProvider = provider != null ? provider.trim().toUpperCase() : null;
        if (normalizedProvider == null) {
            throw new IllegalArgumentException("Thiếu cổng thanh toán");
        }

        PaymentGatewayResultResponse gatewayResult;
        if ("MOMO".equals(normalizedProvider)) {
            gatewayResult = parseMomoResult(params);
        } else if ("ZALOPAY".equals(normalizedProvider)) {
            gatewayResult = parseZaloPayResult(params);
        } else if ("VNPAY".equals(normalizedProvider)) {
            gatewayResult = parseVnPayResult(params);
        } else {
            throw new IllegalArgumentException("Cổng thanh toán không hợp lệ");
        }

        if (gatewayResult.getTransactionCode() == null || gatewayResult.getTransactionCode().isBlank()) {
            throw new IllegalArgumentException("Kết quả thanh toán thiếu mã giao dịch");
        }

        Payment payment = this.paymentRepo.getPaymentByTransactionCode(gatewayResult.getTransactionCode());
        if (payment == null) {
            throw new NoSuchElementException("Không tìm thấy giao dịch thanh toán");
        }

        if (gatewayResult.getAmount() != null && payment.getAmount() != null
                && payment.getAmount().compareTo(gatewayResult.getAmount()) != 0) {
            throw new IllegalArgumentException("Số tiền callback không khớp");
        }

        if (gatewayResult.isSuccess()) {
            if (!PaymentStatus.SUCCESS.getCode().equalsIgnoreCase(payment.getPaymentStatus())) {
                markPaymentSuccess(payment);
                updateInvoiceAndMedicalRecordAfterPayment(payment, InvoiceStatus.PAID.getCode().equalsIgnoreCase(payment.getInvoiceId().getPaymentStatus()));
            }
        } else if (!PaymentStatus.SUCCESS.getCode().equalsIgnoreCase(payment.getPaymentStatus())
                && !PaymentStatus.REFUNDED.getCode().equalsIgnoreCase(payment.getPaymentStatus())) {
            payment.setPaymentStatus(PaymentStatus.FAILED.getCode());
            payment.setUpdatedAt(new Date());
            this.paymentRepo.updatePayment(payment);
        }

        return PaymentMapper.toResponse(payment);
    }

    private void markPaymentSuccess(Payment payment) {
        payment.setPaymentStatus(PaymentStatus.SUCCESS.getCode());
        payment.setPaidAt(new Date());
        payment.setUpdatedAt(new Date());
        this.paymentRepo.updatePayment(payment);
    }

    private void updateInvoiceAndMedicalRecordAfterPayment(Payment payment, boolean invoiceAlreadyPaid) {
        Invoice invoice = payment.getInvoiceId();
        BigDecimal paidAmount = this.paymentRepo.sumSuccessAmountByInvoiceId(invoice.getId());
        if (invoice.getTotalAmount() != null && paidAmount.compareTo(invoice.getTotalAmount()) >= 0) {
            if (!invoiceAlreadyPaid) {
                invoice.setPaymentStatus(InvoiceStatus.PAID.getCode());
                invoice.setPaymentMethod(payment.getPaymentMethod());
                invoice.setPaidAt(new Date());
                invoice.setUpdatedAt(new Date());
                this.invoiceRepo.updateInvoice(invoice);

                MedicalRecord medicalRecord = invoice.getMedicalRecordId();
                if (medicalRecord != null) {
                    medicalRecord.setPaymentStatus(InvoiceStatus.PAID.getCode());
                    medicalRecord.setUpdatedAt(new Date());
                    this.medicalRecordRepo.updateMedicalRecord(medicalRecord);
                }

                createNotification(
                        invoice,
                        "Thanh toán thành công",
                        "Bạn đã thanh toán hóa đơn " + invoice.getInvoiceCode() + " thành công.",
                        invoice.getId()
                );
            } else {
                invoice.setUpdatedAt(new Date());
                this.invoiceRepo.updateInvoice(invoice);
            }
        }
    }

    private String normalizePaymentMethod(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn phương thức thanh toán");
        }
        return paymentMethod.trim().toUpperCase();
    }

    private PaymentGatewayService resolveGateway(String paymentMethod) {
        for (PaymentGatewayService gateway : this.gatewayServices) {
            if (gateway.supports(paymentMethod)) {
                return gateway;
            }
        }
        throw new IllegalStateException("Payment gateway is not configured.");
    }

    private PaymentGatewayService resolveGatewayByProvider(String provider) {
        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException("Thiếu cổng thanh toán");
        }

        for (PaymentGatewayService gateway : this.gatewayServices) {
            if (gateway.getProvider().equalsIgnoreCase(provider.trim())) {
                return gateway;
            }
        }

        throw new IllegalArgumentException("Cổng thanh toán không hợp lệ");
    }

    private String generateCode(String prefix) {
        return prefix + "_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                + "_" + String.format("%06d", Math.abs(UUID.randomUUID().hashCode()) % 1_000_000);
    }

    private String generateTransactionCode(String provider) {
        if (provider != null) {
            String normalized = provider.trim().toUpperCase();
            if ("CASH".equals(normalized)) {
                return generateCode("CASH");
            }
            if ("BANK_TRANSFER".equals(normalized)) {
                return generateCode("BANK");
            }
            if ("VIETQR".equals(normalized)) {
                return generateCode("VIETQR");
            }
        }

        if (provider != null && "ZALOPAY".equalsIgnoreCase(provider.trim())) {
            String datePrefix = LocalDate.now(VIETNAM_ZONE).format(DateTimeFormatter.ofPattern("yyMMdd"));
            return datePrefix + "_" + String.format("%d_%d",
                    Math.abs(UUID.randomUUID().hashCode()) % 10,
                    System.currentTimeMillis());
        }

        return generateCode("PAY");
    }

    private PaymentGatewayResultResponse parseMomoResult(Map<String, String> params) {
        String secretKey = PaymentGatewaySupport.optionalProperty(this.env, "payment.momo.secretKey");
        String accessKey = PaymentGatewaySupport.optionalProperty(this.env, "payment.momo.accessKey");
        if (secretKey == null || accessKey == null) {
            throw new IllegalStateException("Payment gateway is not configured.");
        }

        String signature = PaymentGatewaySupport.value(params, "signature", "sign");
        if (signature == null) {
            throw new IllegalArgumentException("Chữ ký redirect không hợp lệ");
        }

        String raw = "accessKey=" + accessKey
                + "&amount=" + valueOrEmpty(params, "amount")
                + "&extraData=" + valueOrEmpty(params, "extraData")
                + "&message=" + valueOrEmpty(params, "message")
                + "&orderId=" + valueOrEmpty(params, "orderId")
                + "&orderInfo=" + valueOrEmpty(params, "orderInfo")
                + "&orderType=" + valueOrEmpty(params, "orderType")
                + "&partnerCode=" + valueOrEmpty(params, "partnerCode")
                + "&payType=" + valueOrEmpty(params, "payType")
                + "&requestId=" + valueOrEmpty(params, "requestId")
                + "&responseTime=" + valueOrEmpty(params, "responseTime")
                + "&resultCode=" + valueOrEmpty(params, "resultCode")
                + "&transId=" + valueOrEmpty(params, "transId");
        logger.debug("MOMO result rawData: {}", raw);

        String expected = PaymentGatewaySupport.hmacSha256(secretKey, raw);
        if (!expected.equalsIgnoreCase(signature)) {
            throw new IllegalArgumentException("Chữ ký redirect không hợp lệ");
        }

        PaymentGatewayResultResponse result = new PaymentGatewayResultResponse();
        result.setTransactionCode(PaymentGatewaySupport.value(params, "orderId", "requestId"));
        result.setGatewayTransactionId(PaymentGatewaySupport.value(params, "transId"));
        result.setAmount(PaymentGatewaySupport.amountFromString(PaymentGatewaySupport.value(params, "amount")));
        result.setSuccess("0".equals(PaymentGatewaySupport.value(params, "resultCode")));
        result.setMessage(PaymentGatewaySupport.value(params, "message"));
        return result;
    }

    private PaymentGatewayResultResponse parseZaloPayResult(Map<String, String> params) {
        String key2 = PaymentGatewaySupport.optionalProperty(this.env, "payment.zalopay.key2");
        if (key2 == null) {
            throw new IllegalStateException("Payment gateway is not configured.");
        }

        String rawData = String.join("|",
                valueOrEmpty(params, "appid"),
                valueOrEmpty(params, "apptransid"),
                valueOrEmpty(params, "pmcid"),
                valueOrEmpty(params, "bankcode"),
                valueOrEmpty(params, "amount"),
                valueOrEmpty(params, "discountamount"),
                valueOrEmpty(params, "status")
        );
        logger.debug("ZALOPAY result rawData: {}", rawData);

        String checksum = PaymentGatewaySupport.value(params, "checksum");
        String expectedChecksum = PaymentGatewaySupport.hmacSha256(key2, rawData);
        if (checksum == null || !expectedChecksum.equalsIgnoreCase(checksum)) {
            throw new IllegalArgumentException("Chữ ký redirect không hợp lệ");
        }

        PaymentGatewayResultResponse result = new PaymentGatewayResultResponse();
        result.setTransactionCode(PaymentGatewaySupport.value(params, "apptransid"));
        result.setGatewayTransactionId(PaymentGatewaySupport.value(params, "pmcid"));
        result.setAmount(PaymentGatewaySupport.amountFromString(PaymentGatewaySupport.value(params, "amount")));
        String status = valueOrEmpty(params, "status");
        result.setSuccess("1".equals(status) || "2".equals(status) || "success".equalsIgnoreCase(status));
        result.setMessage(valueOrEmpty(params, "message"));
        return result;
    }

    private PaymentGatewayResultResponse parseVnPayResult(Map<String, String> params) {
        String hashSecret = PaymentGatewaySupport.optionalProperty(this.env, "payment.vnpay.hashSecret");
        if (hashSecret == null) {
            throw new IllegalStateException("Payment gateway is not configured.");
        }

        String secureHash = PaymentGatewaySupport.value(params, "vnp_SecureHash", "secureHash");
        if (secureHash == null) {
            throw new IllegalArgumentException("Chữ ký redirect không hợp lệ");
        }

        Map<String, String> filtered = new java.util.HashMap<>(params);
        filtered.remove("vnp_SecureHash");
        filtered.remove("vnp_SecureHashType");
        filtered.remove("secureHash");
        filtered.remove("frontendReturnUrl");
        filtered.remove("paymentId");
        filtered.remove("invoiceId");
        filtered.remove("transactionCode");
        filtered.remove("paymentStatus");

        String raw = PaymentGatewaySupport.buildQueryString(filtered);
        logger.debug("VNPAY result rawData: {}", raw);
        String expected = PaymentGatewaySupport.hmacSha512(hashSecret, raw);
        if (!expected.equalsIgnoreCase(secureHash)) {
            throw new IllegalArgumentException("Chữ ký redirect không hợp lệ");
        }

        PaymentGatewayResultResponse result = new PaymentGatewayResultResponse();
        result.setTransactionCode(PaymentGatewaySupport.value(params, "vnp_TxnRef", "txnRef"));
        result.setGatewayTransactionId(PaymentGatewaySupport.value(params, "vnp_TransactionNo", "transactionNo"));
        result.setAmount(PaymentGatewaySupport.amountFromScaledString(PaymentGatewaySupport.value(params, "vnp_Amount"), 100));
        result.setSuccess("00".equals(PaymentGatewaySupport.value(params, "vnp_ResponseCode"))
                && "00".equals(PaymentGatewaySupport.value(params, "vnp_TransactionStatus")));
        result.setMessage(PaymentGatewaySupport.value(params, "vnp_Message", "message"));
        return result;
    }

    private String valueOrEmpty(Map<String, String> params, String key) {
        String value = PaymentGatewaySupport.value(params, key);
        return value != null ? value : "";
    }

    private PaymentResultResponse createPaymentForInvoice(Invoice invoice, PaymentRequest request) {
        applyHealthInsuranceDiscount(invoice);
        validateInvoiceForPayment(invoice);

        String paymentMethod = normalizePaymentMethod(request != null ? request.getPaymentMethod() : null);
        if (isCounterPaymentMethod(paymentMethod)) {
            return recordCounterPayment(invoice, paymentMethod);
        }

        if (isOnlinePaymentMethod(paymentMethod)) {
            return createOnlinePayment(invoice, request, paymentMethod);
        }

        throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ");
    }

    private PaymentResultResponse recordCounterPayment(Invoice invoice, String paymentMethod) {
        BigDecimal amount = resolveRemainingAmount(invoice);
        Date now = new Date();

        Payment payment = new Payment();
        payment.setInvoiceId(invoice);
        payment.setAmount(amount);
        payment.setPaymentMethod(paymentMethod);
        payment.setPaymentProvider(paymentMethod);
        payment.setTransactionCode(generateTransactionCode(paymentMethod));
        payment.setPaymentStatus(PaymentStatus.SUCCESS.getCode());
        payment.setPaidAt(now);
        payment.setCreatedAt(now);
        payment.setUpdatedAt(now);
        payment.setActive(true);
        this.paymentRepo.createPayment(payment);

        completeInvoiceAfterCounterPayment(payment);
        return PaymentMapper.toResultResponse(payment, null, invoice);
    }

    private PaymentResultResponse createOnlinePayment(Invoice invoice, PaymentRequest request, String paymentMethod) {
        BigDecimal amount = resolveRemainingAmount(invoice);
        PaymentGatewayService gateway = resolveGateway(paymentMethod);
        String transactionCode = generateTransactionCode(gateway.getProvider());
        Date now = new Date();

        Payment payment = new Payment();
        payment.setInvoiceId(invoice);
        payment.setAmount(amount);
        payment.setPaymentMethod(paymentMethod);
        payment.setPaymentProvider(gateway.getProvider());
        payment.setTransactionCode(transactionCode);
        payment.setPaymentStatus(PaymentStatus.PENDING.getCode());
        payment.setCreatedAt(now);
        payment.setUpdatedAt(now);
        payment.setActive(true);
        this.paymentRepo.createPayment(payment);

        try {
            String paymentUrl = gateway.createPaymentUrl(invoice, payment, request);
            return PaymentMapper.toResultResponse(payment, paymentUrl, invoice);
        } catch (Exception ex) {
            payment.setPaymentStatus(PaymentStatus.FAILED.getCode());
            payment.setUpdatedAt(new Date());
            this.paymentRepo.updatePayment(payment);
            throw new IllegalStateException(ex.getMessage() != null ? ex.getMessage() : "Không thể tạo payment URL", ex);
        }
    }

    private void validateInvoiceForPayment(Invoice invoice) {
        if (invoice == null) {
            throw new NoSuchElementException("Không tìm thấy hóa đơn");
        }

        if (InvoiceStatus.PAID.getCode().equalsIgnoreCase(invoice.getPaymentStatus())
                || InvoiceStatus.REFUNDED.getCode().equalsIgnoreCase(invoice.getPaymentStatus())) {
            throw new IllegalStateException("Hóa đơn đã được thanh toán");
        }

        if (invoice.getTotalAmount() == null || invoice.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Tổng tiền hóa đơn không hợp lệ");
        }
    }

    private void applyHealthInsuranceDiscount(Invoice invoice) {
        if (invoice == null || invoice.getPatientId() == null) {
            return;
        }

        Patient patient = invoice.getPatientId();
        if (patient.getHealthInsuranceNo() == null || patient.getHealthInsuranceNo().isBlank()) {
            return;
        }

        BigDecimal baseAmount = normalizeAmount(invoice.getTotalServiceAmount())
                .add(normalizeAmount(invoice.getTotalMedicineAmount()));
        if (baseAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        BigDecimal discountRate = resolveInsuranceDiscountRate();
        if (discountRate.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        BigDecimal discountAmount = baseAmount.multiply(discountRate)
                .min(baseAmount)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = baseAmount.subtract(discountAmount).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

        boolean changed = invoice.getDiscountAmount() == null
                || invoice.getDiscountAmount().compareTo(discountAmount) != 0
                || invoice.getTotalAmount() == null
                || invoice.getTotalAmount().compareTo(totalAmount) != 0;

        if (!changed) {
            return;
        }

        invoice.setDiscountAmount(discountAmount);
        invoice.setTotalAmount(totalAmount);
        invoice.setUpdatedAt(new Date());
        this.invoiceRepo.updateInvoice(invoice);
    }

    private BigDecimal resolveInsuranceDiscountRate() {
        if (this.env == null) {
            return DEFAULT_INSURANCE_DISCOUNT_RATE;
        }

        String configuredValue = this.env.getProperty(INSURANCE_DISCOUNT_RATE_PROPERTY);
        if (configuredValue == null || configuredValue.isBlank()) {
            return DEFAULT_INSURANCE_DISCOUNT_RATE;
        }

        try {
            BigDecimal parsed = new BigDecimal(configuredValue.trim());
            if (parsed.compareTo(BigDecimal.ZERO) < 0) {
                return BigDecimal.ZERO;
            }
            if (parsed.compareTo(BigDecimal.ONE) > 0) {
                return BigDecimal.ONE;
            }
            return parsed;
        } catch (NumberFormatException ex) {
            logger.warn("Invalid insurance discount rate '{}', using default {}", configuredValue, DEFAULT_INSURANCE_DISCOUNT_RATE);
            return DEFAULT_INSURANCE_DISCOUNT_RATE;
        }
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        return amount != null ? amount : BigDecimal.ZERO;
    }

    private BigDecimal resolveRemainingAmount(Invoice invoice) {
        BigDecimal alreadyPaid = this.paymentRepo.sumSuccessAmountByInvoiceId(invoice.getId());
        BigDecimal remaining = invoice.getTotalAmount().subtract(alreadyPaid);
        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Hóa đơn đã được thanh toán");
        }
        return remaining;
    }

    private void completeInvoiceAfterCounterPayment(Payment payment) {
        Invoice invoice = payment.getInvoiceId();
        if (invoice == null) {
            return;
        }

        invoice.setPaymentStatus(InvoiceStatus.PAID.getCode());
        invoice.setPaymentMethod(payment.getPaymentMethod());
        invoice.setPaidAt(new Date());
        invoice.setUpdatedAt(new Date());
        this.invoiceRepo.updateInvoice(invoice);

        MedicalRecord medicalRecord = invoice.getMedicalRecordId();
        if (medicalRecord != null) {
            medicalRecord.setPaymentStatus(InvoiceStatus.PAID.getCode());
            medicalRecord.setUpdatedAt(new Date());
            this.medicalRecordRepo.updateMedicalRecord(medicalRecord);
        }

        createNotification(
                invoice,
                "Thanh toán thành công",
                "Bạn đã thanh toán hóa đơn " + invoice.getInvoiceCode() + " thành công.",
                invoice.getId()
        );
    }

    private boolean isCounterPaymentMethod(String paymentMethod) {
        return "CASH".equalsIgnoreCase(paymentMethod)
                || "BANK_TRANSFER".equalsIgnoreCase(paymentMethod)
                || "VIETQR".equalsIgnoreCase(paymentMethod);
    }

    private boolean isOnlinePaymentMethod(String paymentMethod) {
        return "MOMO".equalsIgnoreCase(paymentMethod)
                || "ZALOPAY".equalsIgnoreCase(paymentMethod)
                || "VNPAY".equalsIgnoreCase(paymentMethod);
    }

    private void createNotification(Invoice invoice, String title, String content, Long relatedId) {
        try {
            if (invoice == null || invoice.getPatientId() == null || invoice.getPatientId().getUserId() == null) {
                return;
            }

            Notification notification = new Notification();
            notification.setUserId(invoice.getPatientId().getUserId());
            notification.setTitle(title);
            notification.setContent(content);
            notification.setNotificationType(PAYMENT_TYPE);
            if (relatedId != null) {
                notification.setRelatedId(BigInteger.valueOf(relatedId));
            }
            notification.setActive(true);
            notification.setCreatedAt(new Date());
            this.notificationRepo.createNotification(notification);
        } catch (Exception ex) {
            // Notification is best-effort only.
        }
    }
}
