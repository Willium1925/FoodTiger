package com.matsuzaka.foodtiger.dao.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "orderr_item")
@Data
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderr_id", nullable = false)
    private Orderr orderr;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(name = "price_at_orderr", nullable = false)
    private Integer priceAtOrderr;
}
