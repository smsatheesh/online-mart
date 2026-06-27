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

    @Bean
    public NewTopic orderCreatedTopic() {
        return TopicBuilder.name(orderTopicCreated)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic orderCancelledtopic() {
        return TopicBuilder.name(orderTopicCancelled)
                .partitions(1)
                .replicas(1)
                .build();
    }

}