package com.example.sajilo_tayaar.controller;

import com.example.sajilo_tayaar.dto.request.CreateShopUserRequest;
import com.example.sajilo_tayaar.dto.response.ShopUserSummaryResponse;
import com.example.sajilo_tayaar.entity.User;
import com.example.sajilo_tayaar.entity.enums.Role;
import com.example.sajilo_tayaar.repository.ShopRepository;
import com.example.sajilo_tayaar.repository.UserRepository;
import com.example.sajilo_tayaar.security.helper.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/shop-users")
@RequiredArgsConstructor
public class ShopUserController {

    // Shop owners or staff use these routes to manage internal team accounts.

    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/shop/{shopId}")
    public List<ShopUserSummaryResponse> listForShop(@PathVariable Long shopId) {
        validateShopAccess(shopId);
        return userRepository.findAll().stream()
                .filter(user -> user.getShop() != null && shopId.equals(user.getShop().getId()))
                .map(this::toResponse)
                .toList();
    }

    @PostMapping
    public ShopUserSummaryResponse create(@Valid @RequestBody CreateShopUserRequest req) {
        validateShopAccess(req.shopId());
        if (req.role() != Role.SHOP_STAFF && req.role() != Role.TECHNICIAN) {
            throw new IllegalArgumentException("Only SHOP_STAFF or TECHNICIAN can be created here");
        }
        if (userRepository.findByEmail(req.email()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        var shop = shopRepository.findById(req.shopId())
                .orElseThrow(() -> new IllegalArgumentException("Shop not found"));

        User user = new User();
        user.setShop(shop);
        user.setFullName(req.fullName());
        user.setEmail(req.email());
        user.setPhone(req.phone());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setRole(req.role());
        user.setIsActive(true);

        return toResponse(userRepository.save(user));
    }

    @PatchMapping("/{userId}/toggle-active")
    public ShopUserSummaryResponse toggleActive(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.getShop() == null) throw new IllegalArgumentException("User is not assigned to a shop");
        validateShopAccess(user.getShop().getId());
        user.setIsActive(!Boolean.TRUE.equals(user.getIsActive()));
        return toResponse(userRepository.save(user));
    }


    private void validateShopAccess(Long shopId) {
        String role = SecurityUtil.currentRole();
        if (role == null) throw new IllegalArgumentException("Authentication required");
        if ("PLATFORM_ADMIN".equals(role)) return;
        Long tenantId = SecurityUtil.currentTenantId();
        if (!("SHOP_OWNER".equals(role) || "SHOP_STAFF".equals(role)) || tenantId == null || !tenantId.equals(shopId)) {
            throw new IllegalArgumentException("You can only manage users for your own shop");
        }
    }

    private ShopUserSummaryResponse toResponse(User user) {
        return new ShopUserSummaryResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.getIsActive(),
                user.getShop() != null ? user.getShop().getId() : null
        );
    }
}
