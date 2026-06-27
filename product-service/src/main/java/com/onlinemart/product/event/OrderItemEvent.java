package com.onlinemart.product.event;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemEvent {

    private Long productId;
    private Long quantity;
    private Long price;

}