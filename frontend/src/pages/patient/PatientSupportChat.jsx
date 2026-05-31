import { useCallback, useEffect, useMemo, useState } from "react";
import { Alert, Badge, Button, Card, Form, ListGroup } from "react-bootstrap";
import { BsChatDots, BsCheck2Circle, BsPlusCircle, BsSend } from "react-icons/bs";
import EmptyState from "../../components/common/EmptyState";
import LoadingState from "../../components/common/LoadingState";
import {
  closePatientSupportConversation,
  createPatientSupportConversation,
  getPatientSupportConversations,
  getPatientSupportMessages,
  sendPatientSupportMessage,
} from "../../services/patient/patientSupportApi";

const statusMeta = {
  OPEN: { label: "Đang chờ lễ tân", variant: "warning" },
  ASSIGNED: { label: "Đã tiếp nhận", variant: "primary" },
  IN_PROGRESS: { label: "Đang hỗ trợ", variant: "info" },
  CLOSED: { label: "Đã đóng", variant: "secondary" },
};

const getErrorMessage = (error) => error?.response?.data?.message || error?.message || "Đã có lỗi xảy ra.";

const formatDateTime = (value) => {
  if (!value) return "--";
  const date = new Date(String(value).replace(" ", "T"));
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat("vi-VN", { dateStyle: "short", timeStyle: "short" }).format(date);
};

const pickConversationId = (items, preferredId, fallbackId) => {
  const hasId = (id) => id && items.some((item) => String(item.id) === String(id));
  if (hasId(preferredId)) return preferredId;
  if (hasId(fallbackId)) return fallbackId;
  return items[0]?.id || null;
};

