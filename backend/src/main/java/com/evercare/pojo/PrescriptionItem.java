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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
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
@Table(name = "prescription_item")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "PrescriptionItem.findAll", query = "SELECT p FROM PrescriptionItem p"),
    @NamedQuery(name = "PrescriptionItem.findById", query = "SELECT p FROM PrescriptionItem p WHERE p.id = :id"),
    @NamedQuery(name = "PrescriptionItem.findByQuantity", query = "SELECT p FROM PrescriptionItem p WHERE p.quantity = :quantity"),
    @NamedQuery(name = "PrescriptionItem.findByUnitPrice", query = "SELECT p FROM PrescriptionItem p WHERE p.unitPrice = :unitPrice"),
    @NamedQuery(name = "PrescriptionItem.findByDosage", query = "SELECT p FROM PrescriptionItem p WHERE p.dosage = :dosage"),
    @NamedQuery(name = "PrescriptionItem.findByFrequency", query = "SELECT p FROM PrescriptionItem p WHERE p.frequency = :frequency"),
    @NamedQuery(name = "PrescriptionItem.findByDuration", query = "SELECT p FROM PrescriptionItem p WHERE p.duration = :duration"),
    @NamedQuery(name = "PrescriptionItem.findByInstruction", query = "SELECT p FROM PrescriptionItem p WHERE p.instruction = :instruction"),
    @NamedQuery(name = "PrescriptionItem.findByCreatedAt", query = "SELECT p FROM PrescriptionItem p WHERE p.createdAt = :createdAt"),
    @NamedQuery(name = "PrescriptionItem.findByUpdatedAt", query = "SELECT p FROM PrescriptionItem p WHERE p.updatedAt = :updatedAt"),
    @NamedQuery(name = "PrescriptionItem.findByActive", query = "SELECT p FROM PrescriptionItem p WHERE p.active = :active")})
public class PrescriptionItem implements Serializable {

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
    @Size(max = 100)
    @Column(name = "dosage")
    private String dosage;
    @Size(max = 100)
    @Column(name = "frequency")
    private String frequency;
    @Size(max = 100)
    @Column(name = "duration")
    private String duration;
    @Size(max = 255)
    @Column(name = "instruction")
    private String instruction;
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    @Column(name = "active")
    private Boolean active;
    @OneToMany(mappedBy = "prescriptionItemId")
    private Set<InventoryTransaction> inventoryTransactionSet;
    @JoinColumn(name = "medicine_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Medicine medicineId;
    @JoinColumn(name = "prescription_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Prescription prescriptionId;

    public PrescriptionItem() {
    }

    public PrescriptionItem(Long id) {
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

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
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
    public Set<InventoryTransaction> getInventoryTransactionSet() {
        return inventoryTransactionSet;
    }

    public void setInventoryTransactionSet(Set<InventoryTransaction> inventoryTransactionSet) {
        this.inventoryTransactionSet = inventoryTransactionSet;
    }

    public Medicine getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(Medicine medicineId) {
        this.medicineId = medicineId;
    }

    public Prescription getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(Prescription prescriptionId) {
        this.prescriptionId = prescriptionId;
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
        if (!(object instanceof PrescriptionItem)) {
            return false;
        }
        PrescriptionItem other = (PrescriptionItem) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.evercare.pojo.PrescriptionItem[ id=" + id + " ]";
    }
    
}
