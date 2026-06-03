import { useCallback, useMemo, useReducer } from "react";
import cookies from "react-cookies";
import { authActionTypes, authReducer, createAuthState } from "../reducers/authReducer";
import { normalizeRoles } from "../routes/authRouteUtils";
import AuthContext from "./authContextValue";
const COOKIE_PATH = "/";
const REMEMBER_MAX_AGE = 7 * 24 * 60 * 60;

const readJsonCookie = (name) => {
  const value = cookies.load(name);

  if (!value || typeof value === "object") {
    return value || null;
  }

  try {
    return JSON.parse(value);
  } catch {
    return null;
  }
};

const authCookieOptions = (remember = true) => {
  const options = { path: COOKIE_PATH };

  if (remember) {
    options.maxAge = REMEMBER_MAX_AGE;
  }

  return options;
};

const loadInitialAuthState = () => createAuthState({
  token: cookies.load("token") || null,
  user: readJsonCookie("user"),
  roles: readJsonCookie("role") || [],
});

function AuthProvider({ children }) {
  const [state, dispatch] = useReducer(authReducer, undefined, loadInitialAuthState);

  const startSession = useCallback((token, { remember = true } = {}) => {
    cookies.save("token", token, authCookieOptions(remember));
    dispatch({ type: authActionTypes.SET_TOKEN, payload: { token } });
  }, []);

  const setSessionUser = useCallback((user, { remember = true } = {}) => {
    const roles = normalizeRoles(user?.roles);

    if (user) {
      cookies.save("user", JSON.stringify(user), authCookieOptions(remember));
    }

    if (roles.length) {
      cookies.save("role", JSON.stringify(roles), authCookieOptions(remember));
    }

    dispatch({ type: authActionTypes.SET_USER, payload: { user, roles } });
  }, []);

  const logout = useCallback(() => {
    cookies.remove("token", { path: COOKIE_PATH });
    cookies.remove("user", { path: COOKIE_PATH });
    cookies.remove("role", { path: COOKIE_PATH });
    dispatch({ type: authActionTypes.LOGOUT });
  }, []);

  const value = useMemo(() => ({
    ...state,
    startSession,
    setSessionUser,
    logout,
  }), [logout, setSessionUser, startSession, state]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export default AuthProvider;
