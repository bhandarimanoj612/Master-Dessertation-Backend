package com.example.sajilo_tayaar;

import com.example.sajilo_tayaar.dto.response.UserResponse;
import com.example.sajilo_tayaar.entity.Shop;
import com.example.sajilo_tayaar.entity.User;
import com.example.sajilo_tayaar.entity.enums.Role;
import com.example.sajilo_tayaar.repository.UserRepository;
import com.example.sajilo_tayaar.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        Shop shop = new Shop();
        shop.setId(1L);
        shop.setName("Sajilo Repair Center");

        user = new User();
        user.setId(1L);
        user.setFullName("Manoj Bhandari");
        user.setEmail("manoj@gmail.com");
        user.setPhone("9800");
        user.setRole(Role.SHOP_OWNER);
        user.setIsActive(true);
        user.setShop(shop);
        user.setCreatedAt(Instant.parse("2026-01-01T10:00:00Z"));
    }

    @Test
    void getAllUsers_shouldMapEntityToDtoCorrectly() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserResponse> responses = userService.getAllUsers();

        assertEquals(1, responses.size());
        UserResponse response = responses.get(0);
        assertEquals("Manoj Bhandari", response.fullName());
        assertEquals("SHOP_OWNER", response.role());
        assertEquals(1L, response.shopId());
        assertEquals("Sajilo Repair Center", response.shopName());
    }
}
