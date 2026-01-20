package com.attendance.attendanceservice.service;

import com.attendance.attendanceservice.dto.UserInfoDto;
import com.attendance.attendanceservice.error.Errors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class UserClient {

    private final RestClient restClient = RestClient.create();

    @Value("${services.user-service.base-url}")
    private String userServiceUrl;

    public UserInfoDto getUserByPublicId(String publicId) {
        try {
            return restClient.get()
                    .uri(userServiceUrl + "/api/internal/users/{publicId}", publicId)
                    .retrieve()
                    .body(UserInfoDto.class);
        } catch (Exception e) {
            throw Errors.notFound("User not found", java.util.Map.of("publicId", publicId));
        }
    }
}
