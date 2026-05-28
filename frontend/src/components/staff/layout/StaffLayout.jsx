import { Outlet } from "react-router-dom";
import StaffSidebar from "./StaffSidebar";
import StaffTopbar from "./StaffTopbar";

function StaffLayout() {
  return (
    <div className="doctor-shell">
      <StaffSidebar />
      <div className="doctor-main">
        <StaffTopbar />
        <main className="doctor-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

export default StaffLayout;
