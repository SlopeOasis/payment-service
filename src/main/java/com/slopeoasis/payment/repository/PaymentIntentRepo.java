package com.slopeoasis.payment.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.slopeoasis.payment.entity.PaymentIntent;
import com.slopeoasis.payment.entity.PaymentIntent.PaymentStatus;

@Repository
public interface PaymentIntentRepo extends JpaRepository<PaymentIntent, UUID> {
    List<PaymentIntent> findByBuyerClerkIdOrderByCreatedAtDesc(String buyerClerkId);
    List<PaymentIntent> findBySellerClerkIdOrderByCreatedAtDesc(String sellerClerkId);
    List<PaymentIntent> findByPostIdOrderByCreatedAtDesc(Integer postId);
    List<PaymentIntent> findByStatus(PaymentStatus status);
}
