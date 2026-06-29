package com.onlinemart.order.client.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Schema(description = "Cart Payload returned in the response")
public class CartDataDto implements Serializable {

    private Long cartId;

    private Long customerId;

    private String platform;

    private Boolean status;

    private Long cartTotal;

    private CartItemsDataDto[] items;

}
