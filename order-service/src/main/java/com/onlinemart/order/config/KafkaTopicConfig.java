package com.onlinemart.order.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class KafkaTopicConfig {

    @Value("${spring.kafka.topic.order.created}")
    private String orderTopicCreated;

    @Value("${spring.kafka.topic.order.cancelled}")
    private String orderTopicCancelled;

    @Value("${spring.kafka.topic.inventory.reserved}")
    private String inventoryTopicReserved;

    @Value("${spring.kafka.topic.inventory.failed}")
    private String inventoryTopicFailed;

    @Value("${spring.kafka.topic.customer.created")
    private String customerTopicCreated;

    @Bean
    public NewTopic orderCreatedTopic() {
        return TopicBuilder.name(orderTopicCreated)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic orderCancelledTopic() {
        return TopicBuilder.name(orderTopicCancelled)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic inventoryReservedTopic() {
        return TopicBuilder.name(inventoryTopicReserved)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic invenotryFailedTopic() {
        return TopicBuilder.name(inventoryTopicFailed)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic customerCreatedTopic() {
        return TopicBuilder.name(customerTopicCreated)
                .partitions(1)
                .replicas(1)
                .build();
    }

}