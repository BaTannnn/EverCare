import { Outlet } from "react-router-dom";
import PharmacistSidebar from "./PharmacistSidebar";
import PharmacistTopbar from "./PharmacistTopbar";

function PharmacistLayout() {
  return (
    <div className="doctor-shell pharmacist-shell">
      <PharmacistSidebar />
      <div className="doctor-main">
        <PharmacistTopbar />
        <main className="doctor-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

export default PharmacistLayout;
