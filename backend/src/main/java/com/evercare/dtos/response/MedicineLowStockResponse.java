package com.evercare.dtos.response;

import java.math.BigDecimal;

public class MedicineLowStockResponse {
    private Long id;
    private String medicineCode;
    private String name;
    private String unit;
    private BigDecimal unitPrice;
    private Integer minStockQuantity;
    private Long totalRemainingQuantity;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMedicineCode() {
        return medicineCode;
    }

    public void setMedicineCode(String medicineCode) {
        this.medicineCode = medicineCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getMinStockQuantity() {
        return minStockQuantity;
    }

    public void setMinStockQuantity(Integer minStockQuantity) {
        this.minStockQuantity = minStockQuantity;
    }

    public Long getTotalRemainingQuantity() {
        return totalRemainingQuantity;
    }

    public void setTotalRemainingQuantity(Long totalRemainingQuantity) {
        this.totalRemainingQuantity = totalRemainingQuantity;
    }
}
