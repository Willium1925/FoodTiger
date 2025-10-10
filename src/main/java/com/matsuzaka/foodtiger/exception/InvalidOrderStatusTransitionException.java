package com.matsuzaka.foodtiger.exception;

public class InvalidOrderStatusTransitionException extends InvalidOperationException {
    public InvalidOrderStatusTransitionException(String message) {
        super(message);
    }
}
