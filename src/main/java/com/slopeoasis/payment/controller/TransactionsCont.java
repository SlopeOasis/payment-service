package com.slopeoasis.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import com.slopeoasis.payment.service.TransactionsServ;

@RestController
@RequestMapping("/transactions")
public class TransactionsCont {
    private final TransactionsServ transactionsServ;

    public TransactionsCont(TransactionsServ transactionsServ) {
        this.transactionsServ = transactionsServ;
    }

    @Operation(summary = "Get my transactions")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "401", ref = "#/components/responses/UnauthorizedError")
    })
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
