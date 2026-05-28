import { Alert, Button } from "react-bootstrap";

function ErrorState({ message = "Đã có lỗi xảy ra.", onRetry }) {
  return (
    <Alert variant="danger" className="doctor-alert">
      <div>
        <strong>Không thể tải dữ liệu</strong>
        <p>{message}</p>
      </div>
      {onRetry && (
        <Button type="button" variant="outline-danger" size="sm" onClick={onRetry}>
          Thử lại
        </Button>
      )}
    </Alert>
  );
}

export default ErrorState;
