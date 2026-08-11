package com.evercare.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {
    private static final String SECRET = "0123456789abcdef0123456789abcdef";

    @Test
    void generatesAndValidatesToken() throws Exception {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("jwt.secret", SECRET)
                .withProperty("jwt.expiration-ms", "60000");
        JwtService service = serviceWith(environment);

        String token = service.generateToken("doctor01");

        assertEquals("doctor01", service.validateTokenAndGetUsername(token));
    }

    @Test
    void rejectsTokenSignedWithDifferentSecret() throws Exception {
        JwtService issuer = serviceWith(new MockEnvironment().withProperty("jwt.secret", SECRET));
        JwtService verifier = serviceWith(new MockEnvironment().withProperty(
                "jwt.secret",
                "abcdef0123456789abcdef0123456789"
        ));

        assertNull(verifier.validateTokenAndGetUsername(issuer.generateToken("patient01")));
    }

    @Test
    void requiresConfiguredStrongSecret() {
        JwtService missing = serviceWith(new MockEnvironment());
        JwtService shortSecret = serviceWith(new MockEnvironment().withProperty("jwt.secret", "too-short"));

        assertThrows(IllegalStateException.class, () -> missing.generateToken("user"));
        assertThrows(IllegalStateException.class, () -> shortSecret.generateToken("user"));
    }
    private JwtService serviceWith(MockEnvironment environment) {
        JwtService service = new JwtService();
        ReflectionTestUtils.setField(service, "environment", environment);
        return service;
    }

}
