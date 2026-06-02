export const patientShellInitialState = {
  profile: null,
  notifications: [],
  loading: true,
};

export const patientShellActionTypes = {
  LOAD_SUCCESS: "LOAD_SUCCESS",
  SET_PROFILE: "SET_PROFILE",
  SET_NOTIFICATIONS: "SET_NOTIFICATIONS",
};

export function patientShellReducer(state, action) {
  switch (action.type) {
    case patientShellActionTypes.LOAD_SUCCESS:
      return {
        ...state,
        loading: false,
        profile: action.payload.profile,
        notifications: action.payload.notifications,
      };
    case patientShellActionTypes.SET_PROFILE:
      return {
        ...state,
        profile: action.payload,
      };
    case patientShellActionTypes.SET_NOTIFICATIONS:
      return {
        ...state,
        notifications: action.payload,
      };
    default:
      return state;
  }
}
