package com.onlinemart.product.service;

import com.onlinemart.product.dto.request.ProductRequestDto;
import com.onlinemart.product.dto.response.ProductResponseDto;
import com.onlinemart.product.dto.response.AvailabilityResponseDto;
import com.onlinemart.product.dto.response.AvailabilityDataDto;
import com.onlinemart.product.dto.response.ErrorResponseDto;
import com.onlinemart.product.entity.Product;
import com.onlinemart.product.exception.ProductServiceException;
import com.onlinemart.product.mapper.ProductMapper;
import com.onlinemart.product.repository.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
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
                                existing.setAvailableStpockQuantity(requestDto.getAvailableQuantity());
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
                        Long qty = product.getAvailableStpockQuantity() != null ? product.getAvailableStpockQuantity() : 0L;
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

}