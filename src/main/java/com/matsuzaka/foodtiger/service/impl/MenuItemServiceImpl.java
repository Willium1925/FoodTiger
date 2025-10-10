package com.matsuzaka.foodtiger.service.impl;

import com.matsuzaka.foodtiger.dao.entity.MenuItem;
import com.matsuzaka.foodtiger.dao.repository.MenuItemRepository;
import com.matsuzaka.foodtiger.service.MenuItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MenuItemServiceImpl implements MenuItemService {

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Override
    public List<MenuItem> findAllMenuItems() {
        return menuItemRepository.findAll();
    }

    @Override
    public Optional<MenuItem> findMenuItemById(Long id) {
        return menuItemRepository.findById(id);
    }

    @Override
    public MenuItem saveMenuItem(MenuItem menuItem) {
        return menuItemRepository.save(menuItem);
    }

    @Override
    public void deleteMenuItem(Long id) {
        menuItemRepository.deleteById(id);
    }

    @Override
    public List<MenuItem> findMenuItemsByRestaurantId(Long restaurantId) {
        return menuItemRepository.findByRestaurantId(restaurantId);
    }

    @Override
    public List<MenuItem> findAvailableMenuItemsByRestaurantId(Long restaurantId) {
        return menuItemRepository.findByRestaurantIdAndAvailableTrue(restaurantId);
    }

    @Override
    public List<MenuItem> searchMenuItemsByTitle(String title) {
        return menuItemRepository.findByTitleContainingIgnoreCase(title);
    }
}
