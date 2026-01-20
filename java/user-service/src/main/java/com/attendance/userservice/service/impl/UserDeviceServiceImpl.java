package com.attendance.userservice.service.impl;

import com.attendance.userservice.error.Errors;
import com.attendance.userservice.model.User;
import com.attendance.userservice.model.UserDevice;
import com.attendance.userservice.repository.UserDeviceRepository;
import com.attendance.userservice.repository.UserRepository;
import com.attendance.userservice.service.UserDeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserDeviceServiceImpl implements UserDeviceService {

    private final UserRepository userRepository;
    private final UserDeviceRepository userDeviceRepository;

    @Override
    @Transactional
    public void trackDevice(String userPublicId, String sessionId, String ip, String userAgent) {

        if (sessionId == null || sessionId.isBlank()) return;

        User user = userRepository.findByPublicIdAndDeletedAtIsNull(userPublicId)
                .orElseThrow(() -> Errors.notFound("User not found", Map.of("publicId", userPublicId)));

        Instant now = Instant.now();

        UserDevice device = userDeviceRepository.findBySessionId(sessionId)
                .orElseGet(() -> UserDevice.builder()
                        .id(UUID.randomUUID())
                        .user(user)
                        .sessionId(sessionId)
                        .ip(ip)
                        .userAgent(userAgent)
                        .firstSeenAt(now)
                        .lastSeenAt(now)
                        .banned(false)
                        .build());

        device.setLastSeenAt(now);
        device.setIp(ip);
        device.setUserAgent(userAgent);

        userDeviceRepository.save(device);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isDeviceBanned(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) return false;
        return userDeviceRepository.existsBySessionIdAndBannedTrue(sessionId);
    }
}
