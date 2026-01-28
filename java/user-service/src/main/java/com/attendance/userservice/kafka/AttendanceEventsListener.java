package com.attendance.userservice.kafka;

import com.attendance.userservice.kafka.dto.AttendanceEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceEventsListener {

    private final EventDedupService dedupService;

    @KafkaListener(
            topics = "${app.kafka.topics.attendance-events}",
            groupId = "${app.kafka.groups.attendance-events}"
    )
    public void listen(AttendanceEvent event) {
        if (event == null) {
            return;
        }
        String eventId = event.eventId();
        if (eventId != null && !eventId.isBlank() && !dedupService.markIfNew(eventId)) {
            return;
        }
        log.info("AttendanceEvent received: type={}, userPublicId={}, recordId={}, occurredAt={}",
                event.type(), event.userPublicId(), event.recordId(), event.occurredAt());
    }
}
