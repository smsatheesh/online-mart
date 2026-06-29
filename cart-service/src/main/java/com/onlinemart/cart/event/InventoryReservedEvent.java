package com.onlinemart.cart.event;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReservedEvent {

    private Long orderId;

    private Long cartId;

}