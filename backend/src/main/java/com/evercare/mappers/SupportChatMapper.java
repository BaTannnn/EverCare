package com.evercare.mappers;

import com.evercare.dtos.response.SupportConsultationScheduleResponse;
import com.evercare.dtos.response.SupportConversationResponse;
import com.evercare.dtos.response.SupportMessageResponse;
import com.evercare.pojo.Doctor;
import com.evercare.pojo.Employee;
import com.evercare.pojo.Patient;
import com.evercare.pojo.SupportConsultationSchedule;
import com.evercare.pojo.SupportConversation;
import com.evercare.pojo.SupportMessage;
import com.evercare.pojo.User;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class SupportChatMapper {

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private SupportChatMapper() {
    }

    public static SupportConversationResponse toConversationResponse(
            SupportConversation conversation,
            SupportMessage latestMessage,
            Long unreadCount
    ) {
        if (conversation == null) {
            return null;
        }

        SupportConversationResponse response = new SupportConversationResponse();
        response.setId(conversation.getId());
        response.setSubject(conversation.getSubject());
        response.setStatus(conversation.getStatus());
        response.setLastMessageAt(format(conversation.getUpdatedAt()));
        response.setCreatedAt(format(conversation.getCreatedAt()));
        response.setUpdatedAt(format(conversation.getUpdatedAt()));
        response.setClosedAt(format(conversation.getClosedAt()));
        response.setLatestMessage(latestMessage != null ? latestMessage.getContent() : null);
        response.setUnreadCount(unreadCount != null ? unreadCount : 0L);

        Patient patient = conversation.getPatientId();
        if (patient != null) {
            response.setPatientId(patient.getId());
            response.setPatientCode(patient.getPatientCode());
            response.setPatientName(patient.getFullName());
        }

        Employee staff = conversation.getStaffId();
        if (staff != null) {
            response.setStaffId(staff.getId());
            response.setStaffName(staff.getFullName());
        }

        return response;
    }

    public static SupportMessageResponse toMessageResponse(SupportMessage message) {
        if (message == null) {
            return null;
        }

        SupportMessageResponse response = new SupportMessageResponse();
        response.setId(message.getId());
        response.setContent(message.getContent());
        response.setSenderRole(message.getSenderRole());
        response.setMessageType(message.getMessageType());
        response.setRead(message.getRead());
        response.setCreatedAt(format(message.getCreatedAt()));

        if (message.getConversationId() != null) {
            response.setConversationId(message.getConversationId().getId());
        }

        User sender = message.getSenderId();
        if (sender != null) {
            response.setSenderId(sender.getId());
            response.setSenderName(sender.getFullName());
        }

        return response;
    }

    public static SupportConsultationScheduleResponse toScheduleResponse(SupportConsultationSchedule schedule) {
        if (schedule == null) {
            return null;
        }

        SupportConsultationScheduleResponse response = new SupportConsultationScheduleResponse();
        response.setId(schedule.getId());
        response.setScheduledStart(format(schedule.getScheduledStart()));
        response.setScheduledEnd(format(schedule.getScheduledEnd()));
        response.setMeetLink(schedule.getMeetLink());
        response.setStatus(schedule.getStatus());
        response.setNote(schedule.getNote());
        response.setCreatedAt(format(schedule.getCreatedAt()));

        if (schedule.getConversationId() != null) {
            response.setConversationId(schedule.getConversationId().getId());
        }

        Patient patient = schedule.getPatientId();
        if (patient != null) {
            response.setPatientId(patient.getId());
            response.setPatientName(patient.getFullName());
        }

        Doctor doctor = schedule.getDoctorId();
        if (doctor != null) {
            response.setDoctorId(doctor.getId());
            response.setDoctorName(doctor.getFullName());
        }

        Employee staff = schedule.getStaffId();
        if (staff != null) {
            response.setStaffId(staff.getId());
            response.setStaffName(staff.getFullName());
        }

        return response;
    }

    private static String format(Date date) {
        if (date == null) {
            return null;
        }
        return new SimpleDateFormat(DATE_TIME_PATTERN).format(date);
    }
}
