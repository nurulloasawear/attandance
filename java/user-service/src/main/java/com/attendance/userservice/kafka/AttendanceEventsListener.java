package com.attendance.userservice.kafka;

import com.attendance.userservice.attendanceevent.service.AttendanceEventLogService;
import com.attendance.userservice.kafka.dto.AttendanceEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AttendanceEventsListener {

    private final EventDedupService dedupService;
    private final AttendanceEventLogService logService;

    @KafkaListener(
            topics = "${app.kafka.topics.attendance-events}",
            groupId = "${app.kafka.groups.attendance-events}"
    )
    public void listen(AttendanceEvent event) {
        if (event == null) return;

        String eventId = event.eventId();
        if (eventId != null && !eventId.isBlank() && !dedupService.markIfNew(eventId)) {
            return;
        }

        logService.saveIfNew(event);
    }
}
