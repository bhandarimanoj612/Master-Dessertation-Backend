package com.example.sajilo_tayaar.security.helper;

public record AuthUser( Long userId,
                        String role,
                        Long tenantId) {

}
