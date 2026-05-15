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
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author batan
 */
@Entity
@Table(name = "inventory_transactions")
@NamedQueries({
    @NamedQuery(name = "InventoryTransactions.findAll", query = "SELECT i FROM InventoryTransactions i"),
    @NamedQuery(name = "InventoryTransactions.findById", query = "SELECT i FROM InventoryTransactions i WHERE i.id = :id"),
    @NamedQuery(name = "InventoryTransactions.findByTransactionType", query = "SELECT i FROM InventoryTransactions i WHERE i.transactionType = :transactionType"),
    @NamedQuery(name = "InventoryTransactions.findByQuantity", query = "SELECT i FROM InventoryTransactions i WHERE i.quantity = :quantity"),
    @NamedQuery(name = "InventoryTransactions.findByTransactionDate", query = "SELECT i FROM InventoryTransactions i WHERE i.transactionDate = :transactionDate"),
    @NamedQuery(name = "InventoryTransactions.findByNote", query = "SELECT i FROM InventoryTransactions i WHERE i.note = :note")})
public class InventoryTransactions implements Serializable {

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
    @Basic(optional = false)
    @NotNull
    @Column(name = "transaction_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date transactionDate;
    @Size(max = 255)
    @Column(name = "note")
    private String note;
    @JoinColumn(name = "medicine_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Medicines medicineId;
    @JoinColumn(name = "batch_id", referencedColumnName = "id")
    @ManyToOne
    private MedicineBatches batchId;
    @JoinColumn(name = "prescription_item_id", referencedColumnName = "id")
    @ManyToOne
    private PrescriptionItems prescriptionItemId;
    @JoinColumn(name = "created_by", referencedColumnName = "id")
    @ManyToOne
    private Users createdBy;

    public InventoryTransactions() {
    }

    public InventoryTransactions(Long id) {
        this.id = id;
    }

    public InventoryTransactions(Long id, String transactionType, int quantity, Date transactionDate) {
        this.id = id;
        this.transactionType = transactionType;
        this.quantity = quantity;
        this.transactionDate = transactionDate;
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

    public Medicines getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(Medicines medicineId) {
        this.medicineId = medicineId;
    }

    public MedicineBatches getBatchId() {
        return batchId;
    }

    public void setBatchId(MedicineBatches batchId) {
        this.batchId = batchId;
    }

    public PrescriptionItems getPrescriptionItemId() {
        return prescriptionItemId;
    }

    public void setPrescriptionItemId(PrescriptionItems prescriptionItemId) {
        this.prescriptionItemId = prescriptionItemId;
    }

    public Users getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Users createdBy) {
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
        if (!(object instanceof InventoryTransactions)) {
            return false;
        }
        InventoryTransactions other = (InventoryTransactions) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.evercare.pojo.InventoryTransactions[ id=" + id + " ]";
    }
    
}
