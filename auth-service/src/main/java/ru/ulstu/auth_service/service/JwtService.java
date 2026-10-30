package ru.ulstu.auth_service.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration-ms:3600000}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generate(String username, List<String> roles) {
        return generate(username, roles, Map.of()).token();
    }

    public TokenResult generate(String username,
                                List<String> roles,
                                Map<String, Object> extraClaims) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(expirationMs);

        var builder = Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt));

        extraClaims.forEach(builder::claim);

        String token = builder.signWith(key).compact();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", username);
        payload.put("roles", roles);
        payload.putAll(extraClaims);
        payload.put("iat", now.getEpochSecond());
        payload.put("exp", expiresAt.getEpochSecond());

        return new TokenResult(token, payload);
    }

    public record TokenResult(String token, Map<String, Object> payload) {}
}