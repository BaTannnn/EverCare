import { useCallback, useEffect, useMemo, useReducer } from "react";
import { getPatientNotifications } from "../services/patient/patientNotificationApi";
import { getPatientProfile } from "../services/patient/patientProfileApi";
import {
  patientShellActionTypes,
  patientShellInitialState,
  patientShellReducer,
} from "../reducers/patientShellReducer";
import PatientShellContext from "./patientShellContextValue";

function PatientShellProvider({ children }) {
  const [state, dispatch] = useReducer(patientShellReducer, patientShellInitialState);

  useEffect(() => {
    let mounted = true;

    const loadShellData = async () => {
      try {
        const [profileResult, notificationResult] = await Promise.allSettled([getPatientProfile(), getPatientNotifications()]);

        if (!mounted) {
          return;
        }

        dispatch({
          type: patientShellActionTypes.LOAD_SUCCESS,
          payload: {
            profile: profileResult.status === "fulfilled" ? profileResult.value.data || null : null,
            notifications: notificationResult.status === "fulfilled" ? notificationResult.value.data || [] : [],
          },
        });
      } catch (error) {
        console.error("Failed to load patient shell data", error);
        if (mounted) {
          dispatch({
            type: patientShellActionTypes.LOAD_SUCCESS,
            payload: { profile: null, notifications: [] },
          });
        }
      }
    };

    loadShellData();

    return () => {
      mounted = false;
    };
  }, []);

  const setProfile = useCallback((valueOrUpdater) => {
    dispatch({
      type: patientShellActionTypes.SET_PROFILE,
      payload: typeof valueOrUpdater === "function" ? valueOrUpdater(state.profile) : valueOrUpdater,
    });
  }, [state.profile]);

  const setNotifications = useCallback((valueOrUpdater) => {
    dispatch({
      type: patientShellActionTypes.SET_NOTIFICATIONS,
      payload: typeof valueOrUpdater === "function" ? valueOrUpdater(state.notifications) : valueOrUpdater,
    });
  }, [state.notifications]);

  const unreadNotifications = useMemo(
    () => state.notifications.filter((notification) => !notification.read).length,
    [state.notifications],
  );

  const value = useMemo(() => ({
    ...state,
    setProfile,
    setNotifications,
    unreadNotifications,
  }), [setNotifications, setProfile, state, unreadNotifications]);

  return <PatientShellContext.Provider value={value}>{children}</PatientShellContext.Provider>;
}

export default PatientShellProvider;
