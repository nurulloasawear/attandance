package com.attendance.attendanceservice.service;

import com.attendance.attendanceservice.dto.UserInfoDto;
import com.attendance.attendanceservice.error.ApiException;
import com.attendance.attendanceservice.error.Errors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Map;

@Slf4j
@Service
public class UserClient {

    private final RestClient restClient;

    public UserClient(
            RestClient.Builder builder,
            @Value("${services.user-service.base-url:${USER_SERVICE_BASE_URL:http://user-service:8081}}")
            String baseUrl
    ) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw Errors.internal("services.user-service.base-url is not configured");
        }

        this.restClient = builder
                .baseUrl(baseUrl.trim())
                .build();
    }

    public UserInfoDto getUserByPublicId(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            throw Errors.badRequest("publicId is required");
        }

        String bearer = resolveBearerTokenOrNull();
        if (bearer == null || bearer.isBlank()) {
            // тут лучше сразу 401/403, потому что это вызов из защищённого attendance endpoint
            throw Errors.forbidden("Missing bearer token for user-service call");
        }

        try {
            return restClient.get()
                    .uri("/api/internal/users/{publicId}", publicId.trim())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearer)
                    .retrieve()
                    .body(UserInfoDto.class);

        } catch (ApiException e) {
            // ВАЖНО: если где-то выше выбросили Errors.xxx — НЕ превращаем в 500
            throw e;

        } catch (RestClientResponseException e) {
            int status = e.getStatusCode().value();
            String body = e.getResponseBodyAsString();
            log.warn("UserClient error: status={} body={}", status, body);

            if (status == 404) {
                throw Errors.notFound("User not found", Map.of("publicId", publicId));
            }
            if (status == 401) {
                throw Errors.forbidden("User-service unauthorized"); // если есть Errors.unauthorized — лучше его
            }
            if (status == 403) {
                throw Errors.forbidden("User-service access denied");
            }
            if (status >= 500) {
                throw Errors.internal("User-service unavailable");
            }

            throw Errors.badRequest("Invalid request to user-service", Map.of(
                    "publicId", publicId,
                    "status", status
            ));

        } catch (Exception e) {
            log.error("UserClient unexpected error", e);
            throw Errors.internal("Failed to connect to user-service");
        }
    }

    private String resolveBearerTokenOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;

        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken().getTokenValue();
        }

        Object cred = auth.getCredentials();
        return cred == null ? null : cred.toString();
    }
}
