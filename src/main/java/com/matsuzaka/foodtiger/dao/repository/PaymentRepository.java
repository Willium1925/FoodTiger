package com.matsuzaka.foodtiger.dao.repository;

import com.matsuzaka.foodtiger.dao.entity.Payment;
import com.matsuzaka.foodtiger.dao.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderrId(Long orderrId);
    List<Payment> findByStatus(PaymentStatus status);
    Optional<Payment> findByTransactionId(String transactionId);
}
