package com.matsuzaka.foodtiger.dao.repository;

import com.matsuzaka.foodtiger.dao.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    List<Restaurant> findByOwnerId(Long ownerId);
    List<Restaurant> findByNameContainingIgnoreCase(String name);
    List<Restaurant> findByRatingGreaterThanEqual(Double rating);
}
