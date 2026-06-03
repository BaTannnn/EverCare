const ROLE_PREFIX = "ROLE_";

export const normalizeRoles = (value) => {
  if (!value) {
    return [];
  }

  if (Array.isArray(value)) {
    return value.flatMap(normalizeRoles);
  }

  if (typeof value === "object") {
    return normalizeRoles(value.roles || value.role || value.authorities);
  }

  const text = String(value).trim();

  if (!text) {
    return [];
  }

  try {
    return normalizeRoles(JSON.parse(text));
  } catch {
    return text
      .split(",")
      .map((role) => role.trim().replace(/^"|"$/g, "").toUpperCase())
      .filter(Boolean);
  }
};

const hasAnyRole = (roles, candidates) => roles.some((role) => {
  const normalizedRole = role.startsWith(ROLE_PREFIX) ? role.slice(ROLE_PREFIX.length) : role;

  return candidates.some((candidate) => (
    role === candidate
    || role === `${ROLE_PREFIX}${candidate}`
    || normalizedRole === candidate
  ));
});

export const getDashboardPath = (roles) => {
  const normalizedRoles = normalizeRoles(roles);

  if (hasAnyRole(normalizedRoles, ["DOCTOR"])) {
    return "/doctor/dashboard";
  }

  if (hasAnyRole(normalizedRoles, ["PHARMACIST"])) {
    return "/pharmacist/dashboard";
  }

  if (hasAnyRole(normalizedRoles, ["ADMIN"])) {
    return "/admin/dashboard";
  }

  if (hasAnyRole(normalizedRoles, ["RECEPTIONIST", "CASHIER"])) {
    return "/receptionist/dashboard";
  }

  if (hasAnyRole(normalizedRoles, ["MANAGER", "LAB_TECH"])) {
    return "/staff/dashboard";
  }

  if (hasAnyRole(normalizedRoles, ["PATIENT"])) {
    return "/patient/dashboard";
  }

  return null;
};

export const isAllowedRole = (savedRoles, allowedRoles) => {
  const normalizedRoles = normalizeRoles(savedRoles);
  const normalizedAllowedRoles = normalizeRoles(allowedRoles);

  if (!normalizedAllowedRoles.length) {
    return true;
  }

  return normalizedRoles.some((role) => normalizedAllowedRoles.includes(role));
};
