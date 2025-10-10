package com.matsuzaka.foodtiger.service.impl;

import com.matsuzaka.foodtiger.dao.entity.OrderItem;
import com.matsuzaka.foodtiger.dao.repository.OrderItemRepository;
import com.matsuzaka.foodtiger.service.OrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderItemServiceImpl implements OrderItemService {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Override
    public List<OrderItem> findAllOrderItems() {
        return orderItemRepository.findAll();
    }

    @Override
    public Optional<OrderItem> findOrderItemById(Long id) {
        return orderItemRepository.findById(id);
    }

    @Override
    public OrderItem saveOrderItem(OrderItem orderItem) {
        return orderItemRepository.save(orderItem);
    }

    @Override
    public void deleteOrderItem(Long id) {
        orderItemRepository.deleteById(id);
    }

    @Override
    public List<OrderItem> findOrderItemsByOrderrId(Long orderrId) {
        return orderItemRepository.findByOrderrId(orderrId);
    }

    @Override
    public List<OrderItem> findOrderItemsByMenuItemId(Long menuItemId) {
        return orderItemRepository.findByMenuItemId(menuItemId);
    }
}
