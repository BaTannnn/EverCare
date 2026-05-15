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
@Table(name = "patients")
@NamedQueries({
    @NamedQuery(name = "Patients.findAll", query = "SELECT p FROM Patients p"),
    @NamedQuery(name = "Patients.findById", query = "SELECT p FROM Patients p WHERE p.id = :id"),
    @NamedQuery(name = "Patients.findByPatientCode", query = "SELECT p FROM Patients p WHERE p.patientCode = :patientCode"),
    @NamedQuery(name = "Patients.findByFullName", query = "SELECT p FROM Patients p WHERE p.fullName = :fullName"),
    @NamedQuery(name = "Patients.findByGender", query = "SELECT p FROM Patients p WHERE p.gender = :gender"),
    @NamedQuery(name = "Patients.findByDateOfBirth", query = "SELECT p FROM Patients p WHERE p.dateOfBirth = :dateOfBirth"),
    @NamedQuery(name = "Patients.findByPhone", query = "SELECT p FROM Patients p WHERE p.phone = :phone"),
    @NamedQuery(name = "Patients.findByEmail", query = "SELECT p FROM Patients p WHERE p.email = :email"),
    @NamedQuery(name = "Patients.findByCitizenId", query = "SELECT p FROM Patients p WHERE p.citizenId = :citizenId"),
    @NamedQuery(name = "Patients.findByHealthInsuranceNo", query = "SELECT p FROM Patients p WHERE p.healthInsuranceNo = :healthInsuranceNo"),
    @NamedQuery(name = "Patients.findByAddress", query = "SELECT p FROM Patients p WHERE p.address = :address"),
    @NamedQuery(name = "Patients.findByEmergencyContactName", query = "SELECT p FROM Patients p WHERE p.emergencyContactName = :emergencyContactName"),
    @NamedQuery(name = "Patients.findByEmergencyContactPhone", query = "SELECT p FROM Patients p WHERE p.emergencyContactPhone = :emergencyContactPhone"),
    @NamedQuery(name = "Patients.findByBloodType", query = "SELECT p FROM Patients p WHERE p.bloodType = :bloodType"),
    @NamedQuery(name = "Patients.findByCreatedAt", query = "SELECT p FROM Patients p WHERE p.createdAt = :createdAt"),
    @NamedQuery(name = "Patients.findByUpdatedAt", query = "SELECT p FROM Patients p WHERE p.updatedAt = :updatedAt"),
    @NamedQuery(name = "Patients.findByActive", query = "SELECT p FROM Patients p WHERE p.active = :active")})
public class Patients implements Serializable {

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
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "patientId")
    private Collection<Appointments> appointmentsCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "patientId")
    private Collection<MedicalRecords> medicalRecordsCollection;
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @OneToOne
    private Users userId;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "patientId")
    private Collection<Invoices> invoicesCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "patientId")
    private Collection<Prescriptions> prescriptionsCollection;

    public Patients() {
    }

    public Patients(Long id) {
        this.id = id;
    }

    public Patients(Long id, String patientCode, String fullName, String phone, Date createdAt, Date updatedAt, boolean active) {
        this.id = id;
        this.patientCode = patientCode;
        this.fullName = fullName;
        this.phone = phone;
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

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Collection<Appointments> getAppointmentsCollection() {
        return appointmentsCollection;
    }

    public void setAppointmentsCollection(Collection<Appointments> appointmentsCollection) {
        this.appointmentsCollection = appointmentsCollection;
    }

    public Collection<MedicalRecords> getMedicalRecordsCollection() {
        return medicalRecordsCollection;
    }

    public void setMedicalRecordsCollection(Collection<MedicalRecords> medicalRecordsCollection) {
        this.medicalRecordsCollection = medicalRecordsCollection;
    }

    public Users getUserId() {
        return userId;
    }

    public void setUserId(Users userId) {
        this.userId = userId;
    }

    public Collection<Invoices> getInvoicesCollection() {
        return invoicesCollection;
    }

    public void setInvoicesCollection(Collection<Invoices> invoicesCollection) {
        this.invoicesCollection = invoicesCollection;
    }

    public Collection<Prescriptions> getPrescriptionsCollection() {
        return prescriptionsCollection;
    }

    public void setPrescriptionsCollection(Collection<Prescriptions> prescriptionsCollection) {
        this.prescriptionsCollection = prescriptionsCollection;
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
        if (!(object instanceof Patients)) {
            return false;
        }
        Patients other = (Patients) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.evercare.pojo.Patients[ id=" + id + " ]";
    }
    
}
