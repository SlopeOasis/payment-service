package com.slopeoasis.payment.entity;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class PaymentIntent {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private Integer postId;
    @Column(nullable = false)
    private String buyerClerkId;
    @Column(nullable = false)
    private String sellerClerkId;
    @Column(nullable = false)
    private BigInteger amountWei;
    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal usdAmount;
    @Column(nullable = false)
    private Integer chainId; // Polygon = 137

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public enum PaymentStatus {
        PENDING,
        PAID,
        FAILED
    }

    public PaymentIntent() {}


    // getters and setters
    public UUID getId() {return id;}
    public void setId(UUID id) {this.id = id;}

    public Integer getPostId() {return postId;}
    public void setPostId(Integer postId) {this.postId = postId;}

    public String getBuyerClerkId() {return buyerClerkId;}
    public void setBuyerClerkId(String buyerClerkId) {this.buyerClerkId = buyerClerkId;}

    public String getSellerClerkId() {return sellerClerkId;}
    public void setSellerClerkId(String sellerClerkId) {this.sellerClerkId = sellerClerkId;}
    
    public BigInteger getAmountWei() {return amountWei;}
    public void setAmountWei(BigInteger amountWei) {this.amountWei = amountWei;}
    
    public BigDecimal getUsdAmount() {return usdAmount;}
    public void setUsdAmount(BigDecimal usdAmount) {this.usdAmount = usdAmount;}

    public Integer getChainId() {return chainId;}
    public void setChainId(Integer chainId) {this.chainId = chainId;}

    public PaymentStatus getStatus() {return status;}
    public void setStatus(PaymentStatus status) {this.status = status;}
    
    public Instant getCreatedAt() {return createdAt;}
}
