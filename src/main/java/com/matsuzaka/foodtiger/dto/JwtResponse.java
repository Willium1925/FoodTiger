package com.matsuzaka.foodtiger.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

@Data
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String role; // Assuming one role per user for simplicity
}
