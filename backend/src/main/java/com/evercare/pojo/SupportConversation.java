package com.evercare.pojo;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "support_conversation")
public class SupportConversation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @Size(max = 150)
    @Column(name = "subject")
    private String subject;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 30)
    @Column(name = "status")
    private String status;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @Column(name = "closed_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date closedAt;

    @Column(name = "active")
    private Boolean active;

    @JoinColumn(name = "patient_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Patient patientId;

    @JoinColumn(name = "staff_id", referencedColumnName = "id")
    @ManyToOne
    private Employee staffId;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "conversationId")
    private Set<SupportMessage> supportMessageSet;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "conversationId")
    private Set<SupportConsultationSchedule> supportConsultationScheduleSet;

    public SupportConversation() {
    }

    public SupportConversation(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public Date getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Date closedAt) {
        this.closedAt = closedAt;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Patient getPatientId() {
        return patientId;
    }

    public void setPatientId(Patient patientId) {
        this.patientId = patientId;
    }

    public Employee getStaffId() {
        return staffId;
    }

    public void setStaffId(Employee staffId) {
        this.staffId = staffId;
    }

    public Set<SupportMessage> getSupportMessageSet() {
        return supportMessageSet;
    }

    public void setSupportMessageSet(Set<SupportMessage> supportMessageSet) {
        this.supportMessageSet = supportMessageSet;
    }

    public Set<SupportConsultationSchedule> getSupportConsultationScheduleSet() {
        return supportConsultationScheduleSet;
    }

    public void setSupportConsultationScheduleSet(Set<SupportConsultationSchedule> supportConsultationScheduleSet) {
        this.supportConsultationScheduleSet = supportConsultationScheduleSet;
    }
}
