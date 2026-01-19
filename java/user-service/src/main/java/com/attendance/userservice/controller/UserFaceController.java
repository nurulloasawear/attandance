package com.attendance.userservice.controller;

import com.attendance.userservice.dto.FaceTemplateRequest;
import com.attendance.userservice.error.Errors;
import com.attendance.userservice.model.UserFace;
import com.attendance.userservice.service.UserFaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/userface")
@RequiredArgsConstructor
public class UserFaceController {

    private final UserFaceService userFaceService;

    // ✅ CREATE (JSON + Base64)
    @PostMapping(
            value = "/{publicId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, Object>> create(
            @PathVariable String publicId,
            @RequestBody FaceTemplateRequest request
    ) {
        if (publicId == null || publicId.isBlank()) {
            throw Errors.badRequest("publicId is required");
        }

        String format = normalizeFormat(request.getFormat());
        byte[] data = decodeBase64(request.getDataBase64());

        userFaceService.create(publicId, data, format);

        return ResponseEntity.status(201).body(Map.of(
                "status", "created",
                "publicId", publicId,
                "format", format,
                "sizeBytes", data.length
        ));
    }


    @PutMapping(
            value = "/{publicId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable String publicId,
            @RequestBody FaceTemplateRequest request
    ) {
        if (publicId == null || publicId.isBlank()) {
            throw Errors.badRequest("publicId is required");
        }

        String format = normalizeFormat(request.getFormat());
        byte[] data = decodeBase64(request.getDataBase64());

        userFaceService.update(publicId, data, format);

        return ResponseEntity.ok(Map.of(
                "status", "updated",
                "publicId", publicId,
                "format", format,
                "sizeBytes", data.length
        ));
    }


    @GetMapping(
            value = "/{publicId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, Object>> get(@PathVariable String publicId) {
        if (publicId == null || publicId.isBlank()) {
            throw Errors.badRequest("publicId is required");
        }

        UserFace face = userFaceService.get(publicId);

        return ResponseEntity.ok(Map.of(
                "publicId", publicId,
                "format", face.getFormat() == null ? "FACE_TEMPLATE_V1" : face.getFormat(),
                "sizeBytes", face.getSizeBytes(),
                "dataBase64", Base64.getEncoder().encodeToString(face.getFaceData())
        ));
    }


    @GetMapping(
            value = "/{publicId}/meta",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, Object>> getMeta(@PathVariable String publicId) {
        if (publicId == null || publicId.isBlank()) {
            throw Errors.badRequest("publicId is required");
        }

        UserFace face = userFaceService.get(publicId);

        return ResponseEntity.ok(Map.of(
                "publicId", publicId,
                "format", face.getFormat() == null ? "FACE_TEMPLATE_V1" : face.getFormat(),
                "sizeBytes", face.getSizeBytes(),
                "createdAt", face.getCreatedAt(),
                "updatedAt", face.getUpdatedAt()
        ));
    }


    @GetMapping(
            value = "/{publicId}/exists",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, Object>> exists(@PathVariable String publicId) {
        if (publicId == null || publicId.isBlank()) {
            throw Errors.badRequest("publicId is required");
        }

        try {
            userFaceService.get(publicId);
            return ResponseEntity.ok(Map.of(
                    "publicId", publicId,
                    "exists", true
            ));
        } catch (RuntimeException e) {
            // если notFound -> значит нет face
            return ResponseEntity.ok(Map.of(
                    "publicId", publicId,
                    "exists", false
            ));
        }
    }

    @DeleteMapping(
            value = "/{publicId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, Object>> delete(@PathVariable String publicId) {
        if (publicId == null || publicId.isBlank()) {
            throw Errors.badRequest("publicId is required");
        }

        userFaceService.delete(publicId);

        return ResponseEntity.ok(Map.of(
                "status", "deleted",
                "publicId", publicId
        ));
    }


    private byte[] decodeBase64(String dataBase64) {
        if (dataBase64 == null || dataBase64.isBlank()) {
            throw Errors.badRequest("dataBase64 is required");
        }
        try {
            return Base64.getDecoder().decode(dataBase64);
        } catch (IllegalArgumentException e) {
            throw Errors.badRequest("dataBase64 is invalid");
        }
    }

    private String normalizeFormat(String format) {
        if (format == null || format.isBlank()) return "FACE_TEMPLATE_V1";
        return format.trim().toUpperCase();
    }
}