function PatientSupportChat() {
  const [conversations, setConversations] = useState([]);
  const [selectedId, setSelectedId] = useState(null);
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [messagesLoading, setMessagesLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [notice, setNotice] = useState("");
  const [error, setError] = useState("");
  const [newConversation, setNewConversation] = useState({
    subject: "Tư vấn trực tuyến",
    initialMessage: "",
  });
  const [messageText, setMessageText] = useState("");

  const selectedConversation = useMemo(
    () => conversations.find((conversation) => String(conversation.id) === String(selectedId)),
    [conversations, selectedId],
  );

  const loadConversations = useCallback(async (preferredId, fallbackId) => {
    setError("");
    const response = await getPatientSupportConversations();
    const items = response.data || [];
    setConversations(items);

    const nextSelectedId = pickConversationId(items, preferredId, fallbackId);
    setSelectedId(nextSelectedId);
    return nextSelectedId;
  }, []);

  const loadMessages = useCallback(async (conversationId, silent = false) => {
    if (!conversationId) {
      setMessages([]);
      return;
    }

    if (!silent) setMessagesLoading(true);
    try {
      const response = await getPatientSupportMessages(conversationId);
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
      try {
        const nextSelectedId = await loadConversations();
        if (mounted && nextSelectedId) {
          await loadMessages(nextSelectedId);
        }
      } catch (err) {
        if (mounted) {
          setError(getErrorMessage(err));
        }
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

  const createConversation = async (event) => {
    event.preventDefault();
    setSubmitting(true);
    setError("");
    setNotice("");

    try {
      const response = await createPatientSupportConversation(newConversation);
      const created = response.data;
      setNewConversation({ subject: "Tư vấn trực tuyến", initialMessage: "" });
      await loadConversations(created?.id);
      await loadMessages(created?.id);
      setNotice("Đã gửi yêu cầu hỗ trợ tới lễ tân.");
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
      await sendPatientSupportMessage(selectedId, { content: messageText.trim() });
      setMessageText("");
      await loadMessages(selectedId);
      await loadConversations(selectedId, selectedId);
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
      await closePatientSupportConversation(selectedId);
      await loadConversations(selectedId, selectedId);
      setNotice("Cuộc trò chuyện đã được đóng.");
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return <LoadingState message="Đang tải tư vấn trực tuyến..." />;
  }

  return (
    <div className="patient-page support-chat-page">
      <div className="patient-page-header-row">
        <div>
          <p className="patient-eyebrow">Tư vấn trực tuyến</p>
          <h2>Nhắn tin với lễ tân và nhân viên hỗ trợ</h2>
        </div>
      </div>

      {error && <Alert variant="danger">{error}</Alert>}
      {notice && <Alert variant="success">{notice}</Alert>}

      <div className="support-chat-layout">
        <Card className="support-chat-list-card">
          <Card.Body>
            <h5>Tạo yêu cầu hỗ trợ</h5>
            <Form onSubmit={createConversation} className="support-new-chat-form">
              <Form.Group className="mb-2">
                <Form.Label>Chủ đề</Form.Label>
                <Form.Control
                  value={newConversation.subject}
                  onChange={(event) => setNewConversation((current) => ({ ...current, subject: event.target.value }))}
                  placeholder="Ví dụ: Cần tư vấn đặt lịch"
                />
              </Form.Group>
              <Form.Group className="mb-3">
                <Form.Label>Nội dung</Form.Label>
                <Form.Control
                  as="textarea"
                  rows={3}
                  value={newConversation.initialMessage}
                  onChange={(event) => setNewConversation((current) => ({ ...current, initialMessage: event.target.value }))}
                  placeholder="Nhập vấn đề bạn cần lễ tân hỗ trợ..."
                />
              </Form.Group>
              <Button type="submit" className="patient-primary-soft w-100" disabled={submitting || !newConversation.initialMessage.trim()}>
                <BsPlusCircle /> Gửi yêu cầu
              </Button>
            </Form>

            <hr />

            <h5>Cuộc trò chuyện</h5>
            {conversations.length === 0 ? (
              <EmptyState title="Chưa có cuộc trò chuyện" description="Tạo yêu cầu hỗ trợ để bắt đầu nhắn tin với lễ tân." />
            ) : (
              <ListGroup className="support-conversation-list">
                {conversations.map((conversation) => {
                  const meta = statusMeta[conversation.status] || { label: conversation.status, variant: "secondary" };
                  return (
                    <ListGroup.Item
                      key={conversation.id}
                      action
                      active={String(conversation.id) === String(selectedId)}
                      onClick={() => selectConversation(conversation.id)}
                    >
                      <div className="support-conversation-title">
                        <strong>{conversation.subject || "Tư vấn trực tuyến"}</strong>
                        {conversation.unreadCount > 0 && <Badge bg="danger">{conversation.unreadCount}</Badge>}
                      </div>
                      <span>{conversation.latestMessage || "Chưa có tin nhắn"}</span>
                      <Badge bg={meta.variant}>{meta.label}</Badge>
                    </ListGroup.Item>
                  );
                })}
              </ListGroup>
            )}
          </Card.Body>
        </Card>

        <Card className="support-chat-window-card">
          <Card.Body>
            {selectedConversation ? (
              <>
                <div className="support-chat-header">
                  <div>
                    <h5>{selectedConversation.subject || "Tư vấn trực tuyến"}</h5>
                    <span>Nhân viên phụ trách: {selectedConversation.staffName || "Đang chờ tiếp nhận"}</span>
                  </div>
                  <div className="support-chat-actions">
                    <Badge bg={(statusMeta[selectedConversation.status] || statusMeta.OPEN).variant}>
                      {(statusMeta[selectedConversation.status] || statusMeta.OPEN).label}
                    </Badge>
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
                    <EmptyState title="Chưa có tin nhắn" description="Tin nhắn sẽ hiển thị tại đây." />
                  ) : (
                    messages.map((message) => (
                      <div
                        key={message.id}
                        className={`support-message ${message.senderRole === "PATIENT" ? "mine" : "theirs"} ${message.messageType === "MEET_SCHEDULE" ? "schedule" : ""}`}
                      >
                        <div className="support-message-bubble">
                          <strong>{message.senderRole === "PATIENT" ? "Bạn" : message.senderName || "Lễ tân"}</strong>
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
                    placeholder={selectedConversation.status === "CLOSED" ? "Cuộc trò chuyện đã đóng" : "Nhập tin nhắn..."}
                    disabled={selectedConversation.status === "CLOSED" || submitting}
                  />
                  <Button type="submit" disabled={selectedConversation.status === "CLOSED" || submitting || !messageText.trim()}>
                    <BsSend />
                  </Button>
                </Form>
              </>
            ) : (
              <EmptyState icon={BsChatDots} title="Chọn cuộc trò chuyện" description="Chọn một yêu cầu hỗ trợ hoặc tạo yêu cầu mới." />
            )}
          </Card.Body>
        </Card>
      </div>
    </div>
  );
}

export default PatientSupportChat;
