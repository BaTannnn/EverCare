import { Outlet } from "react-router-dom";
import ReceptionistSidebar from "./ReceptionistSidebar";
import ReceptionistTopbar from "./ReceptionistTopbar";

function ReceptionistLayout() {
  return (
    <div className="doctor-shell receptionist-shell">
      <ReceptionistSidebar />
      <div className="doctor-main">
        <ReceptionistTopbar />
        <main className="doctor-content receptionist-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

export default ReceptionistLayout;
