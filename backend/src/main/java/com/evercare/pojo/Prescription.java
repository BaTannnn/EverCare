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
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

/**
 *
 * @author cadic
 */
@Entity
@Table(name = "prescription")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Prescription.findAll", query = "SELECT p FROM Prescription p"),
    @NamedQuery(name = "Prescription.findById", query = "SELECT p FROM Prescription p WHERE p.id = :id"),
    @NamedQuery(name = "Prescription.findByPrescriptionCode", query = "SELECT p FROM Prescription p WHERE p.prescriptionCode = :prescriptionCode"),
    @NamedQuery(name = "Prescription.findByPrescribedAt", query = "SELECT p FROM Prescription p WHERE p.prescribedAt = :prescribedAt"),
    @NamedQuery(name = "Prescription.findByStatus", query = "SELECT p FROM Prescription p WHERE p.status = :status"),
    @NamedQuery(name = "Prescription.findByCreatedAt", query = "SELECT p FROM Prescription p WHERE p.createdAt = :createdAt"),
    @NamedQuery(name = "Prescription.findByUpdatedAt", query = "SELECT p FROM Prescription p WHERE p.updatedAt = :updatedAt"),
    @NamedQuery(name = "Prescription.findByActive", query = "SELECT p FROM Prescription p WHERE p.active = :active")})
public class Prescription implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 30)
    @Column(name = "prescription_code")
    private String prescriptionCode;
    @Column(name = "prescribed_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date prescribedAt;
    @Size(max = 30)
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
    @JoinColumn(name = "doctor_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Doctor doctorId;
    @JoinColumn(name = "medical_record_id", referencedColumnName = "id")
    @OneToOne(optional = false)
    private MedicalRecord medicalRecordId;
    @JoinColumn(name = "patient_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Patient patientId;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "prescriptionId")
    private Set<PrescriptionItem> prescriptionItemSet;

    public Prescription() {
    }

    public Prescription(Long id) {
        this.id = id;
    }

    public Prescription(Long id, String prescriptionCode) {
        this.id = id;
        this.prescriptionCode = prescriptionCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPrescriptionCode() {
        return prescriptionCode;
    }

    public void setPrescriptionCode(String prescriptionCode) {
        this.prescriptionCode = prescriptionCode;
    }

    public Date getPrescribedAt() {
        return prescribedAt;
    }

    public void setPrescribedAt(Date prescribedAt) {
        this.prescribedAt = prescribedAt;
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

    public Doctor getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Doctor doctorId) {
        this.doctorId = doctorId;
    }

    public MedicalRecord getMedicalRecordId() {
        return medicalRecordId;
    }

    public void setMedicalRecordId(MedicalRecord medicalRecordId) {
        this.medicalRecordId = medicalRecordId;
    }

    public Patient getPatientId() {
        return patientId;
    }

    public void setPatientId(Patient patientId) {
        this.patientId = patientId;
    }

    @XmlTransient
    public Set<PrescriptionItem> getPrescriptionItemSet() {
        return prescriptionItemSet;
    }

    public void setPrescriptionItemSet(Set<PrescriptionItem> prescriptionItemSet) {
        this.prescriptionItemSet = prescriptionItemSet;
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
        if (!(object instanceof Prescription)) {
            return false;
        }
        Prescription other = (Prescription) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.evercare.pojo.Prescription[ id=" + id + " ]";
    }
    
}
