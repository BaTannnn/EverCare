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
@Table(name = "medicine")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Medicine.findAll", query = "SELECT m FROM Medicine m"),
    @NamedQuery(name = "Medicine.findById", query = "SELECT m FROM Medicine m WHERE m.id = :id"),
    @NamedQuery(name = "Medicine.findByMedicineCode", query = "SELECT m FROM Medicine m WHERE m.medicineCode = :medicineCode"),
    @NamedQuery(name = "Medicine.findByName", query = "SELECT m FROM Medicine m WHERE m.name = :name"),
    @NamedQuery(name = "Medicine.findByUnit", query = "SELECT m FROM Medicine m WHERE m.unit = :unit"),
    @NamedQuery(name = "Medicine.findByUnitPrice", query = "SELECT m FROM Medicine m WHERE m.unitPrice = :unitPrice"),
    @NamedQuery(name = "Medicine.findByMinStockQuantity", query = "SELECT m FROM Medicine m WHERE m.minStockQuantity = :minStockQuantity"),
    @NamedQuery(name = "Medicine.findByCreatedAt", query = "SELECT m FROM Medicine m WHERE m.createdAt = :createdAt"),
    @NamedQuery(name = "Medicine.findByUpdatedAt", query = "SELECT m FROM Medicine m WHERE m.updatedAt = :updatedAt"),
    @NamedQuery(name = "Medicine.findByActive", query = "SELECT m FROM Medicine m WHERE m.active = :active")})
public class Medicine implements Serializable {

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
    @Size(max = 30)
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
    @Column(name = "unit_price")
    private BigDecimal unitPrice;
    @Column(name = "min_stock_quantity")
    private Integer minStockQuantity;
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    @Column(name = "active")
    private Boolean active;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "medicineId")
    private Set<MedicineBatch> medicineBatchSet;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "medicineId")
    private Set<InventoryTransaction> inventoryTransactionSet;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "medicineId")
    private Set<PrescriptionItem> prescriptionItemSet;

    public Medicine() {
    }

    public Medicine(Long id) {
        this.id = id;
    }

    public Medicine(Long id, String medicineCode, String name) {
        this.id = id;
        this.medicineCode = medicineCode;
        this.name = name;
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

    public Integer getMinStockQuantity() {
        return minStockQuantity;
    }

    public void setMinStockQuantity(Integer minStockQuantity) {
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @XmlTransient
    public Set<MedicineBatch> getMedicineBatchSet() {
        return medicineBatchSet;
    }

    public void setMedicineBatchSet(Set<MedicineBatch> medicineBatchSet) {
        this.medicineBatchSet = medicineBatchSet;
    }

    @XmlTransient
    public Set<InventoryTransaction> getInventoryTransactionSet() {
        return inventoryTransactionSet;
    }

    public void setInventoryTransactionSet(Set<InventoryTransaction> inventoryTransactionSet) {
        this.inventoryTransactionSet = inventoryTransactionSet;
    }

    @XmlTransient
    public Set<PrescriptionItem> getPrescriptionItemSet() {
        return prescriptionItemSet;
    }

    public void setPrescriptionItemSet(Set<PrescriptionItem> prescriptionItemSet) {
        this.prescriptionItemSet = prescriptionItemSet;
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
        if (!(object instanceof Medicine)) {
            return false;
        }
        Medicine other = (Medicine) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.evercare.pojo.Medicine[ id=" + id + " ]";
    }
    
}
