package com.onlinemart.customer.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

@Getter
@Setter
@Entity
@Table(name = "customer_details")
public class CustomerDetails extends BaseAuditEntity implements Persistable<Long> {

    @Id
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "password_hash", nullable = false)
    private String password;

    @Column(name = "profile_image_url")
    private String profilePic;

    @Column(name = "is_email_verified", nullable = false)
    private Boolean isEmailVerified;

    @Column(name = "is_phone_verified", nullable = false)
    private Boolean isPhoneVerified;

    @Column(name = "account_status")
    private String accountStatus;

    @Transient
    private boolean isNew = true;

    @Override
    public Long getId() {
        return customerId;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostPersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }
}