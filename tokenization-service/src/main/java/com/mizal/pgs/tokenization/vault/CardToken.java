package com.mizal.pgs.tokenization.vault;

import com.mizal.pgs.tokenization.card.CardBrand;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.YearMonth;

@Entity
@Table(name = "card_tokens")
public class CardToken {

    @Id
    private String token;

    @Column(name = "encrypted_pan", nullable = false)
    private String encryptedPan;

    @Column(nullable = false)
    private String fingerprint;

    @Column(nullable = false)
    private String last4;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardBrand brand;

    @Column(name = "expiry_month", nullable = false)
    private int expiryMonth;

    @Column(name = "expiry_year", nullable = false)
    private int expiryYear;

    @Column(name = "cardholder_name")
    private String cardholderName;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected CardToken() {
    }

    public CardToken(String token, String encryptedPan, String fingerprint, String last4, CardBrand brand,
                     int expiryMonth, int expiryYear, String cardholderName) {
        this.token = token;
        this.encryptedPan = encryptedPan;
        this.fingerprint = fingerprint;
        this.last4 = last4;
        this.brand = brand;
        this.expiryMonth = expiryMonth;
        this.expiryYear = expiryYear;
        this.cardholderName = cardholderName;
        this.createdAt = Instant.now();
    }

    public boolean isExpired(YearMonth now) {
        return YearMonth.of(expiryYear, expiryMonth).isBefore(now);
    }

    public String getToken() {
        return token;
    }

    public String getEncryptedPan() {
        return encryptedPan;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public String getLast4() {
        return last4;
    }

    public CardBrand getBrand() {
        return brand;
    }

    public int getExpiryMonth() {
        return expiryMonth;
    }

    public int getExpiryYear() {
        return expiryYear;
    }

    public String getCardholderName() {
        return cardholderName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
