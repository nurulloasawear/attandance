package com.attendance.userservice.audit;

public record AuditContext(
        String actorPublicId,
        String sessionId,
        String ip,
        String device
) {
    public static AuditContext empty() {
        return new AuditContext(null, null, null, null);
    }
}
