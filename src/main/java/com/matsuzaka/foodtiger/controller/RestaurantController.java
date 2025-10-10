package com.matsuzaka.foodtiger.controller;

import com.matsuzaka.foodtiger.dao.entity.Restaurant;
import com.matsuzaka.foodtiger.service.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    @Autowired
    private RestaurantService restaurantService;

    @GetMapping
    public ResponseEntity<List<Restaurant>> getAllRestaurants() {
        List<Restaurant> restaurants = restaurantService.findAllRestaurants();
        return new ResponseEntity<>(restaurants, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Restaurant> getRestaurantById(@PathVariable Long id) {
        return restaurantService.findRestaurantById(id)
                .map(restaurant -> new ResponseEntity<>(restaurant, HttpStatus.OK))n                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Restaurant> createRestaurant(@RequestBody Restaurant restaurant) {
        Restaurant savedRestaurant = restaurantService.saveRestaurant(restaurant);
        return new ResponseEntity<>(savedRestaurant, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Restaurant> updateRestaurant(@PathVariable Long id, @RequestBody Restaurant restaurant) {
        return restaurantService.findRestaurantById(id)
                .map(existingRestaurant -> {
                    existingRestaurant.setName(restaurant.getName());
                    existingRestaurant.setOwner(restaurant.getOwner());
                    existingRestaurant.setAddress(restaurant.getAddress());
                    existingRestaurant.setDescription(restaurant.getDescription());
                    existingRestaurant.setRating(restaurant.getRating());
                    Restaurant updatedRestaurant = restaurantService.saveRestaurant(existingRestaurant);
                    return new ResponseEntity<>(updatedRestaurant, HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable Long id) {
        if (restaurantService.findRestaurantById(id).isPresent()) {
            restaurantService.deleteRestaurant(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<Restaurant>> getRestaurantsByOwnerId(@PathVariable Long ownerId) {
        List<Restaurant> restaurants = restaurantService.findRestaurantsByOwnerId(ownerId);
        if (restaurants.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(restaurants, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Restaurant>> searchRestaurants(@RequestParam String name) {
        List<Restaurant> restaurants = restaurantService.searchRestaurantsByName(name);
        if (restaurants.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(restaurants, HttpStatus.OK);
    }

    @GetMapping("/rating/{minRating}")
    public ResponseEntity<List<Restaurant>> getRestaurantsByMinRating(@PathVariable Double minRating) {
        List<Restaurant> restaurants = restaurantService.findRestaurantsByMinRating(minRating);
        if (restaurants.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(restaurants, HttpStatus.OK);
    }
}
