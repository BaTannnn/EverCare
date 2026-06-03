package com.evercare.controllers.api;

import com.evercare.dtos.response.AiSuggestedReplyResponse;
import com.evercare.services.AiSuggestedReplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/conversations")
@CrossOrigin
public class ApiAiSuggestionController {

    @Autowired
    private AiSuggestedReplyService aiSuggestedReplyService;

    @PostMapping("/{conversationId}/suggest-reply")
    public ResponseEntity<AiSuggestedReplyResponse> suggestReply(
            @PathVariable("conversationId") Long conversationId
    ) {
        return ResponseEntity.ok(this.aiSuggestedReplyService.suggestReceptionistReply(conversationId));
    }
}
