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
@Table(name = "medicine_batches")
@NamedQueries({
    @NamedQuery(name = "MedicineBatches.findAll", query = "SELECT m FROM MedicineBatches m"),
    @NamedQuery(name = "MedicineBatches.findById", query = "SELECT m FROM MedicineBatches m WHERE m.id = :id"),
    @NamedQuery(name = "MedicineBatches.findByBatchCode", query = "SELECT m FROM MedicineBatches m WHERE m.batchCode = :batchCode"),
    @NamedQuery(name = "MedicineBatches.findByImportDate", query = "SELECT m FROM MedicineBatches m WHERE m.importDate = :importDate"),
    @NamedQuery(name = "MedicineBatches.findByExpiryDate", query = "SELECT m FROM MedicineBatches m WHERE m.expiryDate = :expiryDate"),
    @NamedQuery(name = "MedicineBatches.findByQuantity", query = "SELECT m FROM MedicineBatches m WHERE m.quantity = :quantity"),
    @NamedQuery(name = "MedicineBatches.findByRemainingQuantity", query = "SELECT m FROM MedicineBatches m WHERE m.remainingQuantity = :remainingQuantity"),
    @NamedQuery(name = "MedicineBatches.findByImportPrice", query = "SELECT m FROM MedicineBatches m WHERE m.importPrice = :importPrice"),
    @NamedQuery(name = "MedicineBatches.findBySupplierName", query = "SELECT m FROM MedicineBatches m WHERE m.supplierName = :supplierName"),
    @NamedQuery(name = "MedicineBatches.findByCreatedAt", query = "SELECT m FROM MedicineBatches m WHERE m.createdAt = :createdAt"),
    @NamedQuery(name = "MedicineBatches.findByUpdatedAt", query = "SELECT m FROM MedicineBatches m WHERE m.updatedAt = :updatedAt"),
    @NamedQuery(name = "MedicineBatches.findByActive", query = "SELECT m FROM MedicineBatches m WHERE m.active = :active")})
public class MedicineBatches implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "batch_code")
    private String batchCode;
    @Basic(optional = false)
    @NotNull
    @Column(name = "import_date")
    @Temporal(TemporalType.DATE)
    private Date importDate;
    @Basic(optional = false)
    @NotNull
    @Column(name = "expiry_date")
    @Temporal(TemporalType.DATE)
    private Date expiryDate;
    @Basic(optional = false)
    @NotNull
    @Column(name = "quantity")
    private int quantity;
    @Basic(optional = false)
    @NotNull
    @Column(name = "remaining_quantity")
    private int remainingQuantity;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "import_price")
    private BigDecimal importPrice;
    @Size(max = 150)
    @Column(name = "supplier_name")
    private String supplierName;
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
    @JoinColumn(name = "medicine_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Medicines medicineId;
    @OneToMany(mappedBy = "batchId")
    private Collection<InventoryTransactions> inventoryTransactionsCollection;

    public MedicineBatches() {
    }

    public MedicineBatches(Long id) {
        this.id = id;
    }

    public MedicineBatches(Long id, String batchCode, Date importDate, Date expiryDate, int quantity, int remainingQuantity, Date createdAt, Date updatedAt, boolean active) {
        this.id = id;
        this.batchCode = batchCode;
        this.importDate = importDate;
        this.expiryDate = expiryDate;
        this.quantity = quantity;
        this.remainingQuantity = remainingQuantity;
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

    public String getBatchCode() {
        return batchCode;
    }

    public void setBatchCode(String batchCode) {
        this.batchCode = batchCode;
    }

    public Date getImportDate() {
        return importDate;
    }

    public void setImportDate(Date importDate) {
        this.importDate = importDate;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getRemainingQuantity() {
        return remainingQuantity;
    }

    public void setRemainingQuantity(int remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
    }

    public BigDecimal getImportPrice() {
        return importPrice;
    }

    public void setImportPrice(BigDecimal importPrice) {
        this.importPrice = importPrice;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
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

    public Medicines getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(Medicines medicineId) {
        this.medicineId = medicineId;
    }

    public Collection<InventoryTransactions> getInventoryTransactionsCollection() {
        return inventoryTransactionsCollection;
    }

    public void setInventoryTransactionsCollection(Collection<InventoryTransactions> inventoryTransactionsCollection) {
        this.inventoryTransactionsCollection = inventoryTransactionsCollection;
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
        if (!(object instanceof MedicineBatches)) {
            return false;
        }
        MedicineBatches other = (MedicineBatches) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.evercare.pojo.MedicineBatches[ id=" + id + " ]";
    }
    
}
