package com.evercare.pojo;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "support_message")
public class SupportMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @Basic(optional = false)
    @NotNull
    @Lob
    @Size(min = 1, max = 65535)
    @Column(name = "content")
    private String content;

    @Size(min = 1, max = 30)
    @Transient
    private String senderRole;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 30)
    @Column(name = "message_type")
    private String messageType;

    @Column(name = "is_read")
    private Boolean read;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "active")
    private Boolean active;

    @JoinColumn(name = "conversation_id", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private SupportConversation conversationId;

    @JoinColumn(name = "sender_id", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User senderId;

    public SupportMessage() {
    }

    public SupportMessage(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSenderRole() {
        return senderRole;
    }

    public void setSenderRole(String senderRole) {
        this.senderRole = senderRole;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public SupportConversation getConversationId() {
        return conversationId;
    }

    public void setConversationId(SupportConversation conversationId) {
        this.conversationId = conversationId;
    }

    public User getSenderId() {
        return senderId;
    }

    public void setSenderId(User senderId) {
        this.senderId = senderId;
    }
}
