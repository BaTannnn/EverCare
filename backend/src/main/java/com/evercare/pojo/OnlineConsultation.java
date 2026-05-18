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
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
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
@Table(name = "online_consultation")
@NamedQueries({
    @NamedQuery(name = "OnlineConsultation.findAll", query = "SELECT o FROM OnlineConsultation o"),
    @NamedQuery(name = "OnlineConsultation.findById", query = "SELECT o FROM OnlineConsultation o WHERE o.id = :id"),
    @NamedQuery(name = "OnlineConsultation.findByConsultationType", query = "SELECT o FROM OnlineConsultation o WHERE o.consultationType = :consultationType"),
    @NamedQuery(name = "OnlineConsultation.findByRoomUrl", query = "SELECT o FROM OnlineConsultation o WHERE o.roomUrl = :roomUrl"),
    @NamedQuery(name = "OnlineConsultation.findByStartedAt", query = "SELECT o FROM OnlineConsultation o WHERE o.startedAt = :startedAt"),
    @NamedQuery(name = "OnlineConsultation.findByEndedAt", query = "SELECT o FROM OnlineConsultation o WHERE o.endedAt = :endedAt"),
    @NamedQuery(name = "OnlineConsultation.findByStatus", query = "SELECT o FROM OnlineConsultation o WHERE o.status = :status"),
    @NamedQuery(name = "OnlineConsultation.findByCreatedAt", query = "SELECT o FROM OnlineConsultation o WHERE o.createdAt = :createdAt"),
    @NamedQuery(name = "OnlineConsultation.findByUpdatedAt", query = "SELECT o FROM OnlineConsultation o WHERE o.updatedAt = :updatedAt"),
    @NamedQuery(name = "OnlineConsultation.findByActive", query = "SELECT o FROM OnlineConsultation o WHERE o.active = :active")})
public class OnlineConsultation implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "consultation_type")
    private String consultationType;
    @Size(max = 255)
    @Column(name = "room_url")
    private String roomUrl;
    @Column(name = "started_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date startedAt;
    @Column(name = "ended_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date endedAt;
    @Lob
    @Size(max = 65535)
    @Column(name = "summary")
    private String summary;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 30)
    @Column(name = "status")
    private String status;
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
    @JoinColumn(name = "appointment_id", referencedColumnName = "id")
    @OneToOne(optional = false)
    private Appointment appointmentId;

    public OnlineConsultation() {
    }

    public OnlineConsultation(Long id) {
        this.id = id;
    }

    public OnlineConsultation(Long id, String consultationType, String status, Date createdAt, Date updatedAt, boolean active) {
        this.id = id;
        this.consultationType = consultationType;
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

    public String getConsultationType() {
        return consultationType;
    }

    public void setConsultationType(String consultationType) {
        this.consultationType = consultationType;
    }

    public String getRoomUrl() {
        return roomUrl;
    }

    public void setRoomUrl(String roomUrl) {
        this.roomUrl = roomUrl;
    }

    public Date getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
    }

    public Date getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(Date endedAt) {
        this.endedAt = endedAt;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public Appointment getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Appointment appointmentId) {
        this.appointmentId = appointmentId;
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
        if (!(object instanceof OnlineConsultation)) {
            return false;
        }
        OnlineConsultation other = (OnlineConsultation) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.evercare.pojo.OnlineConsultation[ id=" + id + " ]";
    }
    
}
