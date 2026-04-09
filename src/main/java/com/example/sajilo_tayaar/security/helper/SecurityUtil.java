package com.example.sajilo_tayaar.security.helper;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    public static AuthUser currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AuthUser)) {
            return null;
        }
        return (AuthUser) auth.getPrincipal();
    }

    public static Long currentUserId() {
        AuthUser u = currentUser();
        return u == null ? null : u.userId();
    }

    public static Long currentTenantId() {
        AuthUser u = currentUser();
        return u == null ? null : u.tenantId();
    }

    public static String currentRole() {
        AuthUser u = currentUser();
        return u == null ? null : u.role();
    }
}
