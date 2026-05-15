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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
@Table(name = "invoices")
@NamedQueries({
    @NamedQuery(name = "Invoices.findAll", query = "SELECT i FROM Invoices i"),
    @NamedQuery(name = "Invoices.findById", query = "SELECT i FROM Invoices i WHERE i.id = :id"),
    @NamedQuery(name = "Invoices.findByInvoiceCode", query = "SELECT i FROM Invoices i WHERE i.invoiceCode = :invoiceCode"),
    @NamedQuery(name = "Invoices.findByTotalServiceAmount", query = "SELECT i FROM Invoices i WHERE i.totalServiceAmount = :totalServiceAmount"),
    @NamedQuery(name = "Invoices.findByTotalMedicineAmount", query = "SELECT i FROM Invoices i WHERE i.totalMedicineAmount = :totalMedicineAmount"),
    @NamedQuery(name = "Invoices.findByDiscountAmount", query = "SELECT i FROM Invoices i WHERE i.discountAmount = :discountAmount"),
    @NamedQuery(name = "Invoices.findByTotalAmount", query = "SELECT i FROM Invoices i WHERE i.totalAmount = :totalAmount"),
    @NamedQuery(name = "Invoices.findByPaymentMethod", query = "SELECT i FROM Invoices i WHERE i.paymentMethod = :paymentMethod"),
    @NamedQuery(name = "Invoices.findByPaymentStatus", query = "SELECT i FROM Invoices i WHERE i.paymentStatus = :paymentStatus"),
    @NamedQuery(name = "Invoices.findByPaidAt", query = "SELECT i FROM Invoices i WHERE i.paidAt = :paidAt"),
    @NamedQuery(name = "Invoices.findByNote", query = "SELECT i FROM Invoices i WHERE i.note = :note"),
    @NamedQuery(name = "Invoices.findByCreatedAt", query = "SELECT i FROM Invoices i WHERE i.createdAt = :createdAt"),
    @NamedQuery(name = "Invoices.findByUpdatedAt", query = "SELECT i FROM Invoices i WHERE i.updatedAt = :updatedAt"),
    @NamedQuery(name = "Invoices.findByActive", query = "SELECT i FROM Invoices i WHERE i.active = :active")})
public class Invoices implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 30)
    @Column(name = "invoice_code")
    private String invoiceCode;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Basic(optional = false)
    @NotNull
    @Column(name = "total_service_amount")
    private BigDecimal totalServiceAmount;
    @Basic(optional = false)
    @NotNull
    @Column(name = "total_medicine_amount")
    private BigDecimal totalMedicineAmount;
    @Basic(optional = false)
    @NotNull
    @Column(name = "discount_amount")
    private BigDecimal discountAmount;
    @Basic(optional = false)
    @NotNull
    @Column(name = "total_amount")
    private BigDecimal totalAmount;
    @Size(max = 40)
    @Column(name = "payment_method")
    private String paymentMethod;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 30)
    @Column(name = "payment_status")
    private String paymentStatus;
    @Column(name = "paid_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date paidAt;
    @Size(max = 255)
    @Column(name = "note")
    private String note;
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
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "invoiceId")
    private Collection<Payments> paymentsCollection;
    @JoinColumn(name = "medical_record_id", referencedColumnName = "id")
    @OneToOne(optional = false)
    private MedicalRecords medicalRecordId;
    @JoinColumn(name = "patient_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Patients patientId;
    @JoinColumn(name = "cashier_id", referencedColumnName = "id")
    @ManyToOne
    private Users cashierId;

    public Invoices() {
    }

    public Invoices(Long id) {
        this.id = id;
    }

    public Invoices(Long id, String invoiceCode, BigDecimal totalServiceAmount, BigDecimal totalMedicineAmount, BigDecimal discountAmount, BigDecimal totalAmount, String paymentStatus, Date createdAt, Date updatedAt, boolean active) {
        this.id = id;
        this.invoiceCode = invoiceCode;
        this.totalServiceAmount = totalServiceAmount;
        this.totalMedicineAmount = totalMedicineAmount;
        this.discountAmount = discountAmount;
        this.totalAmount = totalAmount;
        this.paymentStatus = paymentStatus;
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

    public String getInvoiceCode() {
        return invoiceCode;
    }

    public void setInvoiceCode(String invoiceCode) {
        this.invoiceCode = invoiceCode;
    }

    public BigDecimal getTotalServiceAmount() {
        return totalServiceAmount;
    }

    public void setTotalServiceAmount(BigDecimal totalServiceAmount) {
        this.totalServiceAmount = totalServiceAmount;
    }

    public BigDecimal getTotalMedicineAmount() {
        return totalMedicineAmount;
    }

    public void setTotalMedicineAmount(BigDecimal totalMedicineAmount) {
        this.totalMedicineAmount = totalMedicineAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Date getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Date paidAt) {
        this.paidAt = paidAt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
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

    public Collection<Payments> getPaymentsCollection() {
        return paymentsCollection;
    }

    public void setPaymentsCollection(Collection<Payments> paymentsCollection) {
        this.paymentsCollection = paymentsCollection;
    }

    public MedicalRecords getMedicalRecordId() {
        return medicalRecordId;
    }

    public void setMedicalRecordId(MedicalRecords medicalRecordId) {
        this.medicalRecordId = medicalRecordId;
    }

    public Patients getPatientId() {
        return patientId;
    }

    public void setPatientId(Patients patientId) {
        this.patientId = patientId;
    }

    public Users getCashierId() {
        return cashierId;
    }

    public void setCashierId(Users cashierId) {
        this.cashierId = cashierId;
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
        if (!(object instanceof Invoices)) {
            return false;
        }
        Invoices other = (Invoices) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.evercare.pojo.Invoices[ id=" + id + " ]";
    }
    
}
