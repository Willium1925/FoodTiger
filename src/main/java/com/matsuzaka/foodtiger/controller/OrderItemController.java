package com.matsuzaka.foodtiger.controller;

import com.matsuzaka.foodtiger.dao.entity.OrderItem;
import com.matsuzaka.foodtiger.service.OrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order-items")
public class OrderItemController {

    @Autowired
    private OrderItemService orderItemService;

    @GetMapping
    public ResponseEntity<List<OrderItem>> getAllOrderItems() {
        List<OrderItem> orderItems = orderItemService.findAllOrderItems();
        return new ResponseEntity<>(orderItems, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderItem> getOrderItemById(@PathVariable Long id) {
        return orderItemService.findOrderItemById(id)
                .map(orderItem -> new ResponseEntity<>(orderItem, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<OrderItem> createOrderItem(@RequestBody OrderItem orderItem) {
        OrderItem savedOrderItem = orderItemService.saveOrderItem(orderItem);
        return new ResponseEntity<>(savedOrderItem, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderItem> updateOrderItem(@PathVariable Long id, @RequestBody OrderItem orderItem) {
        return orderItemService.findOrderItemById(id)
                .map(existingOrderItem -> {
                    existingOrderItem.setOrderr(orderItem.getOrderr());
                    existingOrderItem.setMenuItem(orderItem.getMenuItem());
                    existingOrderItem.setQuantity(orderItem.getQuantity());
                    existingOrderItem.setPriceAtOrderr(orderItem.getPriceAtOrderr());
                    OrderItem updatedOrderItem = orderItemService.saveOrderItem(existingOrderItem);
                    return new ResponseEntity<>(updatedOrderItem, HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrderItem(@PathVariable Long id) {
        if (orderItemService.findOrderItemById(id).isPresent()) {
            orderItemService.deleteOrderItem(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/orderr/{orderrId}")
    public ResponseEntity<List<OrderItem>> getOrderItemsByOrderrId(@PathVariable Long orderrId) {
        List<OrderItem> orderItems = orderItemService.findOrderItemsByOrderrId(orderrId);
        if (orderItems.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(orderItems, HttpStatus.OK);
    }

    @GetMapping("/menu-item/{menuItemId}")
    public ResponseEntity<List<OrderItem>> getOrderItemsByMenuItemId(@PathVariable Long menuItemId) {
        List<OrderItem> orderItems = orderItemService.findOrderItemsByMenuItemId(menuItemId);
        if (orderItems.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(orderItems, HttpStatus.OK);
    }
}
