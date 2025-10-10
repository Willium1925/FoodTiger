package com.matsuzaka.foodtiger.exception;

public class UserAlreadyExistsException extends FoodTigerException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
