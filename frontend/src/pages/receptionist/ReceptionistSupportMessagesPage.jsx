import { useCallback, useEffect, useMemo, useState } from "react";
import { Alert, Badge, Button, Card, Form, ListGroup, Row, Col } from "react-bootstrap";
import { BsCalendar2Plus, BsChatDots, BsCheck2Circle, BsPersonCheck, BsSearch, BsSend } from "react-icons/bs";
import EmptyState from "../../components/common/EmptyState";
import LoadingState from "../../components/common/LoadingState";
import { getReceptionistDoctors } from "../../services/receptionist/receptionistReferenceApi";
import {
  acceptReceptionistSupportConversation,
  closeReceptionistSupportConversation,
  createReceptionistSupportSchedule,
  getReceptionistSupportConversations,
  getReceptionistSupportMessages,
  sendReceptionistSupportMessage,
} from "../../services/receptionist/receptionistSupportApi";
import { getErrorMessage } from "./receptionistPageUtils";

const statusMeta = {
  OPEN: { label: "Chờ tiếp nhận", variant: "warning" },
  ASSIGNED: { label: "Đã tiếp nhận", variant: "primary" },
  IN_PROGRESS: { label: "Đang hỗ trợ", variant: "info" },
  CLOSED: { label: "Đã đóng", variant: "secondary" },
};

const formatDateTime = (value) => {
  if (!value) return "--";
  const date = new Date(String(value).replace(" ", "T"));
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat("vi-VN", { dateStyle: "short", timeStyle: "short" }).format(date);
};

const defaultSchedule = () => ({
  doctorId: "",
  consultationDate: "",
  startTime: "",
  endTime: "",
  meetLink: "",
  note: "",
});

const CONVERSATIONS_PAGE_SIZE = 6;

const pickConversationId = (items, preferredId, fallbackId) => {
  const hasId = (id) => id && items.some((item) => String(item.id) === String(id));
  if (hasId(preferredId)) return preferredId;
  if (hasId(fallbackId)) return fallbackId;
  return items[0]?.id || null;
};

