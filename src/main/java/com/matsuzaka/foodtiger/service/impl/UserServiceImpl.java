package com.matsuzaka.foodtiger.service.impl;

import com.matsuzaka.foodtiger.dao.entity.User;
import com.matsuzaka.foodtiger.dao.repository.UserRepository;
import com.matsuzaka.foodtiger.dto.UserRegistrationRequest;
import com.matsuzaka.foodtiger.exception.UserAlreadyExistsException;
import com.matsuzaka.foodtiger.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // Import PasswordEncoder
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class); // Logger instance

    @Autowired
    private UserRepository userRepository;

    @Autowired(required = false) // Make PasswordEncoder optional for now, user needs to configure it
    private PasswordEncoder passwordEncoder; // Autowire PasswordEncoder

    @Override
    public List<User> findAllUsers() {
        logger.info("正在查詢所有用戶"); // INFO 級別日誌
        return userRepository.findAll();
    }

    @Override
    public Optional<User> findUserById(Long id) {
        logger.info("正在查詢 ID 為 {} 的用戶", id); // INFO 級別日誌
        return userRepository.findById(id);
    }

    @Override
    public User saveUser(User user) {
        logger.info("正在保存用戶: {}", user.getUsername()); // INFO 級別日誌
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        logger.warn("正在刪除 ID 為 {} 的用戶", id); // WARN 級別日誌，因為刪除是敏感操作
        userRepository.deleteById(id);
    }

    @Override
    public User findByUsername(String username) {
        logger.debug("正在按用戶名查詢用戶: {}", username); // DEBUG 級別日誌
        return userRepository.findByUsername(username);
    }

    @Override
    public User findByEmail(String email) {
        logger.debug("正在按電子郵件查詢用戶: {}", email); // DEBUG 級別日誌
        return userRepository.findByEmail(email);
    }

    @Override
    public User findByPhone(String phone) {
        logger.debug("正在按電話號碼查詢用戶: {}", phone); // DEBUG 級別日誌
        return userRepository.findByPhone(phone);
    }

    /**
     * 註冊新用戶。
     * 檢查用戶名、電子郵件和電話是否已存在。
     * 如果存在，則拋出 UserAlreadyExistsException (Checked Exception)。
     * 密碼會被加密後保存。
     *
     * @param request 包含用戶註冊資訊的請求 DTO
     * @return 註冊成功的用戶實體
     * @throws UserAlreadyExistsException 如果用戶名、電子郵件或電話已存在
     */
    @Override
    public User registerUser(UserRegistrationRequest request) throws UserAlreadyExistsException {
        // 檢查用戶名是否已存在
        if (userRepository.findByUsername(request.getUsername()) != null) {
            logger.warn("註冊失敗：用戶名 '{}' 已存在", request.getUsername()); // WARN 級別日誌
            throw new UserAlreadyExistsException("用戶名 '" + request.getUsername() + "' 已存在");
        }
        // 檢查電子郵件是否已存在
        if (request.getEmail() != null && userRepository.findByEmail(request.getEmail()) != null) {
            logger.warn("註冊失敗：電子郵件 '{}' 已存在", request.getEmail()); // WARN 級別日誌
            throw new UserAlreadyExistsException("電子郵件 '" + request.getEmail() + "' 已存在");
        }
        // 檢查電話號碼是否已存在
        if (request.getPhone() != null && userRepository.findByPhone(request.getPhone()) != null) {
            logger.warn("註冊失敗：電話號碼 '{}' 已存在", request.getPhone()); // WARN 級別日誌
            throw new UserAlreadyExistsException("電話號碼 '" + request.getPhone() + "' 已存在");
        }

        User newUser = new User();
        newUser.setUsername(request.getUsername());
        // 檢查 passwordEncoder 是否已配置
        if (passwordEncoder != null) {
            newUser.setPassword(passwordEncoder.encode(request.getPassword())); // 加密密碼
        } else {
            logger.error("PasswordEncoder 未配置，密碼將以明文形式保存。請確保 Spring Security 已正確配置。"); // ERROR 級別日誌
            newUser.setPassword(request.getPassword()); // 如果沒有配置，則直接保存明文密碼 (不推薦)
        }
        newUser.setEmail(request.getEmail());
        newUser.setPhone(request.getPhone());
        newUser.setRole(request.getRole());

        User savedUser = userRepository.save(newUser);
        logger.info("新用戶 '{}' 註冊成功，角色為 {}", savedUser.getUsername(), savedUser.getRole()); // INFO 級別日誌
        return savedUser;
    }
}