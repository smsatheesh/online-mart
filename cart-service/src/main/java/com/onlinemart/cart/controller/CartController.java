package com.onlinemart.cart.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.onlinemart.cart.dto.request.CreateCartRequestDto;
import com.onlinemart.cart.dto.request.UpdateCartItemRequestDto;
import com.onlinemart.cart.dto.request.CreateCartItemRequestDto;
import com.onlinemart.cart.dto.response.CartResponseDto;
import com.onlinemart.cart.dto.response.CartDetailResponseDto;
import com.onlinemart.cart.dto.response.ErrorResponseDto;
import com.onlinemart.cart.service.CartService;

@RestController
@RequestMapping("/api/carts")
@Validated
@Tag(name = "Cart", description = "Operations related to customer carts")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // internal implementation used by the mapped endpoint
    private ResponseEntity<CartResponseDto> createCartInternal(CreateCartRequestDto request) {
        CartResponseDto response = cartService.saveCart(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Create a cart for a customer", description = "Creates a cart for given customer and returns cart meta information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cart created",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CartResponseDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))})
    @PostMapping
    public ResponseEntity<CartResponseDto> createCartSwagger(@RequestBody CreateCartRequestDto request) {
        // delegate to internal implementation
        return createCartInternal(request);
    }

    @Operation(summary = "Fetch cart details for a customer", description = "Fetches cart details (without items) for a given customer id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cart fetched",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CartDetailResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Cart Not Found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))})
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<CartDetailResponseDto> fetchCart(@PathVariable Long customerId) {
        CartDetailResponseDto response = cartService.fetchDetails(customerId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Add an item to a cart", description = "Adds an item to the specified cart and returns updated cart payload")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Item added",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CartResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Cart or Product Not Found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))})
    @PostMapping("/{cartId}/items")
    public ResponseEntity<CartResponseDto> addItemToCart(@PathVariable Long cartId,
                                                         @RequestBody CreateCartItemRequestDto request) {
        // ensure path cartId is set on request DTO
        request.setCartId(cartId);
        CartResponseDto response = cartService.saveCartItems(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Update an item quantity in the cart", description = "Updates an item to the specified product in the cart items")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Item updated",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CartResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Cart or Product Not Found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))})
    @PutMapping("/{cartId}/items/{itemId}")
    public ResponseEntity<CartResponseDto> updateItemInCart(@PathVariable Long cartId,
                                                            @PathVariable Long itemId,
                                                            @RequestBody UpdateCartItemRequestDto request) {
        request.setCartId(cartId);
        request.setItemId(itemId);
        CartResponseDto response = cartService.updateCartItems(request);
        return ResponseEntity.ok(response);
    }

}