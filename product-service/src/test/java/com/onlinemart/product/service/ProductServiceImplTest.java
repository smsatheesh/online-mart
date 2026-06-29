package com.onlinemart.product.service;

import com.onlinemart.product.dto.request.CreateProductRequestDto;
import com.onlinemart.product.dto.request.UpdateProductRequestDto;
import com.onlinemart.product.dto.response.AvailabilityDataDto;
import com.onlinemart.product.dto.response.AvailabilityResponseDto;
import com.onlinemart.product.dto.response.ErrorResponseDto;
import com.onlinemart.product.dto.response.ProductDataDto;
import com.onlinemart.product.dto.response.ProductResponseDto;
import com.onlinemart.product.entity.Product;
import com.onlinemart.product.exception.ProductServiceException;
import com.onlinemart.product.mapper.ProductMapper;
import com.onlinemart.product.repository.ProductRepository;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductServiceImpl.
 *
 * DTOs used:
 *  - CreateProductRequestDto  (implements ProductRequestDto, getId() always returns null)
 *  - UpdateProductRequestDto  (implements ProductRequestDto, has id field)
 *  - ProductResponseDto       (wrapper: success, message, data → ProductDataDto)
 *  - ProductDataDto           (payload: productId String, price Long, etc.)
 *  - AvailabilityResponseDto  (wrapper: success, message, data → AvailabilityDataDto)
 *  - AvailabilityDataDto      (payload: productId Long, isAvailable, availableQuantity)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductServiceImpl – Unit Tests")
class ProductServiceImplTest {

    private static final Logger log = Logger.getLogger(ProductServiceImplTest.class.getName());

    // -----------------------------------------------------------------------
    // Mocks & system under test
    // -----------------------------------------------------------------------
    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    // -----------------------------------------------------------------------
    // Shared fixtures — rebuilt fresh before every test
    // -----------------------------------------------------------------------

    /** The entity that lives in the (mocked) database */
    private Product sampleProduct;

    /** A typical successful ProductResponseDto (wraps ProductDataDto) */
    private ProductResponseDto sampleProductResponse;

