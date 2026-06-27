package com.onlinemart.product.event;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReservedEvent {

    private Long orderId;

}