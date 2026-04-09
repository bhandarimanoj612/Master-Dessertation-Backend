package com.example.sajilo_tayaar.controller;

import com.example.sajilo_tayaar.dto.request.CreateShopRequest;
import com.example.sajilo_tayaar.dto.request.UpdateShopRequest;
import com.example.sajilo_tayaar.dto.response.ShopDetailsResponse;
import com.example.sajilo_tayaar.dto.response.ShopListResponse;
import com.example.sajilo_tayaar.entity.Shop;
import com.example.sajilo_tayaar.repository.ShopRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
public class ShopController {

    private final ShopRepository shopRepository;

    // ✅ CREATE (ADMIN / PLATFORM_ADMIN / public for now)
    @PostMapping
    public ShopDetailsResponse create(@Valid @RequestBody CreateShopRequest req) {

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
        return toDetails(saved);
    }

    // ✅ LIST (PUBLIC)
    @GetMapping
    public List<ShopListResponse> list() {
        Sort sort = Sort.by(Sort.Direction.DESC, "avgRating")
                .and(Sort.by(Sort.Direction.DESC, "ratingCount"));

        return shopRepository.findByIsActiveTrue(sort)
                .stream()
                .map(this::toList)
                .toList();
    }

    // ✅ DETAILS (PUBLIC)
    @GetMapping("/{id}")
    public ShopDetailsResponse getById(@PathVariable Long id) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Shop not found"));

        // public: only active shops
        if (shop.getIsActive() == null || !shop.getIsActive()) {
            throw new ResponseStatusException(NOT_FOUND, "Shop not found");
        }

        return toDetails(shop);
    }

    // ✅ UPDATE (ADMIN / SHOP_OWNER later)
    @PutMapping("/{id}")
    public ShopDetailsResponse update(@PathVariable Long id, @Valid @RequestBody UpdateShopRequest req) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Shop not found"));

        if (req.name() != null) shop.setName(req.name());

        if (req.streetAddress() != null) shop.setStreetAddress(req.streetAddress());
        if (req.area() != null) shop.setArea(req.area());
        if (req.city() != null) shop.setCity(req.city());
        if (req.state() != null) shop.setState(req.state());
        if (req.postalCode() != null) shop.setPostalCode(req.postalCode());

        if (req.phone() != null) shop.setPhone(req.phone());
        if (req.description() != null) shop.setDescription(req.description());

        if (req.lat() != null) shop.setLat(req.lat());
        if (req.lng() != null) shop.setLng(req.lng());

        Shop saved = shopRepository.save(shop);
        return toDetails(saved);
    }

    // ✅ TOGGLE ACTIVE (ADMIN)
    @PatchMapping("/{id}/active")
    public ShopDetailsResponse setActive(@PathVariable Long id, @RequestParam boolean value) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Shop not found"));

        shop.setIsActive(value);
        Shop saved = shopRepository.save(shop);
        return toDetails(saved);
    }

    // ✅ TOGGLE VERIFIED (ADMIN)
    @PatchMapping("/{id}/verified")
    public ShopDetailsResponse setVerified(@PathVariable Long id, @RequestParam boolean value) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Shop not found"));

        shop.setVerified(value);
        Shop saved = shopRepository.save(shop);
        return toDetails(saved);
    }

    // ------------------------
    // DTO MAPPERS
    // ------------------------

    private ShopListResponse toList(Shop s) {
        String shortAddress = buildShortAddress(s);

        return new ShopListResponse(
                s.getId(),
                s.getName(),
                shortAddress,
                s.getPhone(),
                s.getAvgRating(),
                s.getRatingCount(),
                s.getLat(),
                s.getLng(),
                s.getVerified()
        );
    }

    private ShopDetailsResponse toDetails(Shop s) {
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

    private String buildShortAddress(Shop s) {
        // good for shop cards: "Itahari-6, Sunsari"
        String a = s.getArea();
        String c = s.getCity();
        String st = s.getState();

        if (a != null && c != null) return a + ", " + c;
        if (c != null && st != null) return c + ", " + st;
        if (c != null) return c;
        if (st != null) return st;
        return s.getStreetAddress();
    }

    @GetMapping("/admin/all")
    public List<ShopDetailsResponse> getAllForAdmin() {
        return shopRepository.findAll()
                .stream()
                .map(this::toDetails)
                .toList();
    }
}