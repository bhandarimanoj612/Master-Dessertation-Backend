//package com.example.sajilo_tayaar.dto.response;
//
//import com.example.sajilo_tayaar.entity.enums.Role;
//import lombok.*;
//
//import java.time.Instant;
//
//@Getter @Setter
//@AllArgsConstructor @NoArgsConstructor
//public class UserResponse {
//
//    private Long id;
//    private String fullName;
//    private String email;
//    private String phone;
//    private Role role;
//    private Long tenantId; // shopId
//}
package com.example.sajilo_tayaar.dto.response;

import java.time.Instant;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        String role,
        Boolean isActive,
        Long shopId,
        String shopName,
        Instant createdAt
) {}