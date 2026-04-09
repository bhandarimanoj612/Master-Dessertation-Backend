package com.example.sajilo_tayaar.controller;

import com.example.sajilo_tayaar.dto.request.LoginRequest;
import com.example.sajilo_tayaar.dto.request.RegisterRequest;
import com.example.sajilo_tayaar.dto.request.RegisterShopRequest;
import com.example.sajilo_tayaar.dto.response.AuthResponse;
import com.example.sajilo_tayaar.dto.response.AuthShopResponse;
import com.example.sajilo_tayaar.dto.response.UserResponse;
import com.example.sajilo_tayaar.entity.Shop;
import com.example.sajilo_tayaar.entity.User;
import com.example.sajilo_tayaar.entity.enums.Role;
import com.example.sajilo_tayaar.repository.ShopRepository;
import com.example.sajilo_tayaar.repository.UserRepository;
import com.example.sajilo_tayaar.security.JwtService;
import com.example.sajilo_tayaar.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final UserService userService;

    @PostMapping("/register-customer")
    public String registerCustomer(@Valid @RequestBody RegisterRequest req) {

        if (userRepository.findByEmail(req.email()).isPresent())
            throw new RuntimeException("Email already registered");

        User user = new User();
        user.setFullName(req.fullName());
        user.setEmail(req.email());
        user.setPhone(req.phone());
        user.setPasswordHash(encoder.encode(req.password()));
        user.setRole(Role.CUSTOMER);

        userRepository.save(user);
        return "Customer registered";
    }

    @PostMapping("/register-shop")
    public AuthResponse registerShop(@Valid @RequestBody RegisterShopRequest req) {

        if (userRepository.findByEmail(req.ownerEmail()).isPresent())
            throw new RuntimeException("Owner email already registered");

        if (shopRepository.findByName(req.shopName()).isPresent())
            throw new RuntimeException("Shop name already exists");

        Shop shop = new Shop();
        shop.setName(req.shopName());
        shop.setStreetAddress(req.shopStreetAddress());
        shop.setArea(req.area());
        shop.setCity(req.city());
        shop.setState(req.state());
        shop.setPostalCode(req.postalCode());
        shop.setPhone(req.shopPhone());
        shop.setDescription(req.description());
        shop.setLat(req.lat());
        shop.setLng(req.lng());
        shop.setIsActive(true);
        shop.setVerified(false);

        shopRepository.save(shop);

        User owner = new User();
        owner.setFullName(req.ownerFullName());
        owner.setEmail(req.ownerEmail());
        owner.setPhone(req.ownerPhone());
        owner.setPasswordHash(encoder.encode(req.ownerPassword()));
        owner.setRole(Role.SHOP_OWNER);
        owner.setShop(shop);
        userRepository.save(owner);

        Long tenantId = shop.getId();
        String token = jwtService.generateToken(owner.getId(), owner.getRole().name(), tenantId);

        AuthShopResponse shopResponse = new AuthShopResponse(
                shop.getId(),
                shop.getName(),
                shop.getStreetAddress(),
                shop.getArea(),
                shop.getCity(),
                shop.getState(),
                shop.getPostalCode(),
                shop.getPhone(),
                shop.getDescription(),
                shop.getLat(),
                shop.getLng(),
                shop.getVerified(),
                shop.getIsActive()
        );

        return new AuthResponse(
                token,
                owner.getId(),
                owner.getRole().name(),
                owner.getEmail(),
                owner.getPhone(),
                tenantId,
                owner.getFullName(),
                shopResponse
        );
    }
    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest req) {

        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!encoder.matches(req.password(), user.getPasswordHash()))
            throw new RuntimeException("Invalid credentials");

        if (!Boolean.TRUE.equals(user.getIsActive()))
            throw new RuntimeException("Account is inactive");

        Long tenantId = user.getShop() == null ? null : user.getShop().getId();
        String token = jwtService.generateToken(user.getId(), user.getRole().name(), tenantId);

        AuthShopResponse shopResponse = null;

        if (user.getShop() != null) {
            Shop shop = user.getShop();

            shopResponse = new AuthShopResponse(
                    shop.getId(),
                    shop.getName(),
                    shop.getStreetAddress(),
                    shop.getArea(),
                    shop.getCity(),
                    shop.getState(),
                    shop.getPostalCode(),
                    shop.getPhone(),
                    shop.getDescription(),
                    shop.getLat(),
                    shop.getLng(),
                    shop.getVerified(),
                    shop.getIsActive()
            );
        }

        return new AuthResponse(
                token,
                user.getId(),
                user.getRole().name(),
                user.getEmail(),
                user.getPhone(),
                tenantId,
                user.getFullName(),
                shopResponse
        );
    }

    // get-all users details
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }
}