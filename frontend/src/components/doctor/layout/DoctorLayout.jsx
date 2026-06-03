import { Outlet } from "react-router-dom";
import DoctorSidebar from "./DoctorSidebar";
import DoctorTopbar from "./DoctorTopbar";

function DoctorLayout() {
  return (
    <div className="doctor-shell">
      <DoctorSidebar />
      <div className="doctor-main">
        <DoctorTopbar />
        <main className="doctor-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

export default DoctorLayout;
