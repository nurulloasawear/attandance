package com.attendance.userservice.service;

import com.attendance.userservice.error.Errors;
import com.attendance.userservice.model.User;
import com.attendance.userservice.model.UserFace;
import com.attendance.userservice.repository.UserFaceRepository;
import com.attendance.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserFaceService {

    private static final Set<String> ALLOWED_FORMATS = Set.of(
            "FACE_TEMPLATE_V1"
    );

    private static final int MAX_BYTES = 256_000; // 256 KB

    private final UserRepository userRepository;
    private final UserFaceRepository userFaceRepository;

    @Transactional
    public void create(String publicId, byte[] bytes, String format) {
        validate(bytes, format);

        if (userFaceRepository.existsByUser_PublicId(publicId)) {
            throw Errors.conflict(
                    "UserFace already exists",
                    Map.of("publicId", publicId)
            );
        }

        User user = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> Errors.notFound(
                        "User not found",
                        Map.of("publicId", publicId)
                ));

        Instant now = Instant.now();

        UserFace face = UserFace.builder()
                .id(UUID.randomUUID())
                .user(user)
                .faceData(bytes)
                .format(normalizeFormat(format))
                .sizeBytes(bytes.length)
                .createdAt(now)
                .updatedAt(now)
                .build();

        userFaceRepository.save(face);
    }

    @Transactional
    public void update(String publicId, byte[] bytes, String format) {
        validate(bytes, format);

        UserFace face = userFaceRepository.findByUser_PublicId(publicId)
                .orElseGet(() -> {
                    User user = userRepository.findByPublicId(publicId)
                            .orElseThrow(() -> Errors.notFound(
                                    "User not found",
                                    Map.of("publicId", publicId)
                            ));
                    Instant now = Instant.now();
                    return UserFace.builder()
                            .id(UUID.randomUUID())
                            .user(user)
                            .createdAt(now)
                            .updatedAt(now)
                            .build();
                });

        face.setFaceData(bytes);
        face.setFormat(normalizeFormat(format));
        face.setSizeBytes(bytes.length);
        face.setUpdatedAt(Instant.now());

        userFaceRepository.save(face);
    }

    @Transactional(readOnly = true)
    public UserFace get(String publicId) {
        return userFaceRepository.findByUser_PublicId(publicId)
                .orElseThrow(() -> Errors.notFound(
                        "UserFace not found",
                        Map.of("publicId", publicId)
                ));
    }

    @Transactional
    public void delete(String publicId) {
        if (!userFaceRepository.existsByUser_PublicId(publicId)) {
            throw Errors.notFound(
                    "UserFace not found",
                    Map.of("publicId", publicId)
            );
        }
        userFaceRepository.deleteByUser_PublicId(publicId);
    }

    private static void validate(byte[] bytes, String format) {
        if (bytes == null || bytes.length == 0) {
            throw Errors.validation(
                    "Face template is empty",
                    Map.of("size", 0)
            );
        }

        if (bytes.length > MAX_BYTES) {
            throw Errors.validation(
                    "Face template too large",
                    Map.of(
                            "maxBytes", MAX_BYTES,
                            "actualBytes", bytes.length
                    )
            );
        }

        String f = normalizeFormat(format);
        if (!ALLOWED_FORMATS.contains(f)) {
            throw Errors.validation(
                    "Unsupported face format",
                    Map.of(
                            "allowed", ALLOWED_FORMATS,
                            "provided", f
                    )
            );
        }
    }

    private static String normalizeFormat(String format) {
        if (format == null || format.isBlank()) return "FACE_TEMPLATE_V1";
        return format.trim().toUpperCase();
    }
}
