package com.evercare.dtos.response;

import java.math.BigDecimal;

public class RefundResponse {
    private Boolean refundEligible;
    private String refundStatus;
    private String refundMessage;
    private BigDecimal refundAmount;
    private String refundTransactionCode;

    public Boolean getRefundEligible() {
        return refundEligible;
    }

    public void setRefundEligible(Boolean refundEligible) {
        this.refundEligible = refundEligible;
    }

    public String getRefundStatus() {
        return refundStatus;
    }

    public void setRefundStatus(String refundStatus) {
        this.refundStatus = refundStatus;
    }

    public String getRefundMessage() {
        return refundMessage;
    }

    public void setRefundMessage(String refundMessage) {
        this.refundMessage = refundMessage;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public String getRefundTransactionCode() {
        return refundTransactionCode;
    }

    public void setRefundTransactionCode(String refundTransactionCode) {
        this.refundTransactionCode = refundTransactionCode;
    }
}
