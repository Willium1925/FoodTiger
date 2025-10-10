package com.matsuzaka.foodtiger.dao.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;
import java.util.List; // Import List

@Entity
@Table(name = "orderr")
@Data
public class Orderr {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_person_id")
    private User deliveryPerson; // Nullable

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_address_id", nullable = false)
    private Address deliveryAddress;

    @Column(name = "total_amount", nullable = false)
    private Integer totalAmount;

    @Column(name = "delivery_fee")
    private Integer deliveryFee = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.處理中;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "orderr_time", updatable = false)
    private Date orderrTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "estimated_delivery_time")
    private Date estimatedDeliveryTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "completed_time")
    private Date completedTime;

    @Column(name = "rating")
    private Integer rating; // CHECK (rating BETWEEN 1 AND 5) - handled by validation in service/controller

    // 新增：與 OrderItem 的一對多關係
    @OneToMany(mappedBy = "orderr", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;
}