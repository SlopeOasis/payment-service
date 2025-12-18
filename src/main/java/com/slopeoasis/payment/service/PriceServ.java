package com.slopeoasis.payment.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PriceServ {
    private static final String COINGECKO_ID = "polygon-ecosystem-token"; // POL

    private static final long CACHE_DURATION_MS = 60 * 1000; // 1 minute

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${COINGECKO_BASE_URL:https://api.coingecko.com/api/v3}")
    private String baseUrl;

    // CoinGecko demo key header (matches your snippet). If you switch to paid, set to x-cg-pro-api-key.
    @Value("${COINGECKO_API_KEY_HEADER:x-cg-demo-api-key}")
    private String apiKeyHeader;

    @Value("${COINGECKO_API_KEY:}")
    private String apiKey;

    private volatile BigDecimal cachedPrice;
    private volatile long lastFetchTimeMs;

    public BigDecimal getPolUsdPrice() {
        long now = System.currentTimeMillis();
        String url = String.format("%s/simple/price?vs_currencies=usd&ids=%s", baseUrl, COINGECKO_ID);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        if (apiKey != null && !apiKey.isBlank()) {
            headers.set(apiKeyHeader, apiKey);
        }

        try {
            ResponseEntity<Map<String, Object>> resp = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    }
            );
            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                return fallbackOrThrow("CoinGecko non-2xx response: " + resp.getStatusCode());
            }

            Map<String, Object> response = Objects.requireNonNull(resp.getBody());
            // CoinGecko errors often return: {"status": {"error_code": 429, ...}}
            if (response.containsKey("status")) {
                return fallbackOrThrow("CoinGecko error payload returned (status present)");
            }

            BigDecimal price = null;
            Object tokenObj = response.get(COINGECKO_ID);
            if (tokenObj instanceof Map) {
                Object usdObj = ((Map<?, ?>) tokenObj).get("usd");
                if (usdObj != null) {
                    price = new BigDecimal(usdObj.toString());
                }
            }

            if (price == null) {
                return fallbackOrThrow("USD price not found in CoinGecko response");
            }

            cachedPrice = price;
            lastFetchTimeMs = now;
            return price;
        } catch (RuntimeException e) {
            return fallbackOrThrow("CoinGecko request failed: " + e.getMessage());
        }
    }

    private BigDecimal fallbackOrThrow(String reason) {
        long ageMs = System.currentTimeMillis() - lastFetchTimeMs;
        if (cachedPrice != null && ageMs >= 0 && ageMs < CACHE_DURATION_MS) {
            System.err.println("[PriceServ] " + reason + "; using cached price " + cachedPrice);
            return cachedPrice;
        }

        String keyHint = (apiKey == null || apiKey.isBlank())
                ? "COINGECKO_API_KEY is not set"
                : "COINGECKO_API_KEY is set";

        String cacheHint = (cachedPrice == null)
                ? "no cached price available"
                : "cached price too old (ageMs=" + ageMs + ")";

        throw new IllegalStateException(reason + " (" + keyHint + ", " + cacheHint + ")");
    }
}
