package com.attendance.userservice.kafka;

import com.attendance.commonlib.kafka.AttendanceEvent;
import com.attendance.userservice.attendanceevent.service.AttendanceEventLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AttendanceEventsListener {

    private final AttendanceEventLogService logService;

    @KafkaListener(
            topics = "${app.kafka.topics.attendance-events}",
            groupId = "${app.kafka.groups.attendance-events}"
    )
    public void listen(AttendanceEvent event) {
        logService.handle(event);
    }
}
