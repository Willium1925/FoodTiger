package com.matsuzaka.foodtiger.exception;

public class OrderNotFoundException extends ResourceNotFoundException {
    public OrderNotFoundException(String message) {
        super(message);
    }
}
