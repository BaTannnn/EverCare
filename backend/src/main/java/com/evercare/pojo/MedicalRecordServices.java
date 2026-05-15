/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.evercare.pojo;

import jakarta.persistence.Basic;
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
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author batan
 */
@Entity
@Table(name = "medical_record_services")
@NamedQueries({
    @NamedQuery(name = "MedicalRecordServices.findAll", query = "SELECT m FROM MedicalRecordServices m"),
    @NamedQuery(name = "MedicalRecordServices.findById", query = "SELECT m FROM MedicalRecordServices m WHERE m.id = :id"),
    @NamedQuery(name = "MedicalRecordServices.findByQuantity", query = "SELECT m FROM MedicalRecordServices m WHERE m.quantity = :quantity"),
    @NamedQuery(name = "MedicalRecordServices.findByUnitPrice", query = "SELECT m FROM MedicalRecordServices m WHERE m.unitPrice = :unitPrice"),
    @NamedQuery(name = "MedicalRecordServices.findByCreatedAt", query = "SELECT m FROM MedicalRecordServices m WHERE m.createdAt = :createdAt"),
    @NamedQuery(name = "MedicalRecordServices.findByUpdatedAt", query = "SELECT m FROM MedicalRecordServices m WHERE m.updatedAt = :updatedAt"),
    @NamedQuery(name = "MedicalRecordServices.findByActive", query = "SELECT m FROM MedicalRecordServices m WHERE m.active = :active")})
public class MedicalRecordServices implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Basic(optional = false)
    @NotNull
    @Column(name = "quantity")
    private int quantity;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Basic(optional = false)
    @NotNull
    @Column(name = "unit_price")
    private BigDecimal unitPrice;
    @Lob
    @Size(max = 65535)
    @Column(name = "result_summary")
    private String resultSummary;
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
    @JoinColumn(name = "medical_record_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private MedicalRecords medicalRecordId;
    @JoinColumn(name = "service_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private MedicalServices serviceId;

    public MedicalRecordServices() {
    }

    public MedicalRecordServices(Long id) {
        this.id = id;
    }

    public MedicalRecordServices(Long id, int quantity, BigDecimal unitPrice, Date createdAt, Date updatedAt, boolean active) {
        this.id = id;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getResultSummary() {
        return resultSummary;
    }

    public void setResultSummary(String resultSummary) {
        this.resultSummary = resultSummary;
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

    public MedicalRecords getMedicalRecordId() {
        return medicalRecordId;
    }

    public void setMedicalRecordId(MedicalRecords medicalRecordId) {
        this.medicalRecordId = medicalRecordId;
    }

    public MedicalServices getServiceId() {
        return serviceId;
    }

    public void setServiceId(MedicalServices serviceId) {
        this.serviceId = serviceId;
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
        if (!(object instanceof MedicalRecordServices)) {
            return false;
        }
        MedicalRecordServices other = (MedicalRecordServices) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.evercare.pojo.MedicalRecordServices[ id=" + id + " ]";
    }
    
}
