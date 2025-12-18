package com.slopeoasis.payment.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.protocol.Web3j;

import com.slopeoasis.payment.entity.PaymentIntent;
import com.slopeoasis.payment.entity.PaymentIntent.PaymentStatus;
import com.slopeoasis.payment.entity.Transactions;
import com.slopeoasis.payment.repository.PaymentIntentRepo;
import com.slopeoasis.payment.repository.TransactionsRepo;

@Service
public class PaymentVerificationServ {

    public enum Result { PAID, PENDING, FAILED }

    private final PaymentIntentRepo intentRepo;
    private final TransactionsRepo txRepo;
    private final Web3j web3j;
    private final UserClient userClient;
    private final PostClient postClient;

    public PaymentVerificationServ(
            PaymentIntentRepo intentRepo,
            TransactionsRepo txRepo,
            Web3j web3j,
            UserClient userClient,
            PostClient postClient
    ) {
        this.intentRepo = intentRepo;
        this.txRepo = txRepo;
        this.web3j = web3j;
        this.userClient = userClient;
        this.postClient = postClient;
    }

    @Transactional
    public Result verifyAndFinalize(UUID paymentId, String txHash, String buyerClerkId) throws IOException {

        PaymentIntent intent = intentRepo.findById(paymentId)
                .orElseThrow(() -> new IllegalStateException("Payment intent not found"));

        // idempotent: if already paid, OK
        if (intent.getStatus() == PaymentStatus.PAID) {
            return Result.PAID;
        }

        // buyer must match intent
        if (!buyerClerkId.equals(intent.getBuyerClerkId())) {
            throw new IllegalStateException("Buyer mismatch");
        }

        // prevent txHash reuse
        if (txRepo.existsByTxHash(txHash)) {
            throw new IllegalStateException("txHash already used");
        }

        // wallets from user service (account-bound model)
        String buyerWalletAddress = userClient.getPolygonWalletAddress(buyerClerkId);
        if (buyerWalletAddress == null) throw new IllegalStateException("Buyer wallet not verified");

        String sellerWalletAddress = userClient.getPolygonWalletAddress(intent.getSellerClerkId());
        if (sellerWalletAddress == null) throw new IllegalStateException("Seller wallet not verified");

        // fetch tx
        var txOpt = web3j.ethGetTransactionByHash(txHash).send().getTransaction();
        if (txOpt.isEmpty()) throw new IllegalStateException("Transaction not found");

        var tx = txOpt.get();

        // validate from/to/value
        if (!tx.getFrom().equalsIgnoreCase(buyerWalletAddress)) {
            throw new IllegalStateException("Sender mismatch");
        }

        if (tx.getTo() == null || !tx.getTo().equalsIgnoreCase(sellerWalletAddress)) {
            throw new IllegalStateException("Recipient mismatch");
        }

        if (!tx.getValue().equals(intent.getAmountWei())) {
            throw new IllegalStateException("Amount mismatch");
        }

        // receipt (mined?)
        var receiptOpt = web3j.ethGetTransactionReceipt(txHash).send().getTransactionReceipt();
        if (receiptOpt.isEmpty()) {
            // not mined yet
            return Result.PENDING;
        }

        var receipt = receiptOpt.get();

        if (receipt.getStatus() == null || !"0x1".equalsIgnoreCase(receipt.getStatus())) {
            intent.setStatus(PaymentStatus.FAILED);
            intentRepo.save(intent);
            return Result.FAILED;
        }

        // create immutable transaction record
        Transactions txEntity = new Transactions();
        txEntity.setBuyerClerkId(intent.getBuyerClerkId());
        txEntity.setBuyerWallet(tx.getFrom());
        txEntity.setSellerClerkId(intent.getSellerClerkId());
        txEntity.setSellerWallet(tx.getTo());
        txEntity.setUsdAmount(intent.getUsdAmount());

        // wei -> POL decimal (for logging only)
        BigDecimal polAmount = new BigDecimal(intent.getAmountWei()).movePointLeft(18);
        txEntity.setPolAmount(polAmount);

        txEntity.setPostId(intent.getPostId());
        txEntity.setChainId(intent.getChainId());
        txEntity.setTransactionId(txHash);
        txEntity.setPaymentIntentId(intent.getId());
        txRepo.save(txEntity);

        // mark paid
        intent.setStatus(PaymentStatus.PAID);
        intentRepo.save(intent);

        // grant access
        postClient.grantAccess(intent.getPostId(), buyerClerkId, intent.getId());

        return Result.PAID;
    }
}
