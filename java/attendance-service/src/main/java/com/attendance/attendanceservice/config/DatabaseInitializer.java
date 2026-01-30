package com.attendance.attendanceservice.config;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class DatabaseInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final JdbcTemplate jdbc;
    private final TransactionTemplate tx;

    private final AtomicBoolean initialized = new AtomicBoolean(false);

    @Override
    public void run(ApplicationArguments args) {
        if (!initialized.compareAndSet(false, true)) return;

        log.info("Attendance DB init: start");

        tx.executeWithoutResult(status -> {
            execCritical("CREATE EXTENSION IF NOT EXISTS pgcrypto");

            execCritical("""
                CREATE OR REPLACE FUNCTION set_updated_at()
                RETURNS TRIGGER AS $$
                BEGIN
                    NEW.updated_at = NOW();
                    RETURN NEW;
                END;
                $$ LANGUAGE plpgsql;
            """);

            execCritical("""
                CREATE TABLE IF NOT EXISTS public.attendance_records (
                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                    user_id UUID NOT NULL,
                    user_public_id VARCHAR(64) NOT NULL,

                    work_date DATE NOT NULL,
                    check_in TIMESTAMPTZ NULL,
                    check_out TIMESTAMPTZ NULL,
                    status VARCHAR(32) NOT NULL DEFAULT 'PRESENT',
                    note TEXT NULL,

                    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                    deleted_at TIMESTAMPTZ NULL,

                    created_by VARCHAR(64),
                    updated_by VARCHAR(64)
                );
            """);

            execOptional("ALTER TABLE public.attendance_records ALTER COLUMN user_public_id TYPE VARCHAR(64)");
            execOptional("ALTER TABLE public.attendance_records ALTER COLUMN created_by TYPE VARCHAR(64)");
            execOptional("ALTER TABLE public.attendance_records ALTER COLUMN updated_by TYPE VARCHAR(64)");

            execCritical("""
                CREATE UNIQUE INDEX IF NOT EXISTS ux_attendance_user_date_active
                ON public.attendance_records(user_id, work_date)
                WHERE deleted_at IS NULL;
            """);

            execOptional("""
                CREATE INDEX IF NOT EXISTS ix_attendance_work_date
                ON public.attendance_records(work_date);
            """);

            execOptional("""
                CREATE INDEX IF NOT EXISTS ix_attendance_user_public_id
                ON public.attendance_records(user_public_id);
            """);

            execOptional("""
                DO $$
                BEGIN
                    IF NOT EXISTS (
                        SELECT 1 FROM pg_trigger WHERE tgname = 'trg_attendance_records_set_updated_at'
                    ) THEN
                        CREATE TRIGGER trg_attendance_records_set_updated_at
                        BEFORE UPDATE ON public.attendance_records
                        FOR EACH ROW
                        EXECUTE FUNCTION set_updated_at();
                    END IF;
                END $$;
            """);
        });

        log.info("Attendance DB init: done");
    }

    private void execCritical(String sql) {
        try {
            jdbc.execute(sql);
        } catch (Exception e) {
            log.error("Attendance DB init CRITICAL failed. SQL: {}", shortSql(sql), e);
            throw e;
        }
    }

    private void execOptional(String sql) {
        try {
            jdbc.execute(sql);
        } catch (Exception e) {
            log.warn("Attendance DB init optional failed (ignored). SQL: {} ; reason={}", shortSql(sql), e.getMessage());
        }
    }

    private String shortSql(String sql) {
        String s = sql == null ? "" : sql.trim().replaceAll("\\s+", " ");
        return s.length() > 220 ? s.substring(0, 220) + " ..." : s;
    }
}
