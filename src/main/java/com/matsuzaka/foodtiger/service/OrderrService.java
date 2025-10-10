package com.matsuzaka.foodtiger.service;

import com.matsuzaka.foodtiger.dao.entity.Orderr;
import com.matsuzaka.foodtiger.dao.entity.OrderStatus;
import com.matsuzaka.foodtiger.dto.DeliveryAssignmentRequest;
import com.matsuzaka.foodtiger.dto.OrderRequest;
import com.matsuzaka.foodtiger.dto.OrderStatusUpdateRequest;
import com.matsuzaka.foodtiger.exception.InvalidOperationException;
import com.matsuzaka.foodtiger.exception.InvalidOrderStatusTransitionException;
import com.matsuzaka.foodtiger.exception.MenuItemUnavailableException;
import com.matsuzaka.foodtiger.exception.ResourceNotFoundException;
import com.matsuzaka.foodtiger.exception.UnauthorizedException;

import java.util.List;
import java.util.Optional;

public interface OrderrService {
    List<Orderr> findAllOrderrs();
    Optional<Orderr> findOrderrById(Long id);
    Orderr saveOrderr(Orderr orderr);
    void deleteOrderr(Long id);
    List<Orderr> findOrderrsByUserId(Long userId);
    List<Orderr> findOrderrsByRestaurantId(Long restaurantId);
    List<Orderr> findOrderrsByDeliveryPersonId(Long deliveryPersonId);
    List<Orderr> findOrderrsByStatus(OrderStatus status);
    Orderr createOrder(OrderRequest orderRequest) throws ResourceNotFoundException, MenuItemUnavailableException;
    Orderr updateOrderStatus(Long orderId, OrderStatusUpdateRequest request, Long restaurantOwnerId) throws ResourceNotFoundException, InvalidOrderStatusTransitionException, UnauthorizedException;
    Orderr assignDeliveryPerson(Long orderId, DeliveryAssignmentRequest request) throws ResourceNotFoundException, InvalidOperationException; // New method
    Orderr acceptDelivery(Long orderId, Long deliveryPersonId) throws ResourceNotFoundException, UnauthorizedException, InvalidOperationException; // New method
    Orderr rejectDelivery(Long orderId, Long deliveryPersonId) throws ResourceNotFoundException, UnauthorizedException, InvalidOperationException; // New method
}