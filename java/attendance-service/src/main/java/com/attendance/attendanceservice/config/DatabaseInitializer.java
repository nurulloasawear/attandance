package com.attendance.attendanceservice.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DatabaseInitializer {

    private final JdbcTemplate jdbc;

    @PostConstruct
    @Transactional
    public void init() {

        jdbc.execute("CREATE EXTENSION IF NOT EXISTS pgcrypto;");

        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS attendance_records (
                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                user_id UUID NOT NULL,
                user_public_id VARCHAR(8) NOT NULL,

                work_date DATE NOT NULL,
                check_in TIMESTAMPTZ NULL,
                check_out TIMESTAMPTZ NULL,
                status VARCHAR(32) NOT NULL DEFAULT 'PRESENT',
                note TEXT NULL,

                created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                deleted_at TIMESTAMPTZ NULL,

                created_by VARCHAR(8),
                updated_by VARCHAR(8)
            );
        """);

        jdbc.execute("""
            CREATE UNIQUE INDEX IF NOT EXISTS ux_attendance_user_date
            ON attendance_records(user_id, work_date)
            WHERE deleted_at IS NULL;
        """);

        jdbc.execute("""
            CREATE INDEX IF NOT EXISTS ix_attendance_work_date
            ON attendance_records(work_date);
        """);

        jdbc.execute("""
            CREATE INDEX IF NOT EXISTS ix_attendance_user_public_id
            ON attendance_records(user_public_id);
        """);
    }
}
