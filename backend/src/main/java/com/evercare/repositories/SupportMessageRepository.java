package com.evercare.repositories;

import com.evercare.pojo.SupportMessage;
import java.util.List;
import java.util.Map;

public interface SupportMessageRepository {
    List<SupportMessage> getMessagesByConversationId(Long conversationId);

    List<SupportMessage> getMessagesByConversationId(Long conversationId, Long afterId, Integer limit);

    SupportMessage getLatestMessageByConversationId(Long conversationId);

    Map<Long, SupportMessage> getLatestMessagesByConversationIds(List<Long> conversationIds);

    long countUnreadMessages(Long conversationId, Long readerUserId);

    Map<Long, Long> countUnreadMessagesByConversationIds(List<Long> conversationIds, Long readerUserId);

    void markMessagesRead(Long conversationId, Long readerUserId);

    SupportMessage save(SupportMessage message);
}
