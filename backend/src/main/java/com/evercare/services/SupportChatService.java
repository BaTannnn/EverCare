package com.evercare.services;

import com.evercare.dtos.request.SupportConsultationScheduleRequest;
import com.evercare.dtos.request.SupportConversationRequest;
import com.evercare.dtos.request.SupportMessageRequest;
import com.evercare.dtos.response.SupportConsultationScheduleResponse;
import com.evercare.dtos.response.SupportConversationResponse;
import com.evercare.dtos.response.SupportMessageResponse;
import java.util.List;
import java.util.Map;

public interface SupportChatService {
    List<SupportConversationResponse> getPatientConversations();

    SupportConversationResponse createPatientConversation(SupportConversationRequest request);

    List<SupportMessageResponse> getPatientMessages(Long conversationId);

    SupportMessageResponse sendPatientMessage(Long conversationId, SupportMessageRequest request);

    SupportConversationResponse closePatientConversation(Long conversationId);

    List<SupportConversationResponse> getReceptionistConversations(Map<String, String> params);

    SupportConversationResponse acceptConversation(Long conversationId);

    List<SupportMessageResponse> getReceptionistMessages(Long conversationId);

    SupportMessageResponse sendReceptionistMessage(Long conversationId, SupportMessageRequest request);

    SupportConsultationScheduleResponse createConsultationSchedule(Long conversationId, SupportConsultationScheduleRequest request);

    SupportConversationResponse closeReceptionistConversation(Long conversationId);
}
