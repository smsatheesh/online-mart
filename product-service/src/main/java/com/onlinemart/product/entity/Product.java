package com.onlinemart.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.onlinemart.product.entity.BaseAuditEntity;

@Getter
@Setter
@Entity
@Table(name = "products")
public class Product extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "price", nullable = false)
    private Long price;

    @Column(name = "stock_quantity", nullable = false)
    private Long availableStockQuantity;

    @Column(name = "thumbnail_url", nullable = false)
    private String thumbnailUrl;

    @Column(name = "status", nullable = true)
    private Boolean status;
}