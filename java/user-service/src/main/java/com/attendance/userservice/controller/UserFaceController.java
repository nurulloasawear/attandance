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

    @PostMapping(value = "/{publicId}", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<?> create(
            @PathVariable String publicId,
            @RequestBody byte[] data,
            @RequestHeader(value = "X-Face-Format", required = false) String format
    ) {
        userFaceService.create(publicId, data, format);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("status", "created"));
    }

    @PutMapping(value = "/{publicId}", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<?> update(
            @PathVariable String publicId,
            @RequestBody byte[] data,
            @RequestHeader(value = "X-Face-Format", required = false) String format
    ) {
        userFaceService.update(publicId, data, format);
        return ResponseEntity.ok(Map.of("status", "updated"));
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<byte[]> get(@PathVariable String publicId) {
        UserFace face = userFaceService.get(publicId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(face.getSizeBytes()))
                .header("X-Face-Format", face.getFormat() == null ? "" : face.getFormat())
                .body(face.getFaceData());
    }

    @DeleteMapping("/{publicId}")
    public ResponseEntity<?> delete(@PathVariable String publicId) {
        userFaceService.delete(publicId);
        return ResponseEntity.ok(Map.of("status", "deleted"));
    }
}
