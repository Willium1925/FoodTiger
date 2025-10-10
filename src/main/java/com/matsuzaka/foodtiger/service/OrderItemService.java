package com.matsuzaka.foodtiger.service;

import com.matsuzaka.foodtiger.dao.entity.OrderItem;

import java.util.List;
import java.util.Optional;

public interface OrderItemService {
    List<OrderItem> findAllOrderItems();
    Optional<OrderItem> findOrderItemById(Long id);
    OrderItem saveOrderItem(OrderItem orderItem);
    void deleteOrderItem(Long id);
    List<OrderItem> findOrderItemsByOrderrId(Long orderrId);
    List<OrderItem> findOrderItemsByMenuItemId(Long menuItemId);
}
