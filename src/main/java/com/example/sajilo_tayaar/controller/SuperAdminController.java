package com.example.sajilo_tayaar.controller;

import com.example.sajilo_tayaar.dto.request.ChangePasswordRequest;
import com.example.sajilo_tayaar.dto.request.CreateShopRequest;
import com.example.sajilo_tayaar.dto.request.UpdateShopRequest;
import com.example.sajilo_tayaar.dto.request.UpdateUserRequest;
import com.example.sajilo_tayaar.dto.request.VerificationActionRequest;
import com.example.sajilo_tayaar.dto.response.PendingVerificationResponse;
import com.example.sajilo_tayaar.dto.response.ShopAdminResponse;
import com.example.sajilo_tayaar.dto.response.ShopDetailsResponse;
import com.example.sajilo_tayaar.dto.response.ShopVerificationStatusResponse;
import com.example.sajilo_tayaar.dto.response.UserResponse;
import com.example.sajilo_tayaar.services.ShopVerificationService;
import com.example.sajilo_tayaar.services.SuperAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/superadmin")
@RequiredArgsConstructor
public class SuperAdminController {

    private final SuperAdminService superAdminService;
    private final ShopVerificationService verificationService;

    // ── USER ENDPOINTS ────────────────────────────────────────────────────────────

    @GetMapping("/users")
    public List<UserResponse> getAllUsers() {
        return superAdminService.getAllUsers();
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        superAdminService.deleteUser(id);
        return ResponseEntity.ok("User deleted");
    }

    @PatchMapping("/users/{id}/toggle-active")
    public ResponseEntity<String> toggleUserActive(@PathVariable Long id) {
        boolean newState = superAdminService.toggleUserActive(id);
        return ResponseEntity.ok(newState ? "User activated" : "User deactivated");
    }

    @PatchMapping("/users/{id}/password")
    public ResponseEntity<String> changeUserPassword(
            @PathVariable Long id,
            @RequestBody ChangePasswordRequest request
    ) {
        superAdminService.changeUserPassword(id, request.newPassword());
        return ResponseEntity.ok("Password updated");
    }

    @PatchMapping("/users/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @RequestBody UpdateUserRequest request
    ) {
        return ResponseEntity.ok(superAdminService.updateUser(id, request));
    }

    // ── SHOP ENDPOINTS ────────────────────────────────────────────────────────────

    @GetMapping("/shops")
    public List<ShopAdminResponse> getAllShops() {
        return superAdminService.getAllShops();
    }

    // ✅ FIXED: Added missing GET endpoint for shop details
    @GetMapping("/shops/{id}")
    public ResponseEntity<ShopDetailsResponse> getShopById(@PathVariable Long id) {
        return ResponseEntity.ok(superAdminService.getShopById(id));
    }

    @PostMapping("/shops")
    public ResponseEntity<ShopDetailsResponse> createShop(@RequestBody CreateShopRequest request) {
        return ResponseEntity.ok(superAdminService.createShop(request));
    }

    @PutMapping("/shops/{id}")
    public ResponseEntity<ShopDetailsResponse> updateShop(
            @PathVariable Long id,
            @RequestBody UpdateShopRequest request
    ) {
        return ResponseEntity.ok(superAdminService.updateShop(id, request));
    }

    @DeleteMapping("/shops/{id}")
    public ResponseEntity<String> deleteShop(@PathVariable Long id) {
        superAdminService.deleteShop(id);
        return ResponseEntity.ok("Shop deleted");
    }

    @PatchMapping("/shops/{id}/toggle-active")
    public ResponseEntity<String> toggleShopActive(@PathVariable Long id) {
        boolean newState = superAdminService.toggleShopActive(id);
        return ResponseEntity.ok(newState ? "Shop activated" : "Shop deactivated");
    }

    @PatchMapping("/shops/{id}/toggle-verified")
    public ResponseEntity<String> toggleShopVerified(@PathVariable Long id) {
        boolean newState = superAdminService.toggleShopVerified(id);
        return ResponseEntity.ok(newState ? "Shop verified" : "Shop unverified");
    }

    // ── VERIFICATION REVIEW ENDPOINTS ─────────────────────────────────────────────

    @GetMapping("/verification/pending")
    public ResponseEntity<List<PendingVerificationResponse>> getPendingVerifications() {
        return ResponseEntity.ok(verificationService.getPendingVerifications());
    }

    @PostMapping("/verification/{shopId}/approve")
    public ResponseEntity<ShopVerificationStatusResponse> approveVerification(
            @PathVariable Long shopId,
            @RequestBody(required = false) VerificationActionRequest request
    ) {
        String notes = request != null ? request.notes() : null;
        return ResponseEntity.ok(verificationService.approveVerification(shopId, notes));
    }

    @PostMapping("/verification/{shopId}/reject")
    public ResponseEntity<ShopVerificationStatusResponse> rejectVerification(
            @PathVariable Long shopId,
            @RequestBody(required = false) VerificationActionRequest request
    ) {
        String notes = request != null ? request.notes() : null;
        return ResponseEntity.ok(verificationService.rejectVerification(shopId, notes));
    }
}