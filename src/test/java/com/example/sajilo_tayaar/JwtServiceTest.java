package com.example.sajilo_tayaar;

import com.example.sajilo_tayaar.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService();

    @Test
    void generateToken_shouldContainSubjectRoleAndTenantId() {
        String token = jwtService.generateToken(7L, "SHOP_OWNER", 11L);

        Claims claims = jwtService.extractAll(token);

        assertEquals("7", claims.getSubject());
        assertEquals("SHOP_OWNER", claims.get("role", String.class));
        assertEquals(11, claims.get("tenantId", Integer.class));
    }

    @Test
    void extractAll_shouldReturnValidClaims() {
        String token = jwtService.generateToken(99L, "CUSTOMER", 1L);

        Claims claims = jwtService.extractAll(token);

        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
    }

    @Test
    void extractAll_shouldRejectTamperedToken() {
        String token = jwtService.generateToken(99L, "CUSTOMER", 1L);
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThrows(SignatureException.class, () -> jwtService.extractAll(tampered));
    }
}
