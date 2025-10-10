package com.matsuzaka.foodtiger.dao.repository;

import com.matsuzaka.foodtiger.dao.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderrId(Long orderrId);
    List<OrderItem> findByMenuItemId(Long menuItemId);
}
