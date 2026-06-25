package com.onlinemart.cart.service;

import com.onlinemart.cart.client.ProductClient;
import com.onlinemart.cart.client.dto.ProductDataDto;
import com.onlinemart.cart.client.dto.ProductResponseDto;
import com.onlinemart.cart.dto.request.CreateCartRequestDto;
import com.onlinemart.cart.dto.request.CreateCartItemRequestDto;
import com.onlinemart.cart.dto.request.UpdateCartItemRequestDto;
import com.onlinemart.cart.dto.response.CartDataDto;
import com.onlinemart.cart.dto.response.CartDetailResponseDto;
import com.onlinemart.cart.dto.response.CartItemsDataDto;
import com.onlinemart.cart.dto.response.CartResponseDto;
import com.onlinemart.cart.entity.Cart;
import com.onlinemart.cart.entity.CartItems;
import com.onlinemart.cart.exception.CartServiceException;
import com.onlinemart.cart.mapper.CartMapper;
import com.onlinemart.cart.repository.CartItemRepository;
import com.onlinemart.cart.repository.CartRepository;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CartServiceImpl.
 *
 * Methods covered:
 *  1. saveCart()
 *  2. fetchDetails()
 *  3. saveCartItems()       ← most complex: involves ProductClient + fetchAllItems()
 *  4. updateCartItems()
 *  5. removeCartItem()
 *  6. clearCartItems()
 *  7. fetchCartAndDetails()
 *
 * Key things to note about this service:
 *  - ProductClient is an OpenFeign client (mocked here — no HTTP in unit tests)
 *  - fetchAllItems() is a private method called inside saveCartItems, updateCartItems,
 *    fetchCartAndDetails — we test it INDIRECTLY through those methods
 *  - cartTotal = sum of (quantity * unitPrice) across all CartItems for that cart
 *  - CartServiceException is never re-wrapped (catch-rethrow guard exists)
 *  - DataIntegrityViolationException in saveCartItems → DUPLICATE_CART_ITEM
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CartServiceImpl – Unit Tests")
class CartServiceImplTest {

    private static final Logger log = Logger.getLogger(CartServiceImplTest.class.getName());

    // -----------------------------------------------------------------------
    // Mocks
    // -----------------------------------------------------------------------
    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private CartMapper cartMapper;
    @Mock
    private ProductClient productClient;

    @InjectMocks
    private CartServiceImpl cartService;

    // -----------------------------------------------------------------------
    // Shared fixtures
    // -----------------------------------------------------------------------

    /** A cart entity that "exists in the database" */
    private Cart sampleCart;

    /** A single cart item in that cart */
    private CartItems sampleItem;

    /** A ProductResponseDto returned by the (mocked) ProductClient */
    private ProductResponseDto sampleProductResponse;

    @BeforeEach
    void setUp() {
        log.info("━━━ setUp: building shared fixtures ━━━");

        // --- Cart entity ---
        sampleCart = new Cart();
        sampleCart.setId(1L);
        sampleCart.setCustomerId(100L);
        sampleCart.setPlatform("WEB");
        sampleCart.setStatus(Boolean.TRUE);

        // --- CartItems entity ---
        sampleItem = new CartItems();
        sampleItem.setId(10L);
        sampleItem.setCartId(1L);
        sampleItem.setProductId(200L);
        sampleItem.setQuantity(2L);
        sampleItem.setUnitPrice(1000L);

        // --- ProductDataDto (inner payload from product-service) ---
        ProductDataDto productData = ProductDataDto.builder()
                .productId("PROD200")
                .price(1000L)
                .availableQuantity(20L)
                .build();

        // --- ProductResponseDto (outer wrapper from product-service) ---
        sampleProductResponse = ProductResponseDto.builder()
                .success(true)
                .message("Product fetched successfully")
                .data(productData)
                .build();
    }

