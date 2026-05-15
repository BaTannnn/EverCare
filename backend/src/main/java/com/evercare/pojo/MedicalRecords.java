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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

/**
 *
 * @author batan
 */
@Entity
@Table(name = "medical_records")
@NamedQueries({
    @NamedQuery(name = "MedicalRecords.findAll", query = "SELECT m FROM MedicalRecords m"),
    @NamedQuery(name = "MedicalRecords.findById", query = "SELECT m FROM MedicalRecords m WHERE m.id = :id"),
    @NamedQuery(name = "MedicalRecords.findByRecordCode", query = "SELECT m FROM MedicalRecords m WHERE m.recordCode = :recordCode"),
    @NamedQuery(name = "MedicalRecords.findByVisitDate", query = "SELECT m FROM MedicalRecords m WHERE m.visitDate = :visitDate"),
    @NamedQuery(name = "MedicalRecords.findByPaymentStatus", query = "SELECT m FROM MedicalRecords m WHERE m.paymentStatus = :paymentStatus"),
    @NamedQuery(name = "MedicalRecords.findByCreatedAt", query = "SELECT m FROM MedicalRecords m WHERE m.createdAt = :createdAt"),
    @NamedQuery(name = "MedicalRecords.findByUpdatedAt", query = "SELECT m FROM MedicalRecords m WHERE m.updatedAt = :updatedAt"),
    @NamedQuery(name = "MedicalRecords.findByActive", query = "SELECT m FROM MedicalRecords m WHERE m.active = :active")})
public class MedicalRecords implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 30)
    @Column(name = "record_code")
    private String recordCode;
    @Basic(optional = false)
    @NotNull
    @Column(name = "visit_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date visitDate;
    @Lob
    @Size(max = 65535)
    @Column(name = "chief_complaint")
    private String chiefComplaint;
    @Lob
    @Size(max = 65535)
    @Column(name = "diagnosis")
    private String diagnosis;
    @Lob
    @Size(max = 65535)
    @Column(name = "treatment_plan")
    private String treatmentPlan;
    @Lob
    @Size(max = 65535)
    @Column(name = "doctor_note")
    private String doctorNote;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 30)
    @Column(name = "payment_status")
    private String paymentStatus;
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
    @JoinColumn(name = "appointment_id", referencedColumnName = "id")
    @OneToOne
    private Appointments appointmentId;
    @JoinColumn(name = "doctor_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Doctors doctorId;
    @JoinColumn(name = "patient_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Patients patientId;
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "medicalRecordId")
    private Invoices invoices;
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "medicalRecordId")
    private Prescriptions prescriptions;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "medicalRecordId")
    private Collection<TestResults> testResultsCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "medicalRecordId")
    private Collection<MedicalRecordServices> medicalRecordServicesCollection;

    public MedicalRecords() {
    }

    public MedicalRecords(Long id) {
        this.id = id;
    }

    public MedicalRecords(Long id, String recordCode, Date visitDate, String paymentStatus, Date createdAt, Date updatedAt, boolean active) {
        this.id = id;
        this.recordCode = recordCode;
        this.visitDate = visitDate;
        this.paymentStatus = paymentStatus;
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

    public String getRecordCode() {
        return recordCode;
    }

    public void setRecordCode(String recordCode) {
        this.recordCode = recordCode;
    }

    public Date getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(Date visitDate) {
        this.visitDate = visitDate;
    }

    public String getChiefComplaint() {
        return chiefComplaint;
    }

    public void setChiefComplaint(String chiefComplaint) {
        this.chiefComplaint = chiefComplaint;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getTreatmentPlan() {
        return treatmentPlan;
    }

    public void setTreatmentPlan(String treatmentPlan) {
        this.treatmentPlan = treatmentPlan;
    }

    public String getDoctorNote() {
        return doctorNote;
    }

    public void setDoctorNote(String doctorNote) {
        this.doctorNote = doctorNote;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
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

    public Appointments getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Appointments appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Doctors getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Doctors doctorId) {
        this.doctorId = doctorId;
    }

    public Patients getPatientId() {
        return patientId;
    }

    public void setPatientId(Patients patientId) {
        this.patientId = patientId;
    }

    public Invoices getInvoices() {
        return invoices;
    }

    public void setInvoices(Invoices invoices) {
        this.invoices = invoices;
    }

    public Prescriptions getPrescriptions() {
        return prescriptions;
    }

    public void setPrescriptions(Prescriptions prescriptions) {
        this.prescriptions = prescriptions;
    }

    public Collection<TestResults> getTestResultsCollection() {
        return testResultsCollection;
    }

    public void setTestResultsCollection(Collection<TestResults> testResultsCollection) {
        this.testResultsCollection = testResultsCollection;
    }

    public Collection<MedicalRecordServices> getMedicalRecordServicesCollection() {
        return medicalRecordServicesCollection;
    }

    public void setMedicalRecordServicesCollection(Collection<MedicalRecordServices> medicalRecordServicesCollection) {
        this.medicalRecordServicesCollection = medicalRecordServicesCollection;
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
        if (!(object instanceof MedicalRecords)) {
            return false;
        }
        MedicalRecords other = (MedicalRecords) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.evercare.pojo.MedicalRecords[ id=" + id + " ]";
    }
    
}
