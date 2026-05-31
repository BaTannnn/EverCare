package com.evercare.repositories;

import com.evercare.pojo.SupportConversation;
import java.util.List;
import java.util.Map;

public interface SupportConversationRepository {
    SupportConversation getConversationById(Long conversationId);

    List<SupportConversation> getConversationsByPatientId(Long patientId);

    List<SupportConversation> getConversationsForReceptionist(Map<String, String> params);

    SupportConversation save(SupportConversation conversation);

    SupportConversation update(SupportConversation conversation);
}
