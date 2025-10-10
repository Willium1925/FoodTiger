package com.matsuzaka.foodtiger.exception;

public class FoodTigerException extends Exception {
    public FoodTigerException(String message) {
        super(message);
    }

    public FoodTigerException(String message, Throwable cause) {
        super(message, cause);
    }
}
