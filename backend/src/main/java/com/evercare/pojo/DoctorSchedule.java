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
 * @author cadic
 */
@Entity
@Table(name = "doctor_schedule")
@NamedQueries({
    @NamedQuery(name = "DoctorSchedule.findAll", query = "SELECT d FROM DoctorSchedule d"),
    @NamedQuery(name = "DoctorSchedule.findById", query = "SELECT d FROM DoctorSchedule d WHERE d.id = :id"),
    @NamedQuery(name = "DoctorSchedule.findByWorkDate", query = "SELECT d FROM DoctorSchedule d WHERE d.workDate = :workDate"),
    @NamedQuery(name = "DoctorSchedule.findByStartTime", query = "SELECT d FROM DoctorSchedule d WHERE d.startTime = :startTime"),
    @NamedQuery(name = "DoctorSchedule.findByEndTime", query = "SELECT d FROM DoctorSchedule d WHERE d.endTime = :endTime"),
    @NamedQuery(name = "DoctorSchedule.findByMaxPatients", query = "SELECT d FROM DoctorSchedule d WHERE d.maxPatients = :maxPatients"),
    @NamedQuery(name = "DoctorSchedule.findByStatus", query = "SELECT d FROM DoctorSchedule d WHERE d.status = :status"),
    @NamedQuery(name = "DoctorSchedule.findByNote", query = "SELECT d FROM DoctorSchedule d WHERE d.note = :note"),
    @NamedQuery(name = "DoctorSchedule.findByCreatedAt", query = "SELECT d FROM DoctorSchedule d WHERE d.createdAt = :createdAt"),
    @NamedQuery(name = "DoctorSchedule.findByUpdatedAt", query = "SELECT d FROM DoctorSchedule d WHERE d.updatedAt = :updatedAt"),
    @NamedQuery(name = "DoctorSchedule.findByActive", query = "SELECT d FROM DoctorSchedule d WHERE d.active = :active")})
public class DoctorSchedule implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Basic(optional = false)
    @NotNull
    @Column(name = "work_date")
    @Temporal(TemporalType.DATE)
    private Date workDate;
    @Basic(optional = false)
    @NotNull
    @Column(name = "start_time")
    @Temporal(TemporalType.TIME)
    private Date startTime;
    @Basic(optional = false)
    @NotNull
    @Column(name = "end_time")
    @Temporal(TemporalType.TIME)
    private Date endTime;
    @Basic(optional = false)
    @NotNull
    @Column(name = "max_patients")
    private int maxPatients;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 30)
    @Column(name = "status")
    private String status;
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
    @JoinColumn(name = "doctor_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Doctor doctorId;

    public DoctorSchedule() {
    }

    public DoctorSchedule(Long id) {
        this.id = id;
    }

    public DoctorSchedule(Long id, Date workDate, Date startTime, Date endTime, int maxPatients, String status, Date createdAt, Date updatedAt, boolean active) {
        this.id = id;
        this.workDate = workDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.maxPatients = maxPatients;
        this.status = status;
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

    public Date getWorkDate() {
        return workDate;
    }

    public void setWorkDate(Date workDate) {
        this.workDate = workDate;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public int getMaxPatients() {
        return maxPatients;
    }

    public void setMaxPatients(int maxPatients) {
        this.maxPatients = maxPatients;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public Doctor getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Doctor doctorId) {
        this.doctorId = doctorId;
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
        if (!(object instanceof DoctorSchedule)) {
            return false;
        }
        DoctorSchedule other = (DoctorSchedule) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.evercare.pojo.DoctorSchedule[ id=" + id + " ]";
    }
    
}
