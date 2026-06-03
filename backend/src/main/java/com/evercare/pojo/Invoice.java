/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.evercare.pojo;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
@Table(name = "invoice")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Invoice.findAll", query = "SELECT i FROM Invoice i"),
    @NamedQuery(name = "Invoice.findById", query = "SELECT i FROM Invoice i WHERE i.id = :id"),
    @NamedQuery(name = "Invoice.findByInvoiceCode", query = "SELECT i FROM Invoice i WHERE i.invoiceCode = :invoiceCode"),
    @NamedQuery(name = "Invoice.findByTotalServiceAmount", query = "SELECT i FROM Invoice i WHERE i.totalServiceAmount = :totalServiceAmount"),
    @NamedQuery(name = "Invoice.findByTotalMedicineAmount", query = "SELECT i FROM Invoice i WHERE i.totalMedicineAmount = :totalMedicineAmount"),
    @NamedQuery(name = "Invoice.findByDiscountAmount", query = "SELECT i FROM Invoice i WHERE i.discountAmount = :discountAmount"),
    @NamedQuery(name = "Invoice.findByTotalAmount", query = "SELECT i FROM Invoice i WHERE i.totalAmount = :totalAmount"),
    @NamedQuery(name = "Invoice.findByPaymentMethod", query = "SELECT i FROM Invoice i WHERE i.paymentMethod = :paymentMethod"),
    @NamedQuery(name = "Invoice.findByPaymentStatus", query = "SELECT i FROM Invoice i WHERE i.paymentStatus = :paymentStatus"),
    @NamedQuery(name = "Invoice.findByPaidAt", query = "SELECT i FROM Invoice i WHERE i.paidAt = :paidAt"),
    @NamedQuery(name = "Invoice.findByNote", query = "SELECT i FROM Invoice i WHERE i.note = :note"),
    @NamedQuery(name = "Invoice.findByCreatedAt", query = "SELECT i FROM Invoice i WHERE i.createdAt = :createdAt"),
    @NamedQuery(name = "Invoice.findByUpdatedAt", query = "SELECT i FROM Invoice i WHERE i.updatedAt = :updatedAt"),
    @NamedQuery(name = "Invoice.findByActive", query = "SELECT i FROM Invoice i WHERE i.active = :active")})
public class Invoice implements Serializable {

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
    @Column(name = "total_service_amount")
    private BigDecimal totalServiceAmount;
    @Column(name = "total_medicine_amount")
    private BigDecimal totalMedicineAmount;
    @Column(name = "discount_amount")
    private BigDecimal discountAmount;
    @Column(name = "total_amount")
    private BigDecimal totalAmount;
    @Size(max = 40)
    @Column(name = "payment_method")
    private String paymentMethod;
    @Size(max = 30)
    @Column(name = "payment_status")
    private String paymentStatus;
    @Column(name = "paid_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date paidAt;
    @Size(max = 255)
    @Column(name = "note")
    private String note;
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    @Column(name = "active")
    private Boolean active;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "invoiceId")
    private Set<Payment> paymentSet;
    @JoinColumn(name = "medical_record_id", referencedColumnName = "id")
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    private MedicalRecord medicalRecordId;
    @JoinColumn(name = "patient_id", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Patient patientId;
    @JoinColumn(name = "cashier_id", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User cashierId;

    public Invoice() {
    }

    public Invoice(Long id) {
        this.id = id;
    }

    public Invoice(Long id, String invoiceCode) {
        this.id = id;
        this.invoiceCode = invoiceCode;
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @XmlTransient
    public Set<Payment> getPaymentSet() {
        return paymentSet;
    }

    public void setPaymentSet(Set<Payment> paymentSet) {
        this.paymentSet = paymentSet;
    }

    public MedicalRecord getMedicalRecordId() {
        return medicalRecordId;
    }

    public void setMedicalRecordId(MedicalRecord medicalRecordId) {
        this.medicalRecordId = medicalRecordId;
    }

    public Patient getPatientId() {
        return patientId;
    }

    public void setPatientId(Patient patientId) {
        this.patientId = patientId;
    }

    public User getCashierId() {
        return cashierId;
    }

    public void setCashierId(User cashierId) {
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
        if (!(object instanceof Invoice)) {
            return false;
        }
        Invoice other = (Invoice) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.evercare.pojo.Invoice[ id=" + id + " ]";
    }
    
}
