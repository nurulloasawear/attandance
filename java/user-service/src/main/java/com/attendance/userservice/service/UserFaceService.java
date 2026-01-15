package com.attendance.userservice.service;

import com.attendance.userservice.model.User;
import com.attendance.userservice.model.UserFace;
import com.attendance.userservice.repository.UserFaceRepository;
import com.attendance.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
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
            throw new IllegalStateException("UserFace already exists (use PUT to update)");
        }

        User user = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalStateException("User not found by publicId: " + publicId));

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
                            .orElseThrow(() -> new IllegalStateException("User not found by publicId: " + publicId));
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
                .orElseThrow(() -> new IllegalStateException("UserFace not found"));
    }

    @Transactional
    public void delete(String publicId) {
        if (!userFaceRepository.existsByUser_PublicId(publicId)) return;
        userFaceRepository.deleteByUser_PublicId(publicId);
    }

    private static void validate(byte[] bytes, String format) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalStateException("faceData is empty");
        }
        if (bytes.length > MAX_BYTES) {
            throw new IllegalStateException("Face template too large (max " + MAX_BYTES + " bytes)");
        }

        String f = normalizeFormat(format);
        if (!ALLOWED_FORMATS.contains(f)) {
            throw new IllegalStateException("Unsupported face format: " + f);
        }
    }

    private static String normalizeFormat(String format) {
        if (format == null || format.isBlank()) return "FACE_TEMPLATE_V1";
        return format.trim().toUpperCase();
    }
}
