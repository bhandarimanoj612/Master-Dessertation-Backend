package com.example.sajilo_tayaar.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    // IMPORTANT: this must be BASE64 encoded and long enough
    // Example below is a valid base64 string (you can change it later)
    private static final String SECRET_BASE64 =
            "bWFrZS10aGlzLWEtc3VwZXItbG9uZy1zZWNyZXQta2V5LWZvci1qand0LWhzMjU2ISE=";

    private static final long EXPIRATION_MS = 1000L * 60 * 60 * 24; // 24 hours

    private Key signingKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_BASE64);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Long userId, String role, Long tenantId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("role", role)
                .claim("tenantId", tenantId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(signingKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims extractAll(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
