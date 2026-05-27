import { Navigate, Outlet } from "react-router-dom";
import cookies from "react-cookies";

function PublicOnlyRoute({ children }) {
  const token = cookies.load("token");
  const savedRole = cookies.load("role");
  const roles = Array.isArray(savedRole) ? savedRole : [savedRole].filter(Boolean);
  const isDoctor = roles.includes("DOCTOR") || roles.includes("ROLE_DOCTOR");

  if (token) {
    return <Navigate to={isDoctor ? "/doctor/dashboard" : "/admin/dashboard"} replace />;
  }

  return children || <Outlet />;
}

export default PublicOnlyRoute;
