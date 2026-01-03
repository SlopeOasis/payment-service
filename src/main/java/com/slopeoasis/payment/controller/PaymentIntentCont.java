package com.slopeoasis.payment.controller;

import java.math.BigInteger;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.slopeoasis.payment.entity.PaymentIntent;
import com.slopeoasis.payment.service.PaymentIntentServ;

@RestController
@RequestMapping("/paymentIntents")
public class PaymentIntentCont {
    private final PaymentIntentServ paymentIntentServ;

    public PaymentIntentCont(PaymentIntentServ paymentIntentServ) {
        this.paymentIntentServ = paymentIntentServ;
    }

    @Operation(summary = "Create payment intent")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Intent created"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", ref = "#/components/responses/UnauthorizedError")
    })
    @PostMapping("/intent")
    public ResponseEntity<PaymentIntentResponse> createIntent(
            @RequestBody CreateIntentRequest request,
            @RequestAttribute(name = "X-User-Id", required = false) String buyerClerkId,
            @RequestHeader(name = "Authorization", required = false) String authorization
    ) {
        if (buyerClerkId == null) {
            return ResponseEntity.status(401).build();
        }

        if (request == null || request.getPostId() == null) {
            return ResponseEntity.badRequest().build();
        }

        PaymentIntent intent =
                paymentIntentServ.createIntent(
                request.getPostId(),
            buyerClerkId,
            authorization
                );

        return ResponseEntity.ok(new PaymentIntentResponse(
                intent.getId(),
                137,
                "POL",
                intent.getAmountWei(),
                paymentIntentServ.getSellerPolygonWalletAddress(intent.getSellerClerkId())
        ));
    }






    
    public static class CreateIntentRequest {
        private Integer postId;

        public CreateIntentRequest() {}

        public CreateIntentRequest(Integer postId) {
            this.postId = postId;
        }

        public Integer getPostId() {
            return postId;
        }

        public void setPostId(Integer postId) {
            this.postId = postId;
        }
    }

    public static class PaymentIntentResponse {
        private final UUID paymentId;
        private final int chainId;
        private final String currency;
        @JsonSerialize(using = ToStringSerializer.class)
        private final BigInteger amountWei;
        private final String sellerWalletAddress;

        public PaymentIntentResponse(
                UUID paymentId,
                int chainId,
                String currency,
                BigInteger amountWei,
                String sellerWalletAddress
        ) {
            this.paymentId = paymentId;
            this.chainId = chainId;
            this.currency = currency;
            this.amountWei = amountWei;
            this.sellerWalletAddress = sellerWalletAddress;
        }

        public UUID getPaymentId() {
            return paymentId;
        }

        public int getChainId() {
            return chainId;
        }

        public String getCurrency() {
            return currency;
        }

        public BigInteger getAmountWei() {
            return amountWei;
        }

        public String getSellerWalletAddress() {
            return sellerWalletAddress;
        }
    }
}
