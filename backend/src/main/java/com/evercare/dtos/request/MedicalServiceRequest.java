package com.evercare.dtos.request;

import java.math.BigDecimal;

public class MedicalServiceRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private String serviceType;
    private Long departmentId;

    public MedicalServiceRequest() {
    }

    public MedicalServiceRequest(String name, String description, BigDecimal price,
                                 String serviceType, Long departmentId) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.serviceType = serviceType;
        this.departmentId = departmentId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getServiceType() {
        return serviceType;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }
}
