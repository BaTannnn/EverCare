import { Spinner } from "react-bootstrap";

function LoadingState({ message = "Đang tải dữ liệu..." }) {
  return (
    <div className="state-box">
      <Spinner animation="border" size="sm" />
      <span>{message}</span>
    </div>
  );
}

export default LoadingState;
