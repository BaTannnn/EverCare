import { useCallback, useEffect, useMemo, useReducer, useRef } from "react";
import { Alert } from "react-bootstrap";
import LoadingState from "../../components/common/LoadingState";
import SupportChatWindow from "../../components/receptionist/support/SupportChatWindow";
import SupportConversationListPanel from "../../components/receptionist/support/SupportConversationListPanel";
import { getReceptionistDoctors } from "../../services/receptionist/receptionistReferenceApi";
import {
  acceptReceptionistSupportConversation,
  closeReceptionistSupportConversation,
  createReceptionistSupportSchedule,
  getReceptionistSupportConversations,
  getReceptionistSupportMessages,
  sendReceptionistSupportMessage,
} from "../../services/receptionist/receptionistSupportApi";
import {
  receptionistSupportActionTypes,
  receptionistSupportInitialState,
  receptionistSupportReducer,
} from "../../reducers/receptionistSupportReducer";
import { getErrorMessage } from "./receptionistPageUtils";
import {
  CONVERSATION_POLL_HIDDEN_MS,
  CONVERSATION_POLL_VISIBLE_MS,
  CONVERSATIONS_PAGE_SIZE,
  MESSAGE_INITIAL_LIMIT,
  MESSAGE_POLL_HIDDEN_MS,
  MESSAGE_POLL_LIMIT,
  MESSAGE_POLL_VISIBLE_MS,
  defaultSupportSchedule,
  getLatestMessageId,
  getPollDelay,
  mergeMessagesById,
  pickConversationId,
} from "./receptionistSupportUtils";

