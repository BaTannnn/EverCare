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
import jakarta.persistence.Lob;
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
@Table(name = "medicines")
@NamedQueries({
    @NamedQuery(name = "Medicines.findAll", query = "SELECT m FROM Medicines m"),
    @NamedQuery(name = "Medicines.findById", query = "SELECT m FROM Medicines m WHERE m.id = :id"),
    @NamedQuery(name = "Medicines.findByMedicineCode", query = "SELECT m FROM Medicines m WHERE m.medicineCode = :medicineCode"),
    @NamedQuery(name = "Medicines.findByName", query = "SELECT m FROM Medicines m WHERE m.name = :name"),
    @NamedQuery(name = "Medicines.findByUnit", query = "SELECT m FROM Medicines m WHERE m.unit = :unit"),
    @NamedQuery(name = "Medicines.findByUnitPrice", query = "SELECT m FROM Medicines m WHERE m.unitPrice = :unitPrice"),
    @NamedQuery(name = "Medicines.findByMinStockQuantity", query = "SELECT m FROM Medicines m WHERE m.minStockQuantity = :minStockQuantity"),
    @NamedQuery(name = "Medicines.findByCreatedAt", query = "SELECT m FROM Medicines m WHERE m.createdAt = :createdAt"),
    @NamedQuery(name = "Medicines.findByUpdatedAt", query = "SELECT m FROM Medicines m WHERE m.updatedAt = :updatedAt"),
    @NamedQuery(name = "Medicines.findByActive", query = "SELECT m FROM Medicines m WHERE m.active = :active")})
public class Medicines implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 30)
    @Column(name = "medicine_code")
    private String medicineCode;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 150)
    @Column(name = "name")
    private String name;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 30)
    @Column(name = "unit")
    private String unit;
    @Lob
    @Size(max = 65535)
    @Column(name = "description")
    private String description;
    @Lob
    @Size(max = 65535)
    @Column(name = "usage_note")
    private String usageNote;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Basic(optional = false)
    @NotNull
    @Column(name = "unit_price")
    private BigDecimal unitPrice;
    @Basic(optional = false)
    @NotNull
    @Column(name = "min_stock_quantity")
    private int minStockQuantity;
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
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "medicineId")
    private Collection<MedicineBatches> medicineBatchesCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "medicineId")
    private Collection<InventoryTransactions> inventoryTransactionsCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "medicineId")
    private Collection<PrescriptionItems> prescriptionItemsCollection;

    public Medicines() {
    }

    public Medicines(Long id) {
        this.id = id;
    }

    public Medicines(Long id, String medicineCode, String name, String unit, BigDecimal unitPrice, int minStockQuantity, Date createdAt, Date updatedAt, boolean active) {
        this.id = id;
        this.medicineCode = medicineCode;
        this.name = name;
        this.unit = unit;
        this.unitPrice = unitPrice;
        this.minStockQuantity = minStockQuantity;
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

    public String getMedicineCode() {
        return medicineCode;
    }

    public void setMedicineCode(String medicineCode) {
        this.medicineCode = medicineCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUsageNote() {
        return usageNote;
    }

    public void setUsageNote(String usageNote) {
        this.usageNote = usageNote;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getMinStockQuantity() {
        return minStockQuantity;
    }

    public void setMinStockQuantity(int minStockQuantity) {
        this.minStockQuantity = minStockQuantity;
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

    public Collection<MedicineBatches> getMedicineBatchesCollection() {
        return medicineBatchesCollection;
    }

    public void setMedicineBatchesCollection(Collection<MedicineBatches> medicineBatchesCollection) {
        this.medicineBatchesCollection = medicineBatchesCollection;
    }

    public Collection<InventoryTransactions> getInventoryTransactionsCollection() {
        return inventoryTransactionsCollection;
    }

    public void setInventoryTransactionsCollection(Collection<InventoryTransactions> inventoryTransactionsCollection) {
        this.inventoryTransactionsCollection = inventoryTransactionsCollection;
    }

    public Collection<PrescriptionItems> getPrescriptionItemsCollection() {
        return prescriptionItemsCollection;
    }

    public void setPrescriptionItemsCollection(Collection<PrescriptionItems> prescriptionItemsCollection) {
        this.prescriptionItemsCollection = prescriptionItemsCollection;
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
        if (!(object instanceof Medicines)) {
            return false;
        }
        Medicines other = (Medicines) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.evercare.pojo.Medicines[ id=" + id + " ]";
    }
    
}
