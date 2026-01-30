package com.attendance.attendanceservice.kafka;

import com.attendance.commonlib.kafka.AttendanceEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@RequiredArgsConstructor
public class AttendanceEventPublisher {

    private final KafkaTemplate<String, AttendanceEvent> kafkaTemplate;

    @Value("${app.kafka.topics.attendance-events}")
    private String topic;

    public void publishAfterCommit(AttendanceEvent event) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void suspend() {}
                @Override public void resume() {}
                @Override public void flush() {}
                @Override public void beforeCommit(boolean readOnly) {}
                @Override public void beforeCompletion() {}
                @Override public void afterCommit() { send(event); }
                @Override public void afterCompletion(int status) {}
            });
            return;
        }
        send(event);
    }

    private void send(AttendanceEvent event) {
        String key = event.userPublicId() != null ? event.userPublicId() : event.eventId();
        kafkaTemplate.send(topic, key, event);
    }
}
