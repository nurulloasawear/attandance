//package com.attendance.attendanceservice.service;
//
//import com.attendance.attendanceservice.model.AttendanceEventLog;
//import com.attendance.attendanceservice.repository.AttendanceEventLogRepository;
//import com.attendance.userservice.kafka.dto.AttendanceEvent;
//import lombok.RequiredArgsConstructor;
//import org.springframework.dao.DataIntegrityViolationException;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//@Service
//@RequiredArgsConstructor
//public class AttendanceEventLogService {
//
//    private final AttendanceEventLogRepository repo;
//
//    @Transactional
//    public void handle(AttendanceEvent event) {
//        if (event == null || event.eventId() == null || event.eventId().isBlank()) return;
//
//        AttendanceEventLog log = AttendanceEventLog.builder()
//                .eventId(event.eventId())
//                .type(event.type())
//                .recordId(event.recordId())
//                .userId(event.userId())
//                .userPublicId(event.userPublicId())
//                .workDate(event.workDate())
//                .occurredAt(event.occurredAt())
//                .build();
//
//        try {
//            repo.save(log);
//        } catch (DataIntegrityViolationException ignored) {
//        }
//    }
//}
