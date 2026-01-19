package com.attendance.userservice.controller;

import com.attendance.userservice.model.UserFace;
import com.attendance.userservice.service.UserFaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/userface")
@RequiredArgsConstructor
public class UserFaceController {

    private final UserFaceService userFaceService;

    @PostMapping(
            value = "/{publicId}",
            consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, Object>> create(
            @PathVariable String publicId,
            @RequestBody byte[] data,
            @RequestHeader(value = "X-Face-Format", required = false) String format
    ) {
        userFaceService.create(publicId, data, format);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "status", "created",
                        "publicId", publicId
                ));
    }

    @PutMapping(
            value = "/{publicId}",
            consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable String publicId,
            @RequestBody byte[] data,
            @RequestHeader(value = "X-Face-Format", required = false) String format
    ) {
        userFaceService.update(publicId, data, format);

        return ResponseEntity.ok(Map.of(
                "status", "updated",
                "publicId", publicId
        ));
    }

    @GetMapping(
            value = "/{publicId}",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public ResponseEntity<byte[]> get(@PathVariable String publicId) {
        UserFace face = userFaceService.get(publicId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(face.getSizeBytes()))
                .header("X-Face-Format", face.getFormat() == null ? "FACE_TEMPLATE_V1" : face.getFormat())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"face-template.bin\"")
                .body(face.getFaceData());
    }

    @GetMapping(
            value = "/{publicId}/meta",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, Object>> getMeta(@PathVariable String publicId) {
        UserFace face = userFaceService.get(publicId);

        return ResponseEntity.ok(Map.of(
                "publicId", publicId,
                "format", face.getFormat() == null ? "FACE_TEMPLATE_V1" : face.getFormat(),
                "sizeBytes", face.getSizeBytes(),
                "createdAt", face.getCreatedAt(),
                "updatedAt", face.getUpdatedAt()
        ));
    }

    @DeleteMapping(
            value = "/{publicId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, Object>> delete(@PathVariable String publicId) {
        userFaceService.delete(publicId);

        return ResponseEntity.ok(Map.of(
                "status", "deleted",
                "publicId", publicId
        ));
    }
}
