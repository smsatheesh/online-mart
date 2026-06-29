package com.onlinemart.product.service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;

import com.onlinemart.product.dto.request.ProductRequestDto;
import com.onlinemart.product.dto.response.ProductResponseDto;
import com.onlinemart.product.dto.response.AvailabilityResponseDto;
import com.onlinemart.product.dto.response.AvailabilityDataDto;
import com.onlinemart.product.dto.response.ErrorResponseDto;
import com.onlinemart.product.entity.Product;
import com.onlinemart.product.exception.ProductServiceException;
import com.onlinemart.product.mapper.ProductMapper;
import com.onlinemart.product.repository.ProductRepository;
import com.onlinemart.product.event.InventoryEventPublisher;
import com.onlinemart.product.event.InventoryFailedEvent;
import com.onlinemart.product.event.InventoryReservedEvent;
import com.onlinemart.product.outbox.OutboxWriter;
import com.onlinemart.product.dto.response.BrowseResponseDto;
import com.onlinemart.product.dto.response.ProductBrowseResponseDto;
import com.onlinemart.product.dto.request.BrowseRequestDto;
import com.onlinemart.product.helper.BrowseHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.stream.Collectors;
import com.onlinemart.product.dto.response.BrowseMetaDto;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private InventoryEventPublisher inventoryEventPublisher;

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final OutboxWriter outboxWriter;

    @Value("${spring.kafka.topic.inventory.reserved}")
    private String inventoryReservedTopic;

    @Value("${spring.kafka.topic.inventory.failed}")
    private String inventoryFailedTopic;

    public ProductServiceImpl(
            ProductRepository productRepository,
            ProductMapper productMapper,
            InventoryEventPublisher inventoryEventPublisher,
            OutboxWriter outboxWriter
    ) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.inventoryEventPublisher = inventoryEventPublisher;
        this.outboxWriter = outboxWriter;
    }

    @Override
    public ProductResponseDto saveProduct(ProductRequestDto requestDto) {
        try {
            if (requestDto.getId() != null) {
                // update flow
                Long id = requestDto.getId();
                return productRepository.findById(id)
                        .map(existing -> {
                            // copy updatable fields only when provided in the request (non-null)
                            if (requestDto.getCategoryId() != null) {
                                existing.setCategoryId(requestDto.getCategoryId());
                            }
                            if (requestDto.getProductName() != null) {
                                existing.setName(requestDto.getProductName());
                            }
                            if (requestDto.getDescription() != null) {
                                // allow empty string to clear description
                                existing.setDescription(requestDto.getDescription());
                            }
                            if (requestDto.getPrice() != null) {
                                existing.setPrice(requestDto.getPrice());
                            }
                            if (requestDto.getAvailableQuantity() != null) {
                                existing.setAvailableStockQuantity(requestDto.getAvailableQuantity());
                            }
                            if (requestDto.getThumbnailUrl() != null) {
                                existing.setThumbnailUrl(requestDto.getThumbnailUrl());
                            }
                            // keep status as is or set true if null
                            if (existing.getStatus() == null) existing.setStatus(Boolean.TRUE);
                            Product updated = productRepository.save(existing);
                            return productMapper.toSaveResponseDto(updated);
                        })
                        .orElseThrow(() -> {
                            ErrorResponseDto error = ErrorResponseDto.builder()
                                    .success(false)
                                    .message("Product not found for id: " + id)
                                    .errorCode("PRODUCT_NOT_FOUND")
                                    .build();
                            return new ProductServiceException(error);
                        });
            } else {
                Product product = productMapper.toEntity(requestDto);
                Product saved = productRepository.save(product);
                return productMapper.toSaveResponseDto(saved);
            }
        } catch (ProductServiceException ex) {
            throw ex; // rethrow custom exceptions as-is
        } catch (Exception ex) {
            ErrorResponseDto error = ErrorResponseDto.builder()
                    .success(false)
                    .message("Failed to save product")
                    .errorCode("PRODUCT_SAVE_ERROR")
                    .build();
            throw new ProductServiceException(error);
        }
    }

    @Override
    public ProductResponseDto fetchProduct(Long productId) {
        try {
            return productRepository.findById(productId)
                    .map(productMapper::toSaveResponseDto)
                    .orElseThrow(() -> {
                        ErrorResponseDto error = ErrorResponseDto.builder()
                                .success(false)
                                .message("Product not found for id: " + productId)
                                .errorCode("PRODUCT_NOT_FOUND")
                                .build();
                        return new ProductServiceException(error);
                    });
        } catch (ProductServiceException ex) {
            throw ex; // rethrow custom exceptions as-is
        } catch (Exception ex) {
            ErrorResponseDto error = ErrorResponseDto.builder()
                    .success(false)
                    .message("Failed to fetch product")
                    .errorCode("PRODUCT_FETCH_ERROR")
                    .build();
            throw new ProductServiceException(error);
        }
    }

    @Override
    public AvailabilityResponseDto checkAvailability(Long productId) {
        try {
            return productRepository.findById(productId)
                    .map(product -> {
                        Long qty = product.getAvailableStockQuantity() != null ? product.getAvailableStockQuantity() : 0L;
                        AvailabilityDataDto data = AvailabilityDataDto.builder()
                                .productId(product.getId())
                                .isAvailable(qty > 0)
                                .availableQuantity(qty)
                                .build();

                        return AvailabilityResponseDto.builder()
                                .success(true)
                                .message("Inventory fetched successfully")
                                .data(data)
                                .build();
                    })
                    .orElseThrow(() -> {
                        ErrorResponseDto error = ErrorResponseDto.builder()
                                .success(false)
                                .message("Product not found for id: " + productId)
                                .errorCode("PRODUCT_NOT_FOUND")
                                .build();
                        return new ProductServiceException(error);
                    });
        } catch (ProductServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            ErrorResponseDto error = ErrorResponseDto.builder()
                    .success(false)
                    .message("Failed to check availability")
                    .errorCode("PRODUCT_AVAILABILITY_CHECK_FAILED")
                    .build();
            throw new ProductServiceException(error);
        }
    }

    @Override
    @Transactional
    public void deductStock(Long productId, Long quantity, Long orderId, Long cartId) {
        try {
            AvailabilityResponseDto availability = checkAvailability(productId);

            if (!availability.getData().getIsAvailable()
                    || availability.getData().getAvailableQuantity() < quantity) {
//            inventoryEventPublisher.publishFailed(
//                    new InventoryFailedEvent(orderId, cartId,
//                            "Insufficient stock for productId: " + productId));
                outboxWriter.write(
                        orderId.toString(),
                        "INVENTORY_FAILED",
                        inventoryFailedTopic,
                        new InventoryFailedEvent(orderId, cartId,
                                "Insufficient stock for productId: " + productId
                                        + " available=" + availability.getData().getAvailableQuantity()
                                        + " requested=" + quantity)
                );
                log.warn("Insufficient stock: productId={} available={} requested={}",
                        productId, availability.getData().getAvailableQuantity(), quantity);
                return;
            }

            Product product = productRepository.findById(productId).get();
            product.setAvailableStockQuantity(product.getAvailableStockQuantity() - quantity);
            productRepository.save(product);

//          inventoryEventPublisher.publishReserved(new InventoryReservedEvent(orderId));
            outboxWriter.write(
                    orderId.toString(),
                    "INVENTORY_RESERVED",
                    inventoryReservedTopic,
                    new InventoryReservedEvent(orderId)
            );
            log.info("Stock deducted: productId={} qty={} orderId={} remaining={}",
                    productId, quantity, orderId, product.getAvailableStockQuantity());

        } catch (ProductServiceException ex) {
//                    inventoryEventPublisher.publishFailed(
//                            new InventoryFailedEvent(orderId, cartId, "Product not found: " + productId));
            outboxWriter.write(
                    orderId.toString(),
                    "INVENTORY_FAILED",
                    inventoryFailedTopic,
                    new InventoryFailedEvent(orderId, cartId, ex.getErrorResponse().getMessage())
            );
            log.error("Stock deduction failed: productId={} orderId={} reason={}",
                    productId, orderId, ex.getErrorResponse().getMessage());
        } catch (Exception ex) {
            outboxWriter.write(
                    orderId.toString(),
                    "INVENTORY_FAILED",
                    inventoryFailedTopic,
                    new InventoryFailedEvent(orderId, cartId,
                            "Unexpected error during stock deduction for productId: " + productId)
            );
            log.error("Unexpected error in deductStock: productId={} orderId={}",
                    productId, orderId, ex);
        }
    }

    @Override
    @Transactional
    public void restoreStock(Long productId, Long quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    ErrorResponseDto error = ErrorResponseDto.builder()
                            .success(false)
                            .message("Product not found for id: " + productId)
                            .errorCode("PRODUCT_NOT_FOUND")
                            .build();
                    return new ProductServiceException(error);
                });

        product.setAvailableStockQuantity(product.getAvailableStockQuantity() + quantity);
        productRepository.save(product);

        log.info("Restored stock for productId={} by qty={} newStock={}",
                productId, quantity, product.getAvailableStockQuantity());
    }

    @Override
    public BrowseResponseDto<ProductBrowseResponseDto> browse(BrowseRequestDto req) {
        Specification<Product> spec = BrowseHelper.buildSpecification(req.getFilters());
        Pageable pageable = BrowseHelper.buildPageable(req);

        Page<Product> resultPage = productRepository.findAll(spec, pageable);

        List<ProductBrowseResponseDto> data = resultPage.getContent()
                .stream()
                .map(productMapper::toProductBrowseDto)
                .collect(Collectors.toList());

        BrowseMetaDto meta = new BrowseMetaDto(
                req.getPage(),
                req.getSize(),
                req.getLimit(),
                resultPage.getTotalElements()
        );

        return new BrowseResponseDto<>(true, "Products fetched successfully", data, meta);
    }

}