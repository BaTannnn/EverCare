import { Navigate, Outlet } from "react-router-dom";
import cookies from "react-cookies";

const getSavedRole = () => cookies.load("role");

function ProtectedRoute({ children, roles }) {
  const token = cookies.load("token");

  if (!token) {
    return <Navigate to="/login" replace />;
  }

  if (roles?.length) {
    const savedRole = getSavedRole();
    const userRoles = Array.isArray(savedRole) ? savedRole : [savedRole].filter(Boolean);

    if (!userRoles.some((role) => roles.includes(role))) {
      return <Navigate to="/login" replace />;
    }
  }

  return children || <Outlet />;
}

export default ProtectedRoute;
