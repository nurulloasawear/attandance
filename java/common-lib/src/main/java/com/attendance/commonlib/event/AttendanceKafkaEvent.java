package com.attendance.commonlib.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceKafkaEvent implements Serializable {

    private String userId;
    private String type;
    private long timestampEpochMilli;
    private String employeeName;
}
