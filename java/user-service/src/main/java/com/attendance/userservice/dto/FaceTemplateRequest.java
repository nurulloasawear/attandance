package com.attendance.userservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FaceTemplateRequest {
    private String format;
    private String dataBase64;
}