function ReceptionistSupportMessagesPage() {
  const [state, dispatch] = useReducer(receptionistSupportReducer, receptionistSupportInitialState);
  const {
    conversationPage,
    conversations,
    doctors,
    error,
    filters,
    loading,
    messages,
    messagesLoading,
    messageText,
    notice,
    schedule,
    selectedId,
    submitting,
  } = state;
  const latestMessageIdRef = useRef(0);
  const messagePollInFlightRef = useRef(false);
  const conversationPollInFlightRef = useRef(false);

  const setSupportField = useCallback((field, value) => {
    dispatch({ type: receptionistSupportActionTypes.SET_FIELD, field, value });
  }, []);

  const patchSupportState = useCallback((payload) => {
    dispatch({ type: receptionistSupportActionTypes.PATCH, payload });
  }, []);

  const setConversations = useCallback((value) => setSupportField("conversations", value), [setSupportField]);
  const setSelectedId = useCallback((value) => setSupportField("selectedId", value), [setSupportField]);
  const setMessages = useCallback((value) => setSupportField("messages", value), [setSupportField]);
  const setDoctors = useCallback((value) => setSupportField("doctors", value), [setSupportField]);
  const setFilters = useCallback((value) => setSupportField("filters", value), [setSupportField]);
  const setLoading = useCallback((value) => setSupportField("loading", value), [setSupportField]);
  const setMessagesLoading = useCallback((value) => setSupportField("messagesLoading", value), [setSupportField]);
  const setSubmitting = useCallback((value) => setSupportField("submitting", value), [setSupportField]);
  const setError = useCallback((value) => setSupportField("error", value), [setSupportField]);
  const setNotice = useCallback((value) => setSupportField("notice", value), [setSupportField]);
  const setMessageText = useCallback((value) => setSupportField("messageText", value), [setSupportField]);
  const setSchedule = useCallback((value) => setSupportField("schedule", value), [setSupportField]);
  const setConversationPage = useCallback((value) => setSupportField("conversationPage", value), [setSupportField]);

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
  }, [filters.keyword, filters.status, setConversations, setSelectedId]);

  const loadMessages = useCallback(async (conversationId, options = {}) => {
    const { silent = false, afterId = null, append = false } = options;
    if (!conversationId) {
      latestMessageIdRef.current = 0;
      setMessages([]);
      return;
    }

    if (!silent) setMessagesLoading(true);
    try {
      const params = {
        limit: afterId ? MESSAGE_POLL_LIMIT : MESSAGE_INITIAL_LIMIT,
      };
      if (afterId) params.afterId = afterId;

      const response = await getReceptionistSupportMessages(conversationId, params);
      const incoming = response.data || [];
      setMessages((current) => (append ? mergeMessagesById(current, incoming) : incoming));
    } catch (err) {
      if (!silent) {
        setError(getErrorMessage(err));
        setMessages([]);
      }
    } finally {
      if (!silent) setMessagesLoading(false);
    }
  }, [setError, setMessages, setMessagesLoading]);

  useEffect(() => {
    latestMessageIdRef.current = getLatestMessageId(messages);
  }, [messages]);

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
  }, [loadConversations, loadMessages, setDoctors, setError, setLoading]);

  useEffect(() => {
    if (!selectedId) return undefined;
    let timerId;
    let cancelled = false;

    function scheduleNext() {
      window.clearTimeout(timerId);
      timerId = window.setTimeout(poll, getPollDelay(MESSAGE_POLL_VISIBLE_MS, MESSAGE_POLL_HIDDEN_MS));
    }

    async function poll() {
      if (!messagePollInFlightRef.current) {
        messagePollInFlightRef.current = true;
        const afterId = latestMessageIdRef.current || null;
        try {
          await loadMessages(selectedId, {
            silent: true,
            afterId,
            append: Boolean(afterId),
          });
        } finally {
          messagePollInFlightRef.current = false;
        }
      }

      if (!cancelled) {
        scheduleNext();
      }
    }

    const handleVisibilityChange = () => {
      if (!messagePollInFlightRef.current) {
        scheduleNext();
      }
    };

    scheduleNext();
    document.addEventListener("visibilitychange", handleVisibilityChange);
    return () => {
      cancelled = true;
      window.clearTimeout(timerId);
      document.removeEventListener("visibilitychange", handleVisibilityChange);
    };
  }, [loadMessages, selectedId]);

  useEffect(() => {
    let timerId;
    let cancelled = false;

    function scheduleNext() {
      window.clearTimeout(timerId);
      timerId = window.setTimeout(poll, getPollDelay(CONVERSATION_POLL_VISIBLE_MS, CONVERSATION_POLL_HIDDEN_MS));
    }

    async function poll() {
      if (!conversationPollInFlightRef.current) {
        conversationPollInFlightRef.current = true;
        try {
          const nextSelectedId = await loadConversations(selectedId, selectedId);
          if (!selectedId && nextSelectedId) {
            await loadMessages(nextSelectedId);
          }
        } finally {
          conversationPollInFlightRef.current = false;
        }
      }

      if (!cancelled) {
        scheduleNext();
      }
    }

    const handleVisibilityChange = () => {
      if (!conversationPollInFlightRef.current) {
        scheduleNext();
      }
    };

    scheduleNext();
    document.addEventListener("visibilitychange", handleVisibilityChange);
    return () => {
      cancelled = true;
      window.clearTimeout(timerId);
      document.removeEventListener("visibilitychange", handleVisibilityChange);
    };
  }, [loadConversations, loadMessages, selectedId]);

  const selectConversation = (conversationId) => {
    setSelectedId(conversationId);
    latestMessageIdRef.current = 0;
    loadMessages(conversationId);
  };

  const updateFilters = (patch) => {
    setFilters((current) => ({ ...current, ...patch }));
  };

  const updateSchedule = (patch) => {
    setSchedule((current) => ({ ...current, ...patch }));
  };

  const submitFilters = async (event) => {
    event.preventDefault();
    patchSupportState({ loading: true, error: "" });
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
      setSchedule(defaultSupportSchedule());
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
        <SupportConversationListPanel
          conversationPage={conversationPage}
          conversations={conversations}
          filters={filters}
          paginatedConversations={paginatedConversations}
          selectedId={selectedId}
          totalConversationPages={totalConversationPages}
          onFilterChange={updateFilters}
          onPageChange={setConversationPage}
          onSelectConversation={selectConversation}
          onSubmitFilters={submitFilters}
        />

        <SupportChatWindow
          doctors={doctors}
          messageText={messageText}
          messages={messages}
          messagesLoading={messagesLoading}
          schedule={schedule}
          selectedConversation={selectedConversation}
          submitting={submitting}
          onAcceptConversation={acceptConversation}
          onCloseConversation={closeConversation}
          onCreateSchedule={createSchedule}
          onMessageTextChange={setMessageText}
          onScheduleChange={updateSchedule}
          onSendMessage={sendMessage}
        />
      </div>
    </div>
  );
}

export default ReceptionistSupportMessagesPage;
