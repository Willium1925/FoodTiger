package com.matsuzaka.foodtiger.service;

import com.matsuzaka.foodtiger.dao.entity.Restaurant;

import java.util.List;
import java.util.Optional;

public interface RestaurantService {
    List<Restaurant> findAllRestaurants();
    Optional<Restaurant> findRestaurantById(Long id);
    Restaurant saveRestaurant(Restaurant restaurant);
    void deleteRestaurant(Long id);
    List<Restaurant> findRestaurantsByOwnerId(Long ownerId);
    List<Restaurant> searchRestaurantsByName(String name);
    List<Restaurant> findRestaurantsByMinRating(Double rating);
}
