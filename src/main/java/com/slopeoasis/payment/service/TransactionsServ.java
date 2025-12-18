package com.slopeoasis.payment.service;
import java.util.List;

import org.springframework.stereotype.Service;

import com.slopeoasis.payment.entity.Transactions;
import com.slopeoasis.payment.repository.TransactionsRepo;



@Service
public class TransactionsServ {

    private final TransactionsRepo repo;

    public TransactionsServ(TransactionsRepo repo) {
        this.repo = repo;
    }

    public List<Transactions> getMyTransactions(String buyerClerkId) {
        return repo.findByBuyerClerkIdOrderByCreatedAtDesc(buyerClerkId);
    }
}
