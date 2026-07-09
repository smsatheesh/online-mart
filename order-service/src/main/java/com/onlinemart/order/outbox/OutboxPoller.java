package com.onlinemart.order.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPoller {

    private static final int MAX_RETRIES = 3;
    private static final long BASE_DELAY_SECONDS = 5;
    private static final long KAFKA_SEND_TIMEOUT_SECONDS = 5;

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final Logger log = LoggerFactory.getLogger(OutboxPoller.class);

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void pollAndPublish() {
        List<OutboxEvent> pendingEvents =
                outboxEventRepository.findRetryableEvents(LocalDateTime.now());

        if (pendingEvents.isEmpty()) return;

        log.info("Outbox poller found {} retryable events", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                publishToKafka(event);

                event.setStatus("PUBLISHED");
                event.setPublishedAt(LocalDateTime.now());
                log.info("Outbox published: outBoxId={} eventType={} orderId={} aggregateId={} attempt={}",
                        event.getId(), event.getEventType(), event.getAggregateId(), event.getAggregateId(), event.getRetryCount() + 1);
            } catch (Exception e) {
                int attempts = event.getRetryCount() + 1;
                event.setRetryCount(attempts);

                if (attempts >= MAX_RETRIES) {
                    event.setStatus("FAILED");
                    event.setFailedAt(LocalDateTime.now());
                    event.setNextRetryAt(null);
                    log.error("Outbox event permanently failed after {} attempts: orderId={} eventId={} eventType={}",
                            attempts, event.getAggregateId(), event.getId(), event.getEventType(), e);
                } else {
                    long delaySeconds = BASE_DELAY_SECONDS * (long) Math.pow(2, attempts);
                    event.setNextRetryAt(LocalDateTime.now().plusSeconds(delaySeconds));
                    log.warn("Outbox event failed, retry {} of {} in {}s: orderId={} eventId={} eventType={}",
                            attempts, MAX_RETRIES, delaySeconds,
                            event.getAggregateId(), event.getId(), event.getEventType());
                }
            }
            outboxEventRepository.save(event);
        }
    }

    @CircuitBreaker(name = "kafkaBroker", fallbackMethod = "kafkaFallback")
    public void publishToKafka(OutboxEvent event) throws Exception {
        Object payload = objectMapper.readValue(event.getPayload(), Object.class);
        kafkaTemplate.send(event.getTopic(), event.getAggregateId(), payload)
                .get(KAFKA_SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    public void kafkaFallback(OutboxEvent event, Throwable t) {
        log.error("Kafka circuit breaker OPEN — broker unavailable, skipping publish for event id={} type={}",
                event.getId(), event.getEventType());
        throw new RuntimeException("Kafka broker circuit breaker is OPEN", t);
    }
}