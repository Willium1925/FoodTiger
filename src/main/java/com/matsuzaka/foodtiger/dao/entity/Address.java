package com.matsuzaka.foodtiger.dao.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Entity
@Table(name = "address")
@Data
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // Can be null if it's a restaurant address

    @Column(name = "zip_code", length = 20)
    private String zipCode;

    @Column(nullable = false, length = 50)
    private String city;

    @Column(length = 50)
    private String district;

    @Column(nullable = false, length = 255)
    private String street;

    @Column(name = "extra_details", length = 50)
    private String extraDetails;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", updatable = false)
    private Date createdAt;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;
}
