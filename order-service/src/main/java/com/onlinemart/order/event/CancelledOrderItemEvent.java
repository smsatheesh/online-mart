package com.onlinemart.order.event;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CancelledOrderItemEvent {

    private Long productId;
    private Long quantity;
    private Long unitPrice;

}