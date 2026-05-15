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
@Table(name = "medical_services")
@NamedQueries({
    @NamedQuery(name = "MedicalServices.findAll", query = "SELECT m FROM MedicalServices m"),
    @NamedQuery(name = "MedicalServices.findById", query = "SELECT m FROM MedicalServices m WHERE m.id = :id"),
    @NamedQuery(name = "MedicalServices.findByCode", query = "SELECT m FROM MedicalServices m WHERE m.code = :code"),
    @NamedQuery(name = "MedicalServices.findByName", query = "SELECT m FROM MedicalServices m WHERE m.name = :name"),
    @NamedQuery(name = "MedicalServices.findByPrice", query = "SELECT m FROM MedicalServices m WHERE m.price = :price"),
    @NamedQuery(name = "MedicalServices.findByServiceType", query = "SELECT m FROM MedicalServices m WHERE m.serviceType = :serviceType"),
    @NamedQuery(name = "MedicalServices.findByCreatedAt", query = "SELECT m FROM MedicalServices m WHERE m.createdAt = :createdAt"),
    @NamedQuery(name = "MedicalServices.findByUpdatedAt", query = "SELECT m FROM MedicalServices m WHERE m.updatedAt = :updatedAt"),
    @NamedQuery(name = "MedicalServices.findByActive", query = "SELECT m FROM MedicalServices m WHERE m.active = :active")})
public class MedicalServices implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 30)
    @Column(name = "code")
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
    @Basic(optional = false)
    @NotNull
    @Column(name = "price")
    private BigDecimal price;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "service_type")
    private String serviceType;
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
    @OneToMany(mappedBy = "serviceId")
    private Collection<Appointments> appointmentsCollection;
    @JoinColumn(name = "department_id", referencedColumnName = "id")
    @ManyToOne
    private Departments departmentId;
    @OneToMany(mappedBy = "serviceId")
    private Collection<TestResults> testResultsCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "serviceId")
    private Collection<MedicalRecordServices> medicalRecordServicesCollection;

    public MedicalServices() {
    }

    public MedicalServices(Long id) {
        this.id = id;
    }

    public MedicalServices(Long id, String code, String name, BigDecimal price, String serviceType, Date createdAt, Date updatedAt, boolean active) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.price = price;
        this.serviceType = serviceType;
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

    public Departments getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Departments departmentId) {
        this.departmentId = departmentId;
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
        if (!(object instanceof MedicalServices)) {
            return false;
        }
        MedicalServices other = (MedicalServices) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.evercare.pojo.MedicalServices[ id=" + id + " ]";
    }
    
}
