package com.onlinemart.cart.service;

import org.springframework.stereotype.Service;

import com.onlinemart.cart.dto.request.CartRequestDto;
import com.onlinemart.cart.dto.request.CreateCartItemRequestDto;
import com.onlinemart.cart.dto.request.UpdateCartItemRequestDto;
import com.onlinemart.cart.dto.response.CartResponseDto;
import com.onlinemart.cart.dto.response.CartDataDto;
import com.onlinemart.cart.dto.response.CartDetailResponseDto;
import com.onlinemart.cart.dto.response.CartItemsDataDto;
import com.onlinemart.cart.dto.response.ErrorResponseDto;
import com.onlinemart.cart.entity.Cart;
import com.onlinemart.cart.entity.CartItems;
import com.onlinemart.cart.exception.CartServiceException;
import com.onlinemart.cart.mapper.CartMapper;
import com.onlinemart.cart.repository.CartRepository;
import com.onlinemart.cart.repository.CartItemRepository;
import com.onlinemart.cart.client.ProductClient;
import com.onlinemart.cart.client.dto.ProductResponseDto;
import com.onlinemart.cart.client.dto.ProductDataDto;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import com.onlinemart.cart.dto.request.BrowseRequestDto;
import com.onlinemart.cart.dto.response.BrowseMetaDto;
import com.onlinemart.cart.dto.response.BrowseResponseDto;
import com.onlinemart.cart.dto.response.CartBrowseResponseDto;
import com.onlinemart.cart.helper.BrowseHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartMapper cartMapper;
    private final CartItemRepository cartItemRepository;
    private final ProductClient productClient;
    private static final Logger log = LoggerFactory.getLogger(CartServiceImpl.class);

    public CartServiceImpl(CartRepository cartRepository, CartMapper cartMapper,
                           CartItemRepository cartItemRepository, ProductClient productClient) {
        this.cartRepository = cartRepository;
        this.cartMapper = cartMapper;
        this.cartItemRepository = cartItemRepository;
        this.productClient = productClient;
    }

    @Override
    public CartResponseDto saveCart(CartRequestDto requestDto) {
        try {
            Cart existingCart = cartRepository.findByCustomerIdAndPlatform(requestDto.getCustomerId(), requestDto.getPlatform());
            if (existingCart != null) {
                log.error("Cart already exists for platform {}", requestDto.getPlatform());
                ErrorResponseDto error = ErrorResponseDto.builder()
                        .success(Boolean.FALSE)
                        .message("Cart exists for platform " + "'" + requestDto.getPlatform() + "'" + " for the customer")
                        .errorCode("CART_ALREADY_EXISTS")
                        .build();
                throw new CartServiceException(error);
            }

            Cart cart = cartMapper.toEntity(requestDto);
            Cart saved = cartRepository.save(cart);
            return cartMapper.toSaveResponseDto(saved);
        } catch (CartServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            ErrorResponseDto error = ErrorResponseDto.builder()
                    .success(Boolean.FALSE)
                    .message("Failed to save cart")
                    .errorCode("CART_SAVE_ERROR")
                    .build();
            throw new CartServiceException(error);
        }
    }

    @Override
    public CartDetailResponseDto fetchDetails(Long customerId) {
        List<Cart> carts;
        try {
            carts = cartRepository.findByCustomerId(customerId);
        } catch (Exception ex) {
            log.error("Failed to fetch cart", ex.getMessage(), ex);
            ErrorResponseDto error = ErrorResponseDto.builder()
                    .success(Boolean.FALSE)
                    .message("Failed to fetch cart")
                    .errorCode("CART_FETCH_FAILED")
                    .build();
            throw new CartServiceException(error);
        }

        if (carts.isEmpty()) {
            ErrorResponseDto error = ErrorResponseDto.builder()
                    .success(Boolean.FALSE)
                    .message("Cart not found for the customer: " + customerId)
                    .errorCode("CART_NOT_FOUND")
                    .build();
            throw new CartServiceException(error);
        }

        return cartMapper.toDetailResponse(carts);
    }

    @Override
    public CartResponseDto saveCartItems(CreateCartItemRequestDto requestDto) {
        try {
            if (requestDto == null) {
                throw buildException("Invalid request", "INVALID_REQUEST");
            }

            Long cartId = requestDto.getCartId();
            if (cartId == null) {
                throw buildException("Cart id is required", "CART_ID_REQUIRED");
            }

            Cart cart = cartRepository.findById(cartId)
                    .orElseThrow(() -> buildException("Cart not found for id: " + cartId, "CART_NOT_FOUND"));

            Long productId = requestDto.getProductId();
            ProductResponseDto prodResp = productClient.fetchProduct(productId);
            if (prodResp == null || prodResp.getData() == null) {
                throw buildException("Product not found for id: " + productId, "PRODUCT_NOT_FOUND");
            }

            ProductDataDto prod = prodResp.getData();
            Long price = prod.getPrice();
            if (price == null) {
                throw buildException("Product price missing for id: " + productId, "PRODUCT_PRICE_MISSING");
            }

            CartItems item = new CartItems();
            item.setCartId(cartId);
            item.setProductId(productId);
            item.setQuantity(requestDto.getQuantity());
            item.setUnitPrice(price);

            cartItemRepository.save(item);

            // Fetch all items for this cart and calculate total
            CartDataDto data = fetchAllItems(cartId, cart);

            return CartResponseDto.builder()
                    .success(Boolean.TRUE)
                    .message("Item added to cart successfully")
                    .data(data)
                    .build();

        } catch (CartServiceException ex) {
            throw ex;
        } catch (DataIntegrityViolationException ex) {
            log.error("Duplicate cart item: {}", ex.getMessage());
            throw buildException("Product already exists in cart", "DUPLICATE_CART_ITEM");
        } catch (Exception ex) {
            log.error("Unexpected error in saveCartItems: {}", ex.getMessage(), ex);
            throw buildException("Failed to save cart item", "CART_ITEM_SAVE_ERROR");
        }
    }

    @Override
    public CartResponseDto updateCartItems(UpdateCartItemRequestDto requestDto) {
        try {
            Cart cart = cartRepository.findById(requestDto.getCartId())
                    .orElseThrow(
                            () -> buildException("Cart not found for id: " + requestDto.getCartId(), "CART_NOT_FOUND"));

            CartItems items = cartItemRepository.findById(requestDto.getItemId())
                    .map(existing -> {
                        existing.setQuantity(requestDto.getQuantity());
                        return existing;
                    })
                    .orElseThrow(
                            () -> buildException("Cart Item not found for id: " + requestDto.getItemId(), "CART_ITEM_NOT_FOUND"));

            cartItemRepository.save(items);


            CartDataDto data = fetchAllItems(requestDto.getCartId(), cart);

            return CartResponseDto.builder()
                    .success(Boolean.TRUE)
                    .message("Cart item updated successfully")
                    .data(data)
                    .build();

        } catch (CartServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            ErrorResponseDto error = ErrorResponseDto.builder()
                    .success(Boolean.FALSE)
                    .message("Failed to update cart items")
                    .errorCode("CART_ITEM_UPDATE_FAILED")
                    .build();
            throw new CartServiceException(error);
        }
    }

    @Override
    public void removeCartItem(Long cartId, Long itemId) {
        try {
            cartRepository.findById(cartId)
                    .orElseThrow(
                            () -> buildException("Cart not found for id: " + cartId, "CART_NOT_FOUND"));

            CartItems cartItem = cartItemRepository.findById(itemId)
                    .orElseThrow(
                            () -> buildException("Cart Item not found for id: " + itemId, "CART_ITEM_NOT_FOUND"));

            cartItemRepository.delete(cartItem);

        } catch (CartServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error in removeCartItem: {}", ex.getMessage(), ex);
            ErrorResponseDto error = ErrorResponseDto.builder()
                    .success(Boolean.FALSE)
                    .message("Failed to remove cart item")
                    .errorCode("CART_ITEM_REMOVE_FAILED")
                    .build();
            throw new CartServiceException(error);
        }
    }

    @Override
    @Transactional
    public void clearCartItems(Long cartId) {
        try {
            Cart cart = cartRepository.findById(cartId)
                    .orElseThrow(
                            () -> buildException("Cart not found for id: " + cartId, "CART_NOT_FOUND")
                    );

            cartItemRepository.deleteAllByCartId(cartId);

        } catch (CartServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error in clearCartItems: {}", ex.getMessage(), ex);
            ErrorResponseDto error = ErrorResponseDto.builder()
                    .success(Boolean.FALSE)
                    .message("Failed to clear cart items")
                    .errorCode("CART_ITEMS_CLEAR_FAILED")
                    .build();
            throw new CartServiceException(error);
        }
    }

    @Override
    public CartResponseDto fetchCartAndDetails(Long cartId) {
        try {
            Cart cart = cartRepository.findById(cartId)
                    .orElseThrow(
                            () -> buildException("Cart not found for id: " + cartId, "CART_NOT_FOUND")
                    );

            CartDataDto data = fetchAllItems(cartId, cart);
            return CartResponseDto.builder()
                    .success(Boolean.TRUE)
                    .message("Cart and details fetched successfully")
                    .data(data)
                    .build();
        } catch (CartServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            ErrorResponseDto error = ErrorResponseDto.builder()
                    .success(Boolean.FALSE)
                    .message("Failed to fetch cart and details")
                    .errorCode("CART_FETCH_FAILED")
                    .build();
            throw new CartServiceException(error);
        }
    }

    @Override
    public BrowseResponseDto<CartBrowseResponseDto> browse(BrowseRequestDto req) {
        Specification<Cart> spec = BrowseHelper.buildSpecification(req.getFilters());
        Pageable pageable = BrowseHelper.buildPageable(req);

        Page<Cart> resultPage = cartRepository.findAll(spec, pageable);

        List<CartBrowseResponseDto> data = resultPage.getContent()
                .stream()
                .map(cartMapper::toCartBrowseDto)   // via mapper, not private method
                .collect(Collectors.toList());

        BrowseMetaDto meta = new BrowseMetaDto(
                req.getPage(),
                req.getSize(),
                req.getLimit(),
                resultPage.getTotalElements()
        );

        return new BrowseResponseDto<>(true, "Carts fetched successfully", data, meta);
    }

    private CartDataDto fetchAllItems(Long cartId, Cart cart) {
        // Fetch all items for this cart and calculate total
        List<CartItems> allItems = cartItemRepository.findByCartId(cartId);
        long cartTotal = allItems.stream()
                .mapToLong(i -> (long) i.getQuantity() * i.getUnitPrice())
                .sum();

        CartItemsDataDto[] itemDtos = allItems.stream()
                .map(i -> CartItemsDataDto.builder()
                        .itemId(i.getId())
                        .productId(i.getProductId())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .build())
                .toArray(CartItemsDataDto[]::new);

        CartDataDto data = new CartDataDto();
        data.setCartId(cart.getId());
        data.setCustomerId(cart.getCustomerId());
        data.setPlatform(cart.getPlatform());
        data.setStatus(cart.getStatus());
        data.setCartTotal(cartTotal);
        data.setItems(itemDtos);
        data.setCreatedBy(cart.getCreatedBy());
        data.setCreatedAt(cart.getCreatedAt());
        data.setUpdatedBy(cart.getUpdatedBy());
        data.setUpdatedAt(cart.getUpdatedAt());

        return data;
    }

    private CartServiceException buildException(String message, String errorCode) {
        ErrorResponseDto error = ErrorResponseDto.builder()
                .success(Boolean.FALSE)
                .message(message)
                .errorCode(errorCode)
                .build();
        return new CartServiceException(error);
    }

}