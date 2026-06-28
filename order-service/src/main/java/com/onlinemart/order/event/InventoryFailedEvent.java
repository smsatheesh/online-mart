package com.onlinemart.order.event;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryFailedEvent {

    private Long orderId;
    private Long cartId;
    private String reason;

}