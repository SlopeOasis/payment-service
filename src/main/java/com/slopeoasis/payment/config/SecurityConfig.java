package com.slopeoasis.payment.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.slopeoasis.payment.interceptor.JwtInterceptor;

@Configuration
public class SecurityConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Apply JWT validation to all payment endpoints; controller reads X-User-Id via @RequestAttribute
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/paymentIntents/**", "/payments/**", "/transactions/**");
    }
}
