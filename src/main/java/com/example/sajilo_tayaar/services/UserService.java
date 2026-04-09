package com.example.sajilo_tayaar.services;

import com.example.sajilo_tayaar.dto.response.UserResponse;
import com.example.sajilo_tayaar.entity.User;
import com.example.sajilo_tayaar.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }
    private UserResponse mapToDto(User u) {
        return new UserResponse(
                u.getId(),
                u.getFullName(),
                u.getEmail(),
                u.getPhone(),
                u.getRole().name(),        // String, not Role enum
                u.getIsActive(),           // new
                u.getShop() != null ? u.getShop().getId() : null,
                u.getShop() != null ? u.getShop().getName() : null,  // new
                u.getCreatedAt()           // new
        );
    }
}
