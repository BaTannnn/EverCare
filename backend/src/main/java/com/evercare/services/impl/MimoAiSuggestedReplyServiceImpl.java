package com.evercare.services.impl;

import com.evercare.dtos.response.AiSuggestedReplyResponse;
import com.evercare.enums.AiReplyType;
import com.evercare.enums.MedicalServiceType;
import com.evercare.pojo.Employee;
import com.evercare.pojo.MedicalService;
import com.evercare.pojo.SupportConversation;
import com.evercare.pojo.SupportMessage;
import com.evercare.pojo.User;
import com.evercare.repositories.MedicalServiceRepository;
import com.evercare.repositories.SupportConversationRepository;
import com.evercare.repositories.SupportMessageRepository;
import com.evercare.services.AiSuggestedReplyService;
import com.evercare.utils.AuthSupport;
import com.evercare.utils.SensitiveDataSanitizer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MimoAiSuggestedReplyServiceImpl implements AiSuggestedReplyService {

    private static final Logger LOGGER = Logger.getLogger(MimoAiSuggestedReplyServiceImpl.class.getName());
    private static final String DISCLAIMER = "Câu trả lời do AI gợi ý, lễ tân cần kiểm tra trước khi gửi.";
    private static final String FALLBACK_REPLY = "Dạ, em đã ghi nhận thông tin của anh/chị. Với nội dung này, anh/chị nên đặt lịch khám để bác sĩ tư vấn chính xác hơn. Em có thể hỗ trợ anh/chị chọn dịch vụ khám và khung giờ phù hợp ạ.";
    private static final int MESSAGE_CONTEXT_LIMIT = 20;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    @Autowired
    private Environment env;
    @Autowired
    private AuthSupport authSupport;
    @Autowired
    private SupportConversationRepository conversationRepo;
    @Autowired
    private SupportMessageRepository messageRepo;
    @Autowired
    private MedicalServiceRepository medicalServiceRepo;
    @Autowired
    private SensitiveDataSanitizer sensitiveDataSanitizer;

    @Override
    public AiSuggestedReplyResponse suggestReceptionistReply(Long conversationId) {
        User currentUser = this.authSupport.requireReceptionistUser();
        SupportConversation conversation = requireConversation(conversationId);
        ensureReceptionistCanAccess(conversation, currentUser);

        try {
            List<SupportMessage> messages = this.messageRepo.getMessagesByConversationId(
                    conversation.getId(),
                    null,
                    MESSAGE_CONTEXT_LIMIT
            );
            List<MedicalService> examinationServices = this.medicalServiceRepo.getActiveMedicalServicesByType(
                    MedicalServiceType.EXAMINATION.getCode()
            );

            String prompt = buildPrompt(messages, examinationServices);
            String aiContent = requestSuggestedReply(prompt);
            return normalizeResponse(parseAiJson(aiContent));
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "AI suggested reply fallback for conversation " + conversationId, ex);
            return fallbackResponse();
        }
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

    private void ensureReceptionistCanAccess(SupportConversation conversation, User currentUser) {
        if (this.authSupport.hasAnyRole(currentUser, "ROLE_ADMIN")) {
            return;
        }

        Employee employee = this.authSupport.getCurrentReceptionistEmployee(currentUser);
        if (conversation.getStaffId() == null) {
            return;
        }

        if (employee == null || !conversation.getStaffId().getId().equals(employee.getId())) {
            throw new AccessDeniedException("Cuộc trò chuyện đang do nhân viên khác xử lý");
        }
    }

    private String buildPrompt(List<SupportMessage> messages, List<MedicalService> services) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Bạn là trợ lý AI hỗ trợ lễ tân phòng khám EverCare soạn bản nháp trả lời bệnh nhân.\n");
        prompt.append("AI chỉ gợi ý câu trả lời để lễ tân kiểm tra, chỉnh sửa và tự gửi. Không tự gửi tin nhắn.\n\n");
        prompt.append("Quy tắc bắt buộc:\n");
        prompt.append("- Chỉ hỗ trợ đặt lịch, dịch vụ khám, giờ làm việc, quy trình khám và thanh toán.\n");
        prompt.append("- Nếu bệnh nhân mô tả triệu chứng, chỉ gợi ý chọn dịch vụ khám phù hợp hoặc đặt lịch gặp bác sĩ.\n");
        prompt.append("- Không chẩn đoán bệnh, không kê đơn thuốc, không đưa phác đồ điều trị, không khẳng định bệnh nhân bị bệnh gì.\n");
        prompt.append("- Nếu có dấu hiệu nguy hiểm như đau ngực, khó thở, ngất, sốt cao kéo dài, chảy máu nhiều, phải khuyên bệnh nhân đi khám sớm hoặc cấp cứu.\n");
        prompt.append("- Không được tự khẳng định phòng khám còn lịch trống, còn slot, hoặc đã có thể đặt vào một ngày/giờ cụ thể vì bạn không được cung cấp dữ liệu lịch hẹn.\n");
        prompt.append("- Nếu bệnh nhân hỏi về ngày/giờ đặt lịch, chỉ được đề nghị lễ tân kiểm tra lịch trên hệ thống và hỏi bệnh nhân khung giờ mong muốn.\n");
        prompt.append("- Không tự suy luận ngày hiện tại, ngày trong tuần, hoặc quy đổi các cụm như 'thứ 3 tuần sau' thành ngày cụ thể nếu cuộc trò chuyện không nêu rõ.\n");
        prompt.append("- Văn phong lịch sự, thân thiện, chuyên nghiệp, dùng xưng hô dạ, anh/chị.\n");
        prompt.append("- Trả lời ngắn gọn, rõ ý, bằng tiếng Việt.\n\n");
        prompt.append("Danh sách dịch vụ khám active service_type=EXAMINATION:\n");
        if (services == null || services.isEmpty()) {
            prompt.append("- Chưa có dữ liệu dịch vụ khám.\n");
        } else {
            for (MedicalService service : services) {
                prompt.append("- serviceId=").append(service.getId())
                        .append(", serviceName=\"").append(nullToEmpty(service.getName())).append("\"")
                        .append(", price=\"").append(formatPrice(service.getPrice())).append("\"\n");
            }
        }

        prompt.append("\nNội dung cuộc trò chuyện gần nhất:\n");
        if (messages == null || messages.isEmpty()) {
            prompt.append("- Chưa có tin nhắn.\n");
        } else {
            for (SupportMessage message : messages) {
                prompt.append("- [").append(resolveSenderLabel(message)).append("] ")
                        .append(formatDate(message)).append(": ")
                        .append(this.sensitiveDataSanitizer.sanitize(message.getContent())).append("\n");
            }
        }

        prompt.append("\nChỉ trả đúng JSON object, không markdown, không giải thích thêm. Schema:\n");
        prompt.append("{\"suggestedReply\":\"...\",\"replyType\":\"GENERAL_SUPPORT|BOOKING_SUPPORT|MEDICAL_NEEDS_DOCTOR|PAYMENT_SUPPORT|EMERGENCY_WARNING\",\"warning\":\"\",\"disclaimer\":\"")
                .append(DISCLAIMER)
                .append("\"}");
        return prompt.toString();
    }

    private String requestSuggestedReply(String prompt) throws Exception {
        String apiKey = firstNonBlank(
                this.env.getProperty("mimo.api.key"),
                this.env.getProperty("MIMO_API_KEY")
        );
        if (apiKey == null) {
            throw new IllegalStateException("Chưa cấu hình API key cho MiMo/OpenAI-compatible API");
        }

        String url = firstNonBlank(
                this.env.getProperty("mimo.chatCompletionsUrl"),
                this.env.getProperty("mimo.chat-completions-url")
        );
        String model = firstNonBlank(
                this.env.getProperty("mimo.model"),
                this.env.getProperty("MIMO_MODEL")
        );

        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("temperature", 0.2);
        body.put("messages", List.of(userMessage));
        body.put("response_format", Map.of("type", "json_object"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(this.objectMapper.writeValueAsString(body), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("AI API trả về HTTP " + response.statusCode());
        }

        JsonNode root = this.objectMapper.readTree(response.body());
        JsonNode content = root.path("choices").path(0).path("message").path("content");
        if (content.isMissingNode() || content.asText().isBlank()) {
            throw new IllegalStateException("AI API không trả nội dung gợi ý");
        }
        return content.asText();
    }

    private AiSuggestedReplyResponse parseAiJson(String content) throws Exception {
        String normalized = stripCodeFence(content);
        JsonNode node = this.objectMapper.readTree(normalized);

        AiSuggestedReplyResponse response = new AiSuggestedReplyResponse();
        response.setSuggestedReply(textValue(node, "suggestedReply"));
        response.setReplyType(textValue(node, "replyType"));
        response.setWarning(textValue(node, "warning"));
        response.setDisclaimer(textValue(node, "disclaimer"));
        return response;
    }

    private AiSuggestedReplyResponse normalizeResponse(AiSuggestedReplyResponse response) {
        if (response == null || isBlank(response.getSuggestedReply())) {
            throw new IllegalArgumentException("AI JSON thiếu suggestedReply");
        }

        AiReplyType replyType = parseReplyType(response.getReplyType());
        response.setReplyType(replyType.name());
        if (response.getWarning() == null) {
            response.setWarning("");
        }
        response.setDisclaimer(DISCLAIMER);
        return response;
    }

    private AiSuggestedReplyResponse fallbackResponse() {
        AiSuggestedReplyResponse response = new AiSuggestedReplyResponse();
        response.setSuggestedReply(FALLBACK_REPLY);
        response.setReplyType(AiReplyType.MEDICAL_NEEDS_DOCTOR.name());
        response.setWarning("");
        response.setDisclaimer(DISCLAIMER);
        return response;
    }

    private AiReplyType parseReplyType(String value) {
        if (value != null) {
            for (AiReplyType type : AiReplyType.values()) {
                if (type.name().equalsIgnoreCase(value.trim())) {
                    return type;
                }
            }
        }
        return AiReplyType.GENERAL_SUPPORT;
    }

    private String stripCodeFence(String content) {
        String normalized = content != null ? content.trim() : "";
        if (normalized.startsWith("```")) {
            normalized = normalized.replaceFirst("^```(?:json)?\\s*", "");
            normalized = normalized.replaceFirst("\\s*```$", "");
        }
        return normalized.trim();
    }

    private String textValue(JsonNode node, String field) {
        JsonNode value = node != null ? node.get(field) : null;
        return value != null && !value.isNull() ? value.asText() : null;
    }

    private String resolveSenderLabel(SupportMessage message) {
        String role = message.getSenderRole();
        if (role != null && !role.isBlank()) {
            return "PATIENT".equalsIgnoreCase(role) ? "Bệnh nhân" : "Lễ tân";
        }
        return message.getSenderId() != null && message.getSenderId().getPatient() != null ? "Bệnh nhân" : "Lễ tân";
    }

    private String formatDate(SupportMessage message) {
        if (message.getCreatedAt() == null) {
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(message.getCreatedAt());
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) {
            return "Liên hệ";
        }
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return formatter.format(price) + " VND";
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (!isBlank(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private String nullToEmpty(String value) {
        return value != null ? value : "";
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
