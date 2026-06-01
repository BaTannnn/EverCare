package com.evercare.pojo;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "online_consultation_schedule")
public class SupportConsultationSchedule implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @Basic(optional = false)
    @NotNull
    @Column(name = "scheduled_start")
    @Temporal(TemporalType.TIMESTAMP)
    private Date scheduledStart;

    @Basic(optional = false)
    @NotNull
    @Column(name = "scheduled_end")
    @Temporal(TemporalType.TIMESTAMP)
    private Date scheduledEnd;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "meet_link")
    private String meetLink;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 30)
    @Column(name = "status")
    private String status;

    @Lob
    @Size(max = 65535)
    @Column(name = "note")
    private String note;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @Column(name = "active")
    private Boolean active;

    @JoinColumn(name = "conversation_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private SupportConversation conversationId;

    @JoinColumn(name = "patient_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Patient patientId;

    @JoinColumn(name = "doctor_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Doctor doctorId;

    @JoinColumn(name = "staff_id", referencedColumnName = "id")
    @ManyToOne
    private Employee staffId;

    public SupportConsultationSchedule() {
    }

    public SupportConsultationSchedule(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getScheduledStart() {
        return scheduledStart;
    }

    public void setScheduledStart(Date scheduledStart) {
        this.scheduledStart = scheduledStart;
    }

    public Date getScheduledEnd() {
        return scheduledEnd;
    }

    public void setScheduledEnd(Date scheduledEnd) {
        this.scheduledEnd = scheduledEnd;
    }

    public String getMeetLink() {
        return meetLink;
    }

    public void setMeetLink(String meetLink) {
        this.meetLink = meetLink;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public SupportConversation getConversationId() {
        return conversationId;
    }

    public void setConversationId(SupportConversation conversationId) {
        this.conversationId = conversationId;
    }

    public Patient getPatientId() {
        return patientId;
    }

    public void setPatientId(Patient patientId) {
        this.patientId = patientId;
    }

    public Doctor getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Doctor doctorId) {
        this.doctorId = doctorId;
    }

    public Employee getStaffId() {
        return staffId;
    }

    public void setStaffId(Employee staffId) {
        this.staffId = staffId;
    }
}