function ReceptionistSupportMessagesPage() {
  const [conversations, setConversations] = useState([]);
  const [selectedId, setSelectedId] = useState(null);
  const [messages, setMessages] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [filters, setFilters] = useState({ status: "", keyword: "" });
  const [loading, setLoading] = useState(true);
  const [messagesLoading, setMessagesLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [messageText, setMessageText] = useState("");
  const [schedule, setSchedule] = useState(defaultSchedule);
  const [conversationPage, setConversationPage] = useState(1);

  const selectedConversation = useMemo(
    () => conversations.find((conversation) => String(conversation.id) === String(selectedId)),
    [conversations, selectedId],
  );

  const totalConversationPages = Math.max(1, Math.ceil(conversations.length / CONVERSATIONS_PAGE_SIZE));

  const paginatedConversations = useMemo(() => {
    const safePage = Math.min(conversationPage, totalConversationPages);
    const start = (safePage - 1) * CONVERSATIONS_PAGE_SIZE;
    return conversations.slice(start, start + CONVERSATIONS_PAGE_SIZE);
  }, [conversationPage, conversations, totalConversationPages]);

  const loadConversations = useCallback(async (preferredId, fallbackId) => {
    const params = {};
    if (filters.status) params.status = filters.status;
    if (filters.keyword.trim()) params.keyword = filters.keyword.trim();

    const response = await getReceptionistSupportConversations(params);
    const items = response.data || [];
    setConversations(items);

    const nextSelectedId = pickConversationId(items, preferredId, fallbackId);
    setSelectedId(nextSelectedId);
    return nextSelectedId;
  }, [filters.keyword, filters.status]);

  const loadMessages = useCallback(async (conversationId, silent = false) => {
    if (!conversationId) {
      setMessages([]);
      return;
    }

    if (!silent) setMessagesLoading(true);
    try {
      const response = await getReceptionistSupportMessages(conversationId);
      setMessages(response.data || []);
    } catch (err) {
      setError(getErrorMessage(err));
      setMessages([]);
    } finally {
      if (!silent) setMessagesLoading(false);
    }
  }, []);

  useEffect(() => {
    let mounted = true;

    const init = async () => {
      setLoading(true);
      setError("");
      try {
        const [doctorResponse] = await Promise.all([getReceptionistDoctors()]);
        if (mounted) {
          setDoctors(doctorResponse.data || []);
        }
        const nextSelectedId = await loadConversations();
        if (mounted && nextSelectedId) {
          await loadMessages(nextSelectedId);
        }
      } catch (err) {
        if (mounted) setError(getErrorMessage(err));
      } finally {
        if (mounted) setLoading(false);
      }
    };

    init();

    return () => {
      mounted = false;
    };
  }, [loadConversations, loadMessages]);

  useEffect(() => {
    if (!selectedId) return undefined;
    const timer = window.setInterval(() => {
      loadMessages(selectedId, true);
      loadConversations(selectedId, selectedId).catch(() => {});
    }, 3000);
    return () => window.clearInterval(timer);
  }, [loadConversations, loadMessages, selectedId]);

  const selectConversation = (conversationId) => {
    setSelectedId(conversationId);
    loadMessages(conversationId);
  };

  const submitFilters = async (event) => {
    event.preventDefault();
    setLoading(true);
    setError("");
    try {
      setConversationPage(1);
      const nextSelectedId = await loadConversations(null, null);
      await loadMessages(nextSelectedId);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const acceptConversation = async () => {
    if (!selectedId) return;
    setSubmitting(true);
    setError("");
    setNotice("");
    try {
      await acceptReceptionistSupportConversation(selectedId);
      await loadConversations(selectedId, selectedId);
      setNotice("Đã tiếp nhận cuộc trò chuyện.");
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  const sendMessage = async (event) => {
    event.preventDefault();
    if (!selectedId || !messageText.trim()) return;
    setSubmitting(true);
    setError("");
    try {
      await sendReceptionistSupportMessage(selectedId, { content: messageText.trim() });
      setMessageText("");
      await loadMessages(selectedId);
      await loadConversations(selectedId, selectedId);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  const createSchedule = async (event) => {
    event.preventDefault();
    if (!selectedId) return;
    setSubmitting(true);
    setError("");
    setNotice("");
    try {
      await createReceptionistSupportSchedule(selectedId, {
        doctorId: schedule.doctorId,
        scheduledStart: `${schedule.consultationDate}T${schedule.startTime}`,
        scheduledEnd: `${schedule.consultationDate}T${schedule.endTime}`,
        meetLink: schedule.meetLink,
        note: schedule.note,
      });
      setSchedule(defaultSchedule());
      await loadMessages(selectedId);
      await loadConversations(selectedId, selectedId);
      setNotice("Đã gửi lịch tư vấn Google Meet cho bệnh nhân.");
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  const closeConversation = async () => {
    if (!selectedId) return;
    setSubmitting(true);
    setError("");
    try {
      await closeReceptionistSupportConversation(selectedId);
      await loadConversations(selectedId, selectedId);
      setNotice("Đã đóng cuộc trò chuyện.");
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return <LoadingState message="Đang tải tin nhắn hỗ trợ..." />;
  }

  return (
    <div className="receptionist-page support-chat-page">
      <div className="receptionist-page-header">
        <div>
          <p className="receptionist-eyebrow">Tin nhắn hỗ trợ</p>
          <h2>Tư vấn trực tuyến với bệnh nhân</h2>
        </div>
      </div>

      {error && <Alert variant="danger">{error}</Alert>}
      {notice && <Alert variant="success">{notice}</Alert>}

      <div className="support-chat-layout receptionist-support-layout">
        <Card className="support-chat-list-card">
          <Card.Body>
            <Form onSubmit={submitFilters} className="support-filter-form">
              <Form.Group className="mb-2">
                <Form.Label>Tìm kiếm</Form.Label>
                <div className="support-search-control">
                  <BsSearch />
                  <Form.Control
                    value={filters.keyword}
                    onChange={(event) => setFilters((current) => ({ ...current, keyword: event.target.value }))}
                    placeholder="Tên, SĐT, chủ đề..."
                  />
                </div>
              </Form.Group>
              <Form.Group className="mb-3">
                <Form.Label>Trạng thái</Form.Label>
                <Form.Select
                  value={filters.status}
                  onChange={(event) => setFilters((current) => ({ ...current, status: event.target.value }))}
                >
                  <option value="">Tất cả</option>
                  <option value="OPEN">Chờ tiếp nhận</option>
                  <option value="ASSIGNED">Đã tiếp nhận</option>
                  <option value="IN_PROGRESS">Đang hỗ trợ</option>
                  <option value="CLOSED">Đã đóng</option>
                </Form.Select>
              </Form.Group>
              <Button type="submit" variant="outline-primary" className="w-100">
                Lọc cuộc trò chuyện
              </Button>
            </Form>

            <hr />

            {conversations.length === 0 ? (
              <EmptyState title="Chưa có tin nhắn" description="Yêu cầu hỗ trợ của bệnh nhân sẽ hiển thị tại đây." />
            ) : (
              <>
                <ListGroup className="support-conversation-list">
                  {paginatedConversations.map((conversation) => {
                    const meta = statusMeta[conversation.status] || { label: conversation.status, variant: "secondary" };
                    return (
                      <ListGroup.Item
                        key={conversation.id}
                        action
                        active={String(conversation.id) === String(selectedId)}
                        onClick={() => selectConversation(conversation.id)}
                      >
                        <div className="support-conversation-title">
                          <strong>{conversation.patientName || "Bệnh nhân"}</strong>
                          {conversation.unreadCount > 0 && <Badge bg="danger">{conversation.unreadCount}</Badge>}
                        </div>
                        <span>{conversation.subject || "Tư vấn trực tuyến"}</span>
                        <small>{conversation.latestMessage || "Chưa có tin nhắn"}</small>
                        <Badge bg={meta.variant}>{meta.label}</Badge>
                      </ListGroup.Item>
                    );
                  })}
                </ListGroup>

                {totalConversationPages > 1 && (
                  <div className="support-pagination">
                    <Button
                      type="button"
                      variant="outline-secondary"
                      size="sm"
                      disabled={conversationPage <= 1}
                      onClick={() => setConversationPage((current) => Math.max(1, current - 1))}
                    >
                      Trước
                    </Button>
                    <span>
                      Trang {Math.min(conversationPage, totalConversationPages)} / {totalConversationPages}
                    </span>
                    <Button
                      type="button"
                      variant="outline-secondary"
                      size="sm"
                      disabled={conversationPage >= totalConversationPages}
                      onClick={() => setConversationPage((current) => Math.min(totalConversationPages, current + 1))}
                    >
                      Sau
                    </Button>
                  </div>
                )}
              </>
            )}
          </Card.Body>
        </Card>

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
                    <Badge bg={(statusMeta[selectedConversation.status] || statusMeta.OPEN).variant}>
                      {(statusMeta[selectedConversation.status] || statusMeta.OPEN).label}
                    </Badge>
                    {selectedConversation.status === "OPEN" && (
                      <Button type="button" variant="outline-primary" size="sm" onClick={acceptConversation} disabled={submitting}>
                        <BsPersonCheck /> Tiếp nhận
                      </Button>
                    )}
                    {selectedConversation.status !== "CLOSED" && (
                      <Button type="button" variant="outline-secondary" size="sm" onClick={closeConversation} disabled={submitting}>
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
                          <span>{formatDateTime(message.createdAt)}</span>
                        </div>
                      </div>
                    ))
                  )}
                </div>

                <Form onSubmit={sendMessage} className="support-message-form">
                  <Form.Control
                    value={messageText}
                    onChange={(event) => setMessageText(event.target.value)}
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
                    <Form onSubmit={createSchedule}>
                      <Row>
                        <Col md={6}>
                          <Form.Group className="mb-3">
                            <Form.Label>Bác sĩ</Form.Label>
                            <Form.Select
                              value={schedule.doctorId}
                              onChange={(event) => setSchedule((current) => ({ ...current, doctorId: event.target.value }))}
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
                              onChange={(event) => setSchedule((current) => ({ ...current, consultationDate: event.target.value }))}
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
                              onChange={(event) => setSchedule((current) => ({ ...current, startTime: event.target.value }))}
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
                              onChange={(event) => setSchedule((current) => ({ ...current, endTime: event.target.value }))}
                              required
                            />
                          </Form.Group>
                        </Col>
                      </Row>
                      <Form.Group className="mb-3">
                        <Form.Label>Link Google Meet</Form.Label>
                        <Form.Control
                          value={schedule.meetLink}
                          onChange={(event) => setSchedule((current) => ({ ...current, meetLink: event.target.value }))}
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
                          onChange={(event) => setSchedule((current) => ({ ...current, note: event.target.value }))}
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
      </div>
    </div>
  );
}

export default ReceptionistSupportMessagesPage;
