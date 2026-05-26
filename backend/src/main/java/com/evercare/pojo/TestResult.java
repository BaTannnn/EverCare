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
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author cadic
 */
@Entity
@Table(name = "test_result")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "TestResult.findAll", query = "SELECT t FROM TestResult t"),
    @NamedQuery(name = "TestResult.findById", query = "SELECT t FROM TestResult t WHERE t.id = :id"),
    @NamedQuery(name = "TestResult.findByResultCode", query = "SELECT t FROM TestResult t WHERE t.resultCode = :resultCode"),
    @NamedQuery(name = "TestResult.findByResultTitle", query = "SELECT t FROM TestResult t WHERE t.resultTitle = :resultTitle"),
    @NamedQuery(name = "TestResult.findByFileUrl", query = "SELECT t FROM TestResult t WHERE t.fileUrl = :fileUrl"),
    @NamedQuery(name = "TestResult.findByResultDate", query = "SELECT t FROM TestResult t WHERE t.resultDate = :resultDate"),
    @NamedQuery(name = "TestResult.findByCreatedAt", query = "SELECT t FROM TestResult t WHERE t.createdAt = :createdAt"),
    @NamedQuery(name = "TestResult.findByUpdatedAt", query = "SELECT t FROM TestResult t WHERE t.updatedAt = :updatedAt"),
    @NamedQuery(name = "TestResult.findByActive", query = "SELECT t FROM TestResult t WHERE t.active = :active")})
public class TestResult implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 30)
    @Column(name = "result_code")
    private String resultCode;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 150)
    @Column(name = "result_title")
    private String resultTitle;
    @Lob
    @Size(max = 65535)
    @Column(name = "result_content")
    private String resultContent;
    @Size(max = 255)
    @Column(name = "file_url")
    private String fileUrl;
    @Lob
    @Size(max = 65535)
    @Column(name = "conclusion")
    private String conclusion;
    @Column(name = "result_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date resultDate;
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    @Column(name = "active")
    private Boolean active;
    @JoinColumn(name = "performed_by", referencedColumnName = "id")
    @ManyToOne
    private Employee performedBy;
    @JoinColumn(name = "medical_record_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private MedicalRecord medicalRecordId;
    @JoinColumn(name = "service_id", referencedColumnName = "id")
    @ManyToOne
    private MedicalService serviceId;

    public TestResult() {
    }

    public TestResult(Long id) {
        this.id = id;
    }

    public TestResult(Long id, String resultCode, String resultTitle) {
        this.id = id;
        this.resultCode = resultCode;
        this.resultTitle = resultTitle;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultTitle() {
        return resultTitle;
    }

    public void setResultTitle(String resultTitle) {
        this.resultTitle = resultTitle;
    }

    public String getResultContent() {
        return resultContent;
    }

    public void setResultContent(String resultContent) {
        this.resultContent = resultContent;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getConclusion() {
        return conclusion;
    }

    public void setConclusion(String conclusion) {
        this.conclusion = conclusion;
    }

    public Date getResultDate() {
        return resultDate;
    }

    public void setResultDate(Date resultDate) {
        this.resultDate = resultDate;
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

    public Employee getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(Employee performedBy) {
        this.performedBy = performedBy;
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
        if (!(object instanceof TestResult)) {
            return false;
        }
        TestResult other = (TestResult) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.evercare.pojo.TestResult[ id=" + id + " ]";
    }
    
}
