package com.example.sajilo_tayaar.services;

import com.example.sajilo_tayaar.dto.request.CreateShopRequest;
import com.example.sajilo_tayaar.dto.request.UpdateShopRequest;
import com.example.sajilo_tayaar.dto.request.UpdateUserRequest;
import com.example.sajilo_tayaar.dto.response.ShopAdminResponse;
import com.example.sajilo_tayaar.dto.response.ShopDetailsResponse;
import com.example.sajilo_tayaar.dto.response.UserResponse;
import com.example.sajilo_tayaar.entity.Shop;
import com.example.sajilo_tayaar.entity.User;
import com.example.sajilo_tayaar.entity.enums.Role;
import com.example.sajilo_tayaar.entity.enums.VerificationStatus;
import com.example.sajilo_tayaar.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SuperAdminService {

    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final PasswordEncoder passwordEncoder;
    private final BookingRepository bookingRepository;
    private final InvoiceRepository invoiceRepository;
    private final BookingStatusHistoryRepository bookingStatusHistoryRepository;
    private final  ShopVerificationDocumentRepository shopVerificationDocumentRepository;
    private final  TechnicianRepository technicianRepository;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserResponse)
                .toList();
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = findUserOrThrow(id);
        userRepository.delete(user);
    }

    @Transactional
    public boolean toggleUserActive(Long id) {
        User user = findUserOrThrow(id);
        user.setIsActive(!user.getIsActive());
        userRepository.save(user);
        return user.getIsActive();
    }

    @Transactional
    public void changeUserPassword(Long id, String newPassword) {
        User user = findUserOrThrow(id);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest req) {
        User user = findUserOrThrow(id);

        if (req.fullName() != null) user.setFullName(req.fullName());
        if (req.email() != null) user.setEmail(req.email());
        if (req.phone() != null) user.setPhone(req.phone());
        if (req.role() != null) user.setRole(req.role());

        userRepository.save(user);
        return toUserResponse(user);
    }
    public List<ShopAdminResponse> getAllShops() {
        return shopRepository.findAll().stream()
                .map(this::toShopAdminResponse)
                .toList();
    }

    public ShopDetailsResponse getShopById(Long id) {
        Shop shop = findShopOrThrow(id);
        return toShopDetailsResponse(shop);
    }

    @Transactional
    public ShopDetailsResponse createShop(CreateShopRequest req) {
        Shop shop = new Shop();
        shop.setName(req.name());
        shop.setStreetAddress(req.streetAddress());
        shop.setArea(req.area());
        shop.setCity(req.city());
        shop.setState(req.state());
        shop.setPostalCode(req.postalCode());
        shop.setPhone(req.phone());
        shop.setDescription(req.description());
        shop.setLat(req.lat());
        shop.setLng(req.lng());
        shop.setIsActive(true);
        shop.setVerified(false);

        Shop saved = shopRepository.save(shop);
        return toShopDetailsResponse(saved);
    }

    @Transactional
    public ShopDetailsResponse updateShop(Long id, UpdateShopRequest req) {
        Shop shop = findShopOrThrow(id);

        if (req.name() != null) shop.setName(req.name());
        if (req.streetAddress() != null) shop.setStreetAddress(req.streetAddress());
        if (req.area() != null) shop.setArea(req.area());
        if (req.city() != null) shop.setCity(req.city());
        if (req.state() != null) shop.setState(req.state());
        if (req.postalCode() != null) shop.setPostalCode(req.postalCode());
        if (req.phone() != null) shop.setPhone(req.phone());
        if (req.description() != null) shop.setDescription(req.description());
        shop.setLat(req.lat());
        shop.setLng(req.lng());

        Shop saved = shopRepository.save(shop);
        return toShopDetailsResponse(saved);
    }


    @Transactional
    public void deleteShop(Long id) {
        Shop shop = findShopOrThrow(id);
        Long shopId = shop.getId();

        try {
            // Delete in dependency order (MOST DEPENDENT FIRST)

            // 1. Delete ShopVerificationDocuments (depends on Shop)
            shopVerificationDocumentRepository.deleteByShopId(shopId);

            // 2. Delete BookingStatusHistory (depends on Booking)
            bookingStatusHistoryRepository.deleteByShopId(shopId);

            // 3. Delete Bookings (depends on Shop and Technician)
            bookingRepository.deleteByShopId(shopId);

            // 4. Delete Invoices (depends on Shop)
            invoiceRepository.deleteByShopId(shopId);

            // 5. Delete Technicians (depends on Shop)
            technicianRepository.deleteByShopId(shopId);

            // 6. Delete Users (depends on Shop)
            userRepository.deleteByShopId(shopId);

            // 7. Delete Shop (LAST - no more dependencies)
            shopRepository.deleteById(shopId);

        } catch (Exception e) {
            throw new RuntimeException("Failed to delete shop: " + e.getMessage(), e);
        }
    }
    @Transactional
    public boolean toggleShopActive(Long id) {
        Shop shop = findShopOrThrow(id);
        shop.setIsActive(!shop.getIsActive());
        shopRepository.save(shop);
        return shop.getIsActive();
    }

    @Transactional
    public boolean toggleShopVerified(Long id) {
        Shop shop = findShopOrThrow(id);

        boolean newVerifiedState = !shop.getVerified();
        shop.setVerified(newVerifiedState);

        //  Only set status to LISTED when unverifying
        if (!newVerifiedState) {
            shop.setVerificationStatus(VerificationStatus.LISTED);
            shop.setVerificationNotes(null);  // Clear notes on unverify
            shop.setVerifiedAt(null);          // Clear verified date on unverify
        }
        //  Set status to VERIFIED when verifying
        else {
            shop.setVerificationStatus(VerificationStatus.VERIFIED);
            shop.setVerifiedAt(Instant.now());
        }

        shopRepository.save(shop);
        return shop.getVerified();
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }

    private Shop findShopOrThrow(Long id) {
        return shopRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shop not found: " + id));
    }

    private UserResponse toUserResponse(User u) {
        Long shopId = u.getShop() == null ? null : u.getShop().getId();
        String shopName = u.getShop() == null ? null : u.getShop().getName();

        return new UserResponse(
                u.getId(),
                u.getFullName(),
                u.getEmail(),
                u.getPhone(),
                u.getRole().name(),
                u.getIsActive(),
                shopId,
                shopName,
                u.getCreatedAt()
        );
    }

    private ShopAdminResponse toShopAdminResponse(Shop s) {
        return new ShopAdminResponse(
                s.getId(),
                s.getName(),
                s.getCity(),
                s.getPhone(),
                s.getIsActive(),
                s.getVerified(),
                s.getCreatedAt()
        );
    }

    private ShopDetailsResponse toShopDetailsResponse(Shop s) {
        return new ShopDetailsResponse(
                s.getId(),
                s.getName(),
                s.getStreetAddress(),
                s.getArea(),
                s.getCity(),
                s.getState(),
                s.getPostalCode(),
                s.getPhone(),
                s.getDescription(),
                s.getAvgRating(),
                s.getRatingCount(),
                s.getLat(),
                s.getLng(),
                s.getVerified(),
                s.getIsActive()
        );
    }
}