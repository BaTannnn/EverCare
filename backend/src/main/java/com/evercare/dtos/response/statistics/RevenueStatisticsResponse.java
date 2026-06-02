package com.evercare.dtos.response.statistics;

import java.math.BigDecimal;
import java.util.List;

public class RevenueStatisticsResponse {
    private BigDecimal totalRevenue;
    private Long paidInvoiceCount;
    private Long unpaidInvoiceCount;
    private Long refundedInvoiceCount;
    private List<RevenueSeriesResponse> series;
    private List<RevenueDetailResponse> details;

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Long getPaidInvoiceCount() {
        return paidInvoiceCount;
    }

    public void setPaidInvoiceCount(Long paidInvoiceCount) {
        this.paidInvoiceCount = paidInvoiceCount;
    }

    public Long getUnpaidInvoiceCount() {
        return unpaidInvoiceCount;
    }

    public void setUnpaidInvoiceCount(Long unpaidInvoiceCount) {
        this.unpaidInvoiceCount = unpaidInvoiceCount;
    }

    public Long getRefundedInvoiceCount() {
        return refundedInvoiceCount;
    }

    public void setRefundedInvoiceCount(Long refundedInvoiceCount) {
        this.refundedInvoiceCount = refundedInvoiceCount;
    }

    public List<RevenueSeriesResponse> getSeries() {
        return series;
    }

    public void setSeries(List<RevenueSeriesResponse> series) {
        this.series = series;
    }

    public List<RevenueDetailResponse> getDetails() {
        return details;
    }

    public void setDetails(List<RevenueDetailResponse> details) {
        this.details = details;
    }
}
