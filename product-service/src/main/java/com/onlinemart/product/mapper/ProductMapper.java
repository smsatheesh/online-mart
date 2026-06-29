package com.onlinemart.product.mapper;

import com.onlinemart.product.dto.request.ProductRequestDto;
import com.onlinemart.product.dto.response.ProductResponseDto;
import com.onlinemart.product.dto.response.ProductDataDto;
import com.onlinemart.product.dto.response.ProductBrowseResponseDto;
import com.onlinemart.product.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toEntity(ProductRequestDto dto) {
        if (dto == null) return null;
        Product p = new Product();
        if (dto.getId() != null) {
            p.setId(dto.getId());
        }
        p.setCategoryId(dto.getCategoryId());
        p.setName(dto.getProductName());
        p.setDescription(dto.getDescription() == null ? "" : dto.getDescription());
        p.setPrice(dto.getPrice());
        p.setAvailableStockQuantity(dto.getAvailableQuantity());
        p.setThumbnailUrl(dto.getThumbnailUrl());
        p.setStatus(Boolean.TRUE);
        return p;
    }

    public ProductResponseDto toSaveResponseDto(Product product) {
        if (product == null) return null;
        ProductDataDto data = ProductDataDto.builder()
                .categoryId(product.getCategoryId() != null ? product.getCategoryId() : null)
                .productId(product.getId() != null ? product.getId() : null)
                .productName(product.getName())
                .price(product.getPrice())
                .availableQuantity(product.getAvailableStockQuantity())
                .thumbnailUrl(product.getThumbnailUrl())
                .status(product.getStatus())
                .createdBy(product.getCreatedBy())
                .createdAt(product.getCreatedAt())
                .updatedBy(product.getUpdatedBy())
                .updatedAt(product.getUpdatedAt())
                .build();

        return ProductResponseDto.builder()
                .success(true)
                .message("Products created successfully")
                .data(data)
                .build();
    }

    public ProductBrowseResponseDto toProductBrowseDto(Product product) {
        ProductBrowseResponseDto dto = new ProductBrowseResponseDto();
        dto.setCategoryId(product.getCategoryId());
        dto.setProductId(product.getId() != null
                ? product.getId() : null);
        dto.setProductName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setAvailableQuantity(product.getAvailableStockQuantity());
        dto.setThumbnailUrl(product.getThumbnailUrl());
        dto.setStatus(product.getStatus());
        dto.setCreatedBy(product.getCreatedBy() != null
                ? product.getCreatedBy().toString() : null);
        dto.setUpdatedBy(product.getUpdatedBy() != null
                ? product.getUpdatedBy().toString() : null);
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        return dto;
    }

}
