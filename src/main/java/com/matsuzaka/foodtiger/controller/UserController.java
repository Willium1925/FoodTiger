package com.matsuzaka.foodtiger.controller;

import com.matsuzaka.foodtiger.config.security.CustomUserDetails;
import com.matsuzaka.foodtiger.dao.entity.User;
import com.matsuzaka.foodtiger.dto.UserRegistrationRequest;
import com.matsuzaka.foodtiger.exception.ResourceNotFoundException;
import com.matsuzaka.foodtiger.exception.UnauthorizedException;
import com.matsuzaka.foodtiger.exception.UserAlreadyExistsException;
import com.matsuzaka.foodtiger.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Import for authorization
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    // 允許 ADMIN 角色獲取所有用戶
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        logger.info("收到獲取所有用戶的請求 (由 ADMIN 執行)");
        List<User> users = userService.findAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    // 允許用戶獲取自己的資訊，或 ADMIN 獲取任何用戶資訊
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        logger.info("收到獲取 ID 為 {} 用戶的請求", id);
        return userService.findUserById(id)
                .map(user -> {
                    logger.info("成功獲取 ID 為 {} 的用戶", id);
                    return new ResponseEntity<>(user, HttpStatus.OK);
                })
                .orElseGet(() -> {
                    logger.warn("未找到 ID 為 {} 的用戶", id);
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                });
    }

    // 移除原有的 createUser 方法，因為註冊已由 /api/users/register 處理
    // @PostMapping
    // public ResponseEntity<User> createUser(@RequestBody User user) {
    //     logger.info("收到創建用戶的請求: {}", user.getUsername());
    //     User savedUser = userService.saveUser(user);
    //     logger.info("用戶 '{}' 創建成功", savedUser.getUsername());
    //     return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    // }

    // 允許用戶更新自己的資訊，或 ADMIN 更新任何用戶資訊
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) throws ResourceNotFoundException, UnauthorizedException {
        logger.info("收到更新 ID 為 {} 用戶的請求", id);

        // 獲取當前認證用戶的 ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
        Long currentUserId = currentUser.getId();

        // 如果不是 ADMIN 且嘗試更新的 ID 與當前用戶 ID 不符，則拋出未授權例外
        if (!authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) && !id.equals(currentUserId)) {
            logger.warn("用戶 ID {} 嘗試更新非自己的用戶 ID {}", currentUserId, id);
            throw new UnauthorizedException("您無權更新此用戶的資訊");
        }

        return userService.findUserById(id)
                .map(existingUser -> {
                    existingUser.setUsername(user.getUsername());
                    existingUser.setEmail(user.getEmail());
                    existingUser.setPhone(user.getPhone());
                    // 角色更新應由 ADMIN 處理，或有特定業務邏輯
                    // existingUser.setRole(user.getRole());
                    User updatedUser = userService.saveUser(existingUser);
                    logger.info("用戶 ID {} 更新成功", id);
                    return new ResponseEntity<>(updatedUser, HttpStatus.OK);
                })
                .orElseGet(() -> {
                    logger.warn("嘗試更新不存在的用戶 ID {}", id);
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                });
    }

    // 允許 ADMIN 刪除任何用戶，或用戶刪除自己的帳戶
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) throws ResourceNotFoundException, UnauthorizedException {
        logger.warn("收到刪除 ID 為 {} 用戶的請求", id);

        // 獲取當前認證用戶的 ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
        Long currentUserId = currentUser.getId();

        // 如果不是 ADMIN 且嘗試刪除的 ID 與當前用戶 ID 不符，則拋出未授權例外
        if (!authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) && !id.equals(currentUserId)) {
            logger.warn("用戶 ID {} 嘗試刪除非自己的用戶 ID {}", currentUserId, id);
            throw new UnauthorizedException("您無權刪除此用戶的帳戶");
        }

        if (userService.findUserById(id).isPresent()) {
            userService.deleteUser(id);
            logger.info("用戶 ID {} 刪除成功", id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        logger.warn("嘗試刪除不存在的用戶 ID {}", id);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * 用戶註冊端點。
     * 允許所有人訪問。
     *
     * @param request 包含用戶註冊資訊的請求 DTO
     * @return 註冊成功的用戶實體和 CREATED 狀態
     * @throws UserAlreadyExistsException 如果用戶名、電子郵件或電話已存在
     */
    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@Valid @RequestBody UserRegistrationRequest request) throws UserAlreadyExistsException {
        logger.info("收到新用戶註冊請求: 用戶名 '{}'", request.getUsername());
        User registeredUser = userService.registerUser(request);
        logger.info("用戶 '{}' 註冊成功", registeredUser.getUsername());
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }
}
