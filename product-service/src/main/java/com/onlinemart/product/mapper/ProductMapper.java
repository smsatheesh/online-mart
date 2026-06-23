package com.onlinemart.product.mapper;

import com.onlinemart.product.dto.request.SaveProductRequestDto;
import com.onlinemart.product.dto.response.SaveProductResponseDto;
import com.onlinemart.product.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toEntity(SaveProductRequestDto dto) {
        if (dto == null) return null;
        Product p = new Product();
        p.setCategoryId(dto.getCategoryId());
        p.setName(dto.getProductName());
        p.setDescription(dto.getDescription() == null ? "" : dto.getDescription());
        p.setPrice(dto.getPrice());
        p.setAvailableStpockQuantity(dto.getQuantity());
        p.setThumbnailUrl(dto.getThumbnailUrl());
        p.setStatus(Boolean.TRUE);
        return p;
    }

    public SaveProductResponseDto toSaveResponseDto(Product product) {
        if (product == null) return null;
        return SaveProductResponseDto.builder()
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
