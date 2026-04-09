package com.example.sajilo_tayaar.controller;

import com.example.sajilo_tayaar.dto.request.UpdateUserAdminRequest;
import com.example.sajilo_tayaar.dto.response.ShopDetailsResponse;
import com.example.sajilo_tayaar.entity.Shop;
import com.example.sajilo_tayaar.entity.User;
import com.example.sajilo_tayaar.entity.enums.Role;
import com.example.sajilo_tayaar.repository.ShopRepository;
import com.example.sajilo_tayaar.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserRepository userRepository;
    private final ShopRepository shopRepository;

    @PatchMapping("/{userId}")
    public User updateUser(@PathVariable Long userId, @Valid @RequestBody UpdateUserAdminRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // If role is not CUSTOMER, tenantId must be provided (shop-scoped roles)
        if (req.role() != Role.CUSTOMER) {
            if (req.tenantId() == null) {
                throw new RuntimeException("tenantId is required for role: " + req.role());
            }
            Shop shop = shopRepository.findById(req.tenantId())
                    .orElseThrow(() -> new RuntimeException("Shop not found: " + req.tenantId()));
            user.setShop(shop);
        } else {
            // Customer can be global
            user.setShop(null);
        }

        user.setRole(req.role());

        if (req.isActive() != null) {
            user.setIsActive(req.isActive());
        }

        return userRepository.save(user);
    }


}
