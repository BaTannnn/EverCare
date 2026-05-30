import { useParams } from "react-router-dom";
import ReceptionistAppointmentFormPage from "./ReceptionistAppointmentFormPage";

function ReceptionistAppointmentEditPage() {
  const { appointmentId } = useParams();

  return <ReceptionistAppointmentFormPage mode="edit" appointmentId={appointmentId} />;
}

export default ReceptionistAppointmentEditPage;
