package com.onlinemart.product.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(String status);

    @Query("""
    SELECT e FROM OutboxEvent e
    WHERE e.status = 'PENDING'
      AND (e.nextRetryAt IS NULL OR e.nextRetryAt <= :now)
    ORDER BY e.createdAt ASC
    """)
    List<OutboxEvent> findRetryableEvents(@Param("now") LocalDateTime now);

}