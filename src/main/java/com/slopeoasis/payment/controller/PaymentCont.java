package com.slopeoasis.payment.controller;

import java.io.IOException;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import com.slopeoasis.payment.service.PaymentVerificationServ;

@RestController
@RequestMapping("/payments")
public class PaymentCont {

    private final PaymentVerificationServ verificationService;

    public PaymentCont(PaymentVerificationServ verificationService) {
        this.verificationService = verificationService;
    }

    public record ConfirmRequest(UUID paymentId, String txHash) {}

    @Operation(summary = "Confirm payment")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Payment confirmed"),
        @ApiResponse(responseCode = "202", description = "Transaction pending"),
        @ApiResponse(responseCode = "400", description = "Transaction failed or invalid"),
        @ApiResponse(responseCode = "401", ref = "#/components/responses/UnauthorizedError"),
        @ApiResponse(responseCode = "500", description = "IO error while verifying transaction")
    })
    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(
            @RequestBody ConfirmRequest req,
            @RequestAttribute(name = "X-User-Id", required = false) String buyerClerkId
    ) {
        if (buyerClerkId == null) return ResponseEntity.status(401).build();

        try {
            // If tx is not mined yet, you can return 202 (better UX)
            PaymentVerificationServ.Result result = verificationService.verifyAndFinalize(req.paymentId(), req.txHash(), buyerClerkId);

            return switch (result) {
                case PAID -> ResponseEntity.ok().build();
                case PENDING -> ResponseEntity.status(202).body("TX_PENDING");
                case FAILED -> ResponseEntity.status(400).body("TX_FAILED");
            };
        } catch (IOException e) {
            return ResponseEntity.status(500).body("IO_ERROR");
        }
    }
}
