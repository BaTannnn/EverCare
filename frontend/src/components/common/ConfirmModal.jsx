import { Button, Modal } from "react-bootstrap";

function ConfirmModal({
  show,
  title = "Xác nhận",
  message,
  confirmText = "Xác nhận",
  cancelText = "Hủy",
  confirmVariant = "primary",
  loading = false,
  onConfirm,
  onHide,
}) {
  return (
    <Modal show={show} onHide={loading ? undefined : onHide} centered>
      <Modal.Header closeButton={!loading}>
        <Modal.Title>{title}</Modal.Title>
      </Modal.Header>
      <Modal.Body>{message}</Modal.Body>
      <Modal.Footer>
        <Button type="button" variant="outline-secondary" onClick={onHide} disabled={loading}>
          {cancelText}
        </Button>
        <Button type="button" variant={confirmVariant} onClick={onConfirm} disabled={loading}>
          {loading ? "Đang xử lý..." : confirmText}
        </Button>
      </Modal.Footer>
    </Modal>
  );
}

export default ConfirmModal;
