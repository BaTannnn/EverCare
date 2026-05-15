/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.evercare.pojo;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
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
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author batan
 */
@Entity
@Table(name = "appointments")
@NamedQueries({
    @NamedQuery(name = "Appointments.findAll", query = "SELECT a FROM Appointments a"),
    @NamedQuery(name = "Appointments.findById", query = "SELECT a FROM Appointments a WHERE a.id = :id"),
    @NamedQuery(name = "Appointments.findByAppointmentCode", query = "SELECT a FROM Appointments a WHERE a.appointmentCode = :appointmentCode"),
    @NamedQuery(name = "Appointments.findByAppointmentDate", query = "SELECT a FROM Appointments a WHERE a.appointmentDate = :appointmentDate"),
    @NamedQuery(name = "Appointments.findByStartTime", query = "SELECT a FROM Appointments a WHERE a.startTime = :startTime"),
    @NamedQuery(name = "Appointments.findByEndTime", query = "SELECT a FROM Appointments a WHERE a.endTime = :endTime"),
    @NamedQuery(name = "Appointments.findByStatus", query = "SELECT a FROM Appointments a WHERE a.status = :status"),
    @NamedQuery(name = "Appointments.findByReason", query = "SELECT a FROM Appointments a WHERE a.reason = :reason"),
    @NamedQuery(name = "Appointments.findByCancelReason", query = "SELECT a FROM Appointments a WHERE a.cancelReason = :cancelReason"),
    @NamedQuery(name = "Appointments.findByCreatedAt", query = "SELECT a FROM Appointments a WHERE a.createdAt = :createdAt"),
    @NamedQuery(name = "Appointments.findByUpdatedAt", query = "SELECT a FROM Appointments a WHERE a.updatedAt = :updatedAt"),
    @NamedQuery(name = "Appointments.findByActive", query = "SELECT a FROM Appointments a WHERE a.active = :active")})
public class Appointments implements Serializable {

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
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 40)
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
    @Basic(optional = false)
    @NotNull
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Basic(optional = false)
    @NotNull
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    @Basic(optional = false)
    @NotNull
    @Column(name = "active")
    private boolean active;
    @JoinColumn(name = "doctor_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Doctors doctorId;
    @JoinColumn(name = "service_id", referencedColumnName = "id")
    @ManyToOne
    private MedicalServices serviceId;
    @JoinColumn(name = "patient_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Patients patientId;
    @JoinColumn(name = "created_by", referencedColumnName = "id")
    @ManyToOne
    private Users createdBy;
    @OneToOne(mappedBy = "appointmentId")
    private MedicalRecords medicalRecords;
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "appointmentId")
    private OnlineConsultations onlineConsultations;

    public Appointments() {
    }

    public Appointments(Long id) {
        this.id = id;
    }

    public Appointments(Long id, String appointmentCode, Date appointmentDate, Date startTime, String status, Date createdAt, Date updatedAt, boolean active) {
        this.id = id;
        this.appointmentCode = appointmentCode;
        this.appointmentDate = appointmentDate;
        this.startTime = startTime;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.active = active;
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

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Doctors getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Doctors doctorId) {
        this.doctorId = doctorId;
    }

    public MedicalServices getServiceId() {
        return serviceId;
    }

    public void setServiceId(MedicalServices serviceId) {
        this.serviceId = serviceId;
    }

    public Patients getPatientId() {
        return patientId;
    }

    public void setPatientId(Patients patientId) {
        this.patientId = patientId;
    }

    public Users getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Users createdBy) {
        this.createdBy = createdBy;
    }

    public MedicalRecords getMedicalRecords() {
        return medicalRecords;
    }

    public void setMedicalRecords(MedicalRecords medicalRecords) {
        this.medicalRecords = medicalRecords;
    }

    public OnlineConsultations getOnlineConsultations() {
        return onlineConsultations;
    }

    public void setOnlineConsultations(OnlineConsultations onlineConsultations) {
        this.onlineConsultations = onlineConsultations;
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
        if (!(object instanceof Appointments)) {
            return false;
        }
        Appointments other = (Appointments) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.evercare.pojo.Appointments[ id=" + id + " ]";
    }
    
}
