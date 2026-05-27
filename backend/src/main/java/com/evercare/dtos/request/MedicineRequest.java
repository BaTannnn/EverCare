package com.evercare.dtos.request;

import java.math.BigDecimal;

public class MedicineRequest {
    private String medicineCode;
    private String name;
    private String unit;
    private String description;
    private String usageNote;
    private BigDecimal unitPrice;
    private Integer minStockQuantity;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUsageNote() {
        return usageNote;
    }

    public void setUsageNote(String usageNote) {
        this.usageNote = usageNote;
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
}
