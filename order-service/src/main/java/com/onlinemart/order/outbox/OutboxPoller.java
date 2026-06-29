package com.onlinemart.order.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPoller {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void pollAndPublish() {
        List<OutboxEvent> pendingEvents =
                outboxEventRepository.findByStatusOrderByCreatedAtAsc("PENDING");

        if (pendingEvents.isEmpty()) return;

        log.info("Outbox poller found {} pending events", pendingEvents.size());

        for (OutboxEvent outboxEvent : pendingEvents) {
            try {
                Object payload = objectMapper.readValue(
                        outboxEvent.getPayload(), Object.class);

                kafkaTemplate.send(
                        outboxEvent.getTopic(),
                        outboxEvent.getAggregateId(),
                        payload
                ).get();

                outboxEvent.setStatus("PUBLISHED");
                outboxEvent.setPublishedAt(LocalDateTime.now());
                outboxEventRepository.save(outboxEvent);

                log.info("Outbox event published: id={} type={} topic={}",
                        outboxEvent.getId(),
                        outboxEvent.getEventType(),
                        outboxEvent.getTopic());

            } catch (Exception e) {
                log.error("Failed to publish outbox event id={} type={}",
                        outboxEvent.getId(), outboxEvent.getEventType(), e);
                outboxEvent.setStatus("FAILED");
                outboxEventRepository.save(outboxEvent);
            }
        }
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void retryFailed() {
        List<OutboxEvent> failedEvents =
                outboxEventRepository.findByStatusOrderByCreatedAtAsc("FAILED");

        if (failedEvents.isEmpty()) return;

        log.warn("Outbox retry: resetting {} failed events to PENDING", failedEvents.size());
        failedEvents.forEach(event -> {
            event.setStatus("PENDING");
            outboxEventRepository.save(event);
        });
    }
}