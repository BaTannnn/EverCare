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
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

/**
 *
 * @author batan
 */
@Entity
@Table(name = "doctors")
@NamedQueries({
    @NamedQuery(name = "Doctors.findAll", query = "SELECT d FROM Doctors d"),
    @NamedQuery(name = "Doctors.findById", query = "SELECT d FROM Doctors d WHERE d.id = :id"),
    @NamedQuery(name = "Doctors.findByDoctorCode", query = "SELECT d FROM Doctors d WHERE d.doctorCode = :doctorCode"),
    @NamedQuery(name = "Doctors.findByFullName", query = "SELECT d FROM Doctors d WHERE d.fullName = :fullName"),
    @NamedQuery(name = "Doctors.findByPhone", query = "SELECT d FROM Doctors d WHERE d.phone = :phone"),
    @NamedQuery(name = "Doctors.findByEmail", query = "SELECT d FROM Doctors d WHERE d.email = :email"),
    @NamedQuery(name = "Doctors.findByAvatarUrl", query = "SELECT d FROM Doctors d WHERE d.avatarUrl = :avatarUrl"),
    @NamedQuery(name = "Doctors.findByQualification", query = "SELECT d FROM Doctors d WHERE d.qualification = :qualification"),
    @NamedQuery(name = "Doctors.findBySpecialization", query = "SELECT d FROM Doctors d WHERE d.specialization = :specialization"),
    @NamedQuery(name = "Doctors.findByDoctorType", query = "SELECT d FROM Doctors d WHERE d.doctorType = :doctorType"),
    @NamedQuery(name = "Doctors.findByWorkStatus", query = "SELECT d FROM Doctors d WHERE d.workStatus = :workStatus"),
    @NamedQuery(name = "Doctors.findByBaseSalary", query = "SELECT d FROM Doctors d WHERE d.baseSalary = :baseSalary"),
    @NamedQuery(name = "Doctors.findByHourlyRate", query = "SELECT d FROM Doctors d WHERE d.hourlyRate = :hourlyRate"),
    @NamedQuery(name = "Doctors.findByCreatedAt", query = "SELECT d FROM Doctors d WHERE d.createdAt = :createdAt"),
    @NamedQuery(name = "Doctors.findByUpdatedAt", query = "SELECT d FROM Doctors d WHERE d.updatedAt = :updatedAt"),
    @NamedQuery(name = "Doctors.findByActive", query = "SELECT d FROM Doctors d WHERE d.active = :active")})
public class Doctors implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 30)
    @Column(name = "doctor_code")
    private String doctorCode;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "full_name")
    private String fullName;
    // @Pattern(regexp="^\\(?(\\d{3})\\)?[- ]?(\\d{3})[- ]?(\\d{4})$", message="Invalid phone/fax format, should be as xxx-xxx-xxxx")//if the field contains phone or fax number consider using this annotation to enforce field validation
    @Size(max = 20)
    @Column(name = "phone")
    private String phone;
    // @Pattern(regexp="[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?", message="Invalid email")//if the field contains email address consider using this annotation to enforce field validation
    @Size(max = 100)
    @Column(name = "email")
    private String email;
    @Size(max = 255)
    @Column(name = "avatar_url")
    private String avatarUrl;
    @Size(max = 150)
    @Column(name = "qualification")
    private String qualification;
    @Size(max = 150)
    @Column(name = "specialization")
    private String specialization;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 30)
    @Column(name = "doctor_type")
    private String doctorType;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 30)
    @Column(name = "work_status")
    private String workStatus;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "base_salary")
    private BigDecimal baseSalary;
    @Column(name = "hourly_rate")
    private BigDecimal hourlyRate;
    @Lob
    @Size(max = 65535)
    @Column(name = "bio")
    private String bio;
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
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "doctorId")
    private Collection<Appointments> appointmentsCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "doctorId")
    private Collection<MedicalRecords> medicalRecordsCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "doctorId")
    private Collection<DoctorSchedules> doctorSchedulesCollection;
    @JoinColumn(name = "department_id", referencedColumnName = "id")
    @ManyToOne
    private Departments departmentId;
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @OneToOne
    private Users userId;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "doctorId")
    private Collection<Prescriptions> prescriptionsCollection;

    public Doctors() {
    }

    public Doctors(Long id) {
        this.id = id;
    }

    public Doctors(Long id, String doctorCode, String fullName, String doctorType, String workStatus, Date createdAt, Date updatedAt, boolean active) {
        this.id = id;
        this.doctorCode = doctorCode;
        this.fullName = fullName;
        this.doctorType = doctorType;
        this.workStatus = workStatus;
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

    public String getDoctorCode() {
        return doctorCode;
    }

    public void setDoctorCode(String doctorCode) {
        this.doctorCode = doctorCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getDoctorType() {
        return doctorType;
    }

    public void setDoctorType(String doctorType) {
        this.doctorType = doctorType;
    }

    public String getWorkStatus() {
        return workStatus;
    }

    public void setWorkStatus(String workStatus) {
        this.workStatus = workStatus;
    }

    public BigDecimal getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(BigDecimal baseSalary) {
        this.baseSalary = baseSalary;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
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

    public Collection<DoctorSchedules> getDoctorSchedulesCollection() {
        return doctorSchedulesCollection;
    }

    public void setDoctorSchedulesCollection(Collection<DoctorSchedules> doctorSchedulesCollection) {
        this.doctorSchedulesCollection = doctorSchedulesCollection;
    }

    public Departments getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Departments departmentId) {
        this.departmentId = departmentId;
    }

    public Users getUserId() {
        return userId;
    }

    public void setUserId(Users userId) {
        this.userId = userId;
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
        if (!(object instanceof Doctors)) {
            return false;
        }
        Doctors other = (Doctors) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.evercare.pojo.Doctors[ id=" + id + " ]";
    }
    
}
