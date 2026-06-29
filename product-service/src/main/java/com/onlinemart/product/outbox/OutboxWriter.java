package com.onlinemart.product.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OutboxWriter {

    private static final Logger log = LoggerFactory.getLogger(OutboxWriter.class);

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public OutboxWriter(OutboxEventRepository outboxEventRepository,
                        ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    public void write(String aggregateId, String eventType, String topic, Object event) {
        try {
            OutboxEvent outboxEvent = new OutboxEvent();
            outboxEvent.setAggregateId(aggregateId);
            outboxEvent.setEventType(eventType);
            outboxEvent.setTopic(topic);
            outboxEvent.setPayload(objectMapper.writeValueAsString(event));
            outboxEventRepository.save(outboxEvent);
            log.info("Outbox event written: type={} aggregateId={} topic={}",
                    eventType, aggregateId, topic);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize outbox event: type={} aggregateId={}",
                    eventType, aggregateId, e);
            throw new RuntimeException("Outbox write failed for type=" + eventType, e);
        }
    }
}