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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author cadic
 */
@Entity
@Table(name = "inventory_transaction")
@NamedQueries({
    @NamedQuery(name = "InventoryTransaction.findAll", query = "SELECT i FROM InventoryTransaction i"),
    @NamedQuery(name = "InventoryTransaction.findById", query = "SELECT i FROM InventoryTransaction i WHERE i.id = :id"),
    @NamedQuery(name = "InventoryTransaction.findByTransactionType", query = "SELECT i FROM InventoryTransaction i WHERE i.transactionType = :transactionType"),
    @NamedQuery(name = "InventoryTransaction.findByQuantity", query = "SELECT i FROM InventoryTransaction i WHERE i.quantity = :quantity"),
    @NamedQuery(name = "InventoryTransaction.findByTransactionDate", query = "SELECT i FROM InventoryTransaction i WHERE i.transactionDate = :transactionDate"),
    @NamedQuery(name = "InventoryTransaction.findByNote", query = "SELECT i FROM InventoryTransaction i WHERE i.note = :note")})
public class InventoryTransaction implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 30)
    @Column(name = "transaction_type")
    private String transactionType;
    @Basic(optional = false)
    @NotNull
    @Column(name = "quantity")
    private int quantity;
    @Column(name = "transaction_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date transactionDate;
    @Size(max = 255)
    @Column(name = "note")
    private String note;
    @JoinColumn(name = "medicine_id", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Medicine medicineId;
    @JoinColumn(name = "batch_id", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    private MedicineBatch batchId;
    @JoinColumn(name = "prescription_item_id", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    private PrescriptionItem prescriptionItemId;
    @JoinColumn(name = "created_by", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User createdBy;

    public InventoryTransaction() {
    }

    public InventoryTransaction(Long id) {
        this.id = id;
    }

    public InventoryTransaction(Long id, String transactionType, int quantity) {
        this.id = id;
        this.transactionType = transactionType;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Medicine getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(Medicine medicineId) {
        this.medicineId = medicineId;
    }

    public MedicineBatch getBatchId() {
        return batchId;
    }

    public void setBatchId(MedicineBatch batchId) {
        this.batchId = batchId;
    }

    public PrescriptionItem getPrescriptionItemId() {
        return prescriptionItemId;
    }

    public void setPrescriptionItemId(PrescriptionItem prescriptionItemId) {
        this.prescriptionItemId = prescriptionItemId;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
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
        if (!(object instanceof InventoryTransaction)) {
            return false;
        }
        InventoryTransaction other = (InventoryTransaction) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.evercare.pojo.InventoryTransaction[ id=" + id + " ]";
    }
    
}
