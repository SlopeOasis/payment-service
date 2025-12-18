package com.slopeoasis.payment.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class UserClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${USER_SERVICE_BASE_URL:http://localhost:8080}")
    private String userBaseUrl;

    public String getPolygonWalletAddress(String clerkId) {
        // user-service endpoint name is "pol-wallet-addres" (typo kept for compatibility)
        String url = userBaseUrl + "/users/public/pol-wallet-addres?clerkId=" + clerkId;
        try {
            String body = restTemplate.getForObject(url, String.class);
            if (body == null) return null;
            String wallet = body.replace("\"", "").trim();
            return wallet.isEmpty() ? null : wallet;
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        }
    }
}