    // -----------------------------------------------------------------------
    // Helper: build a CartDataDto as fetchAllItems() would produce it
    //         (used to stub cartMapper or verify response fields)
    // -----------------------------------------------------------------------
    private CartDataDto buildCartDataDto(Long cartId, Long customerId,
                                         Long cartTotal, CartItemsDataDto... items) {
        CartDataDto data = new CartDataDto();
        data.setCartId(cartId);
        data.setCustomerId(customerId);
        data.setPlatform("WEB");
        data.setStatus(Boolean.TRUE);
        data.setCartTotal(cartTotal);
        data.setItems(items);
        return data;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  1. saveCart()
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("saveCart")
    class SaveCart {

        @Test
        @DisplayName("✅ Happy path: maps request → entity → saves → returns response")
        void saveCart_success() {
            log.info("▶ TEST: saveCart_success");

            CreateCartRequestDto request = new CreateCartRequestDto();
            request.setCustomerId(100L);
            request.setPlatform("WEB");

            CartResponseDto expectedResponse = CartResponseDto.builder()
                    .success(Boolean.TRUE)
                    .message("Cart created successfully")
                    .data(buildCartDataDto(1L, 100L, 0L))
                    .build();

            when(cartMapper.toEntity(request)).thenReturn(sampleCart);
            when(cartRepository.save(sampleCart)).thenReturn(sampleCart);
            when(cartMapper.toSaveResponseDto(sampleCart)).thenReturn(expectedResponse);

            CartResponseDto result = cartService.saveCart(request);

            assertThat(result.getSuccess()).isTrue();
            assertThat(result.getData().getCartId()).isEqualTo(1L);
            assertThat(result.getData().getCustomerId()).isEqualTo(100L);

            verify(cartMapper).toEntity(request);
            verify(cartRepository).save(sampleCart);
            verify(cartMapper).toSaveResponseDto(sampleCart);

            log.info("✔ saveCart_success PASSED — cartId=" + result.getData().getCartId());
        }

        @Test
        @DisplayName("❌ Repository throws RuntimeException → wraps in CART_SAVE_ERROR")
        void saveCart_repositoryThrows_wrapsException() {
            log.info("▶ TEST: saveCart_repositoryThrows_wrapsException");

            CreateCartRequestDto request = new CreateCartRequestDto();
            request.setCustomerId(100L);

            when(cartMapper.toEntity(request)).thenReturn(sampleCart);
            when(cartRepository.save(any())).thenThrow(new RuntimeException("DB down"));

            CartServiceException ex = catchThrowableOfType(
                    () -> cartService.saveCart(request),
                    CartServiceException.class
            );

            assertThat(ex.getErrorResponse().getErrorCode()).isEqualTo("CART_SAVE_ERROR");
            assertThat(ex.getErrorResponse().isSuccess()).isFalse();

            log.warning("✔ saveCart_repositoryThrows PASSED — errorCode=" + ex.getErrorResponse().getErrorCode());
        }

        @Test
        @DisplayName("❌ CartServiceException from mapper passes through unchanged (no re-wrap)")
        void saveCart_cartServiceException_notRewrapped() {
            log.info("▶ TEST: saveCart_cartServiceException_notRewrapped");

            CreateCartRequestDto request = new CreateCartRequestDto();

            CartServiceException original = buildException("Custom mapper error", "MAPPER_ERROR");
            when(cartMapper.toEntity(request)).thenThrow(original);

            CartServiceException ex = catchThrowableOfType(
                    () -> cartService.saveCart(request),
                    CartServiceException.class
            );

            assertThat(ex.getErrorResponse().getErrorCode()).isEqualTo("MAPPER_ERROR");

            log.warning("✔ saveCart_cartServiceException_notRewrapped PASSED");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  2. fetchDetails()
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("fetchDetails")
    class FetchDetails {

        @Test
        @DisplayName("✅ Cart exists for customerId → returns CartDetailResponseDto")
        void fetchDetails_found_returnsDto() {
            log.info("▶ TEST: fetchDetails_found_returnsDto");

            CartDetailResponseDto expectedDetail = CartDetailResponseDto.builder()
                    .success(Boolean.TRUE)
                    .message("Cart fetched successfully")
                    .build();

            when(cartRepository.findByCustomerId(100L)).thenReturn(Optional.of(sampleCart));
            when(cartMapper.toDetailResponse(sampleCart)).thenReturn(expectedDetail);

            CartDetailResponseDto result = cartService.fetchDetails(100L);

            assertThat(result.getSuccess()).isTrue();
            assertThat(result.getMessage()).isEqualTo("Cart fetched successfully");

            verify(cartRepository).findByCustomerId(100L);
            verify(cartMapper).toDetailResponse(sampleCart);

            log.info("✔ fetchDetails_found PASSED");
        }

        @Test
        @DisplayName("❌ No cart for customerId → throws CartServiceException(CART_NOT_FOUND)")
        void fetchDetails_notFound_throwsException() {
            log.info("▶ TEST: fetchDetails_notFound_throwsException");

            when(cartRepository.findByCustomerId(999L)).thenReturn(Optional.empty());

            CartServiceException ex = catchThrowableOfType(
                    () -> cartService.fetchDetails(999L),
                    CartServiceException.class
            );

            // NOTE: fetchDetails wraps ALL exceptions (including CartServiceException)
            // into CART_FETCH_FAILED because it has no rethrow guard for CartServiceException
            assertThat(ex.getErrorResponse().getErrorCode()).isEqualTo("CART_FETCH_FAILED");

            log.warning("✔ fetchDetails_notFound PASSED — errorCode=" + ex.getErrorResponse().getErrorCode());
        }

        @Test
        @DisplayName("❌ Repository throws RuntimeException → wraps in CART_FETCH_FAILED")
        void fetchDetails_repositoryThrows_wrapsException() {
            log.info("▶ TEST: fetchDetails_repositoryThrows_wrapsException");

            when(cartRepository.findByCustomerId(100L)).thenThrow(new RuntimeException("Connection lost"));

            CartServiceException ex = catchThrowableOfType(
                    () -> cartService.fetchDetails(100L),
                    CartServiceException.class
            );

            assertThat(ex.getErrorResponse().getErrorCode()).isEqualTo("CART_FETCH_FAILED");

            log.warning("✔ fetchDetails_repositoryThrows PASSED");
        }

        @Test
        @DisplayName("✅ findByCustomerId called exactly once with correct customerId")
        void fetchDetails_repositoryCalledOnce() {
            log.info("▶ TEST: fetchDetails_repositoryCalledOnce");

            when(cartRepository.findByCustomerId(100L)).thenReturn(Optional.of(sampleCart));
            when(cartMapper.toDetailResponse(sampleCart)).thenReturn(
                    CartDetailResponseDto.builder().success(Boolean.TRUE).build()
            );

            cartService.fetchDetails(100L);

            verify(cartRepository, times(1)).findByCustomerId(100L);
            verifyNoMoreInteractions(cartRepository);

            log.info("✔ fetchDetails_repositoryCalledOnce PASSED");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  3. saveCartItems()
    //  Most complex method: cartId validation → ProductClient call →
    //  CartItem creation → fetchAllItems() → response
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("saveCartItems")
    class SaveCartItems {

        /** Build a valid CreateCartItemRequestDto using setters */
        private CreateCartItemRequestDto validRequest(Long cartId, Long productId, Long quantity) {
            CreateCartItemRequestDto dto = new CreateCartItemRequestDto();
            dto.setCartId(cartId);
            dto.setProductId(productId);
            dto.setQuantity(quantity);
            return dto;
        }

        @Test
        @DisplayName("✅ Happy path: cart found → product fetched → item saved → returns response with total")
        void saveCartItems_success_returnsCartWithTotal() {
            log.info("▶ TEST: saveCartItems_success_returnsCartWithTotal");

            CreateCartItemRequestDto request = validRequest(1L, 200L, 2L);

            // item that will be saved and returned when fetchAllItems queries the repo
            CartItems savedItem = new CartItems();
            savedItem.setId(10L);
            savedItem.setCartId(1L);
            savedItem.setProductId(200L);
            savedItem.setQuantity(2L);
            savedItem.setUnitPrice(1000L);

            when(cartRepository.findById(1L)).thenReturn(Optional.of(sampleCart));
            when(productClient.fetchProduct(200L)).thenReturn(sampleProductResponse);
            when(cartItemRepository.save(any(CartItems.class))).thenReturn(savedItem);
            when(cartItemRepository.findByCartId(1L)).thenReturn(List.of(savedItem));

            CartResponseDto result = cartService.saveCartItems(request);

            // outer wrapper
            assertThat(result.getSuccess()).isTrue();
            assertThat(result.getMessage()).isEqualTo("Item added to cart successfully");

            // inner data — cartTotal = 2 qty * 1000 unit price = 2000
            CartDataDto data = result.getData();
            assertThat(data.getCartId()).isEqualTo(1L);
            assertThat(data.getCustomerId()).isEqualTo(100L);
            assertThat(data.getCartTotal()).isEqualTo(2000L);  // 2 * 1000
            assertThat(data.getItems()).hasSize(1);
            assertThat(data.getItems()[0].getProductId()).isEqualTo(200L);
            assertThat(data.getItems()[0].getQuantity()).isEqualTo(2L);

            verify(cartRepository).findById(1L);
            verify(productClient).fetchProduct(200L);
            verify(cartItemRepository).save(any(CartItems.class));
            verify(cartItemRepository).findByCartId(1L);

            log.info("✔ saveCartItems_success PASSED — cartTotal=" + data.getCartTotal());
        }

        @Test
        @DisplayName("✅ cartTotal is sum of ALL items (2 items in cart)")
        void saveCartItems_cartTotalSumsAllItems() {
            log.info("▶ TEST: saveCartItems_cartTotalSumsAllItems");

            CreateCartItemRequestDto request = validRequest(1L, 200L, 1L);

            CartItems existingItem = new CartItems();
            existingItem.setId(9L);
            existingItem.setCartId(1L);
            existingItem.setProductId(201L);
            existingItem.setQuantity(3L);
            existingItem.setUnitPrice(500L);  // contributes 1500 to total

            CartItems newItem = new CartItems();
            newItem.setId(10L);
            newItem.setCartId(1L);
            newItem.setProductId(200L);
            newItem.setQuantity(1L);
            newItem.setUnitPrice(1000L);      // contributes 1000 to total
            // Expected total = 3*500 + 1*1000 = 2500

            when(cartRepository.findById(1L)).thenReturn(Optional.of(sampleCart));
            when(productClient.fetchProduct(200L)).thenReturn(sampleProductResponse);
            when(cartItemRepository.save(any())).thenReturn(newItem);
            // fetchAllItems gets BOTH items
            when(cartItemRepository.findByCartId(1L)).thenReturn(List.of(existingItem, newItem));

            CartResponseDto result = cartService.saveCartItems(request);

            assertThat(result.getData().getCartTotal()).isEqualTo(2500L);  // 1500 + 1000
            assertThat(result.getData().getItems()).hasSize(2);

            log.info("✔ saveCartItems_cartTotalSumsAllItems PASSED — total=" + result.getData().getCartTotal());
        }

        @Test
        @DisplayName("✅ CartItems entity is built correctly from request + product price")
        void saveCartItems_cartItemBuiltFromRequestAndProduct() {
            log.info("▶ TEST: saveCartItems_cartItemBuiltFromRequestAndProduct");

            CreateCartItemRequestDto request = validRequest(1L, 200L, 5L);

            when(cartRepository.findById(1L)).thenReturn(Optional.of(sampleCart));
            when(productClient.fetchProduct(200L)).thenReturn(sampleProductResponse);
            when(cartItemRepository.save(any())).thenReturn(sampleItem);
            when(cartItemRepository.findByCartId(1L)).thenReturn(List.of(sampleItem));

            cartService.saveCartItems(request);

            // Capture the CartItems passed to save() and verify its fields
            ArgumentCaptor<CartItems> captor = ArgumentCaptor.forClass(CartItems.class);
            verify(cartItemRepository).save(captor.capture());

            CartItems captured = captor.getValue();
            assertThat(captured.getCartId()).isEqualTo(1L);
            assertThat(captured.getProductId()).isEqualTo(200L);
            assertThat(captured.getQuantity()).isEqualTo(5L);
            assertThat(captured.getUnitPrice()).isEqualTo(1000L);  // price from ProductDataDto

            log.info("✔ saveCartItems_cartItemBuiltCorrectly PASSED — unitPrice=" + captured.getUnitPrice());
        }

        @Test
        @DisplayName("❌ Null request → throws CartServiceException(INVALID_REQUEST)")
        void saveCartItems_nullRequest_throwsInvalidRequest() {
            log.info("▶ TEST: saveCartItems_nullRequest_throwsInvalidRequest");

            CartServiceException ex = catchThrowableOfType(
                    () -> cartService.saveCartItems(null),
                    CartServiceException.class
            );

            assertThat(ex.getErrorResponse().getErrorCode()).isEqualTo("INVALID_REQUEST");

            log.warning("✔ saveCartItems_nullRequest PASSED");
        }

        @Test
        @DisplayName("❌ cartId is null in request → throws CartServiceException(CART_ID_REQUIRED)")
        void saveCartItems_nullCartId_throwsCartIdRequired() {
            log.info("▶ TEST: saveCartItems_nullCartId_throwsCartIdRequired");

            CreateCartItemRequestDto request = new CreateCartItemRequestDto();
            request.setProductId(200L);
            request.setQuantity(1L);
            // cartId intentionally left null

            CartServiceException ex = catchThrowableOfType(
                    () -> cartService.saveCartItems(request),
                    CartServiceException.class
            );

            assertThat(ex.getErrorResponse().getErrorCode()).isEqualTo("CART_ID_REQUIRED");

            log.warning("✔ saveCartItems_nullCartId PASSED");
        }

        @Test
        @DisplayName("❌ Cart not found for cartId → throws CartServiceException(CART_NOT_FOUND)")
        void saveCartItems_cartNotFound_throwsException() {
            log.info("▶ TEST: saveCartItems_cartNotFound_throwsException");

            CreateCartItemRequestDto request = validRequest(999L, 200L, 1L);
            when(cartRepository.findById(999L)).thenReturn(Optional.empty());

            CartServiceException ex = catchThrowableOfType(
                    () -> cartService.saveCartItems(request),
                    CartServiceException.class
            );

            assertThat(ex.getErrorResponse().getErrorCode()).isEqualTo("CART_NOT_FOUND");
            assertThat(ex.getErrorResponse().getMessage()).contains("999");

            log.warning("✔ saveCartItems_cartNotFound PASSED");
        }

        @Test
        @DisplayName("❌ ProductClient returns null response → throws CartServiceException(PRODUCT_NOT_FOUND)")
        void saveCartItems_productClientReturnsNull_throwsException() {
            log.info("▶ TEST: saveCartItems_productClientReturnsNull_throwsException");

            CreateCartItemRequestDto request = validRequest(1L, 200L, 1L);
            when(cartRepository.findById(1L)).thenReturn(Optional.of(sampleCart));
            when(productClient.fetchProduct(200L)).thenReturn(null);

            CartServiceException ex = catchThrowableOfType(
                    () -> cartService.saveCartItems(request),
                    CartServiceException.class
            );

            assertThat(ex.getErrorResponse().getErrorCode()).isEqualTo("PRODUCT_NOT_FOUND");

            log.warning("✔ saveCartItems_productClientReturnsNull PASSED");
        }

        @Test
        @DisplayName("❌ ProductClient returns response with null data → throws CartServiceException(PRODUCT_NOT_FOUND)")
        void saveCartItems_productClientNullData_throwsException() {
            log.info("▶ TEST: saveCartItems_productClientNullData_throwsException");

            CreateCartItemRequestDto request = validRequest(1L, 200L, 1L);
            ProductResponseDto emptyResp = ProductResponseDto.builder()
                    .success(true)
                    .message("ok")
                    .data(null)  // data is null
                    .build();

            when(cartRepository.findById(1L)).thenReturn(Optional.of(sampleCart));
            when(productClient.fetchProduct(200L)).thenReturn(emptyResp);

            CartServiceException ex = catchThrowableOfType(
                    () -> cartService.saveCartItems(request),
                    CartServiceException.class
            );

            assertThat(ex.getErrorResponse().getErrorCode()).isEqualTo("PRODUCT_NOT_FOUND");

            log.warning("✔ saveCartItems_productClientNullData PASSED");
        }

        @Test
        @DisplayName("❌ Product price is null → throws CartServiceException(PRODUCT_PRICE_MISSING)")
        void saveCartItems_productPriceNull_throwsException() {
            log.info("▶ TEST: saveCartItems_productPriceNull_throwsException");

            ProductDataDto noPriceData = ProductDataDto.builder()
                    .productId("PROD200")
                    .price(null)  // price missing
                    .build();
            ProductResponseDto noPriceResp = ProductResponseDto.builder()
                    .success(true)
                    .data(noPriceData)
                    .build();

            CreateCartItemRequestDto request = validRequest(1L, 200L, 2L);
            when(cartRepository.findById(1L)).thenReturn(Optional.of(sampleCart));
            when(productClient.fetchProduct(200L)).thenReturn(noPriceResp);

            CartServiceException ex = catchThrowableOfType(
                    () -> cartService.saveCartItems(request),
                    CartServiceException.class
            );

            assertThat(ex.getErrorResponse().getErrorCode()).isEqualTo("PRODUCT_PRICE_MISSING");

            log.warning("✔ saveCartItems_productPriceNull PASSED");
        }

        @Test
        @DisplayName("❌ DataIntegrityViolationException (duplicate product) → DUPLICATE_CART_ITEM")
        void saveCartItems_duplicateProduct_throwsDuplicateCartItem() {
            log.info("▶ TEST: saveCartItems_duplicateProduct_throwsDuplicateCartItem");

            CreateCartItemRequestDto request = validRequest(1L, 200L, 1L);
            when(cartRepository.findById(1L)).thenReturn(Optional.of(sampleCart));
            when(productClient.fetchProduct(200L)).thenReturn(sampleProductResponse);
            when(cartItemRepository.save(any())).thenThrow(
                    new DataIntegrityViolationException("Unique constraint violation")
            );

            CartServiceException ex = catchThrowableOfType(
                    () -> cartService.saveCartItems(request),
                    CartServiceException.class
            );

            assertThat(ex.getErrorResponse().getErrorCode()).isEqualTo("DUPLICATE_CART_ITEM");
            assertThat(ex.getErrorResponse().getMessage()).isEqualTo("Product already exists in cart");

            log.warning("✔ saveCartItems_duplicateProduct PASSED");
        }

        @Test
        @DisplayName("❌ Unexpected RuntimeException → wraps in CART_ITEM_SAVE_ERROR")
        void saveCartItems_unexpectedException_wrapsInCartItemSaveError() {
            log.info("▶ TEST: saveCartItems_unexpectedException_wrapsInCartItemSaveError");

            CreateCartItemRequestDto request = validRequest(1L, 200L, 1L);
            when(cartRepository.findById(1L)).thenReturn(Optional.of(sampleCart));
            when(productClient.fetchProduct(200L)).thenReturn(sampleProductResponse);
            when(cartItemRepository.save(any())).thenThrow(new RuntimeException("Unexpected DB error"));

            CartServiceException ex = catchThrowableOfType(
                    () -> cartService.saveCartItems(request),
                    CartServiceException.class
            );

            assertThat(ex.getErrorResponse().getErrorCode()).isEqualTo("CART_ITEM_SAVE_ERROR");

            log.warning("✔ saveCartItems_unexpectedException PASSED");
        }

        @Test
        @DisplayName("✅ Empty cart (no items yet) → cartTotal=0, items array is empty")
        void saveCartItems_emptyCart_totalIsZero() {
            log.info("▶ TEST: saveCartItems_emptyCart_totalIsZero");

            CreateCartItemRequestDto request = validRequest(1L, 200L, 1L);
            when(cartRepository.findById(1L)).thenReturn(Optional.of(sampleCart));
            when(productClient.fetchProduct(200L)).thenReturn(sampleProductResponse);
            when(cartItemRepository.save(any())).thenReturn(sampleItem);
            // fetchAllItems returns empty list
            when(cartItemRepository.findByCartId(1L)).thenReturn(List.of());

            CartResponseDto result = cartService.saveCartItems(request);

            assertThat(result.getData().getCartTotal()).isZero();
            assertThat(result.getData().getItems()).isEmpty();

            log.info("✔ saveCartItems_emptyCart PASSED — total=" + result.getData().getCartTotal());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  4. updateCartItems()
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("updateCartItems")
    class UpdateCartItems {

        private UpdateCartItemRequestDto validRequest(Long cartId, Long itemId, Long quantity) {
            UpdateCartItemRequestDto dto = new UpdateCartItemRequestDto();
            dto.setCartId(cartId);
            dto.setItemId(itemId);
            dto.setQuantity(quantity);
            return dto;
        }

        @Test
        @DisplayName("✅ Happy path: cart + item found → quantity updated → returns updated response")
        void updateCartItems_success_returnsUpdatedCart() {
            log.info("▶ TEST: updateCartItems_success_returnsUpdatedCart");

            UpdateCartItemRequestDto request = validRequest(1L, 10L, 5L);

            CartItems updatedItem = new CartItems();
            updatedItem.setId(10L);
            updatedItem.setCartId(1L);
            updatedItem.setProductId(200L);
            updatedItem.setQuantity(5L);      // updated quantity
            updatedItem.setUnitPrice(1000L);  // price unchanged

            when(cartRepository.findById(1L)).thenReturn(Optional.of(sampleCart));
            when(cartItemRepository.findById(10L)).thenReturn(Optional.of(sampleItem));
            when(cartItemRepository.save(sampleItem)).thenReturn(updatedItem);
            when(cartItemRepository.findByCartId(1L)).thenReturn(List.of(updatedItem));

            CartResponseDto result = cartService.updateCartItems(request);

            assertThat(result.getSuccess()).isTrue();
            assertThat(result.getMessage()).isEqualTo("Cart item updated successfully");

            // cartTotal = 5 qty * 1000 unit price = 5000
            assertThat(result.getData().getCartTotal()).isEqualTo(5000L);
            assertThat(result.getData().getItems()[0].getQuantity()).isEqualTo(5L);

            // Verify quantity was set on the entity before save
            assertThat(sampleItem.getQuantity()).isEqualTo(5L);

            log.info("✔ updateCartItems_success PASSED — cartTotal=" + result.getData().getCartTotal());
        }

        @Test
        @DisplayName("✅ Quantity updated correctly on the entity before save (ArgumentCaptor)")
        void updateCartItems_quantitySetOnEntityBeforeSave() {
            log.info("▶ TEST: updateCartItems_quantitySetOnEntityBeforeSave");

            UpdateCartItemRequestDto request = validRequest(1L, 10L, 99L);

            when(cartRepository.findById(1L)).thenReturn(Optional.of(sampleCart));
            when(cartItemRepository.findById(10L)).thenReturn(Optional.of(sampleItem));
            when(cartItemRepository.save(any())).thenReturn(sampleItem);
            when(cartItemRepository.findByCartId(1L)).thenReturn(List.of(sampleItem));

            cartService.updateCartItems(request);

            ArgumentCaptor<CartItems> captor = ArgumentCaptor.forClass(CartItems.class);
            verify(cartItemRepository).save(captor.capture());

            assertThat(captor.getValue().getQuantity()).isEqualTo(99L);

            log.info("✔ updateCartItems_quantitySetOnEntity PASSED");
        }

        @Test
        @DisplayName("❌ Cart not found → throws CartServiceException(CART_NOT_FOUND)")
        void updateCartItems_cartNotFound_throwsException() {
            log.info("▶ TEST: updateCartItems_cartNotFound_throwsException");

            UpdateCartItemRequestDto request = validRequest(999L, 10L, 2L);
            when(cartRepository.findById(999L)).thenReturn(Optional.empty());

            CartServiceException ex = catchThrowableOfType(
                    () -> cartService.updateCartItems(request),
                    CartServiceException.class
            );

            assertThat(ex.getErrorResponse().getErrorCode()).isEqualTo("CART_NOT_FOUND");
            assertThat(ex.getErrorResponse().getMessage()).contains("999");

            log.warning("✔ updateCartItems_cartNotFound PASSED");
        }

        @Test
        @DisplayName("❌ Cart item not found → throws CartServiceException(CART_ITEM_NOT_FOUND)")
        void updateCartItems_cartItemNotFound_throwsException() {
            log.info("▶ TEST: updateCartItems_cartItemNotFound_throwsException");

            UpdateCartItemRequestDto request = validRequest(1L, 999L, 2L);
            when(cartRepository.findById(1L)).thenReturn(Optional.of(sampleCart));
            when(cartItemRepository.findById(999L)).thenReturn(Optional.empty());

            CartServiceException ex = catchThrowableOfType(
                    () -> cartService.updateCartItems(request),
                    CartServiceException.class
            );

            assertThat(ex.getErrorResponse().getErrorCode()).isEqualTo("CART_ITEM_NOT_FOUND");
            assertThat(ex.getErrorResponse().getMessage()).contains("999");

            log.warning("✔ updateCartItems_cartItemNotFound PASSED");
        }

        @Test
        @DisplayName("❌ Repository throws RuntimeException → wraps in CART_ITEM_UPDATE_FAILED")
        void updateCartItems_repositoryThrows_wrapsException() {
            log.info("▶ TEST: updateCartItems_repositoryThrows_wrapsException");

            UpdateCartItemRequestDto request = validRequest(1L, 10L, 3L);
            when(cartRepository.findById(1L)).thenReturn(Optional.of(sampleCart));
            when(cartItemRepository.findById(10L)).thenReturn(Optional.of(sampleItem));
            when(cartItemRepository.save(any())).thenThrow(new RuntimeException("Unexpected failure"));

            CartServiceException ex = catchThrowableOfType(
                    () -> cartService.updateCartItems(request),
                    CartServiceException.class
            );

            assertThat(ex.getErrorResponse().getErrorCode()).isEqualTo("CART_ITEM_UPDATE_FAILED");

            log.warning("✔ updateCartItems_repositoryThrows PASSED");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  5. removeCartItem()
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("removeCartItem")
    class RemoveCartItem {

        @Test
        @DisplayName("✅ Happy path: cart + item found → item deleted (void method)")
        void removeCartItem_success_deletesItem() {
            log.info("▶ TEST: removeCartItem_success_deletesItem");

            when(cartRepository.findById(1L)).thenReturn(Optional.of(sampleCart));
            when(cartItemRepository.findById(10L)).thenReturn(Optional.of(sampleItem));
            doNothing().when(cartItemRepository).delete(sampleItem);

            // removeCartItem returns void — assert it completes without throwing
            assertThatCode(() -> cartService.removeCartItem(1L, 10L))
                    .doesNotThrowAnyException();

            verify(cartRepository).findById(1L);
            verify(cartItemRepository).findById(10L);
            verify(cartItemRepository).delete(sampleItem);

            log.info("✔ removeCartItem_success PASSED");
        }

        @Test
        @DisplayName("✅ delete() is called with the exact CartItems entity returned by findById")
        void removeCartItem_deleteCalledWithCorrectEntity() {
            log.info("▶ TEST: removeCartItem_deleteCalledWithCorrectEntity");

            when(cartRepository.findById(1L)).thenReturn(Optional.of(sampleCart));
            when(cartItemRepository.findById(10L)).thenReturn(Optional.of(sampleItem));

            cartService.removeCartItem(1L, 10L);

            // Must delete the exact entity — not a different object
            verify(cartItemRepository).delete(sampleItem);
            verifyNoMoreInteractions(cartItemRepository);

            log.info("✔ removeCartItem_deleteCalledWithCorrectEntity PASSED");
        }

        @Test
        @DisplayName("❌ Cart not found → throws CartServiceException(CART_NOT_FOUND)")
        void removeCartItem_cartNotFound_throwsException() {
            log.info("▶ TEST: removeCartItem_cartNotFound_throwsException");

            when(cartRepository.findById(999L)).thenReturn(Optional.empty());

            CartServiceException ex = catchThrowableOfType(
                    () -> cartService.removeCartItem(999L, 10L),
                    CartServiceException.class
            );

            assertThat(ex.getErrorResponse().getErrorCode()).isEqualTo("CART_NOT_FOUND");
            // Item lookup must NOT happen when cart is not found
            verify(cartItemRepository, never()).findById(any());

            log.warning("✔ removeCartItem_cartNotFound PASSED");
        }

        @Test
        @DisplayName("❌ Cart item not found → throws CartServiceException(CART_ITEM_NOT_FOUND)")
        void removeCartItem_cartItemNotFound_throwsException() {
            log.info("▶ TEST: removeCartItem_cartItemNotFound_throwsException");

            when(cartRepository.findById(1L)).thenReturn(Optional.of(sampleCart));
            when(cartItemRepository.findById(999L)).thenReturn(Optional.empty());

            CartServiceException ex = catchThrowableOfType(
                    () -> cartService.removeCartItem(1L, 999L),
                    CartServiceException.class
            );

            assertThat(ex.getErrorResponse().getErrorCode()).isEqualTo("CART_ITEM_NOT_FOUND");
            // delete() must NOT be called
            verify(cartItemRepository, never()).delete(any());

            log.warning("✔ removeCartItem_cartItemNotFound PASSED");
        }

        @Test
        @DisplayName("❌ delete() throws RuntimeException → wraps in CART_ITEM_REMOVE_FAILED")
        void removeCartItem_deleteThrows_wrapsException() {
            log.info("▶ TEST: removeCartItem_deleteThrows_wrapsException");

            when(cartRepository.findById(1L)).thenReturn(Optional.of(sampleCart));
            when(cartItemRepository.findById(10L)).thenReturn(Optional.of(sampleItem));
            doThrow(new RuntimeException("FK constraint")).when(cartItemRepository).delete(sampleItem);

            CartServiceException ex = catchThrowableOfType(
                    () -> cartService.removeCartItem(1L, 10L),
                    CartServiceException.class
            );

            assertThat(ex.getErrorResponse().getErrorCode()).isEqualTo("CART_ITEM_REMOVE_FAILED");

            log.warning("✔ removeCartItem_deleteThrows PASSED");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  6. clearCartItems()
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("clearCartItems")
    class ClearCartItems {

        @Test
        @DisplayName("✅ Happy path: cart found → deleteAllByCartId called → completes without throw")
        void clearCartItems_success_deletesAll() {
            log.info("▶ TEST: clearCartItems_success_deletesAll");

            when(cartRepository.findById(1L)).thenReturn(Optional.of(sampleCart));
            doNothing().when(cartItemRepository).deleteAllByCartId(1L);

            assertThatCode(() -> cartService.clearCartItems(1L))
                    .doesNotThrowAnyException();

            verify(cartRepository).findById(1L);
            verify(cartItemRepository).deleteAllByCartId(1L);

            log.info("✔ clearCartItems_success PASSED");
        }

        @Test
        @DisplayName("✅ deleteAllByCartId is called with the correct cartId")
        void clearCartItems_deleteCalledWithCorrectCartId() {
            log.info("▶ TEST: clearCartItems_deleteCalledWithCorrectCartId");

            when(cartRepository.findById(5L)).thenReturn(Optional.of(sampleCart));

            cartService.clearCartItems(5L);

            // Must pass 5L — not 1L or any other id
            verify(cartItemRepository).deleteAllByCartId(5L);

            log.info("✔ clearCartItems_deleteCalledWithCorrectCartId PASSED");
        }

        @Test
        @DisplayName("❌ Cart not found → throws CartServiceException(CART_NOT_FOUND)")
        void clearCartItems_cartNotFound_throwsException() {
            log.info("▶ TEST: clearCartItems_cartNotFound_throwsException");

            when(cartRepository.findById(999L)).thenReturn(Optional.empty());

            CartServiceException ex = catchThrowableOfType(
                    () -> cartService.clearCartItems(999L),
                    CartServiceException.class
            );

            assertThat(ex.getErrorResponse().getErrorCode()).isEqualTo("CART_NOT_FOUND");
            // deleteAllByCartId must NOT be called when cart is missing
            verify(cartItemRepository, never()).deleteAllByCartId(any());

            log.warning("✔ clearCartItems_cartNotFound PASSED");
        }

        @Test
        @DisplayName("❌ deleteAllByCartId throws RuntimeException → wraps in CART_ITEMS_CLEAR_FAILED")
        void clearCartItems_deleteThrows_wrapsException() {
            log.info("▶ TEST: clearCartItems_deleteThrows_wrapsException");

            when(cartRepository.findById(1L)).thenReturn(Optional.of(sampleCart));
            doThrow(new RuntimeException("Bulk delete failed")).when(cartItemRepository).deleteAllByCartId(1L);

            CartServiceException ex = catchThrowableOfType(
                    () -> cartService.clearCartItems(1L),
                    CartServiceException.class
            );

            assertThat(ex.getErrorResponse().getErrorCode()).isEqualTo("CART_ITEMS_CLEAR_FAILED");

            log.warning("✔ clearCartItems_deleteThrows PASSED");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  7. fetchCartAndDetails()
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("fetchCartAndDetails")
    class FetchCartAndDetails {

        @Test
        @DisplayName("✅ Happy path: cart found → returns response with all items and total")
        void fetchCartAndDetails_success_returnsCartWithItems() {
            log.info("▶ TEST: fetchCartAndDetails_success_returnsCartWithItems");

            when(cartRepository.findById(1L)).thenReturn(Optional.of(sampleCart));
            when(cartItemRepository.findByCartId(1L)).thenReturn(List.of(sampleItem));
            // sampleItem: qty=2, unitPrice=1000 → total=2000

            CartResponseDto result = cartService.fetchCartAndDetails(1L);

            assertThat(result.getSuccess()).isTrue();
            assertThat(result.getMessage()).isEqualTo("Cart and details fetched successfully");

            CartDataDto data = result.getData();
            assertThat(data.getCartId()).isEqualTo(1L);
            assertThat(data.getCustomerId()).isEqualTo(100L);
            assertThat(data.getCartTotal()).isEqualTo(2000L);  // 2 * 1000
            assertThat(data.getItems()).hasSize(1);

            log.info("✔ fetchCartAndDetails_success PASSED — total=" + data.getCartTotal());
        }

        @Test
        @DisplayName("✅ Cart with no items → cartTotal=0, items array is empty")
        void fetchCartAndDetails_noItems_totalZero() {
            log.info("▶ TEST: fetchCartAndDetails_noItems_totalZero");

            when(cartRepository.findById(1L)).thenReturn(Optional.of(sampleCart));
            when(cartItemRepository.findByCartId(1L)).thenReturn(List.of());

            CartResponseDto result = cartService.fetchCartAndDetails(1L);

            assertThat(result.getData().getCartTotal()).isZero();
            assertThat(result.getData().getItems()).isEmpty();

            log.info("✔ fetchCartAndDetails_noItems PASSED");
        }

        @Test
        @DisplayName("✅ CartDataDto fields (cartId, customerId, platform, status) match entity")
        void fetchCartAndDetails_dataFieldsMatchCartEntity() {
            log.info("▶ TEST: fetchCartAndDetails_dataFieldsMatchCartEntity");

            sampleCart.setPlatform("MOBILE");
            sampleCart.setStatus(Boolean.FALSE);

            when(cartRepository.findById(1L)).thenReturn(Optional.of(sampleCart));
            when(cartItemRepository.findByCartId(1L)).thenReturn(List.of());

            CartResponseDto result = cartService.fetchCartAndDetails(1L);

            CartDataDto data = result.getData();
            assertThat(data.getPlatform()).isEqualTo("MOBILE");
            assertThat(data.getStatus()).isFalse();
            assertThat(data.getCustomerId()).isEqualTo(100L);

            log.info("✔ fetchCartAndDetails_dataFieldsMatchEntity PASSED");
        }

        @Test
        @DisplayName("✅ Multiple items → cartTotal is correct sum of all (qty * unitPrice)")
        void fetchCartAndDetails_multipleItems_totalIsCorrectSum() {
            log.info("▶ TEST: fetchCartAndDetails_multipleItems_totalIsCorrectSum");

            CartItems item1 = new CartItems();
            item1.setQuantity(3L);
            item1.setUnitPrice(200L); // 600
            CartItems item2 = new CartItems();
            item2.setQuantity(1L);
            item2.setUnitPrice(800L); // 800
            CartItems item3 = new CartItems();
            item3.setQuantity(5L);
            item3.setUnitPrice(100L); // 500
            // Total = 600 + 800 + 500 = 1900

            when(cartRepository.findById(1L)).thenReturn(Optional.of(sampleCart));
            when(cartItemRepository.findByCartId(1L)).thenReturn(List.of(item1, item2, item3));

            CartResponseDto result = cartService.fetchCartAndDetails(1L);

            assertThat(result.getData().getCartTotal()).isEqualTo(1900L);
            assertThat(result.getData().getItems()).hasSize(3);

            log.info("✔ fetchCartAndDetails_multipleItems_total PASSED — total=" + result.getData().getCartTotal());
        }

        @Test
        @DisplayName("✅ CartItemsDataDto fields are mapped correctly from CartItems entity")
        void fetchCartAndDetails_itemDtoFieldsMappedCorrectly() {
            log.info("▶ TEST: fetchCartAndDetails_itemDtoFieldsMappedCorrectly");

            when(cartRepository.findById(1L)).thenReturn(Optional.of(sampleCart));
            when(cartItemRepository.findByCartId(1L)).thenReturn(List.of(sampleItem));

            CartResponseDto result = cartService.fetchCartAndDetails(1L);

            CartItemsDataDto itemDto = result.getData().getItems()[0];
            assertThat(itemDto.getItemId()).isEqualTo(10L);
            assertThat(itemDto.getProductId()).isEqualTo(200L);
            assertThat(itemDto.getQuantity()).isEqualTo(2L);
            assertThat(itemDto.getUnitPrice()).isEqualTo(1000L);

            log.info("✔ fetchCartAndDetails_itemDtoFieldsMapped PASSED");
        }

        @Test
        @DisplayName("❌ Cart not found → throws CartServiceException(CART_NOT_FOUND)")
        void fetchCartAndDetails_cartNotFound_throwsException() {
            log.info("▶ TEST: fetchCartAndDetails_cartNotFound_throwsException");

            when(cartRepository.findById(999L)).thenReturn(Optional.empty());

            CartServiceException ex = catchThrowableOfType(
                    () -> cartService.fetchCartAndDetails(999L),
                    CartServiceException.class
            );

            assertThat(ex.getErrorResponse().getErrorCode()).isEqualTo("CART_NOT_FOUND");
            assertThat(ex.getErrorResponse().getMessage()).contains("999");

            log.warning("✔ fetchCartAndDetails_cartNotFound PASSED");
        }

        @Test
        @DisplayName("❌ Repository throws RuntimeException → wraps in CART_FETCH_FAILED")
        void fetchCartAndDetails_repositoryThrows_wrapsException() {
            log.info("▶ TEST: fetchCartAndDetails_repositoryThrows_wrapsException");

            when(cartRepository.findById(1L)).thenThrow(new RuntimeException("DB timeout"));

            CartServiceException ex = catchThrowableOfType(
                    () -> cartService.fetchCartAndDetails(1L),
                    CartServiceException.class
            );

            assertThat(ex.getErrorResponse().getErrorCode()).isEqualTo("CART_FETCH_FAILED");

            log.warning("✔ fetchCartAndDetails_repositoryThrows PASSED");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Helper — mirrors the service's private buildException() method
    //           so tests can create expected CartServiceExceptions cleanly
    // ═══════════════════════════════════════════════════════════════════════
    private CartServiceException buildException(String message, String errorCode) {
        com.onlinemart.cart.dto.response.ErrorResponseDto error =
                com.onlinemart.cart.dto.response.ErrorResponseDto.builder()
                        .success(Boolean.FALSE)
                        .message(message)
                        .errorCode(errorCode)
                        .build();
        return new CartServiceException(error);
    }
}
