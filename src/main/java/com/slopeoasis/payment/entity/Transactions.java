package com.slopeoasis.payment.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "transactions")
public class Transactions {
    // BUYER/BUYER_WALLET/SELLER/SELLER_WALLET/USD_AMOUNT/POL_AMOUNT_AT_TIME/TIMESTAMP/TRANSACTION_ID/POST_ID

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String buyerClerkId;

    @Column(nullable = false)
    private String buyerWallet;

    @Column(nullable = false)
    private String sellerClerkId;

    @Column(nullable = false)
    private String sellerWallet;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal usdAmount;

    @Column(nullable = false, precision = 36, scale = 18)
    private BigDecimal polAmount;

    @Column(nullable = false)
    private Integer postId;

    @Column(nullable = false)
    private Integer chainId; // 137

    @Column(nullable = false, unique = true)
    private String txHash;

    @Column(nullable = false)
    private UUID paymentIntentId;

    @CreationTimestamp
    private Instant createdAt;  

    public Transactions() {}

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBuyerClerkId() { return buyerClerkId; }
    public void setBuyerClerkId(String buyerClerkId) { this.buyerClerkId = buyerClerkId; }

    public String getBuyerWallet() { return buyerWallet; }
    public void setBuyerWallet(String buyerWallet) { this.buyerWallet = buyerWallet; }

    public String getSellerClerkId() { return sellerClerkId; }
    public void setSellerClerkId(String sellerClerkId) { this.sellerClerkId = sellerClerkId; }

    public String getSellerWallet() { return sellerWallet; }
    public void setSellerWallet(String sellerWallet) { this.sellerWallet = sellerWallet; }

    public BigDecimal getUsdAmount() { return usdAmount; }
    public void setUsdAmount(BigDecimal usdAmount) { this.usdAmount = usdAmount; }

    public BigDecimal getPolAmount() { return polAmount; }
    public void setPolAmount(BigDecimal polAmount) { this.polAmount = polAmount; }
    
    public Integer getPostId() { return postId; }
    public void setPostId(Integer postId) { this.postId = postId; }

    public Integer getChainId() { return chainId; }
    public void setChainId(Integer chainId) { this.chainId = chainId; }

    public UUID getPaymentIntentId() { return paymentIntentId; }
    public void setPaymentIntentId(UUID paymentIntentId) { this.paymentIntentId = paymentIntentId; }

    public Instant getCreatedAt() { return createdAt; }

    public String getTransactionId() { return txHash; }
    public void setTransactionId(String transactionId) { this.txHash = transactionId; }
}
