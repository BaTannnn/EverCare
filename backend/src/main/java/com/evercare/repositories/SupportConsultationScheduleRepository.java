package com.evercare.repositories;

import com.evercare.pojo.SupportConsultationSchedule;
import java.util.List;

public interface SupportConsultationScheduleRepository {
    List<SupportConsultationSchedule> getSchedulesByConversationId(Long conversationId);

    SupportConsultationSchedule save(SupportConsultationSchedule schedule);
}
