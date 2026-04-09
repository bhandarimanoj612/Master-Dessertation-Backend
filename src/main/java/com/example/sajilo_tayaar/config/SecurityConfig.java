package com.example.sajilo_tayaar.config;

import com.example.sajilo_tayaar.security.JwtAuthFilter;
import com.example.sajilo_tayaar.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    // JWT keeps the API stateless while role rules protect shop and admin actions.

    private final JwtService jwtService;

    @Bean
    // Public routes stay open for auth and public shop browsing, everything else requires a token.
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/shops/**").permitAll()
                        .requestMatchers("/api/superadmin/**")                          // ← ADD THIS
                        .permitAll()                                  // ← ADD THIS
                        .requestMatchers("/api/shop/verification/**").authenticated()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/api/admin/technicians/**", "/api/admin/shop-users/**")
                        .hasAnyRole("PLATFORM_ADMIN", "SHOP_OWNER", "SHOP_STAFF")
                        .requestMatchers("/api/admin/users/**")
                        .hasRole("PLATFORM_ADMIN")
                        .requestMatchers("/api/dashboard/**", "/api/customers/**", "/api/inventory/**", "/api/pos/**", "/api/bookings/**")
                        .authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthFilter(jwtService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
