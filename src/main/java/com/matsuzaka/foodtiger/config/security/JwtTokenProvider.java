package com.matsuzaka.foodtiger.config.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    // JWT 密鑰，從 application.properties 讀取，如果沒有則使用預設值
    // 實際應用中，建議從環境變數或安全配置服務中獲取
    @Value("${app.jwtSecret:defaultJwtSecretKeyForDevelopmentAndTestingOnlyPleaseChangeThisInProduction}")
    private String jwtSecret;

    // JWT 過期時間，單位毫秒 (3 天)
    @Value("${app.jwtExpirationMs:259200000}") // 3 days in milliseconds
    private int jwtExpirationMs;

    private Key key;

    /**
     * 在建構後初始化 JWT 密鑰。
     * 如果配置的密鑰是預設值或過短，則動態生成一個新的安全密鑰。
     * 這樣可以確保每次應用程式重啟時，如果沒有提供安全的密鑰，就會生成一個新的密鑰，增加安全性。
     */
    @PostConstruct
    public void init() {
        // 檢查是否使用了預設的、不安全的密鑰，或者密鑰長度不足 (JWT 建議至少 256 位元，Base64 編碼後約 32 字元)
        if (jwtSecret.equals("defaultJwtSecretKeyForDevelopmentAndTestingOnlyPleaseChangeThisInProduction") || jwtSecret.length() < 32) {
            logger.warn("JWT 密鑰未配置或使用預設值/過短。正在動態生成一個新的安全密鑰。");
            // 動態生成一個安全的密鑰
            SecureRandom secureRandom = new SecureRandom();
            byte[] keyBytes = new byte[32]; // 256 bits
            secureRandom.nextBytes(keyBytes);
            this.key = Keys.hmacShaKeyFor(keyBytes);
            logger.info("已動態生成新的 JWT 密鑰。請考慮在生產環境中配置一個持久且安全的密鑰。");
        } else {
            // 使用配置的密鑰
            // 確保密鑰是 Base64 編碼的，並且長度足夠
            try {
                this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtSecret));
                logger.info("已使用配置的 JWT 密鑰。");
            } catch (IllegalArgumentException e) {
                logger.error("配置的 JWT 密鑰不是有效的 Base64 編碼字串或長度不足，正在動態生成一個新的安全密鑰。", e);
                SecureRandom secureRandom = new SecureRandom();
                byte[] keyBytes = new byte[32]; // 256 bits
                secureRandom.nextBytes(keyBytes);
                this.key = Keys.hmacShaKeyFor(keyBytes);
                logger.info("已動態生成新的 JWT 密鑰。請考慮在生產環境中配置一個持久且安全的密鑰。");
            }
        }
    }

    /**
     * 從認證對象生成 JWT。
     *
     * @param authentication 認證對象
     * @return 生成的 JWT 字串
     */
    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername()) // JWT 的主題是用戶名
                .setIssuedAt(new Date()) // 簽發時間
                .setExpiration(expiryDate) // 過期時間
                .signWith(key, SignatureAlgorithm.HS256) // 使用 HS256 演算法和密鑰簽名
                .compact();
    }

    /**
     * 從 JWT 中獲取用戶名。
     *
     * @param token JWT 字串
     * @return 用戶名
     */
    public String getUsernameFromJwt(String token) {
        return Jwts.parser().setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody().getSubject();
    }

    /**
     * 驗證 JWT。
     *
     * @param authToken JWT 字串
     * @return 如果 JWT 有效則為 true，否則為 false
     */
    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(key).build().parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException ex) {
            logger.error("無效的 JWT 令牌: {}", ex.getMessage()); // ERROR 級別日誌
        } catch (ExpiredJwtException ex) {
            logger.error("過期的 JWT 令牌: {}", ex.getMessage()); // ERROR 級別日誌
        } catch (UnsupportedJwtException ex) {
            logger.error("不支持的 JWT 令牌: {}", ex.getMessage()); // ERROR 級別日誌
        } catch (IllegalArgumentException ex) {
            logger.error("JWT 令牌字串為空: {}", ex.getMessage()); // ERROR 級別日誌
        } catch (SignatureException ex) {
            logger.error("無效的 JWT 簽名: {}", ex.getMessage()); // ERROR 級別日誌
        }
        return false;
    }
}
