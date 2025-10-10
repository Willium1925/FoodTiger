package com.matsuzaka.foodtiger.controller;

import com.matsuzaka.foodtiger.dao.entity.MenuItem;
import com.matsuzaka.foodtiger.service.MenuItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu-items")
public class MenuItemController {

    @Autowired
    private MenuItemService menuItemService;

    @GetMapping
    public ResponseEntity<List<MenuItem>> getAllMenuItems() {
        List<MenuItem> menuItems = menuItemService.findAllMenuItems();
        return new ResponseEntity<>(menuItems, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuItem> getMenuItemById(@PathVariable Long id) {
        return menuItemService.findMenuItemById(id)
                .map(menuItem -> new ResponseEntity<>(menuItem, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<MenuItem> createMenuItem(@RequestBody MenuItem menuItem) {
        MenuItem savedMenuItem = menuItemService.saveMenuItem(menuItem);
        return new ResponseEntity<>(savedMenuItem, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MenuItem> updateMenuItem(@PathVariable Long id, @RequestBody MenuItem menuItem) {
        return menuItemService.findMenuItemById(id)
                .map(existingMenuItem -> {
                    existingMenuItem.setRestaurant(menuItem.getRestaurant());
                    existingMenuItem.setTitle(menuItem.getTitle());
                    existingMenuItem.setDescription(menuItem.getDescription());
                    existingMenuItem.setPrice(menuItem.getPrice());
                    existingMenuItem.setImageUrl(menuItem.getImageUrl());
                    existingMenuItem.setAvailable(menuItem.getAvailable());
                    MenuItem updatedMenuItem = menuItemService.saveMenuItem(existingMenuItem);
                    return new ResponseEntity<>(updatedMenuItem, HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long id) {
        if (menuItemService.findMenuItemById(id).isPresent()) {
            menuItemService.deleteMenuItem(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<MenuItem>> getMenuItemsByRestaurantId(@PathVariable Long restaurantId) {
        List<MenuItem> menuItems = menuItemService.findMenuItemsByRestaurantId(restaurantId);
        if (menuItems.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(menuItems, HttpStatus.OK);
    }

    @GetMapping("/restaurant/{restaurantId}/available")
    public ResponseEntity<List<MenuItem>> getAvailableMenuItemsByRestaurantId(@PathVariable Long restaurantId) {
        List<MenuItem> menuItems = menuItemService.findAvailableMenuItemsByRestaurantId(restaurantId);
        if (menuItems.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(menuItems, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<MenuItem>> searchMenuItems(@RequestParam String title) {
        List<MenuItem> menuItems = menuItemService.searchMenuItemsByTitle(title);
        if (menuItems.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(menuItems, HttpStatus.OK);
    }
}
