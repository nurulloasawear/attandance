package com.attendance.attendanceservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceEventPublisher {

    private final KafkaTemplate<String, AttendanceEvent> kafkaTemplate;

    @Value("${app.kafka.topics.attendance-events}")
    private String topic;

    public void publishAfterCommit(AttendanceEvent event) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publishSafe(event);
                }
            });
            return;
        }
        publishSafe(event);
    }

    private void publishSafe(AttendanceEvent event) {
        try {
            kafkaTemplate.send(topic, event.userPublicId(), event);
        } catch (Exception e) {
            log.error("Kafka publish failed: {}", e.getMessage(), e);
        }
    }
}
