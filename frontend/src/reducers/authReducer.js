import { getDashboardPath, normalizeRoles } from "../routes/authRouteUtils";

export const createAuthState = ({ token = null, user = null, roles = [] } = {}) => {
  const normalizedRoles = normalizeRoles(roles.length ? roles : user?.roles);

  return {
    token,
    user,
    roles: normalizedRoles,
    isAuthenticated: Boolean(token),
    dashboardPath: getDashboardPath(normalizedRoles),
  };
};

export const authInitialState = createAuthState();

export const authActionTypes = {
  SET_TOKEN: "SET_TOKEN",
  SET_USER: "SET_USER",
  LOGOUT: "LOGOUT",
};

export function authReducer(state, action) {
  switch (action.type) {
    case authActionTypes.SET_TOKEN:
      return createAuthState({
        token: action.payload.token,
        user: state.user,
        roles: state.roles,
      });
    case authActionTypes.SET_USER:
      return createAuthState({
        token: state.token,
        user: action.payload.user,
        roles: action.payload.roles,
      });
    case authActionTypes.LOGOUT:
      return authInitialState;
    default:
      return state;
  }
}
