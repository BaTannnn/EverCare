package com.evercare.repositories;

import com.evercare.pojo.SupportMessage;
import java.util.List;

public interface SupportMessageRepository {
    List<SupportMessage> getMessagesByConversationId(Long conversationId);

    SupportMessage getLatestMessageByConversationId(Long conversationId);

    long countUnreadMessages(Long conversationId, Long readerUserId);

    void markMessagesRead(Long conversationId, Long readerUserId);

    SupportMessage save(SupportMessage message);
}
