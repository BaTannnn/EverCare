package com.evercare.utils;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class JwtService {
    private static final long DEFAULT_EXPIRATION_MS = 86_400_000L;
    private static final int MIN_SECRET_BYTES = 32;

    @Autowired
    private Environment environment;

    public String generateToken(String username) throws Exception {
        byte[] secret = requireSecret();
        JWSSigner signer = new MACSigner(secret);
        long expirationMs = environment.getProperty(
                "jwt.expiration-ms",
                Long.class,
                DEFAULT_EXPIRATION_MS
        );

        Date now = new Date();
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(username)
                .issueTime(now)
                .expirationTime(new Date(now.getTime() + expirationMs))
                .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
        signedJWT.sign(signer);
        return signedJWT.serialize();
    }

    public String validateTokenAndGetUsername(String token) throws Exception {
        SignedJWT signedJWT = SignedJWT.parse(token);
        if (!JWSAlgorithm.HS256.equals(signedJWT.getHeader().getAlgorithm())) {
            return null;
        }

        JWSVerifier verifier = new MACVerifier(requireSecret());
        if (!signedJWT.verify(verifier)) {
            return null;
        }

        JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
        Date expiration = claims.getExpirationTime();
        String subject = claims.getSubject();
        if (expiration == null || !expiration.after(new Date()) || subject == null || subject.isBlank()) {
            return null;
        }

        return subject;
    }

    private byte[] requireSecret() {
        String value = environment.getProperty("jwt.secret");
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("JWT_SECRET chưa được cấu hình");
        }

        byte[] secret = value.getBytes(StandardCharsets.UTF_8);
        if (secret.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException("JWT_SECRET phải dài ít nhất 32 bytes");
        }
        return secret;
    }
}
