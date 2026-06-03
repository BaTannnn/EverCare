import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../contexts/useAuth";

function PublicOnlyRoute({ children }) {
  const { dashboardPath, isAuthenticated } = useAuth();

  if (isAuthenticated && dashboardPath) {
    return <Navigate to={dashboardPath} replace />;
  }

  return children || <Outlet />;
}

export default PublicOnlyRoute;
