package com.evercare.controllers.api;

import com.evercare.dtos.request.SupportConversationRequest;
import com.evercare.dtos.request.SupportMessageRequest;
import com.evercare.dtos.response.SupportConversationResponse;
import com.evercare.dtos.response.SupportMessageResponse;
import com.evercare.services.SupportChatService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/secure/support/conversations")
@CrossOrigin
public class ApiPatientSupportChatController {

    @Autowired
    private SupportChatService supportChatService;

    @GetMapping
    public ResponseEntity<List<SupportConversationResponse>> listConversations() {
        return ResponseEntity.ok(this.supportChatService.getPatientConversations());
    }

    @PostMapping
    public ResponseEntity<SupportConversationResponse> createConversation(
            @RequestBody SupportConversationRequest request
    ) {
        return new ResponseEntity<>(this.supportChatService.createPatientConversation(request), HttpStatus.CREATED);
    }

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<List<SupportMessageResponse>> listMessages(
            @PathVariable("conversationId") Long conversationId,
            @RequestParam(name = "afterId", required = false) Long afterId,
            @RequestParam(name = "limit", required = false) Integer limit
    ) {
        return ResponseEntity.ok(this.supportChatService.getPatientMessages(conversationId, afterId, limit));
    }

    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<SupportMessageResponse> sendMessage(
            @PathVariable("conversationId") Long conversationId,
            @RequestBody SupportMessageRequest request
    ) {
        return new ResponseEntity<>(this.supportChatService.sendPatientMessage(conversationId, request), HttpStatus.CREATED);
    }

    @PatchMapping("/{conversationId}/close")
    public ResponseEntity<SupportConversationResponse> closeConversation(
            @PathVariable("conversationId") Long conversationId
    ) {
        return ResponseEntity.ok(this.supportChatService.closePatientConversation(conversationId));
    }
}
