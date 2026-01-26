package com.attendance.userservice.kafka;

import com.attendance.userservice.kafka.dto.AttendanceEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class AttendanceEventsListener {

    @KafkaListener(topics = "attendance.events", groupId = "user-service")
    public void listen(AttendanceEvent event) {
        if ("CHECK_IN".equals(event.type())) {
            System.out.println("User checked in: " + event.userPublicId());
        }
        if ("CHECK_OUT".equals(event.type())) {
            System.out.println("User checked out: " + event.userPublicId());
        }
    }
}
