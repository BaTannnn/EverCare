import { Navigate } from "react-router-dom";
import { useAuth } from "../contexts/useAuth";
import { getDashboardPath } from "./authRouteUtils";

function HomeRedirect() {
  const { isAuthenticated, roles } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <Navigate to={getDashboardPath(roles) || "/login"} replace />;
}

export default HomeRedirect;
