package com.attendance.commonlib.event;

import java.io.Serializable;

public class AttendanceKafkaEvent implements Serializable {

    private String userId;
    private String type;
    private long timestampEpochMilli;
    private String employeeName;

    public AttendanceKafkaEvent() {
    }

    public AttendanceKafkaEvent(String userId, String type, long timestampEpochMilli, String employeeName) {
        this.userId = userId;
        this.type = type;
        this.timestampEpochMilli = timestampEpochMilli;
        this.employeeName = employeeName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTimestampEpochMilli() {
        return timestampEpochMilli;
    }

    public void setTimestampEpochMilli(long timestampEpochMilli) {
        this.timestampEpochMilli = timestampEpochMilli;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }
}
