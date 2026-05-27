function EmptyState({ title = "Chưa có dữ liệu", description = "Không tìm thấy nội dung phù hợp." }) {
  return (
    <div className="state-box state-box-muted">
      <strong>{title}</strong>
      <span>{description}</span>
    </div>
  );
}

export default EmptyState;
