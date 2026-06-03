package com.evercare.dtos.response.statistics;

import java.math.BigDecimal;

public class StatisticItemResponse {
    private Long id;
    private String label;
    private String serviceName;
    private String serviceType;
    private Long count;
    private BigDecimal amount;

    public StatisticItemResponse() {
    }

    public StatisticItemResponse(String label, Long count) {
        this.label = label;
        this.count = count;
    }

    public StatisticItemResponse(Long id, String serviceName, String serviceType, Long count, BigDecimal amount) {
        this.id = id;
        this.label = serviceName;
        this.serviceName = serviceName;
        this.serviceType = serviceType;
        this.count = count;
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
