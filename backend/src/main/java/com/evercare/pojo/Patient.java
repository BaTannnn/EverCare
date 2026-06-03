/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.evercare.pojo;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
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
@Table(name = "patient")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Patient.findAll", query = "SELECT p FROM Patient p"),
    @NamedQuery(name = "Patient.findById", query = "SELECT p FROM Patient p WHERE p.id = :id"),
    @NamedQuery(name = "Patient.findByPatientCode", query = "SELECT p FROM Patient p WHERE p.patientCode = :patientCode"),
    @NamedQuery(name = "Patient.findByFullName", query = "SELECT p FROM Patient p WHERE p.fullName = :fullName"),
    @NamedQuery(name = "Patient.findByGender", query = "SELECT p FROM Patient p WHERE p.gender = :gender"),
    @NamedQuery(name = "Patient.findByDateOfBirth", query = "SELECT p FROM Patient p WHERE p.dateOfBirth = :dateOfBirth"),
    @NamedQuery(name = "Patient.findByPhone", query = "SELECT p FROM Patient p WHERE p.phone = :phone"),
    @NamedQuery(name = "Patient.findByEmail", query = "SELECT p FROM Patient p WHERE p.email = :email"),
    @NamedQuery(name = "Patient.findByCitizenId", query = "SELECT p FROM Patient p WHERE p.citizenId = :citizenId"),
    @NamedQuery(name = "Patient.findByHealthInsuranceNo", query = "SELECT p FROM Patient p WHERE p.healthInsuranceNo = :healthInsuranceNo"),
    @NamedQuery(name = "Patient.findByAddress", query = "SELECT p FROM Patient p WHERE p.address = :address"),
    @NamedQuery(name = "Patient.findByEmergencyContactName", query = "SELECT p FROM Patient p WHERE p.emergencyContactName = :emergencyContactName"),
    @NamedQuery(name = "Patient.findByEmergencyContactPhone", query = "SELECT p FROM Patient p WHERE p.emergencyContactPhone = :emergencyContactPhone"),
    @NamedQuery(name = "Patient.findByBloodType", query = "SELECT p FROM Patient p WHERE p.bloodType = :bloodType"),
    @NamedQuery(name = "Patient.findByCreatedAt", query = "SELECT p FROM Patient p WHERE p.createdAt = :createdAt"),
    @NamedQuery(name = "Patient.findByUpdatedAt", query = "SELECT p FROM Patient p WHERE p.updatedAt = :updatedAt"),
    @NamedQuery(name = "Patient.findByActive", query = "SELECT p FROM Patient p WHERE p.active = :active")})
public class Patient implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 30)
    @Column(name = "patient_code")
    private String patientCode;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "full_name")
    private String fullName;
    @Size(max = 20)
    @Column(name = "gender")
    private String gender;
    @Column(name = "date_of_birth")
    @Temporal(TemporalType.DATE)
    private Date dateOfBirth;
    // @Pattern(regexp="^\\(?(\\d{3})\\)?[- ]?(\\d{3})[- ]?(\\d{4})$", message="Invalid phone/fax format, should be as xxx-xxx-xxxx")//if the field contains phone or fax number consider using this annotation to enforce field validation
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "phone")
    private String phone;
    // @Pattern(regexp="[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?", message="Invalid email")//if the field contains email address consider using this annotation to enforce field validation
    @Size(max = 100)
    @Column(name = "email")
    private String email;
    @Size(max = 20)
    @Column(name = "citizen_id")
    private String citizenId;
    @Size(max = 50)
    @Column(name = "health_insurance_no")
    private String healthInsuranceNo;
    @Size(max = 255)
    @Column(name = "address")
    private String address;
    @Size(max = 100)
    @Column(name = "emergency_contact_name")
    private String emergencyContactName;
    @Size(max = 20)
    @Column(name = "emergency_contact_phone")
    private String emergencyContactPhone;
    @Size(max = 5)
    @Column(name = "blood_type")
    private String bloodType;
    @Lob
    @Size(max = 65535)
    @Column(name = "allergy_note")
    private String allergyNote;
    @Lob
    @Size(max = 65535)
    @Column(name = "medical_history_note")
    private String medicalHistoryNote;
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    @Column(name = "active")
    private Boolean active;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "patientId")
    private Set<MedicalRecord> medicalRecordSet;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "patientId")
    private Set<Appointment> appointmentSet;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "patientId")
    private Set<Prescription> prescriptionSet;
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @OneToOne(fetch = FetchType.LAZY)
    private User userId;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "patientId")
    private Set<Invoice> invoiceSet;

    public Patient() {
    }

    public Patient(Long id) {
        this.id = id;
    }

    public Patient(Long id, String patientCode, String fullName, String phone) {
        this.id = id;
        this.patientCode = patientCode;
        this.fullName = fullName;
        this.phone = phone;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPatientCode() {
        return patientCode;
    }

    public void setPatientCode(String patientCode) {
        this.patientCode = patientCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCitizenId() {
        return citizenId;
    }

    public void setCitizenId(String citizenId) {
        this.citizenId = citizenId;
    }

    public String getHealthInsuranceNo() {
        return healthInsuranceNo;
    }

    public void setHealthInsuranceNo(String healthInsuranceNo) {
        this.healthInsuranceNo = healthInsuranceNo;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmergencyContactName() {
        return emergencyContactName;
    }

    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }

    public String getEmergencyContactPhone() {
        return emergencyContactPhone;
    }

    public void setEmergencyContactPhone(String emergencyContactPhone) {
        this.emergencyContactPhone = emergencyContactPhone;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public String getAllergyNote() {
        return allergyNote;
    }

    public void setAllergyNote(String allergyNote) {
        this.allergyNote = allergyNote;
    }

    public String getMedicalHistoryNote() {
        return medicalHistoryNote;
    }

    public void setMedicalHistoryNote(String medicalHistoryNote) {
        this.medicalHistoryNote = medicalHistoryNote;
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

    @XmlTransient
    public Set<MedicalRecord> getMedicalRecordSet() {
        return medicalRecordSet;
    }

    public void setMedicalRecordSet(Set<MedicalRecord> medicalRecordSet) {
        this.medicalRecordSet = medicalRecordSet;
    }

    @XmlTransient
    public Set<Appointment> getAppointmentSet() {
        return appointmentSet;
    }

    public void setAppointmentSet(Set<Appointment> appointmentSet) {
        this.appointmentSet = appointmentSet;
    }

    @XmlTransient
    public Set<Prescription> getPrescriptionSet() {
        return prescriptionSet;
    }

    public void setPrescriptionSet(Set<Prescription> prescriptionSet) {
        this.prescriptionSet = prescriptionSet;
    }

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
    }

    @XmlTransient
    public Set<Invoice> getInvoiceSet() {
        return invoiceSet;
    }

    public void setInvoiceSet(Set<Invoice> invoiceSet) {
        this.invoiceSet = invoiceSet;
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
        if (!(object instanceof Patient)) {
            return false;
        }
        Patient other = (Patient) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.evercare.pojo.Patient[ id=" + id + " ]";
    }
    
}
