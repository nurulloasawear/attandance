package com.attendance.userservice.model.audit;

public enum UserAction {
    USER_CREATED,
    USER_UPDATED,
    USER_DEACTIVATED,
    USER_DELETED,

    LOGIN,
    LOGOUT,
    REFRESH
}
