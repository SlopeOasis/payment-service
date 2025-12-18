package com.slopeoasis.payment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.slopeoasis.payment.entity.Transactions;

@Repository
public interface TransactionsRepo extends JpaRepository<Transactions, Long> {
    List<Transactions> findByBuyerClerkIdOrderByCreatedAtDesc(String buyerClerkId);
    List<Transactions> findBySellerClerkIdOrderByCreatedAtDesc(String sellerClerkId);
    List<Transactions> findByPostIdOrderByCreatedAtDesc(Integer postId);
    Transactions findByTxHash(String txHash);
    boolean existsByTxHash(String txHash);
}
