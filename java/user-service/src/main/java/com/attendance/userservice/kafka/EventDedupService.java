package com.attendance.userservice.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class EventDedupService {

    private static final String PREFIX = "events:processed:";
    private static final Duration TTL = Duration.ofDays(7);

    private final StringRedisTemplate redis;

    public boolean markIfNew(String eventId) {
        if (eventId == null || eventId.isBlank()) return true;
        Boolean ok = redis.opsForValue().setIfAbsent(PREFIX + eventId, "1", TTL);
        return ok != null && ok;
    }
}
