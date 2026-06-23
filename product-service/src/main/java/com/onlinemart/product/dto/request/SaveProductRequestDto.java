package com.onlinemart.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaveProductRequestDto {

    @NotBlank
    private String productName;

    @NotNull
    private Long categoryId;

    private String description;

    @NotNull
    private Long price;

    @NotNull
    private Long quantity;

    private String thumbnailUrl;

}