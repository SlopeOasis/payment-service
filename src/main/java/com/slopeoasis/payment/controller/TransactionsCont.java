package com.slopeoasis.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.slopeoasis.payment.service.TransactionsServ;

@RestController
@RequestMapping("/transactions")
public class TransactionsCont {
    private final TransactionsServ transactionsServ;

    public TransactionsCont(TransactionsServ transactionsServ) {
        this.transactionsServ = transactionsServ;
    }

    @GetMapping("/me")
    public ResponseEntity<?> myTransactions(
            @RequestAttribute(name = "X-User-Id", required = false) String buyerClerkId
    ) {
        if (buyerClerkId == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(
                transactionsServ.getMyTransactions(buyerClerkId)
        );
    }
}
