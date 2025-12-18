package com.slopeoasis.payment.service;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.slopeoasis.payment.entity.PaymentIntent;
import com.slopeoasis.payment.entity.PaymentIntent.PaymentStatus;
import com.slopeoasis.payment.repository.PaymentIntentRepo;

@Service
public class PaymentIntentServ {
    private static final int POLYGON_CHAIN_ID = 137;
    private final PriceServ priceService;
    private final PaymentIntentRepo repo;
    private final RestTemplate restTemplate = new RestTemplate();

    private final String postApiUrl;
    private final String userApiUrl;

    public PaymentIntentServ(
            PriceServ priceService,
            PaymentIntentRepo repo,
            @Value("${POST_SERVICE_BASE_URL:http://localhost:8081}") String postApiUrl,
            @Value("${USER_SERVICE_BASE_URL:http://localhost:8080}") String userApiUrl) {
        this.priceService = priceService;
        this.repo = repo;
        this.postApiUrl = postApiUrl;
        this.userApiUrl = userApiUrl;
    }

    public PaymentIntent createIntent(Integer postId, String buyerClerkId, String authorizationHeader) {
        // Logic to create a payment intent
        BigDecimal polUsd = priceService.getPolUsdPrice();
        // dobi sellerClerkId iz posta preko post service
        String sellerClerkId = getSellerClerkIdFromPost(postId, authorizationHeader);
        // dobi seller polygon wallet verrified iz user service
        if(!getSellerPolygonVerified(sellerClerkId)) {
            throw new IllegalStateException("Seller's wallet is not polygon verified");
        }
        // dobi seller polygon wallet address iz user service
        // dobi usd price iz posta
        BigDecimal usdPricePost = BigDecimal.valueOf(getUsdPriceFromPost(postId, authorizationHeader));
        BigInteger amountWei = convertUsdToWei(usdPricePost, polUsd);

        PaymentIntent intent = new PaymentIntent();
        intent.setBuyerClerkId(buyerClerkId);
        intent.setSellerClerkId(sellerClerkId);
        intent.setPostId(postId);
        intent.setAmountWei(amountWei);
        intent.setUsdAmount(usdPricePost);
        intent.setChainId(POLYGON_CHAIN_ID);
        intent.setStatus(PaymentStatus.PENDING);

        return repo.save(intent);
    }






    // ostale
    // payment intent stuff
    public BigInteger convertUsdToWei(BigDecimal usdPrice, BigDecimal polUsdPrice) {
        BigDecimal polAmount = usdPrice.divide(
            polUsdPrice,
            18,
            RoundingMode.HALF_UP
        );

        return polAmount
            .multiply(BigDecimal.TEN.pow(18))
            .toBigIntegerExact();
    }

    public Double getUsdPriceFromPost(Integer postId, String authorizationHeader) {
        Map<?, ?> postMap = fetchPostMap(postId, authorizationHeader);
        Object price = postMap.get("priceUSD");
        if (price == null) throw new IllegalStateException("Post has no priceUSD");
        if (price instanceof Number numberPrice) return numberPrice.doubleValue();
        return Double.valueOf(price.toString());
    }

    public String getSellerClerkIdFromPost(Integer postId, String authorizationHeader) {
        Map<?, ?> postMap = fetchPostMap(postId, authorizationHeader);
        Object sellerId = postMap.get("sellerId");
        if (sellerId == null) throw new IllegalStateException("Post has no sellerId");
        return sellerId.toString();
    }

    private Map<?, ?> fetchPostMap(Integer postId, String authorizationHeader) {
        String protectedUrl = String.format("%s/posts/%d", postApiUrl, postId);

        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new IllegalStateException("Missing Authorization header for post-service request");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authorizationHeader);
        ResponseEntity<Map<String, Object>> resp = restTemplate.exchange(
            protectedUrl,
            HttpMethod.GET,
            new HttpEntity<>(headers),
            new ParameterizedTypeReference<Map<String, Object>>() {
            }
        );

        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            throw new IllegalStateException("Failed to fetch post from post-service");
        }

        Map<String, Object> body = Objects.requireNonNull(resp.getBody());
        Object postObj = body.get("post");
        if (!(postObj instanceof Map)) throw new IllegalStateException("Unexpected post response shape");
        return (Map<?, ?>) postObj;
    }

    public boolean getSellerPolygonVerified(String sellerClerkId) {
        // Use user-service public wallet address endpoint; if present -> verified
        String url = String.format("%s/users/public/pol-wallet-addres?clerkId=%s", userApiUrl, sellerClerkId);
        try {
            ResponseEntity<String> resp = restTemplate.getForEntity(url, String.class);
            String body = resp.getBody();
            return resp.getStatusCode().is2xxSuccessful() && body != null && !body.trim().isEmpty();
        } catch (RuntimeException e) {
            return false;
        }
    }

    public String getSellerPolygonWalletAddress(String sellerClerkId) {
        // Calls user-service public wallet address endpoint
        String url = String.format("%s/users/public/pol-wallet-addres?clerkId=%s", userApiUrl, sellerClerkId);
        ResponseEntity<String> resp = restTemplate.getForEntity(url, String.class);
        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            throw new IllegalStateException("Failed to fetch seller's wallet address from user-service");
        }
        return resp.getBody();
    }
}