package com.evercare.dtos.request;

import java.util.List;

public class PrescriptionRequest {
    private String note;
    private List<PrescriptionItemRequest> items;

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<PrescriptionItemRequest> getItems() {
        return items;
    }

    public void setItems(List<PrescriptionItemRequest> items) {
        this.items = items;
    }
}
