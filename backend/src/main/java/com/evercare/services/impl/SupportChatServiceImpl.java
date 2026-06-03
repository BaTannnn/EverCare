package com.evercare.services.impl;

import com.evercare.dtos.request.SupportConsultationScheduleRequest;
import com.evercare.dtos.request.SupportConversationRequest;
import com.evercare.dtos.request.SupportMessageRequest;
import com.evercare.dtos.response.SupportConsultationScheduleResponse;
import com.evercare.dtos.response.SupportConversationResponse;
import com.evercare.dtos.response.SupportMessageResponse;
import com.evercare.mappers.SupportChatMapper;
import com.evercare.pojo.Doctor;
import com.evercare.pojo.Employee;
import com.evercare.pojo.Patient;
import com.evercare.pojo.SupportConsultationSchedule;
import com.evercare.pojo.SupportConversation;
import com.evercare.pojo.SupportMessage;
import com.evercare.pojo.User;
import com.evercare.repositories.DoctorRepository;
import com.evercare.repositories.SupportConsultationScheduleRepository;
import com.evercare.repositories.SupportConversationRepository;
import com.evercare.repositories.SupportMessageRepository;
import com.evercare.services.SupportChatService;
import com.evercare.utils.AuthSupport;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SupportChatServiceImpl implements SupportChatService {

    private static final String STATUS_OPEN = "OPEN";
    private static final String STATUS_ASSIGNED = "ASSIGNED";
    private static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    private static final String STATUS_CLOSED = "CLOSED";
    private static final String MESSAGE_TYPE_TEXT = "TEXT";
    private static final String MESSAGE_TYPE_MEET_SCHEDULE = "MEET_SCHEDULE";
    private static final String SENDER_PATIENT = "PATIENT";
    private static final String SENDER_STAFF = "STAFF";
    private static final String SCHEDULE_STATUS_SCHEDULED = "SCHEDULED";

    @Autowired
    private SupportConversationRepository conversationRepo;

    @Autowired
    private SupportMessageRepository messageRepo;

    @Autowired
    private SupportConsultationScheduleRepository scheduleRepo;

    @Autowired
    private DoctorRepository doctorRepo;

    @Autowired
    private AuthSupport authSupport;

    @Override
    public List<SupportConversationResponse> getPatientConversations() {
        User currentUser = this.authSupport.getCurrentUser();
        Patient patient = this.authSupport.requireCurrentPatient(currentUser, "Bạn chưa tạo hồ sơ bệnh nhân");
        return toConversationResponses(this.conversationRepo.getConversationsByPatientId(patient.getId()), currentUser);
    }

    @Override
    public SupportConversationResponse createPatientConversation(SupportConversationRequest request) {
        User currentUser = this.authSupport.getCurrentUser();
        Patient patient = this.authSupport.requireCurrentPatient(currentUser, "Bạn chưa tạo hồ sơ bệnh nhân");
        String initialMessage = trimToNull(request != null ? request.getInitialMessage() : null);
        if (initialMessage == null) {
            throw new IllegalArgumentException("Vui lòng nhập nội dung cần hỗ trợ");
        }

        Date now = new Date();
        SupportConversation conversation = new SupportConversation();
        conversation.setPatientId(patient);
        conversation.setSubject(resolveSubject(request));
        conversation.setStatus(STATUS_OPEN);
        conversation.setCreatedAt(now);
        conversation.setUpdatedAt(now);
        conversation.setActive(true);

        SupportConversation saved = this.conversationRepo.save(conversation);
        createMessage(saved, currentUser, SENDER_PATIENT, MESSAGE_TYPE_TEXT, initialMessage, false, now);

        return toConversationResponse(saved, currentUser);
    }

    @Override
    public List<SupportMessageResponse> getPatientMessages(Long conversationId, Long afterId, Integer limit) {
        User currentUser = this.authSupport.getCurrentUser();
        Patient patient = this.authSupport.requireCurrentPatient(currentUser, "Bạn chưa tạo hồ sơ bệnh nhân");
        SupportConversation conversation = requireConversation(conversationId);
        ensurePatientOwnsConversation(conversation, patient);

        List<SupportMessage> messages = this.messageRepo.getMessagesByConversationId(conversation.getId(), afterId, limit);
        if (!messages.isEmpty()) {
            this.messageRepo.markMessagesRead(conversation.getId(), currentUser.getId());
        }

        return messages
                .stream()
                .map(message -> toMessageResponse(message, conversation))
                .toList();
    }

    @Override
    public SupportMessageResponse sendPatientMessage(Long conversationId, SupportMessageRequest request) {
        User currentUser = this.authSupport.getCurrentUser();
        Patient patient = this.authSupport.requireCurrentPatient(currentUser, "Bạn chưa tạo hồ sơ bệnh nhân");
        SupportConversation conversation = requireConversation(conversationId);
        ensurePatientOwnsConversation(conversation, patient);
        ensureConversationOpen(conversation);

        String content = requireContent(request);
        Date now = new Date();
        if (STATUS_ASSIGNED.equalsIgnoreCase(conversation.getStatus())) {
            conversation.setStatus(STATUS_IN_PROGRESS);
        }
        touchConversation(conversation, now);

        SupportMessage message = createMessage(conversation, currentUser, SENDER_PATIENT, MESSAGE_TYPE_TEXT, content, false, now);
        return toMessageResponse(message, conversation);
    }

    @Override
    public SupportConversationResponse closePatientConversation(Long conversationId) {
        User currentUser = this.authSupport.getCurrentUser();
        Patient patient = this.authSupport.requireCurrentPatient(currentUser, "Bạn chưa tạo hồ sơ bệnh nhân");
        SupportConversation conversation = requireConversation(conversationId);
        ensurePatientOwnsConversation(conversation, patient);
        closeConversation(conversation);
        return toConversationResponse(conversation, currentUser);
    }

    @Override
    public List<SupportConversationResponse> getReceptionistConversations(Map<String, String> params) {
        User currentUser = this.authSupport.requireReceptionistUser();
        Employee employee = this.authSupport.getCurrentReceptionistEmployee(currentUser);
        List<SupportConversation> conversations = this.conversationRepo.getConversationsForReceptionist(params)
                .stream()
                .filter(conversation -> canListConversation(conversation, currentUser, employee))
                .toList();

        return toConversationResponses(conversations, currentUser);
    }

    @Override
    public SupportConversationResponse acceptConversation(Long conversationId) {
        User currentUser = this.authSupport.requireReceptionistUser();
        Employee employee = this.authSupport.getCurrentReceptionistEmployee(currentUser);
        SupportConversation conversation = requireConversation(conversationId);
        ensureConversationOpen(conversation);

        if (conversation.getStaffId() != null && employee != null
                && !conversation.getStaffId().getId().equals(employee.getId())) {
            throw new IllegalStateException("Cuộc trò chuyện đã được nhân viên khác tiếp nhận");
        }

        Date now = new Date();
        if (conversation.getStaffId() == null) {
            conversation.setStaffId(employee);
        }
        if (STATUS_OPEN.equalsIgnoreCase(conversation.getStatus())) {
            conversation.setStatus(STATUS_ASSIGNED);
        }
        conversation.setUpdatedAt(now);
        this.conversationRepo.update(conversation);

        return toConversationResponse(conversation, currentUser);
    }

    @Override
    public List<SupportMessageResponse> getReceptionistMessages(Long conversationId, Long afterId, Integer limit) {
        User currentUser = this.authSupport.requireReceptionistUser();
        SupportConversation conversation = requireConversation(conversationId);
        ensureReceptionistCanAccess(conversation, currentUser);

        List<SupportMessage> messages = this.messageRepo.getMessagesByConversationId(conversation.getId(), afterId, limit);
        if (!messages.isEmpty()) {
            this.messageRepo.markMessagesRead(conversation.getId(), currentUser.getId());
        }

        return messages
                .stream()
                .map(message -> toMessageResponse(message, conversation))
                .toList();
    }

    @Override
    public SupportMessageResponse sendReceptionistMessage(Long conversationId, SupportMessageRequest request) {
        User currentUser = this.authSupport.requireReceptionistUser();
        Employee employee = this.authSupport.getCurrentReceptionistEmployee(currentUser);
        SupportConversation conversation = requireConversation(conversationId);
        ensureReceptionistCanAccess(conversation, currentUser);
        ensureConversationOpen(conversation);

        String content = requireContent(request);
        Date now = new Date();
        if (conversation.getStaffId() == null) {
            conversation.setStaffId(employee);
        }
        if (!STATUS_IN_PROGRESS.equalsIgnoreCase(conversation.getStatus())) {
            conversation.setStatus(STATUS_IN_PROGRESS);
        }
        touchConversation(conversation, now);

        SupportMessage message = createMessage(conversation, currentUser, SENDER_STAFF, MESSAGE_TYPE_TEXT, content, false, now);
        return toMessageResponse(message, conversation);
    }

    @Override
    public SupportConsultationScheduleResponse createConsultationSchedule(
            Long conversationId,
            SupportConsultationScheduleRequest request
    ) {
        User currentUser = this.authSupport.requireReceptionistUser();
        Employee employee = this.authSupport.getCurrentReceptionistEmployee(currentUser);
        SupportConversation conversation = requireConversation(conversationId);
        ensureReceptionistCanAccess(conversation, currentUser);
        ensureConversationOpen(conversation);

        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu lịch tư vấn không hợp lệ");
        }
        if (request.getDoctorId() == null) {
            throw new IllegalArgumentException("Vui lòng chọn bác sĩ tư vấn");
        }

        Doctor doctor = this.doctorRepo.getDoctorById(request.getDoctorId().intValue());
        if (doctor == null || Boolean.FALSE.equals(doctor.getActive())) {
            throw new NoSuchElementException("Không tìm thấy bác sĩ tư vấn");
        }

        Date scheduledStart = parseDateTime(request.getScheduledStart(), "Vui lòng chọn thời gian bắt đầu");
        Date scheduledEnd = parseDateTime(request.getScheduledEnd(), "Vui lòng chọn thời gian kết thúc");
        if (!scheduledEnd.after(scheduledStart)) {
            throw new IllegalArgumentException("Thời gian kết thúc phải sau thời gian bắt đầu");
        }

        String meetLink = trimToNull(request.getMeetLink());
        if (meetLink == null) {
            throw new IllegalArgumentException("Vui lòng nhập link Google Meet");
        }
        if (!(meetLink.startsWith("https://") || meetLink.startsWith("http://"))) {
            throw new IllegalArgumentException("Link Google Meet phải bắt đầu bằng http:// hoặc https://");
        }

        Date now = new Date();
        if (conversation.getStaffId() == null) {
            conversation.setStaffId(employee);
        }
        conversation.setStatus(STATUS_IN_PROGRESS);
        touchConversation(conversation, now);

        SupportConsultationSchedule schedule = new SupportConsultationSchedule();
        schedule.setConversationId(conversation);
        schedule.setPatientId(conversation.getPatientId());
        schedule.setDoctorId(doctor);
        schedule.setStaffId(employee);
        schedule.setScheduledStart(scheduledStart);
        schedule.setScheduledEnd(scheduledEnd);
        schedule.setMeetLink(meetLink);
        schedule.setStatus(SCHEDULE_STATUS_SCHEDULED);
        schedule.setNote(trimToNull(request.getNote()));
        schedule.setCreatedAt(now);
        schedule.setUpdatedAt(now);
        schedule.setActive(true);

        SupportConsultationSchedule saved = this.scheduleRepo.save(schedule);
        String scheduleMessage = buildScheduleMessage(saved);
        createMessage(conversation, currentUser, SENDER_STAFF, MESSAGE_TYPE_MEET_SCHEDULE, scheduleMessage, false, now);

        return SupportChatMapper.toScheduleResponse(saved);
    }

    @Override
    public SupportConversationResponse closeReceptionistConversation(Long conversationId) {
        User currentUser = this.authSupport.requireReceptionistUser();
        SupportConversation conversation = requireConversation(conversationId);
        ensureReceptionistCanAccess(conversation, currentUser);
        closeConversation(conversation);
        return toConversationResponse(conversation, currentUser);
    }

    private SupportConversation requireConversation(Long conversationId) {
        if (conversationId == null) {
            throw new IllegalArgumentException("Conversation không hợp lệ");
        }
        SupportConversation conversation = this.conversationRepo.getConversationById(conversationId);
        if (conversation == null) {
            throw new NoSuchElementException("Không tìm thấy cuộc trò chuyện");
        }
        return conversation;
    }

    private SupportConversationResponse toConversationResponse(SupportConversation conversation, User reader) {
        SupportMessage latestMessage = this.messageRepo.getLatestMessageByConversationId(conversation.getId());
        long unreadCount = reader != null ? this.messageRepo.countUnreadMessages(conversation.getId(), reader.getId()) : 0L;
        return SupportChatMapper.toConversationResponse(conversation, latestMessage, unreadCount);
    }

    private List<SupportConversationResponse> toConversationResponses(List<SupportConversation> conversations, User reader) {
        List<Long> conversationIds = conversations.stream()
                .filter(Objects::nonNull)
                .map(SupportConversation::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, SupportMessage> latestMessages = this.messageRepo.getLatestMessagesByConversationIds(conversationIds);
        Map<Long, Long> unreadCounts = reader != null
                ? this.messageRepo.countUnreadMessagesByConversationIds(conversationIds, reader.getId())
                : new HashMap<>();

        return conversations.stream()
                .map(conversation -> SupportChatMapper.toConversationResponse(
                        conversation,
                        latestMessages.get(conversation.getId()),
                        unreadCounts.getOrDefault(conversation.getId(), 0L)
                ))
                .toList();
    }

    private SupportMessage createMessage(
            SupportConversation conversation,
            User sender,
            String senderRole,
            String messageType,
            String content,
            boolean read,
            Date createdAt
    ) {
        SupportMessage message = new SupportMessage();
        message.setConversationId(conversation);
        message.setSenderId(sender);
        message.setSenderRole(senderRole);
        message.setMessageType(messageType);
        message.setContent(content);
        message.setRead(read);
        message.setCreatedAt(createdAt);
        message.setActive(true);
        return this.messageRepo.save(message);
    }

    private SupportMessageResponse toMessageResponse(SupportMessage message, SupportConversation conversation) {
        SupportMessageResponse response = SupportChatMapper.toMessageResponse(message);
        if (response != null && response.getSenderRole() == null) {
            response.setSenderRole(resolveSenderRole(message, conversation));
        }
        return response;
    }

    private String resolveSenderRole(SupportMessage message, SupportConversation conversation) {
        Long senderUserId = message != null && message.getSenderId() != null ? message.getSenderId().getId() : null;
        Long patientUserId = conversation != null
                && conversation.getPatientId() != null
                && conversation.getPatientId().getUserId() != null
                ? conversation.getPatientId().getUserId().getId()
                : null;

        if (senderUserId != null && senderUserId.equals(patientUserId)) {
            return SENDER_PATIENT;
        }
        return SENDER_STAFF;
    }

    private void touchConversation(SupportConversation conversation, Date now) {
        conversation.setUpdatedAt(now);
        this.conversationRepo.update(conversation);
    }

    private void closeConversation(SupportConversation conversation) {
        if (STATUS_CLOSED.equalsIgnoreCase(conversation.getStatus())) {
            return;
        }
        Date now = new Date();
        conversation.setStatus(STATUS_CLOSED);
        conversation.setClosedAt(now);
        conversation.setUpdatedAt(now);
        this.conversationRepo.update(conversation);
    }

    private void ensurePatientOwnsConversation(SupportConversation conversation, Patient patient) {
        if (conversation.getPatientId() == null || !conversation.getPatientId().getId().equals(patient.getId())) {
            throw new AccessDeniedException("Bạn không có quyền truy cập cuộc trò chuyện này");
        }
    }

    private void ensureReceptionistCanAccess(SupportConversation conversation, User currentUser) {
        if (this.authSupport.hasAnyRole(currentUser, "ROLE_ADMIN")) {
            return;
        }

        Employee employee = this.authSupport.getCurrentReceptionistEmployee(currentUser);
        if (conversation.getStaffId() == null) {
            return;
        }

        if (!conversation.getStaffId().getId().equals(employee.getId())) {
            throw new AccessDeniedException("Cuộc trò chuyện đang do nhân viên khác xử lý");
        }
    }

    private boolean canListConversation(SupportConversation conversation, User currentUser, Employee employee) {
        if (this.authSupport.hasAnyRole(currentUser, "ROLE_ADMIN")) {
            return true;
        }
        return conversation.getStaffId() == null
                || (employee != null && conversation.getStaffId().getId().equals(employee.getId()));
    }

    private void ensureConversationOpen(SupportConversation conversation) {
        if (STATUS_CLOSED.equalsIgnoreCase(conversation.getStatus())) {
            throw new IllegalStateException("Cuộc trò chuyện đã đóng");
        }
    }

    private String resolveSubject(SupportConversationRequest request) {
        String subject = trimToNull(request != null ? request.getSubject() : null);
        return subject != null ? subject : "Tư vấn trực tuyến";
    }

    private String requireContent(SupportMessageRequest request) {
        String content = trimToNull(request != null ? request.getContent() : null);
        if (content == null) {
            throw new IllegalArgumentException("Vui lòng nhập nội dung tin nhắn");
        }
        return content;
    }

    private Date parseDateTime(String value, String requiredMessage) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw new IllegalArgumentException(requiredMessage);
        }

        try {
            LocalDateTime localDateTime = LocalDateTime.parse(normalized, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Thời gian tư vấn không hợp lệ, định dạng đúng là yyyy-MM-ddTHH:mm");
        }
    }

    private String buildScheduleMessage(SupportConsultationSchedule schedule) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm dd/MM/yyyy");
        String start = formatter.format(schedule.getScheduledStart());
        String end = formatter.format(schedule.getScheduledEnd());
        String doctorName = schedule.getDoctorId() != null ? schedule.getDoctorId().getFullName() : "bác sĩ";

        StringBuilder builder = new StringBuilder();
        builder.append("Lễ tân đã tạo lịch tư vấn với ").append(doctorName).append(".\n");
        builder.append("Thời gian: ").append(start).append(" - ").append(end).append(".\n");
        builder.append("Link Google Meet: ").append(schedule.getMeetLink());
        if (schedule.getNote() != null && !schedule.getNote().isBlank()) {
            builder.append("\nGhi chú: ").append(schedule.getNote());
        }
        return builder.toString();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
