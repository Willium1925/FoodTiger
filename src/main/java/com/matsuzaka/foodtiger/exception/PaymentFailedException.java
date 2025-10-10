package com.matsuzaka.foodtiger.exception;

public class PaymentFailedException extends InvalidOperationException {
    public PaymentFailedException(String message) {
        super(message);
    }
}
