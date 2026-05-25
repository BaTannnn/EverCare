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
import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

/**
 *
 * @author cadic
 */
@Entity
@Table(name = "doctor")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Doctor.findAll", query = "SELECT d FROM Doctor d"),
    @NamedQuery(name = "Doctor.findById", query = "SELECT d FROM Doctor d WHERE d.id = :id"),
    @NamedQuery(name = "Doctor.findByDoctorCode", query = "SELECT d FROM Doctor d WHERE d.doctorCode = :doctorCode"),
    @NamedQuery(name = "Doctor.findByFullName", query = "SELECT d FROM Doctor d WHERE d.fullName = :fullName"),
    @NamedQuery(name = "Doctor.findByPhone", query = "SELECT d FROM Doctor d WHERE d.phone = :phone"),
    @NamedQuery(name = "Doctor.findByEmail", query = "SELECT d FROM Doctor d WHERE d.email = :email"),
    @NamedQuery(name = "Doctor.findByAvatarUrl", query = "SELECT d FROM Doctor d WHERE d.avatarUrl = :avatarUrl"),
    @NamedQuery(name = "Doctor.findByQualification", query = "SELECT d FROM Doctor d WHERE d.qualification = :qualification"),
    @NamedQuery(name = "Doctor.findBySpecialization", query = "SELECT d FROM Doctor d WHERE d.specialization = :specialization"),
    @NamedQuery(name = "Doctor.findByDoctorType", query = "SELECT d FROM Doctor d WHERE d.doctorType = :doctorType"),
    @NamedQuery(name = "Doctor.findByWorkStatus", query = "SELECT d FROM Doctor d WHERE d.workStatus = :workStatus"),
    @NamedQuery(name = "Doctor.findByBaseSalary", query = "SELECT d FROM Doctor d WHERE d.baseSalary = :baseSalary"),
    @NamedQuery(name = "Doctor.findByHourlyRate", query = "SELECT d FROM Doctor d WHERE d.hourlyRate = :hourlyRate"),
    @NamedQuery(name = "Doctor.findByCreatedAt", query = "SELECT d FROM Doctor d WHERE d.createdAt = :createdAt"),
    @NamedQuery(name = "Doctor.findByUpdatedAt", query = "SELECT d FROM Doctor d WHERE d.updatedAt = :updatedAt"),
    @NamedQuery(name = "Doctor.findByActive", query = "SELECT d FROM Doctor d WHERE d.active = :active")})
public class Doctor implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Size(min = 1, max = 30)
    @Column(name = "doctor_code", nullable = false, insertable = false)
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
    @Size(max = 30)
    @Column(name = "doctor_type")
    private String doctorType;
    @Size(max = 30)
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
    @Column(name = "created_at", insertable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Column(name = "updated_at", insertable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    @Column(name = "active")
    private Boolean active;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "doctorId")
    private Set<DoctorSchedule> doctorScheduleSet;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "doctorId")
    private Set<MedicalRecord> medicalRecordSet;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "doctorId")
    private Set<Appointment> appointmentSet;
    @JoinColumn(name = "department_id", referencedColumnName = "id")
    @ManyToOne
    private Department departmentId;
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @OneToOne
    private User userId;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "doctorId")
    private Set<Prescription> prescriptionSet;

    public Doctor() {
    }

    public Doctor(Long id) {
        this.id = id;
    }

    public Doctor(Long id, String doctorCode, String fullName) {
        this.id = id;
        this.doctorCode = doctorCode;
        this.fullName = fullName;
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @XmlTransient
    public Set<DoctorSchedule> getDoctorScheduleSet() {
        return doctorScheduleSet;
    }

    public void setDoctorScheduleSet(Set<DoctorSchedule> doctorScheduleSet) {
        this.doctorScheduleSet = doctorScheduleSet;
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

    public Department getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Department departmentId) {
        this.departmentId = departmentId;
    }

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
    }

    @XmlTransient
    public Set<Prescription> getPrescriptionSet() {
        return prescriptionSet;
    }

    public void setPrescriptionSet(Set<Prescription> prescriptionSet) {
        this.prescriptionSet = prescriptionSet;
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
        if (!(object instanceof Doctor)) {
            return false;
        }
        Doctor other = (Doctor) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.evercare.pojo.Doctor[ id=" + id + " ]";
    }
    
}
