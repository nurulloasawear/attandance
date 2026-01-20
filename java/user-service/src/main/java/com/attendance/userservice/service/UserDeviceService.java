package com.attendance.userservice.service;

public interface UserDeviceService {
    void trackDevice(String userPublicId, String sessionId, String ip, String userAgent);
    boolean isDeviceBanned(String sessionId);
}
