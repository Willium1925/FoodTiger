package com.matsuzaka.foodtiger.config.security;

import com.matsuzaka.foodtiger.dao.entity.User;
import com.matsuzaka.foodtiger.dao.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    /**
     * 根據用戶名載入用戶。
     * 這是 Spring Security 認證流程的一部分。
     *
     * @param username 用戶名
     * @return 實現 UserDetails 介面的用戶對象 (CustomUserDetails)
     * @throws UsernameNotFoundException 如果用戶名未找到
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("嘗試載入用戶: {}", username);
        User user = userRepository.findByUsername(username);
        if (user == null) {
            logger.warn("用戶名 '{}' 未找到", username);
            throw new UsernameNotFoundException("用戶名 " + username + " 未找到");
        }
        logger.info("成功載入用戶: {}", username);
        return CustomUserDetails.build(user); // 使用 CustomUserDetails 構建
    }
}