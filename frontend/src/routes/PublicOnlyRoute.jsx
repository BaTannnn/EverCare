import { Navigate, Outlet } from "react-router-dom";
import cookies from "react-cookies";

function PublicOnlyRoute({ children }) {
  const token = cookies.load("token");

  if (token) {
    return <Navigate to="/admin/dashboard" replace />;
  }

  return children || <Outlet />;
}

export default PublicOnlyRoute;
