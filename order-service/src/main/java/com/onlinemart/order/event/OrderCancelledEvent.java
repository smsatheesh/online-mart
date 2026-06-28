package com.onlinemart.order.event;

import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancelledEvent {

    private Long orderId;
    private Long customerId;
    private Long cartId;
    private List<OrderItemEvent> items;

}