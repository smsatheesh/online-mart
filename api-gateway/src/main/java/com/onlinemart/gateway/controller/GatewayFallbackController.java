package com.onlinemart.gateway.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class GatewayFallbackController {

    private static final Logger log = LoggerFactory.getLogger(GatewayFallbackController.class);

    @RequestMapping("/fallback/product")
    public ResponseEntity<Map<String, String>> productFallback() {
        log.error("Gateway circuit breaker OPEN — product-service unavailable");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "SERVICE_UNAVAILABLE",
                        "message", "Product service is temporarily unavailable. Please try again later.",
                        "service", "product-service"
                ));
    }

    @RequestMapping("/fallback/order")
    public ResponseEntity<Map<String, String>> orderFallback() {
        log.error("Gateway circuit breaker OPEN — order-service unavailable");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "SERVICE_UNAVAILABLE",
                        "message", "Order service is temporarily unavailable. Please try again later.",
                        "service", "order-service"
                ));
    }

    @RequestMapping("/fallback/cart")
    public ResponseEntity<Map<String, String>> cartFallback() {
        log.error("Gateway circuit breaker OPEN — cart-service unavailable");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "SERVICE_UNAVAILABLE",
                        "message", "Cart service is temporarily unavailable. Please try again later.",
                        "service", "cart-service"
                ));
    }
}