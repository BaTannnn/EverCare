/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.evercare.pojo;

import jakarta.persistence.Basic;
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
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author cadic
 */
@Entity
@Table(name = "medical_record_service")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MedicalRecordService.findAll", query = "SELECT m FROM MedicalRecordService m"),
    @NamedQuery(name = "MedicalRecordService.findById", query = "SELECT m FROM MedicalRecordService m WHERE m.id = :id"),
    @NamedQuery(name = "MedicalRecordService.findByQuantity", query = "SELECT m FROM MedicalRecordService m WHERE m.quantity = :quantity"),
    @NamedQuery(name = "MedicalRecordService.findByUnitPrice", query = "SELECT m FROM MedicalRecordService m WHERE m.unitPrice = :unitPrice"),
    @NamedQuery(name = "MedicalRecordService.findByCreatedAt", query = "SELECT m FROM MedicalRecordService m WHERE m.createdAt = :createdAt"),
    @NamedQuery(name = "MedicalRecordService.findByUpdatedAt", query = "SELECT m FROM MedicalRecordService m WHERE m.updatedAt = :updatedAt"),
    @NamedQuery(name = "MedicalRecordService.findByActive", query = "SELECT m FROM MedicalRecordService m WHERE m.active = :active")})
public class MedicalRecordService implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Column(name = "quantity")
    private Integer quantity;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "unit_price")
    private BigDecimal unitPrice;
    @Lob
    @Size(max = 65535)
    @Column(name = "result_summary")
    private String resultSummary;
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    @Column(name = "active")
    private Boolean active;
    @JoinColumn(name = "medical_record_id", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private MedicalRecord medicalRecordId;
    @JoinColumn(name = "service_id", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private MedicalService serviceId;

    public MedicalRecordService() {
    }

    public MedicalRecordService(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public MedicalRecord getMedicalRecordId() {
        return medicalRecordId;
    }

    public void setMedicalRecordId(MedicalRecord medicalRecordId) {
        this.medicalRecordId = medicalRecordId;
    }

    public MedicalService getServiceId() {
        return serviceId;
    }

    public void setServiceId(MedicalService serviceId) {
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
        if (!(object instanceof MedicalRecordService)) {
            return false;
        }
        MedicalRecordService other = (MedicalRecordService) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.evercare.pojo.MedicalRecordService[ id=" + id + " ]";
    }
    
}
