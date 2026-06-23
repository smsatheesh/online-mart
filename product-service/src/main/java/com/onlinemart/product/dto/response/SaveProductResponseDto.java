package com.onlinemart.product.dto.response;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import com.onlinemart.product.dto.response.AuditResponseDto;

@Getter
@SuperBuilder
public class SaveProductResponseDto extends AuditResponseDto {

    private Long id;

    private Long categoryId;

    private Long productId;

    private String productName;

    private Long price;

    private Long availableQuantity;

    private String thumbnailUrl;

    private Boolean status;
}