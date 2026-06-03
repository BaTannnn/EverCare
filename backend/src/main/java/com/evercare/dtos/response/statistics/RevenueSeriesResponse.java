package com.evercare.dtos.response.statistics;

import java.math.BigDecimal;

public class RevenueSeriesResponse {
    private String label;
    private BigDecimal amount;

    public RevenueSeriesResponse() {
    }

    public RevenueSeriesResponse(String label, BigDecimal amount) {
        this.label = label;
        this.amount = amount;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
