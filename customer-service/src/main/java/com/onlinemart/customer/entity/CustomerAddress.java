package com.onlinemart.customer.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "customer_addresses")
public class CustomerAddress extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id", nullable = false)
    private Long addressId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "address_type", nullable = false)
    private String addressType;

    @Column(name = "address_first_line", nullable = false)
    private String addressFirstLine;

    @Column(name = "address_second_line", nullable = true)
    private String addressSecondLine;

    @Column(name = "landmark", nullable = true)
    private String landmark;

    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "state", nullable = false)
    private String state;

    @Column(name = "country", nullable = false)
    private String country;

}