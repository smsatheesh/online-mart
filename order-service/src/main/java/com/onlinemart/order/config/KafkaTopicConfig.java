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

    @Value("${spring.kafka.topic.order.failed}")
    private String orderTopicFailed;

    @Value("${spring.kafka.topic.inventory.reserved}")
    private String inventoryTopicReserved;

    @Value("${spring.kafka.topic.inventory.failed}")
    private String inventoryTopicFailed;

    @Bean
    public NewTopic orderCreatedTopic() {
        return TopicBuilder.name(orderTopicCreated)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic orderFailedTopic() {
        return TopicBuilder.name(orderTopicFailed)
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

}