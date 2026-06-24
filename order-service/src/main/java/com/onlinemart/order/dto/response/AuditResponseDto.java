package com.onlinemart.order.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Schema(description = "Audit information for created/updated entities")
public class AuditResponseDto {

    @Schema(description = "ID of user who created the entity", example = "1")
    private Long createdBy;

    @Schema(description = "Timestamp when the entity was created")
    private LocalDateTime createdAt;

    @Schema(description = "ID of user who last updated the entity", example = "2")
    private Long updatedBy;

    @Schema(description = "Timestamp when the entity was last updated")
    private LocalDateTime updatedAt;

}