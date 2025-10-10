package com.matsuzaka.foodtiger.dao.repository;

import com.matsuzaka.foodtiger.dao.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // 您可以在此處添加自定義查詢方法（如果需要）
    User findByUsername(String username);
    User findByEmail(String email);
    User findByPhone(String phone);
}
