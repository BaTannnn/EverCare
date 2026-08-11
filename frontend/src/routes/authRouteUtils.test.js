import test from "node:test";
import assert from "node:assert/strict";
import { getDashboardPath, isAllowedRole, normalizeRoles } from "./authRouteUtils.js";

test("normalizes role values from API formats", () => {
  assert.deepEqual(normalizeRoles('["ROLE_PATIENT","DOCTOR"]'), ["ROLE_PATIENT", "DOCTOR"]);
  assert.deepEqual(normalizeRoles({ roles: ["ROLE_ADMIN"] }), ["ROLE_ADMIN"]);
});

test("routes each role to its own dashboard", () => {
  assert.equal(getDashboardPath(["ROLE_PATIENT"]), "/patient/dashboard");
  assert.equal(getDashboardPath(["ROLE_DOCTOR"]), "/doctor/dashboard");
  assert.equal(getDashboardPath(["ROLE_ADMIN"]), "/admin/dashboard");
});

test("checks allowed role without dropping ROLE prefix semantics", () => {
  assert.equal(isAllowedRole(["ROLE_ADMIN"], ["ROLE_ADMIN"]), true);
  assert.equal(isAllowedRole(["ROLE_PATIENT"], ["ROLE_ADMIN"]), false);
});
