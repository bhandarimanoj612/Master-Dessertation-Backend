package com.example.sajilo_tayaar.controller;

import com.example.sajilo_tayaar.dto.request.CreateTechnicianRequest;
import com.example.sajilo_tayaar.dto.response.TechnicianSummaryResponse;
import com.example.sajilo_tayaar.entity.Shop;
import com.example.sajilo_tayaar.entity.Technician;
import com.example.sajilo_tayaar.entity.User;
import com.example.sajilo_tayaar.entity.enums.Role;
import com.example.sajilo_tayaar.repository.ShopRepository;
import com.example.sajilo_tayaar.repository.TechnicianRepository;
import com.example.sajilo_tayaar.repository.UserRepository;
import com.example.sajilo_tayaar.security.helper.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/technicians")
@RequiredArgsConstructor
public class    TechnicianController {

    private final TechnicianRepository technicianRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping
    public TechnicianSummaryResponse create(@Valid @RequestBody CreateTechnicianRequest req) {
        validateShopAccess(req.shopId());
        Shop shop = shopRepository.findById(req.shopId())
                .orElseThrow(() -> new RuntimeException("Shop not found: " + req.shopId()));

        Technician t = new Technician();
        t.setShop(shop);
        t.setFullName(req.fullName());
        t.setPhone(req.phone());
        t.setSpecialization(req.specialization());

        Technician saved = technicianRepository.save(t);

        if (req.email() != null && !req.email().isBlank() && req.password() != null && !req.password().isBlank()
                && userRepository.findByEmail(req.email()).isEmpty()) {
            User user = new User();
            user.setShop(shop);
            user.setFullName(req.fullName());
            user.setEmail(req.email());
            user.setPhone(req.phone());
            user.setPasswordHash(passwordEncoder.encode(req.password()));
            user.setRole(Role.TECHNICIAN);
            user.setIsActive(true);
            userRepository.save(user);
        }

        return toResponse(saved);
    }

    @GetMapping("/shop/{shopId}")
    public List<TechnicianSummaryResponse> listForShop(@PathVariable Long shopId) {
        validateShopAccess(shopId);
        return technicianRepository.findByShop_IdAndIsActiveTrue(shopId).stream().map(this::toResponse).toList();
    }

    @GetMapping("/technicians/get-all")
    public List<TechnicianSummaryResponse> listMyShop() {
        Long tenantId = SecurityUtil.currentTenantId();
        if (tenantId == null) throw new RuntimeException("No tenant assigned");
        return technicianRepository.findByShop_IdAndIsActiveTrue(tenantId).stream().map(this::toResponse).toList();
    }


    private void validateShopAccess(Long shopId) {
        String role = SecurityUtil.currentRole();
        if (role == null) throw new RuntimeException("Authentication required");
        if ("PLATFORM_ADMIN".equals(role)) return;
        Long tenantId = SecurityUtil.currentTenantId();
        if (!("SHOP_OWNER".equals(role) || "SHOP_STAFF".equals(role)) || tenantId == null || !tenantId.equals(shopId)) {
            throw new RuntimeException("You can only manage technicians for your own shop");
        }
    }

    private TechnicianSummaryResponse toResponse(Technician t) {
        return new TechnicianSummaryResponse(
                t.getId(),
                t.getFullName(),
                t.getPhone(),
                t.getSpecialization()
        );
    }
}
