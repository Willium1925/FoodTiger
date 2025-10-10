package com.matsuzaka.foodtiger.service;

import com.matsuzaka.foodtiger.dao.entity.Payment;
import com.matsuzaka.foodtiger.dao.entity.PaymentStatus;
import com.matsuzaka.foodtiger.dto.PaymentRequest;
import com.matsuzaka.foodtiger.exception.InvalidOperationException;
import com.matsuzaka.foodtiger.exception.PaymentFailedException;
import com.matsuzaka.foodtiger.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;

public interface PaymentService {
    List<Payment> findAllPayments();
    Optional<Payment> findPaymentById(Long id);
    Payment savePayment(Payment payment);
    void deletePayment(Long id);
    Optional<Payment> findPaymentByOrderrId(Long orderrId);
    List<Payment> findPaymentsByStatus(PaymentStatus status);
    Optional<Payment> findPaymentByTransactionId(String transactionId);
    Payment processPayment(PaymentRequest request) throws ResourceNotFoundException, InvalidOperationException, PaymentFailedException; // New method
}