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
@Table(name = "prescription_items")
@NamedQueries({
    @NamedQuery(name = "PrescriptionItems.findAll", query = "SELECT p FROM PrescriptionItems p"),
    @NamedQuery(name = "PrescriptionItems.findById", query = "SELECT p FROM PrescriptionItems p WHERE p.id = :id"),
    @NamedQuery(name = "PrescriptionItems.findByQuantity", query = "SELECT p FROM PrescriptionItems p WHERE p.quantity = :quantity"),
    @NamedQuery(name = "PrescriptionItems.findByUnitPrice", query = "SELECT p FROM PrescriptionItems p WHERE p.unitPrice = :unitPrice"),
    @NamedQuery(name = "PrescriptionItems.findByDosage", query = "SELECT p FROM PrescriptionItems p WHERE p.dosage = :dosage"),
    @NamedQuery(name = "PrescriptionItems.findByFrequency", query = "SELECT p FROM PrescriptionItems p WHERE p.frequency = :frequency"),
    @NamedQuery(name = "PrescriptionItems.findByDuration", query = "SELECT p FROM PrescriptionItems p WHERE p.duration = :duration"),
    @NamedQuery(name = "PrescriptionItems.findByInstruction", query = "SELECT p FROM PrescriptionItems p WHERE p.instruction = :instruction"),
    @NamedQuery(name = "PrescriptionItems.findByCreatedAt", query = "SELECT p FROM PrescriptionItems p WHERE p.createdAt = :createdAt"),
    @NamedQuery(name = "PrescriptionItems.findByUpdatedAt", query = "SELECT p FROM PrescriptionItems p WHERE p.updatedAt = :updatedAt"),
    @NamedQuery(name = "PrescriptionItems.findByActive", query = "SELECT p FROM PrescriptionItems p WHERE p.active = :active")})
public class PrescriptionItems implements Serializable {

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
    @OneToMany(mappedBy = "prescriptionItemId")
    private Collection<InventoryTransactions> inventoryTransactionsCollection;
    @JoinColumn(name = "medicine_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Medicines medicineId;
    @JoinColumn(name = "prescription_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Prescriptions prescriptionId;

    public PrescriptionItems() {
    }

    public PrescriptionItems(Long id) {
        this.id = id;
    }

    public PrescriptionItems(Long id, int quantity, BigDecimal unitPrice, Date createdAt, Date updatedAt, boolean active) {
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

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Collection<InventoryTransactions> getInventoryTransactionsCollection() {
        return inventoryTransactionsCollection;
    }

    public void setInventoryTransactionsCollection(Collection<InventoryTransactions> inventoryTransactionsCollection) {
        this.inventoryTransactionsCollection = inventoryTransactionsCollection;
    }

    public Medicines getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(Medicines medicineId) {
        this.medicineId = medicineId;
    }

    public Prescriptions getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(Prescriptions prescriptionId) {
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
        if (!(object instanceof PrescriptionItems)) {
            return false;
        }
        PrescriptionItems other = (PrescriptionItems) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.evercare.pojo.PrescriptionItems[ id=" + id + " ]";
    }
    
}
