package com.example.sajilo_tayaar.config;

import com.example.sajilo_tayaar.entity.User;
import com.example.sajilo_tayaar.entity.enums.Role;
import com.example.sajilo_tayaar.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class PlatformAdminSeeder {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner seedPlatformAdmin() {
        return args -> {
            String email = "admin@gmail.com";
            String password = "admin";
            String phone = "9800000000";

            if (userRepository.findByEmail(email).isEmpty()) {
                User admin = new User();
                admin.setFullName("Platform Admin");
                admin.setEmail(email);
                admin.setPhone(phone);
                admin.setPasswordHash(passwordEncoder.encode(password));
                admin.setRole(Role.PLATFORM_ADMIN);
                admin.setIsActive(true);

                userRepository.save(admin);

                System.out.println("PLATFORM ADMIN CREATED");
                System.out.println("Email: " + email);
                System.out.println("Password: " + password);
            }
        };
    }
}