package com.evercare.dtos.response;

public class MedicineBatchImportResponse {
    private MedicineBatchResponse batch;
    private Long inventoryTransactionId;

    public MedicineBatchResponse getBatch() {
        return batch;
    }

    public void setBatch(MedicineBatchResponse batch) {
        this.batch = batch;
    }

    public Long getInventoryTransactionId() {
        return inventoryTransactionId;
    }

    public void setInventoryTransactionId(Long inventoryTransactionId) {
        this.inventoryTransactionId = inventoryTransactionId;
    }
}
