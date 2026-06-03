/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.evercare.pojo;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author cadic
 */
@Entity
@Table(name = "appointment")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Appointment.findAll", query = "SELECT a FROM Appointment a"),
    @NamedQuery(name = "Appointment.findById", query = "SELECT a FROM Appointment a WHERE a.id = :id"),
    @NamedQuery(name = "Appointment.findByAppointmentCode", query = "SELECT a FROM Appointment a WHERE a.appointmentCode = :appointmentCode"),
    @NamedQuery(name = "Appointment.findByAppointmentDate", query = "SELECT a FROM Appointment a WHERE a.appointmentDate = :appointmentDate"),
    @NamedQuery(name = "Appointment.findByStartTime", query = "SELECT a FROM Appointment a WHERE a.startTime = :startTime"),
    @NamedQuery(name = "Appointment.findByEndTime", query = "SELECT a FROM Appointment a WHERE a.endTime = :endTime"),
    @NamedQuery(name = "Appointment.findByStatus", query = "SELECT a FROM Appointment a WHERE a.status = :status"),
    @NamedQuery(name = "Appointment.findByReason", query = "SELECT a FROM Appointment a WHERE a.reason = :reason"),
    @NamedQuery(name = "Appointment.findByCancelReason", query = "SELECT a FROM Appointment a WHERE a.cancelReason = :cancelReason"),
    @NamedQuery(name = "Appointment.findByCreatedAt", query = "SELECT a FROM Appointment a WHERE a.createdAt = :createdAt"),
    @NamedQuery(name = "Appointment.findByUpdatedAt", query = "SELECT a FROM Appointment a WHERE a.updatedAt = :updatedAt"),
    @NamedQuery(name = "Appointment.findByActive", query = "SELECT a FROM Appointment a WHERE a.active = :active")})
public class Appointment implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 30)
    @Column(name = "appointment_code")
    private String appointmentCode;
    @Basic(optional = false)
    @NotNull
    @Column(name = "appointment_date")
    @Temporal(TemporalType.DATE)
    private Date appointmentDate;
    @Basic(optional = false)
    @NotNull
    @Column(name = "start_time")
    @Temporal(TemporalType.TIME)
    private Date startTime;
    @Column(name = "end_time")
    @Temporal(TemporalType.TIME)
    private Date endTime;
    @Size(max = 40)
    @Column(name = "status")
    private String status;
    @Size(max = 255)
    @Column(name = "reason")
    private String reason;
    @Lob
    @Size(max = 65535)
    @Column(name = "symptom_note")
    private String symptomNote;
    @Size(max = 255)
    @Column(name = "cancel_reason")
    private String cancelReason;
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    @Column(name = "active")
    private Boolean active;
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "appointmentId")
    private MedicalRecord medicalRecord;
    @JoinColumn(name = "doctor_id", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Doctor doctorId;
    @JoinColumn(name = "service_id", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    private MedicalService serviceId;
    @JoinColumn(name = "patient_id", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Patient patientId;
    @JoinColumn(name = "created_by", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User createdBy;

    public Appointment() {
    }

    public Appointment(Long id) {
        this.id = id;
    }

    public Appointment(Long id, String appointmentCode, Date appointmentDate, Date startTime) {
        this.id = id;
        this.appointmentCode = appointmentCode;
        this.appointmentDate = appointmentDate;
        this.startTime = startTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAppointmentCode() {
        return appointmentCode;
    }

    public void setAppointmentCode(String appointmentCode) {
        this.appointmentCode = appointmentCode;
    }

    public Date getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(Date appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getSymptomNote() {
        return symptomNote;
    }

    public void setSymptomNote(String symptomNote) {
        this.symptomNote = symptomNote;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
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

    public MedicalRecord getMedicalRecord() {
        return medicalRecord;
    }

    public void setMedicalRecord(MedicalRecord medicalRecord) {
        this.medicalRecord = medicalRecord;
    }

    public Doctor getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Doctor doctorId) {
        this.doctorId = doctorId;
    }

    public MedicalService getServiceId() {
        return serviceId;
    }

    public void setServiceId(MedicalService serviceId) {
        this.serviceId = serviceId;
    }

    public Patient getPatientId() {
        return patientId;
    }

    public void setPatientId(Patient patientId) {
        this.patientId = patientId;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Appointment)) {
            return false;
        }
        Appointment other = (Appointment) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.evercare.pojo.Appointment[ id=" + id + " ]";
    }
    
}
