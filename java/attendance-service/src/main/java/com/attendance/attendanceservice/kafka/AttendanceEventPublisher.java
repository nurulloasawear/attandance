package com.attendance.attendanceservice.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AttendanceEventPublisher {

    private final KafkaTemplate<String, AttendanceEvent> kafkaTemplate;

    @Value("${app.kafka.topics.attendance-events}")
    private String topic;

    public void publish(AttendanceEvent event) {
        kafkaTemplate.send(topic, event.userPublicId(), event);
    }
}
