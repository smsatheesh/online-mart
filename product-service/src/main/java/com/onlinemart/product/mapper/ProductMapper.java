package com.onlinemart.product.mapper;

import com.onlinemart.product.dto.request.ProductRequestDto;
import com.onlinemart.product.dto.response.ProductResponseDto;
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
        p.setAvailableStpockQuantity(dto.getAvailableQuantity());
        p.setThumbnailUrl(dto.getThumbnailUrl());
        p.setStatus(Boolean.TRUE);
        return p;
    }

    public ProductResponseDto toSaveResponseDto(Product product) {
        if (product == null) return null;
        return ProductResponseDto.builder()
                .id(product.getId())
                .productId(product.getId())
                .categoryId(product.getCategoryId())
                .productName(product.getName())
                .price(product.getPrice())
                .availableQuantity(product.getAvailableStpockQuantity())
                .thumbnailUrl(product.getThumbnailUrl())
                .status(product.getStatus())
                .createdBy(product.getCreatedBy())
                .createdAt(product.getCreatedAt())
                .updatedBy(product.getUpdatedBy())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

}
