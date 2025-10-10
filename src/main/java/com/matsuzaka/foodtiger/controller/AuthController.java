package com.matsuzaka.foodtiger.controller;

import com.matsuzaka.foodtiger.config.security.CustomUserDetails;
import com.matsuzaka.foodtiger.config.security.JwtTokenProvider;
import com.matsuzaka.foodtiger.dto.JwtResponse;
import com.matsuzaka.foodtiger.dto.LoginRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * 用戶登入端點。
     * 接收用戶名和密碼，進行認證，並在成功後返回 JWT。
     *
     * @param loginRequest 包含用戶名和密碼的 DTO
     * @return 包含 JWT 和用戶資訊的 ResponseEntity
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("收到用戶 '{}' 的登入請求", loginRequest.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_UNKNOWN"); // 假設只有一個角色

        logger.info("用戶 '{}' 登入成功，已生成 JWT", loginRequest.getUsername());
        return ResponseEntity.ok(new JwtResponse(jwt, "Bearer", userDetails.getId(), userDetails.getUsername(), role));
    }
}
