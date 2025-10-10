package com.matsuzaka.foodtiger.service;

import com.matsuzaka.foodtiger.dao.entity.User;
import com.matsuzaka.foodtiger.dto.UserRegistrationRequest;
import com.matsuzaka.foodtiger.exception.UserAlreadyExistsException;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> findAllUsers();
    Optional<User> findUserById(Long id);
    User saveUser(User user);
    void deleteUser(Long id);
    User findByUsername(String username);
    User findByEmail(String email);
    User findByPhone(String phone);
    User registerUser(UserRegistrationRequest request) throws UserAlreadyExistsException; // New method
}