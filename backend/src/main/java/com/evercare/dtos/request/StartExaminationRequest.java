package com.evercare.dtos.request;

public class StartExaminationRequest {
    private String chiefComplaint;
    private String initialNote;

    public String getChiefComplaint() {
        return chiefComplaint;
    }

    public void setChiefComplaint(String chiefComplaint) {
        this.chiefComplaint = chiefComplaint;
    }

    public String getInitialNote() {
        return initialNote;
    }

    public void setInitialNote(String initialNote) {
        this.initialNote = initialNote;
    }
}
