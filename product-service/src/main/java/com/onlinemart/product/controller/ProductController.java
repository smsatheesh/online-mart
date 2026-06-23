package com.onlinemart.product.controller;

import com.onlinemart.product.dto.request.SaveProductRequestDto;
import com.onlinemart.product.dto.response.SaveProductResponseDto;
import com.onlinemart.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<SaveProductResponseDto> createProduct(@Valid @RequestBody SaveProductRequestDto request) {
        SaveProductResponseDto response = productService.saveProduct(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

}
