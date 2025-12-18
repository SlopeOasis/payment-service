package com.slopeoasis.payment.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PostClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${POST_SERVICE_BASE_URL:http://localhost:8081}")
    private String postBaseUrl;

    public record GrantAccessReq(String buyerClerkId, UUID paymentId) {}

    public void grantAccess(Integer postId, String buyerClerkId, UUID paymentId) {
        restTemplate.postForEntity(
                postBaseUrl + "/internal/posts/" + postId + "/grant-access",
                new GrantAccessReq(buyerClerkId, paymentId),
                Void.class
        );
    }
}
