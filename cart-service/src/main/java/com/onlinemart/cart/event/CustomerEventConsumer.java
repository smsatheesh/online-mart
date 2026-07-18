package com.onlinemart.cart.event;

import com.onlinemart.cart.service.CartService;
import com.onlinemart.cart.dto.request.CreateCartRequestDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(CustomerEventConsumer.class);

    private final CartService cartService;

    @Value("${spring.kafka.topic.customer.created}")
    private String CUSTOMER_CREATED_TOPIC;

    @KafkaListener(
            topics = "${spring.kafka.topic.customer.created}",
            groupId = "${spring.kafka.consumer.group-id}",
            properties = {"spring.json.value.default.type=com.onlinemart.cart.event.CustomerCreatedEvent"}
    )
    public void handleCustomerCreated(CustomerCreatedEvent event) {
        log.info("Customer created with customerId={} → creating a new cart", event.getCustomerId());
        CreateCartRequestDto cartRequest = new CreateCartRequestDto();
        cartRequest.setCustomerId(event.getCustomerId());
        cartRequest.setPlatform("MAIN");
        cartService.saveCart(cartRequest);
    }
}
