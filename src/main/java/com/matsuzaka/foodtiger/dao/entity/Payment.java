package com.matsuzaka.foodtiger.dao.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Entity
@Table(name = "payment")
@Data
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderr_id", nullable = false, unique = true)
    private Orderr orderr;

    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.處理中;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "payment_time", updatable = false)
    private Date paymentTime;
}
