package com.attendance.attendanceservice.service;

import com.attendance.attendanceservice.dto.UserInfoDto;
import com.attendance.attendanceservice.error.Errors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

@Slf4j
@Service
public class UserClient {

    private final RestClient restClient;
    private final String baseUrl;

    public UserClient(
            RestClient.Builder builder,
            @Value("${services.user-service.base-url:${USER_SERVICE_BASE_URL:http://user-service:8080}}")
            String baseUrl
    ) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw Errors.internal("services.user-service.base-url is not configured");
        }
        this.baseUrl = baseUrl.trim();

        this.restClient = builder
                .baseUrl(this.baseUrl)
                .build();
    }

    public UserInfoDto getUserByPublicId(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            throw Errors.badRequest("publicId is required");
        }

        try {
            return restClient.get()
                    .uri("/api/internal/users/{publicId}", publicId.trim())
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        if (res.getStatusCode().value() == 404) {
                            throw Errors.notFound("User not found", Map.of("publicId", publicId));
                        }
                        if (res.getStatusCode().value() == 401 || res.getStatusCode().value() == 403) {
                            throw Errors.forbidden("User-service access denied");
                        }
                        throw Errors.badRequest("Invalid request to user-service");
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        throw Errors.internal("User-service unavailable");
                    })
                    .body(UserInfoDto.class);

        } catch (RestClientResponseException e) {
            log.warn("UserClient error: status={} body={}", e.getStatusCode().value(), e.getResponseBodyAsString());

            int status = e.getStatusCode().value();

            if (status == 404) {
                throw Errors.notFound("User not found", Map.of("publicId", publicId));
            }
            if (status == 401 || status == 403) {
                throw Errors.forbidden("User-service access denied");
            }
            if (status >= 500) {
                throw Errors.internal("User-service unavailable");
            }

            throw Errors.internal("Failed to get user", Map.of(
                    "publicId", publicId,
                    "status", status
            ));

        } catch (Exception e) {
            log.error("UserClient unexpected error", e);
            throw Errors.internal("Failed to connect to user-service");
        }
    }
}
