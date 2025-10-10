package com.matsuzaka.foodtiger.config.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);

    /**
     * 當用戶嘗試訪問受保護的資源但未提供任何憑證或憑證無效時觸發。
     *
     * @param request HTTP 請求
     * @param response HTTP 響應
     * @param authException 認證例外
     * @throws IOException IO 例外
     * @throws ServletException Servlet 例外
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        logger.error("未經授權的訪問錯誤: {}", authException.getMessage()); // ERROR 級別日誌
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "未經授權的訪問");
    }
}
