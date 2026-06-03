import { useContext } from "react";
import PatientShellContext from "./patientShellContextValue";

export const usePatientShell = () => {
  const context = useContext(PatientShellContext);

  if (!context) {
    throw new Error("usePatientShell must be used inside PatientShellProvider");
  }

  return context;
};
