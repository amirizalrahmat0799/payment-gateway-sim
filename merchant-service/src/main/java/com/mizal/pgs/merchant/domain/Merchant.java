package com.mizal.pgs.merchant.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "merchants")
public class Merchant {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MerchantStatus status;

    @Column(name = "fee_bps", nullable = false)
    private int feeBps;

    @Column(name = "api_key_prefix", nullable = false)
    private String apiKeyPrefix;

    @Column(name = "api_key_hash", nullable = false, unique = true)
    private String apiKeyHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Merchant() {
    }

    public Merchant(String name, String email, int feeBps, String apiKeyPrefix, String apiKeyHash) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.email = email;
        this.feeBps = feeBps;
        this.apiKeyPrefix = apiKeyPrefix;
        this.apiKeyHash = apiKeyHash;
        this.status = MerchantStatus.ACTIVE;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void changeStatus(MerchantStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = Instant.now();
    }

    public void rotateApiKey(String prefix, String hash) {
        this.apiKeyPrefix = prefix;
        this.apiKeyHash = hash;
        this.updatedAt = Instant.now();
    }

    public boolean isActive() {
        return status == MerchantStatus.ACTIVE;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public MerchantStatus getStatus() {
        return status;
    }

    public int getFeeBps() {
        return feeBps;
    }

    public String getApiKeyPrefix() {
        return apiKeyPrefix;
    }

    public String getApiKeyHash() {
        return apiKeyHash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
