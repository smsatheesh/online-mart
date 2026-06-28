package com.onlinemart.product.service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private InventoryEventPublisher inventoryEventPublisher;

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    public ProductServiceImpl(ProductRepository productRepository, ProductMapper productMapper, InventoryEventPublisher inventoryEventPublisher) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.inventoryEventPublisher = inventoryEventPublisher;
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
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    inventoryEventPublisher.publishFailed(
                            new InventoryFailedEvent(orderId, cartId, "Product not found: " + productId));
                    ErrorResponseDto error = ErrorResponseDto.builder()
                            .success(false)
                            .message("Product not found for id: " + productId)
                            .errorCode("PRODUCT_NOT_FOUND")
                            .build();
                    return new ProductServiceException(error);
                });

        if (product.getAvailableStockQuantity() < quantity) {
            inventoryEventPublisher.publishFailed(
                    new InventoryFailedEvent(orderId, cartId,
                            "Insufficient stock for productId: " + productId));
            return;
        }

        product.setAvailableStockQuantity(product.getAvailableStockQuantity() - quantity);
        productRepository.save(product);

        inventoryEventPublisher.publishReserved(new InventoryReservedEvent(orderId));
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

}