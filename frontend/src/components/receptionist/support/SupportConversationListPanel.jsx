import { Badge, Button, Card, Form, ListGroup } from "react-bootstrap";
import { BsSearch } from "react-icons/bs";
import EmptyState from "../../common/EmptyState";
import { supportStatusMeta } from "../../../pages/receptionist/receptionistSupportUtils";

function SupportConversationListPanel({
  conversationPage,
  conversations,
  filters,
  paginatedConversations,
  selectedId,
  totalConversationPages,
  onFilterChange,
  onPageChange,
  onSelectConversation,
  onSubmitFilters,
}) {
  return (
    <Card className="support-chat-list-card">
      <Card.Body>
        <Form onSubmit={onSubmitFilters} className="support-filter-form">
          <Form.Group className="mb-2">
            <Form.Label>Tìm kiếm</Form.Label>
            <div className="support-search-control">
              <BsSearch />
              <Form.Control
                value={filters.keyword}
                onChange={(event) => onFilterChange({ keyword: event.target.value })}
                placeholder="Tên, SĐT, chủ đề..."
              />
            </div>
          </Form.Group>
          <Form.Group className="mb-3">
            <Form.Label>Trạng thái</Form.Label>
            <Form.Select
              value={filters.status}
              onChange={(event) => onFilterChange({ status: event.target.value })}
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
                const meta = supportStatusMeta[conversation.status] || { label: conversation.status, variant: "secondary" };
                return (
                  <ListGroup.Item
                    key={conversation.id}
                    action
                    active={String(conversation.id) === String(selectedId)}
                    onClick={() => onSelectConversation(conversation.id)}
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
                  onClick={() => onPageChange(Math.max(1, conversationPage - 1))}
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
                  onClick={() => onPageChange(Math.min(totalConversationPages, conversationPage + 1))}
                >
                  Sau
                </Button>
              </div>
            )}
          </>
        )}
      </Card.Body>
    </Card>
  );
}

export default SupportConversationListPanel;
