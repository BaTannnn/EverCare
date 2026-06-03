import { Navigate, Outlet } from "react-router-dom";
import { isAllowedRole } from "./authRouteUtils";
import { useAuth } from "../contexts/useAuth";

function ProtectedRoute({ children, roles }) {
  const { isAuthenticated, roles: savedRoles } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (roles?.length) {
    if (!isAllowedRole(savedRoles, roles)) {
      return <Navigate to="/login" replace />;
    }
  }

  return children || <Outlet />;
}

export default ProtectedRoute;
