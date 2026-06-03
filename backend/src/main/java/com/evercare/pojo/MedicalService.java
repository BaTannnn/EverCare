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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
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
@Table(name = "medical_service")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MedicalService.findAll", query = "SELECT m FROM MedicalService m"),
    @NamedQuery(name = "MedicalService.findById", query = "SELECT m FROM MedicalService m WHERE m.id = :id"),
    @NamedQuery(name = "MedicalService.findByCode", query = "SELECT m FROM MedicalService m WHERE m.code = :code"),
    @NamedQuery(name = "MedicalService.findByName", query = "SELECT m FROM MedicalService m WHERE m.name = :name"),
    @NamedQuery(name = "MedicalService.findByPrice", query = "SELECT m FROM MedicalService m WHERE m.price = :price"),
    @NamedQuery(name = "MedicalService.findByServiceType", query = "SELECT m FROM MedicalService m WHERE m.serviceType = :serviceType"),
    @NamedQuery(name = "MedicalService.findByCreatedAt", query = "SELECT m FROM MedicalService m WHERE m.createdAt = :createdAt"),
    @NamedQuery(name = "MedicalService.findByUpdatedAt", query = "SELECT m FROM MedicalService m WHERE m.updatedAt = :updatedAt"),
    @NamedQuery(name = "MedicalService.findByActive", query = "SELECT m FROM MedicalService m WHERE m.active = :active")})
public class MedicalService implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Size(min = 1, max = 30)
    @Column(name = "code", insertable = false, updatable = false)
    private String code;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 150)
    @Column(name = "name")
    private String name;
    @Lob
    @Size(max = 65535)
    @Column(name = "description")
    private String description;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "price")
    private BigDecimal price;
    @Size(max = 50)
    @Column(name = "service_type")
    private String serviceType;
    @Column(name = "created_at", insertable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Column(name = "updated_at", insertable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    @Column(name = "active")
    private Boolean active;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "serviceId")
    private Set<MedicalRecordService> medicalRecordServiceSet;
    @JoinColumn(name = "department_id", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Department departmentId;
    @OneToMany(mappedBy = "serviceId")
    private Set<Appointment> appointmentSet;
    @OneToMany(mappedBy = "serviceId")
    private Set<TestResult> testResultSet;

    public MedicalService() {
    }

    public MedicalService(Long id) {
        this.id = id;
    }

    public MedicalService(Long id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
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
    public Set<MedicalRecordService> getMedicalRecordServiceSet() {
        return medicalRecordServiceSet;
    }

    public void setMedicalRecordServiceSet(Set<MedicalRecordService> medicalRecordServiceSet) {
        this.medicalRecordServiceSet = medicalRecordServiceSet;
    }

    public Department getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Department departmentId) {
        this.departmentId = departmentId;
    }

    @XmlTransient
    public Set<Appointment> getAppointmentSet() {
        return appointmentSet;
    }

    public void setAppointmentSet(Set<Appointment> appointmentSet) {
        this.appointmentSet = appointmentSet;
    }

    @XmlTransient
    public Set<TestResult> getTestResultSet() {
        return testResultSet;
    }

    public void setTestResultSet(Set<TestResult> testResultSet) {
        this.testResultSet = testResultSet;
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
        if (!(object instanceof MedicalService)) {
            return false;
        }
        MedicalService other = (MedicalService) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.evercare.pojo.MedicalService[ id=" + id + " ]";
    }
    
}
