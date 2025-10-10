package com.matsuzaka.foodtiger.service.impl;

import com.matsuzaka.foodtiger.dao.entity.Restaurant;
import com.matsuzaka.foodtiger.dao.repository.RestaurantRepository;
import com.matsuzaka.foodtiger.service.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RestaurantServiceImpl implements RestaurantService {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Override
    public List<Restaurant> findAllRestaurants() {
        return restaurantRepository.findAll();
    }

    @Override
    public Optional<Restaurant> findRestaurantById(Long id) {
        return restaurantRepository.findById(id);
    }

    @Override
    public Restaurant saveRestaurant(Restaurant restaurant) {
        return restaurantRepository.save(restaurant);
    }

    @Override
    public void deleteRestaurant(Long id) {
        restaurantRepository.deleteById(id);
    }

    @Override
    public List<Restaurant> findRestaurantsByOwnerId(Long ownerId) {
        return restaurantRepository.findByOwnerId(ownerId);
    }

    @Override
    public List<Restaurant> searchRestaurantsByName(String name) {
        return restaurantRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    public List<Restaurant> findRestaurantsByMinRating(Double rating) {
        return restaurantRepository.findByRatingGreaterThanEqual(rating);
    }
}
