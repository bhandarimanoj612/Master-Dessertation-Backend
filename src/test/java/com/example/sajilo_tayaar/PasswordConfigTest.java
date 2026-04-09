package com.example.sajilo_tayaar;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class PasswordConfigTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void passwordEncoder_shouldHashPasswordNotStorePlainText() {
        String raw = "admin123";
        String hash = passwordEncoder.encode(raw);

        assertNotEquals(raw, hash);
    }

    @Test
    void passwordEncoder_shouldMatchCorrectPassword() {
        String raw = "admin123";
        String hash = passwordEncoder.encode(raw);

        assertTrue(passwordEncoder.matches(raw, hash));
    }

    @Test
    void passwordEncoder_shouldRejectWrongPassword() {
        String hash = passwordEncoder.encode("admin123");

        assertFalse(passwordEncoder.matches("wrong123", hash));
    }
}
