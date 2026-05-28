import { Navigate, Outlet } from "react-router-dom";
import cookies from "react-cookies";
import { getDashboardPath } from "./authRouteUtils";

function PublicOnlyRoute({ children }) {
  const token = cookies.load("token");
  const savedRole = cookies.load("role");
  const dashboardPath = getDashboardPath(savedRole);

  if (token && dashboardPath) {
    return <Navigate to={dashboardPath} replace />;
  }

  return children || <Outlet />;
}

export default PublicOnlyRoute;
