import { Badge, Button, Card, Col, Form, Row } from "react-bootstrap";
import { BsCalendar2Plus, BsChatDots, BsCheck2Circle, BsPersonCheck, BsSend } from "react-icons/bs";
import EmptyState from "../../common/EmptyState";
import LoadingState from "../../common/LoadingState";
import {
  formatSupportDateTime,
  supportStatusMeta,
} from "../../../pages/receptionist/receptionistSupportUtils";

function SupportChatWindow({
  doctors,
  messageText,
  messages,
  messagesLoading,
  schedule,
  selectedConversation,
  submitting,
  onAcceptConversation,
  onCloseConversation,
  onCreateSchedule,
  onMessageTextChange,
  onScheduleChange,
  onSendMessage,
}) {
  return (
    <Card className="support-chat-window-card">
      <Card.Body>
        {selectedConversation ? (
          <>
            <div className="support-chat-header">
              <div>
                <h5>{selectedConversation.patientName || "Bệnh nhân"}</h5>
                <span>
                  {selectedConversation.patientCode || "Chưa có mã"} · {selectedConversation.subject || "Tư vấn trực tuyến"}
                </span>
              </div>
              <div className="support-chat-actions">
                <Badge bg={(supportStatusMeta[selectedConversation.status] || supportStatusMeta.OPEN).variant}>
                  {(supportStatusMeta[selectedConversation.status] || supportStatusMeta.OPEN).label}
                </Badge>
                {selectedConversation.status === "OPEN" && (
                  <Button type="button" variant="outline-primary" size="sm" onClick={onAcceptConversation} disabled={submitting}>
                    <BsPersonCheck /> Tiếp nhận
                  </Button>
                )}
                {selectedConversation.status !== "CLOSED" && (
                  <Button type="button" variant="outline-secondary" size="sm" onClick={onCloseConversation} disabled={submitting}>
                    <BsCheck2Circle /> Đóng
                  </Button>
                )}
              </div>
            </div>

            <div className="support-message-list">
              {messagesLoading ? (
                <LoadingState message="Đang tải tin nhắn..." />
              ) : messages.length === 0 ? (
                <EmptyState title="Chưa có tin nhắn" description="Tin nhắn của bệnh nhân sẽ hiển thị tại đây." />
              ) : (
                messages.map((message) => (
                  <div
                    key={message.id}
                    className={`support-message ${message.senderRole === "STAFF" ? "mine" : "theirs"} ${message.messageType === "MEET_SCHEDULE" ? "schedule" : ""}`}
                  >
                    <div className="support-message-bubble">
                      <strong>{message.senderRole === "STAFF" ? "Bạn" : message.senderName || "Bệnh nhân"}</strong>
                      <p>{message.content}</p>
                      <span>{formatSupportDateTime(message.createdAt)}</span>
                    </div>
                  </div>
                ))
              )}
            </div>

            <Form onSubmit={onSendMessage} className="support-message-form">
              <Form.Control
                value={messageText}
                onChange={(event) => onMessageTextChange(event.target.value)}
                placeholder={selectedConversation.status === "CLOSED" ? "Cuộc trò chuyện đã đóng" : "Nhập phản hồi cho bệnh nhân..."}
                disabled={selectedConversation.status === "CLOSED" || submitting}
              />
              <Button type="submit" disabled={selectedConversation.status === "CLOSED" || submitting || !messageText.trim()}>
                <BsSend />
              </Button>
            </Form>

            {selectedConversation.status !== "CLOSED" && (
              <div className="support-schedule-card">
                <h5><BsCalendar2Plus /> Tạo lịch tư vấn Google Meet</h5>
                <Form onSubmit={onCreateSchedule}>
                  <Row>
                    <Col md={6}>
                      <Form.Group className="mb-3">
                        <Form.Label>Bác sĩ</Form.Label>
                        <Form.Select
                          value={schedule.doctorId}
                          onChange={(event) => onScheduleChange({ doctorId: event.target.value })}
                          required
                        >
                          <option value="">Chọn bác sĩ</option>
                          {doctors.map((doctor) => (
                            <option key={doctor.id} value={doctor.id}>
                              {doctor.fullName || doctor.name}
                            </option>
                          ))}
                        </Form.Select>
                      </Form.Group>
                    </Col>
                    <Col md={6}>
                      <Form.Group className="mb-3">
                        <Form.Label>Ngày tư vấn</Form.Label>
                        <Form.Control
                          type="date"
                          value={schedule.consultationDate}
                          onChange={(event) => onScheduleChange({ consultationDate: event.target.value })}
                          required
                        />
                      </Form.Group>
                    </Col>
                    <Col md={6}>
                      <Form.Group className="mb-3">
                        <Form.Label>Giờ bắt đầu</Form.Label>
                        <Form.Control
                          type="time"
                          value={schedule.startTime}
                          onChange={(event) => onScheduleChange({ startTime: event.target.value })}
                          required
                        />
                      </Form.Group>
                    </Col>
                    <Col md={6}>
                      <Form.Group className="mb-3">
                        <Form.Label>Giờ kết thúc</Form.Label>
                        <Form.Control
                          type="time"
                          value={schedule.endTime}
                          onChange={(event) => onScheduleChange({ endTime: event.target.value })}
                          required
                        />
                      </Form.Group>
                    </Col>
                  </Row>
                  <Form.Group className="mb-3">
                    <Form.Label>Link Google Meet</Form.Label>
                    <Form.Control
                      value={schedule.meetLink}
                      onChange={(event) => onScheduleChange({ meetLink: event.target.value })}
                      placeholder="https://meet.google.com/xxx-xxxx-xxx"
                      required
                    />
                  </Form.Group>
                  <Form.Group className="mb-3">
                    <Form.Label>Ghi chú</Form.Label>
                    <Form.Control
                      as="textarea"
                      rows={2}
                      value={schedule.note}
                      onChange={(event) => onScheduleChange({ note: event.target.value })}
                      placeholder="Ví dụ: Bệnh nhân cần chuẩn bị hồ sơ cũ."
                    />
                  </Form.Group>
                  <Button type="submit" disabled={submitting}>
                    Gửi lịch tư vấn
                  </Button>
                </Form>
              </div>
            )}
          </>
        ) : (
          <EmptyState icon={BsChatDots} title="Chọn cuộc trò chuyện" description="Chọn một yêu cầu hỗ trợ để xem và phản hồi." />
        )}
      </Card.Body>
    </Card>
  );
}

export default SupportChatWindow;
