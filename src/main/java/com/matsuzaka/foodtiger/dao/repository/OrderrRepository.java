package com.matsuzaka.foodtiger.dao.repository;

import com.matsuzaka.foodtiger.dao.entity.Orderr;
import com.matsuzaka.foodtiger.dao.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderrRepository extends JpaRepository<Orderr, Long> {
    List<Orderr> findByUserId(Long userId);
    List<Orderr> findByRestaurantId(Long restaurantId);
    List<Orderr> findByDeliveryPersonId(Long deliveryPersonId);
    List<Orderr> findByStatus(OrderStatus status);
}
