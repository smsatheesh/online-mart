package com.onlinemart.product.controller;

import com.onlinemart.product.dto.request.CreateProductRequestDto;
import com.onlinemart.product.dto.request.UpdateProductRequestDto;
import com.onlinemart.product.dto.response.ProductResponseDto;
import com.onlinemart.product.dto.response.ErrorResponseDto;
import com.onlinemart.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Product", description = "Operations related to products in the online mart")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "Create a new product", description = "Creates a new product and returns created product details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Product created",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Bad request - validation failed",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("")
    public ResponseEntity<ProductResponseDto> createProduct(@Valid @RequestBody CreateProductRequestDto request) {
        ProductResponseDto response = productService.saveProduct(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing product", description = "Updates a product when id is provided and returns updated product details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product updated",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Bad request - validation failed",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDto> updateProduct(@PathVariable Long id,
                                                            @Valid @RequestBody UpdateProductRequestDto request) {
        request.setId(id);
        ProductResponseDto response = productService.saveProduct(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Fetching an product", description = "Fetching an product with its details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product fetched",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Product Not Found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))})
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> fetchProduct(@PathVariable Long id) {
        ProductResponseDto response = productService.fetchProduct(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Check product availability", description = "Returns inventory availability for a product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inventory fetched",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.onlinemart.product.dto.response.AvailabilityResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Product Not Found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))})
    @GetMapping("/{id}/availability")
    public ResponseEntity<com.onlinemart.product.dto.response.AvailabilityResponseDto> checkAvailability(@PathVariable Long id) {
        com.onlinemart.product.dto.response.AvailabilityResponseDto response = productService.checkAvailability(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