    @BeforeEach
    void setUp() {
        log.info("━━━ Setting up test fixtures ━━━");

        // --- entity ---
        sampleProduct = new Product();
        sampleProduct.setId(1L);
        sampleProduct.setCategoryId(10L);
        sampleProduct.setName("Wireless Mouse");
        sampleProduct.setDescription("A good mouse");
        sampleProduct.setPrice(1999L);                       // Long, NOT BigDecimal
        sampleProduct.setAvailableStockQuantity(50L);
        sampleProduct.setThumbnailUrl("https://example.com/mouse.png");
        sampleProduct.setStatus(Boolean.TRUE);

        // --- ProductDataDto (the inner payload) ---
        ProductDataDto sampleData = ProductDataDto.builder()
                .productId(1L)                        // Long productId
                .productName("Wireless Mouse")
                .categoryId(10L)
                .price(1999L)
                .availableQuantity(50L)
                .thumbnailUrl("https://example.com/mouse.png")
                .status(Boolean.TRUE)
                .build();

        // --- ProductResponseDto (the outer wrapper) ---
        sampleProductResponse = ProductResponseDto.builder()
                .success(true)
                .message("Product created successfully")
                .data(sampleData)
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Helper: build a CreateProductRequestDto (no builder — use setters)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Creates a fully-populated CreateProductRequestDto using setters.
     * getId() always returns null on this DTO → triggers CREATE branch.
     */
    private CreateProductRequestDto createRequest(String name, Long categoryId,
                                                  String description, Long price,
                                                  Long qty, String thumbnailUrl) {
        CreateProductRequestDto dto = new CreateProductRequestDto();
        dto.setProductName(name);
        dto.setCategoryId(categoryId);
        dto.setDescription(description);
        dto.setPrice(price);
        dto.setAvailableQuantity(qty);
        dto.setThumbnailUrl(thumbnailUrl);
        return dto;
    }

    /**
     * Creates a fully-populated UpdateProductRequestDto using setters.
     * Must set id → triggers UPDATE branch.
     */
    private UpdateProductRequestDto updateRequest(Long id, String name, Long categoryId,
                                                  String description, Long price,
                                                  Long qty, String thumbnailUrl) {
        UpdateProductRequestDto dto = new UpdateProductRequestDto();
        dto.setId(id);
        dto.setProductName(name);
        dto.setCategoryId(categoryId);
        dto.setDescription(description);
        dto.setPrice(price);
        dto.setAvailableQuantity(qty);
        dto.setThumbnailUrl(thumbnailUrl);
        return dto;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  1. saveProduct() — CREATE path  (getId() == null)
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("saveProduct – CREATE flow")
    class SaveProductCreate {

        @Test
        @DisplayName("✅ Happy path: maps request → entity → saves → returns response wrapper")
        void saveProduct_create_success() {
            log.info("▶ TEST: saveProduct_create_success");

            // CreateProductRequestDto.getId() always returns null → CREATE path
            CreateProductRequestDto request = createRequest(
                    "Wireless Mouse", 10L, "A good mouse", 1999L, 50L, "https://example.com/mouse.png"
            );

            when(productMapper.toEntity(request)).thenReturn(sampleProduct);
            when(productRepository.save(sampleProduct)).thenReturn(sampleProduct);
            when(productMapper.toSaveResponseDto(sampleProduct)).thenReturn(sampleProductResponse);

            // Act
            ProductResponseDto result = productService.saveProduct(request);

            // Assert — outer wrapper
            assertThat(result).isNotNull();
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getMessage()).isEqualTo("Product created successfully");

            // Assert — inner data payload
            assertThat(result.getData()).isNotNull();
            assertThat(result.getData().getProductId()).isEqualTo(1L); // Long productId
            assertThat(result.getData().getPrice()).isEqualTo(1999L);         // Long price

            verify(productMapper).toEntity(request);
            verify(productRepository).save(sampleProduct);
            verify(productMapper).toSaveResponseDto(sampleProduct);

            log.info("✔ saveProduct_create_success PASSED — productId=" + result.getData().getProductId());
        }

        @Test
        @DisplayName("❌ Repository throws RuntimeException → wraps in PRODUCT_SAVE_ERROR")
        void saveProduct_create_repositoryThrows_wrapsException() {
            log.info("▶ TEST: saveProduct_create_repositoryThrows_wrapsException");

            CreateProductRequestDto request = createRequest(
                    "Boom Product", 10L, null, 999L, 5L, null
            );

            when(productMapper.toEntity(request)).thenReturn(sampleProduct);
            when(productRepository.save(any())).thenThrow(new RuntimeException("DB is down"));

            ProductServiceException ex = catchThrowableOfType(
                    () -> productService.saveProduct(request),
                    ProductServiceException.class
            );

            assertThat(ex).isNotNull();
            assertThat(ex.getErrorResponse().getErrorCode()).isEqualTo("PRODUCT_SAVE_ERROR");
            assertThat(ex.getErrorResponse().isSuccess()).isFalse();

            log.warning("✔ saveProduct_create_repositoryThrows PASSED — errorCode=" + ex.getErrorResponse().getErrorCode());
        }

        @Test
        @DisplayName("✅ CREATE path never calls findById — only save()")
        void saveProduct_create_neverCallsFindById() {
            log.info("▶ TEST: saveProduct_create_neverCallsFindById");

            CreateProductRequestDto request = createRequest(
                    "Any Product", 10L, null, 500L, 10L, null
            );

            when(productMapper.toEntity(request)).thenReturn(sampleProduct);
            when(productRepository.save(sampleProduct)).thenReturn(sampleProduct);
            when(productMapper.toSaveResponseDto(sampleProduct)).thenReturn(sampleProductResponse);

            productService.saveProduct(request);

            verify(productRepository, never()).findById(any());
            verify(productRepository, times(1)).save(sampleProduct);

            log.info("✔ saveProduct_create_neverCallsFindById PASSED");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  2. saveProduct() — UPDATE path  (getId() != null)
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("saveProduct – UPDATE flow")
    class SaveProductUpdate {

        @Test
        @DisplayName("✅ Full update: all fields provided → all fields copied onto entity")
        void saveProduct_update_allFields_success() {
            log.info("▶ TEST: saveProduct_update_allFields_success");

            UpdateProductRequestDto request = updateRequest(
                    1L, "Updated Mouse", 20L, "Updated desc", 2999L, 100L, "https://example.com/new.png"
            );

            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
            when(productRepository.save(sampleProduct)).thenReturn(sampleProduct);
            when(productMapper.toSaveResponseDto(sampleProduct)).thenReturn(sampleProductResponse);

            ProductResponseDto result = productService.saveProduct(request);

            assertThat(result).isNotNull();
            assertThat(result.isSuccess()).isTrue();

            // Verify the entity fields were mutated before save
            assertThat(sampleProduct.getCategoryId()).isEqualTo(20L);
            assertThat(sampleProduct.getName()).isEqualTo("Updated Mouse");
            assertThat(sampleProduct.getDescription()).isEqualTo("Updated desc");
            assertThat(sampleProduct.getPrice()).isEqualTo(2999L);           // Long comparison
            assertThat(sampleProduct.getAvailableStockQuantity()).isEqualTo(100L);
            assertThat(sampleProduct.getThumbnailUrl()).isEqualTo("https://example.com/new.png");

            verify(productRepository).findById(1L);
            verify(productRepository).save(sampleProduct);

            log.info("✔ saveProduct_update_allFields_success PASSED");
        }

        @Test
        @DisplayName("✅ Partial update: only price set → only price changes, rest untouched")
        void saveProduct_update_onlyPrice_otherFieldsUnchanged() {
            log.info("▶ TEST: saveProduct_update_onlyPrice_otherFieldsUnchanged");

            // Snapshot original values before update
            String originalName = sampleProduct.getName();
            String originalDesc = sampleProduct.getDescription();
            Long originalQty = sampleProduct.getAvailableStockQuantity();
            Long originalCatId = sampleProduct.getCategoryId();
            String originalThumb = sampleProduct.getThumbnailUrl();

            // Only price is set — all other fields are null
            UpdateProductRequestDto request = new UpdateProductRequestDto();
            request.setId(1L);
            request.setPrice(3999L);  // Only this field

            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
            when(productRepository.save(sampleProduct)).thenReturn(sampleProduct);
            when(productMapper.toSaveResponseDto(sampleProduct)).thenReturn(sampleProductResponse);

            productService.saveProduct(request);

            // Price must be updated
            assertThat(sampleProduct.getPrice()).isEqualTo(3999L);

            // Everything else must be unchanged (null-guard branches skipped)
            assertThat(sampleProduct.getName()).isEqualTo(originalName);
            assertThat(sampleProduct.getDescription()).isEqualTo(originalDesc);
            assertThat(sampleProduct.getAvailableStockQuantity()).isEqualTo(originalQty);
            assertThat(sampleProduct.getCategoryId()).isEqualTo(originalCatId);
            assertThat(sampleProduct.getThumbnailUrl()).isEqualTo(originalThumb);

            log.info("✔ saveProduct_update_onlyPrice PASSED — price=" + sampleProduct.getPrice());
        }

        @Test
        @DisplayName("✅ Partial update: only productName set → only name changes")
        void saveProduct_update_onlyProductName_otherFieldsUnchanged() {
            log.info("▶ TEST: saveProduct_update_onlyProductName_otherFieldsUnchanged");

            Long originalPrice = sampleProduct.getPrice();

            UpdateProductRequestDto request = new UpdateProductRequestDto();
            request.setId(1L);
            request.setProductName("Mechanical Keyboard");

            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
            when(productRepository.save(sampleProduct)).thenReturn(sampleProduct);
            when(productMapper.toSaveResponseDto(sampleProduct)).thenReturn(sampleProductResponse);

            productService.saveProduct(request);

            assertThat(sampleProduct.getName()).isEqualTo("Mechanical Keyboard");
            assertThat(sampleProduct.getPrice()).isEqualTo(originalPrice); // untouched

            log.info("✔ saveProduct_update_onlyProductName PASSED");
        }

        @Test
        @DisplayName("✅ Empty string description → description is cleared (empty string != null)")
        void saveProduct_update_emptyStringDescription_clearsDescription() {
            log.info("▶ TEST: saveProduct_update_emptyStringDescription_clearsDescription");

            // The service code: if (requestDto.getDescription() != null) → set it
            // So empty string "" must pass through and clear the description
            UpdateProductRequestDto request = new UpdateProductRequestDto();
            request.setId(1L);
            request.setDescription("");   // explicitly set to empty, not null

            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
            when(productRepository.save(sampleProduct)).thenReturn(sampleProduct);
            when(productMapper.toSaveResponseDto(sampleProduct)).thenReturn(sampleProductResponse);

            productService.saveProduct(request);

            assertThat(sampleProduct.getDescription()).isEmpty();

            log.info("✔ saveProduct_update_emptyStringDescription PASSED");
        }

        @Test
        @DisplayName("✅ Entity status is null → defaulted to TRUE during update")
        void saveProduct_update_statusNull_defaultsToTrue() {
            log.info("▶ TEST: saveProduct_update_statusNull_defaultsToTrue");

            sampleProduct.setStatus(null); // force status to null before update

            UpdateProductRequestDto request = new UpdateProductRequestDto();
            request.setId(1L);
            request.setProductName("Any Name");

            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
            when(productRepository.save(sampleProduct)).thenReturn(sampleProduct);
            when(productMapper.toSaveResponseDto(sampleProduct)).thenReturn(sampleProductResponse);

            productService.saveProduct(request);

            assertThat(sampleProduct.getStatus()).isTrue();

            log.info("✔ saveProduct_update_statusNull_defaultsToTrue PASSED");
        }

        @Test
        @DisplayName("✅ Entity status already TRUE → stays TRUE (null-guard skipped)")
        void saveProduct_update_statusAlreadyTrue_staysTrue() {
            log.info("▶ TEST: saveProduct_update_statusAlreadyTrue_staysTrue");

            sampleProduct.setStatus(Boolean.TRUE); // already set

            UpdateProductRequestDto request = new UpdateProductRequestDto();
            request.setId(1L);
            request.setProductName("Any Name");

            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
            when(productRepository.save(sampleProduct)).thenReturn(sampleProduct);
            when(productMapper.toSaveResponseDto(sampleProduct)).thenReturn(sampleProductResponse);

            productService.saveProduct(request);

            assertThat(sampleProduct.getStatus()).isTrue();

            log.info("✔ saveProduct_update_statusAlreadyTrue PASSED");
        }

        @Test
        @DisplayName("❌ Product not found → throws ProductServiceException(PRODUCT_NOT_FOUND)")
        void saveProduct_update_productNotFound_throwsException() {
            log.info("▶ TEST: saveProduct_update_productNotFound_throwsException");

            UpdateProductRequestDto request = new UpdateProductRequestDto();
            request.setId(999L);
            request.setProductName("Ghost Product");

            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            ProductServiceException ex = catchThrowableOfType(
                    () -> productService.saveProduct(request),
                    ProductServiceException.class
            );

            assertThat(ex).isNotNull();
            assertThat(ex.getErrorResponse().getErrorCode()).isEqualTo("PRODUCT_NOT_FOUND");
            assertThat(ex.getErrorResponse().isSuccess()).isFalse();
            assertThat(ex.getErrorResponse().getMessage()).contains("999");

            log.warning("✔ saveProduct_update_productNotFound PASSED — " + ex.getErrorResponse().getMessage());
        }

        @Test
        @DisplayName("❌ Repository.save() throws during update → wraps in PRODUCT_SAVE_ERROR")
        void saveProduct_update_repositoryThrowsOnSave_wrapsException() {
            log.info("▶ TEST: saveProduct_update_repositoryThrowsOnSave_wrapsException");

            UpdateProductRequestDto request = new UpdateProductRequestDto();
            request.setId(1L);
            request.setProductName("Doomed Update");

            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
            when(productRepository.save(any())).thenThrow(new RuntimeException("Constraint violation"));

            ProductServiceException ex = catchThrowableOfType(
                    () -> productService.saveProduct(request),
                    ProductServiceException.class
            );

            assertThat(ex.getErrorResponse().getErrorCode()).isEqualTo("PRODUCT_SAVE_ERROR");

            log.warning("✔ saveProduct_update_repositoryThrowsOnSave PASSED");
        }

        @Test
        @DisplayName("❌ findById throws RuntimeException → wraps in PRODUCT_SAVE_ERROR")
        void saveProduct_update_findByIdThrows_wrapsException() {
            log.info("▶ TEST: saveProduct_update_findByIdThrows_wrapsException");

            UpdateProductRequestDto request = new UpdateProductRequestDto();
            request.setId(1L);
            request.setProductName("Any Name");

            when(productRepository.findById(1L)).thenThrow(new RuntimeException("DB connection lost"));

            ProductServiceException ex = catchThrowableOfType(
                    () -> productService.saveProduct(request),
                    ProductServiceException.class
            );

            assertThat(ex.getErrorResponse().getErrorCode()).isEqualTo("PRODUCT_SAVE_ERROR");

            log.warning("✔ saveProduct_update_findByIdThrows PASSED");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  3. fetchProduct()
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("fetchProduct")
    class FetchProduct {

        @Test
        @DisplayName("✅ Product exists → returns wrapper with correct data payload")
        void fetchProduct_found_returnsResponseDto() {
            log.info("▶ TEST: fetchProduct_found_returnsResponseDto");

            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
            when(productMapper.toSaveResponseDto(sampleProduct)).thenReturn(sampleProductResponse);

            ProductResponseDto result = productService.fetchProduct(1L);

            // outer wrapper
            assertThat(result.isSuccess()).isTrue();

            // inner data
            assertThat(result.getData()).isNotNull();
            assertThat(result.getData().getProductId()).isEqualTo(1L); // String, not Long
            assertThat(result.getData().getPrice()).isEqualTo(1999L);         // Long

            verify(productRepository, times(1)).findById(1L);
            verify(productMapper, times(1)).toSaveResponseDto(sampleProduct);

            log.info("✔ fetchProduct_found PASSED — productId=" + result.getData().getProductId());
        }

        @Test
        @DisplayName("❌ Product does not exist → throws ProductServiceException(PRODUCT_NOT_FOUND)")
        void fetchProduct_notFound_throwsException() {
            log.info("▶ TEST: fetchProduct_notFound_throwsException");

            when(productRepository.findById(42L)).thenReturn(Optional.empty());

            ProductServiceException ex = catchThrowableOfType(
                    () -> productService.fetchProduct(42L),
                    ProductServiceException.class
            );

            assertThat(ex.getErrorResponse().getErrorCode()).isEqualTo("PRODUCT_NOT_FOUND");
            assertThat(ex.getErrorResponse().isSuccess()).isFalse();
            assertThat(ex.getErrorResponse().getMessage()).contains("42");

            log.warning("✔ fetchProduct_notFound PASSED — " + ex.getErrorResponse().getMessage());
        }

        @Test
        @DisplayName("❌ Repository throws RuntimeException → wraps in PRODUCT_FETCH_ERROR")
        void fetchProduct_repositoryThrows_wrapsException() {
            log.info("▶ TEST: fetchProduct_repositoryThrows_wrapsException");

            when(productRepository.findById(1L)).thenThrow(new RuntimeException("Connection timeout"));

            ProductServiceException ex = catchThrowableOfType(
                    () -> productService.fetchProduct(1L),
                    ProductServiceException.class
            );

            assertThat(ex.getErrorResponse().getErrorCode()).isEqualTo("PRODUCT_FETCH_ERROR");

            log.warning("✔ fetchProduct_repositoryThrows PASSED");
        }

        @Test
        @DisplayName("✅ Repository called exactly once — no extra calls")
        void fetchProduct_repositoryCalledExactlyOnce() {
            log.info("▶ TEST: fetchProduct_repositoryCalledExactlyOnce");

            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
            when(productMapper.toSaveResponseDto(sampleProduct)).thenReturn(sampleProductResponse);

            productService.fetchProduct(1L);

            verify(productRepository, times(1)).findById(1L);
            verifyNoMoreInteractions(productRepository);

            log.info("✔ fetchProduct_repositoryCalledExactlyOnce PASSED");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  4. checkAvailability()
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("checkAvailability")
    class CheckAvailability {

        @Test
        @DisplayName("✅ Product has stock → isAvailable=true, correct quantity returned")
        void checkAvailability_inStock_returnsTrue() {
            log.info("▶ TEST: checkAvailability_inStock_returnsTrue");

            sampleProduct.setAvailableStockQuantity(10L);
            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

            AvailabilityResponseDto result = productService.checkAvailability(1L);

            // outer wrapper
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getMessage()).isEqualTo("Inventory fetched successfully");

            // inner data — productId here is Long (AvailabilityDataDto)
            AvailabilityDataDto data = result.getData();
            assertThat(data.getProductId()).isEqualTo(1L);          // Long productId in AvailabilityDataDto
            assertThat(data.getIsAvailable()).isTrue();
            assertThat(data.getAvailableQuantity()).isEqualTo(10L);

            log.info("✔ checkAvailability_inStock PASSED — qty=" + data.getAvailableQuantity());
        }

        @Test
        @DisplayName("✅ Product has zero stock → isAvailable=false, quantity=0")
        void checkAvailability_zeroStock_returnsFalse() {
            log.info("▶ TEST: checkAvailability_zeroStock_returnsFalse");

            sampleProduct.setAvailableStockQuantity(0L);
            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

            AvailabilityResponseDto result = productService.checkAvailability(1L);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getIsAvailable()).isFalse();
            assertThat(result.getData().getAvailableQuantity()).isZero();

            log.info("✔ checkAvailability_zeroStock PASSED");
        }

        @Test
        @DisplayName("✅ Product quantity field is null → treated as 0, isAvailable=false")
        void checkAvailability_nullQuantity_treatedAsZero() {
            log.info("▶ TEST: checkAvailability_nullQuantity_treatedAsZero");

            // Edge case: the service does: qty = availableStockQuantity != null ? qty : 0L
            sampleProduct.setAvailableStockQuantity(null);
            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

            AvailabilityResponseDto result = productService.checkAvailability(1L);

            assertThat(result.getData().getAvailableQuantity()).isZero();
            assertThat(result.getData().getIsAvailable()).isFalse();

            log.info("✔ checkAvailability_nullQuantity PASSED");
        }

        @Test
        @DisplayName("✅ Response message is always 'Inventory fetched successfully' on success")
        void checkAvailability_successMessage_isCorrect() {
            log.info("▶ TEST: checkAvailability_successMessage_isCorrect");

            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

            AvailabilityResponseDto result = productService.checkAvailability(1L);

            assertThat(result.getMessage()).isEqualTo("Inventory fetched successfully");

            log.info("✔ checkAvailability_successMessage PASSED");
        }

        @Test
        @DisplayName("✅ productId in AvailabilityDataDto matches entity id (Long)")
        void checkAvailability_productIdInDataMatchesEntityId() {
            log.info("▶ TEST: checkAvailability_productIdInDataMatchesEntityId");

            // AvailabilityDataDto uses Long productId (different from ProductDataDto which uses String)
            sampleProduct.setId(7L);
            when(productRepository.findById(7L)).thenReturn(Optional.of(sampleProduct));

            AvailabilityResponseDto result = productService.checkAvailability(7L);

            assertThat(result.getData().getProductId()).isEqualTo(7L);

            log.info("✔ checkAvailability_productIdInData PASSED — productId=" + result.getData().getProductId());
        }

        @Test
        @DisplayName("❌ Product not found → throws ProductServiceException(PRODUCT_NOT_FOUND)")
        void checkAvailability_productNotFound_throwsException() {
            log.info("▶ TEST: checkAvailability_productNotFound_throwsException");

            when(productRepository.findById(77L)).thenReturn(Optional.empty());

            ProductServiceException ex = catchThrowableOfType(
                    () -> productService.checkAvailability(77L),
                    ProductServiceException.class
            );

            assertThat(ex.getErrorResponse().getErrorCode()).isEqualTo("PRODUCT_NOT_FOUND");
            assertThat(ex.getErrorResponse().isSuccess()).isFalse();
            assertThat(ex.getErrorResponse().getMessage()).contains("77");

            log.warning("✔ checkAvailability_productNotFound PASSED");
        }

        @Test
        @DisplayName("❌ Repository throws RuntimeException → wraps in PRODUCT_AVAILABILITY_CHECK_FAILED")
        void checkAvailability_repositoryThrows_wrapsException() {
            log.info("▶ TEST: checkAvailability_repositoryThrows_wrapsException");

            when(productRepository.findById(1L)).thenThrow(new RuntimeException("DB timeout"));

            ProductServiceException ex = catchThrowableOfType(
                    () -> productService.checkAvailability(1L),
                    ProductServiceException.class
            );

            assertThat(ex.getErrorResponse().getErrorCode()).isEqualTo("PRODUCT_AVAILABILITY_CHECK_FAILED");

            log.warning("✔ checkAvailability_repositoryThrows PASSED");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  5. ProductServiceException rethrow behaviour
    //     The service has: catch (ProductServiceException ex) { throw ex; }
    //     Verify a PSE is never re-wrapped into another PSE
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("ProductServiceException passthrough (no double-wrapping)")
    class ExceptionPassthrough {

        @Test
        @DisplayName("fetchProduct: PSE thrown inside is passed through unchanged")
        void fetchProduct_productServiceException_notRewrapped() {
            log.info("▶ TEST: fetchProduct_productServiceException_notRewrapped");

            // Simulate mapper throwing a PSE (unusual, but proves the rethrow guard works)
            ErrorResponseDto originalError = ErrorResponseDto.builder()
                    .success(false)
                    .message("Original PSE message")
                    .errorCode("ORIGINAL_CODE")
                    .build();
            ProductServiceException original = new ProductServiceException(originalError);

            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
            when(productMapper.toSaveResponseDto(sampleProduct)).thenThrow(original);

            ProductServiceException ex = catchThrowableOfType(
                    () -> productService.fetchProduct(1L),
                    ProductServiceException.class
            );

            // Must be the exact same exception, not re-wrapped with PRODUCT_FETCH_ERROR
            assertThat(ex.getErrorResponse().getErrorCode()).isEqualTo("ORIGINAL_CODE");
            assertThat(ex.getErrorResponse().getMessage()).isEqualTo("Original PSE message");

            log.warning("✔ fetchProduct_productServiceException_notRewrapped PASSED");
        }

        @Test
        @DisplayName("checkAvailability: PSE thrown inside is passed through unchanged")
        void checkAvailability_productServiceException_notRewrapped() {
            log.info("▶ TEST: checkAvailability_productServiceException_notRewrapped");

            ErrorResponseDto originalError = ErrorResponseDto.builder()
                    .success(false)
                    .message("Original availability PSE")
                    .errorCode("CUSTOM_ORIGINAL_CODE")
                    .build();
            ProductServiceException original = new ProductServiceException(originalError);

            when(productRepository.findById(1L)).thenThrow(original);

            ProductServiceException ex = catchThrowableOfType(
                    () -> productService.checkAvailability(1L),
                    ProductServiceException.class
            );

            assertThat(ex.getErrorResponse().getErrorCode()).isEqualTo("CUSTOM_ORIGINAL_CODE");

            log.warning("✔ checkAvailability_productServiceException_notRewrapped PASSED");
        }
    }
}
