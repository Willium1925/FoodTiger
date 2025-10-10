package com.matsuzaka.foodtiger.service;

import com.matsuzaka.foodtiger.dao.entity.MenuItem;

import java.util.List;
import java.util.Optional;

public interface MenuItemService {
    List<MenuItem> findAllMenuItems();
    Optional<MenuItem> findMenuItemById(Long id);
    MenuItem saveMenuItem(MenuItem menuItem);
    void deleteMenuItem(Long id);
    List<MenuItem> findMenuItemsByRestaurantId(Long restaurantId);
    List<MenuItem> findAvailableMenuItemsByRestaurantId(Long restaurantId);
    List<MenuItem> searchMenuItemsByTitle(String title);
}
