package com.evercare.services;

import com.evercare.dtos.response.AiSuggestedReplyResponse;

public interface AiSuggestedReplyService {

    AiSuggestedReplyResponse suggestReceptionistReply(Long conversationId);
}
