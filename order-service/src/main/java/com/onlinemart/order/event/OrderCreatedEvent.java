package com.onlinemart.order.event;

import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {

    private Long orderId;
    private Long customerId;
    private Long cartId;
    private Long totalAmount;
    private List<OrderItemEvent> items;

}