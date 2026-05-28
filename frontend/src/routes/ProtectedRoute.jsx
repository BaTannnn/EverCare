import { Navigate, Outlet } from "react-router-dom";
import cookies from "react-cookies";
import { isAllowedRole } from "./authRouteUtils";

function ProtectedRoute({ children, roles }) {
  const token = cookies.load("token");

  if (!token) {
    return <Navigate to="/login" replace />;
  }

  if (roles?.length) {
    const savedRole = cookies.load("role");

    if (!isAllowedRole(savedRole, roles)) {
      return <Navigate to="/login" replace />;
    }
  }

  return children || <Outlet />;
}

export default ProtectedRoute;
