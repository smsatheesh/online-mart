package com.onlinemart.cart.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.onlinemart.cart.client.dto.ProductResponseDto;

@FeignClient(name = "product-service", url = "${product.service.url}")
public interface ProductClient {

    @GetMapping("/api/products/{id}")
    ProductResponseDto fetchProduct(@PathVariable("id") Long id);

}
