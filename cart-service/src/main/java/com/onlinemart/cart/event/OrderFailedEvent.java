package com.onlinemart.cart.event;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderFailedEvent {

    private Long orderId;
    private Long cartId;
    private Long customerId;
    private List<OrderFailedItemEvent> items;

}