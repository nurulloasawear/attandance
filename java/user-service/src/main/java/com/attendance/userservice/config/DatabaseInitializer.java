package com.attendance.userservice.config;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
@RequiredArgsConstructor
public class DatabaseInitializer {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final JdbcTemplate jdbc;
    private final TransactionTemplate tx;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        tx.executeWithoutResult(status -> {
            log.info("DB init: start");

            execCritical("CREATE EXTENSION IF NOT EXISTS pgcrypto");

            execCritical("""
                CREATE OR REPLACE FUNCTION public.set_updated_at()
                RETURNS TRIGGER AS $$
                BEGIN
                    NEW.updated_at = NOW();
                    RETURN NEW;
                END;
                $$ LANGUAGE plpgsql;
            """);

            execCritical("""
                CREATE TABLE IF NOT EXISTS public.users (
                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                    public_id VARCHAR(8) NOT NULL,
                    username  VARCHAR(100) NOT NULL,
                    password  VARCHAR(255) NOT NULL,
                    email     VARCHAR(200) NOT NULL,

                    first_name VARCHAR(100),
                    last_name  VARCHAR(100),
                    role       VARCHAR(50) NOT NULL,
                    is_active  BOOLEAN NOT NULL DEFAULT TRUE,

                    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                    deleted_at TIMESTAMPTZ NULL,

                    created_by VARCHAR(8),
                    updated_by VARCHAR(8)
                )
            """);

            execOptional("ALTER TABLE public.users ADD COLUMN IF NOT EXISTS created_by VARCHAR(8)");
            execOptional("ALTER TABLE public.users ADD COLUMN IF NOT EXISTS updated_by VARCHAR(8)");
            execOptional("ALTER TABLE public.users ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ NULL");

            execOptional("""
                DO $$
                DECLARE c RECORD;
                BEGIN
                    FOR c IN
                        SELECT conname
                        FROM pg_constraint con
                        WHERE con.conrelid = 'public.users'::regclass
                          AND con.contype = 'u'
                          AND (
                              pg_get_constraintdef(con.oid) ILIKE '%(username)%'
                              OR pg_get_constraintdef(con.oid) ILIKE '%(email)%'
                              OR pg_get_constraintdef(con.oid) ILIKE '%(public_id)%'
                          )
                    LOOP
                        EXECUTE format('ALTER TABLE public.users DROP CONSTRAINT %I', c.conname);
                    END LOOP;
                END $$;
            """);

            execCritical("""
                CREATE UNIQUE INDEX IF NOT EXISTS ux_users_public_id_active
                ON public.users (public_id)
                WHERE deleted_at IS NULL
            """);
            execCritical("""
                CREATE UNIQUE INDEX IF NOT EXISTS ux_users_username_active
                ON public.users (username)
                WHERE deleted_at IS NULL
            """);
            execCritical("""
                CREATE UNIQUE INDEX IF NOT EXISTS ux_users_email_active
                ON public.users (email)
                WHERE deleted_at IS NULL
            """);

            execOptional("CREATE INDEX IF NOT EXISTS ix_users_deleted_at ON public.users (deleted_at)");
            execOptional("CREATE INDEX IF NOT EXISTS ix_users_is_active  ON public.users (is_active)");
            execOptional("CREATE INDEX IF NOT EXISTS ix_users_created_at  ON public.users (created_at DESC)");

            execOptional("""
                DO $$
                BEGIN
                    IF NOT EXISTS (
                        SELECT 1 FROM pg_trigger
                        WHERE tgrelid = 'public.users'::regclass
                          AND tgname  = 'trg_users_set_updated_at'
                    ) THEN
                        CREATE TRIGGER trg_users_set_updated_at
                        BEFORE UPDATE ON public.users
                        FOR EACH ROW
                        EXECUTE FUNCTION public.set_updated_at();
                    END IF;
                END $$;
            """);

            execCritical("""
                CREATE TABLE IF NOT EXISTS public.refresh_tokens (
                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                    user_id UUID NOT NULL,
                    token_hash VARCHAR(255) NOT NULL UNIQUE,
                    expires_at TIMESTAMPTZ NOT NULL,
                    revoked BOOLEAN NOT NULL DEFAULT FALSE,
                    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
                )
            """);

            execOptional("""
                DO $$
                BEGIN
                    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_refresh_tokens_user_id') THEN
                        ALTER TABLE public.refresh_tokens
                        ADD CONSTRAINT fk_refresh_tokens_user_id
                        FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;
                    END IF;
                END $$;
            """);

            execOptional("ALTER TABLE public.refresh_tokens ALTER COLUMN revoked SET DEFAULT FALSE");
            execOptional("ALTER TABLE public.refresh_tokens ALTER COLUMN created_at SET DEFAULT NOW()");
            execOptional("CREATE INDEX IF NOT EXISTS ix_refresh_tokens_user_id   ON public.refresh_tokens (user_id)");
            execOptional("CREATE INDEX IF NOT EXISTS ix_refresh_tokens_expires_at ON public.refresh_tokens (expires_at)");
            execOptional("CREATE INDEX IF NOT EXISTS ix_refresh_tokens_revoked    ON public.refresh_tokens (revoked)");

            execCritical("""
                CREATE TABLE IF NOT EXISTS public.user_faces (
                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                    user_id UUID NOT NULL,

                    face_data  BYTEA NOT NULL,
                    format     VARCHAR(50) NOT NULL DEFAULT 'FACE_TEMPLATE_V1',
                    size_bytes BIGINT NOT NULL,

                    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
                )
            """);

            execOptional("""
                DO $$
                BEGIN
                    IF EXISTS (
                        SELECT 1 FROM information_schema.columns
                        WHERE table_schema='public' AND table_name='user_faces' AND column_name='content_type'
                    )
                    AND NOT EXISTS (
                        SELECT 1 FROM information_schema.columns
                        WHERE table_schema='public' AND table_name='user_faces' AND column_name='format'
                    )
                    THEN
                        ALTER TABLE public.user_faces RENAME COLUMN content_type TO format;
                    END IF;

                    IF EXISTS (
                        SELECT 1 FROM information_schema.columns
                        WHERE table_schema='public' AND table_name='user_faces' AND column_name='content_type'
                    )
                    AND EXISTS (
                        SELECT 1 FROM information_schema.columns
                        WHERE table_schema='public' AND table_name='user_faces' AND column_name='format'
                    )
                    THEN
                        UPDATE public.user_faces
                        SET format = content_type
                        WHERE (format IS NULL OR trim(format) = '')
                          AND (content_type IS NOT NULL AND trim(content_type) <> '');

                        ALTER TABLE public.user_faces DROP COLUMN IF EXISTS content_type;
                    END IF;
                END $$;
            """);

            execOptional("""
                UPDATE public.user_faces
                SET format = 'FACE_TEMPLATE_V1'
                WHERE format IS NULL OR trim(format) = ''
            """);

            execOptional("ALTER TABLE public.user_faces ALTER COLUMN format     SET DEFAULT 'FACE_TEMPLATE_V1'");
            execOptional("ALTER TABLE public.user_faces ALTER COLUMN created_at SET DEFAULT NOW()");
            execOptional("ALTER TABLE public.user_faces ALTER COLUMN updated_at SET DEFAULT NOW()");

            execOptional("""
                DO $$
                BEGIN
                    IF NOT EXISTS (
                        SELECT 1 FROM pg_constraint WHERE conname = 'ck_user_faces_format_allowed'
                    ) THEN
                        ALTER TABLE public.user_faces
                        ADD CONSTRAINT ck_user_faces_format_allowed
                        CHECK (format IN ('FACE_TEMPLATE_V1'));
                    END IF;
                END $$;
            """);

            execOptional("""
                CREATE UNIQUE INDEX IF NOT EXISTS ux_user_faces_user_id
                ON public.user_faces (user_id)
            """);

            execOptional("""
                DO $$
                BEGIN
                    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_user_faces_user_id') THEN
                        ALTER TABLE public.user_faces
                        ADD CONSTRAINT fk_user_faces_user_id
                        FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;
                    END IF;
                END $$;
            """);

            execOptional("CREATE INDEX IF NOT EXISTS ix_user_faces_user_id ON public.user_faces (user_id)");

            execOptional("""
                DO $$
                BEGIN
                    IF NOT EXISTS (
                        SELECT 1 FROM pg_trigger
                        WHERE tgrelid = 'public.user_faces'::regclass
                          AND tgname  = 'trg_user_faces_set_updated_at'
                    ) THEN
                        CREATE TRIGGER trg_user_faces_set_updated_at
                        BEFORE UPDATE ON public.user_faces
                        FOR EACH ROW
                        EXECUTE FUNCTION public.set_updated_at();
                    END IF;
                END $$;
            """);

            execCritical("""
                CREATE TABLE IF NOT EXISTS public.user_audit_logs (
                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                    user_id UUID NULL,
                    user_public_id VARCHAR(50),

                    action VARCHAR(50),
                    actor  VARCHAR(100),
                    sid    VARCHAR(100),

                    ip     VARCHAR(100),
                    device VARCHAR(255),

                    details TEXT,
                    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
                )
            """);

            execOptional("""
                DO $$
                BEGIN
                    IF EXISTS (
                        SELECT 1 FROM information_schema.columns
                        WHERE table_schema='public' AND table_name='user_audit_logs' AND column_name='actor_public_id'
                    ) AND NOT EXISTS (
                        SELECT 1 FROM information_schema.columns
                        WHERE table_schema='public' AND table_name='user_audit_logs' AND column_name='user_public_id'
                    ) THEN
                        ALTER TABLE public.user_audit_logs RENAME COLUMN actor_public_id TO user_public_id;
                    END IF;

                    IF EXISTS (
                        SELECT 1 FROM information_schema.columns
                        WHERE table_schema='public' AND table_name='user_audit_logs' AND column_name='session_id'
                    ) AND NOT EXISTS (
                        SELECT 1 FROM information_schema.columns
                        WHERE table_schema='public' AND table_name='user_audit_logs' AND column_name='sid'
                    ) THEN
                        ALTER TABLE public.user_audit_logs RENAME COLUMN session_id TO sid;
                    END IF;

                    IF EXISTS (
                        SELECT 1 FROM information_schema.columns
                        WHERE table_schema='public' AND table_name='user_audit_logs' AND column_name='message'
                    ) AND NOT EXISTS (
                        SELECT 1 FROM information_schema.columns
                        WHERE table_schema='public' AND table_name='user_audit_logs' AND column_name='details'
                    ) THEN
                        ALTER TABLE public.user_audit_logs RENAME COLUMN message TO details;
                    END IF;
                END $$;
            """);

            execOptional("""
                DO $$
                BEGIN
                    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_user_audit_logs_user_id') THEN
                        ALTER TABLE public.user_audit_logs
                        ADD CONSTRAINT fk_user_audit_logs_user_id
                        FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE SET NULL;
                    END IF;
                END $$;
            """);

            execOptional("ALTER TABLE public.user_audit_logs ALTER COLUMN created_at SET DEFAULT NOW()");
            execOptional("""
                CREATE INDEX IF NOT EXISTS ix_user_audit_logs_user_id_created_at
                ON public.user_audit_logs (user_id, created_at DESC)
            """);
            execOptional("""
                CREATE INDEX IF NOT EXISTS ix_user_audit_logs_action_created_at
                ON public.user_audit_logs (action, created_at DESC)
            """);
            execOptional("""
                CREATE INDEX IF NOT EXISTS ix_user_audit_logs_user_public_id_created_at
                ON public.user_audit_logs (user_public_id, created_at DESC)
            """);
            execOptional("""
                CREATE INDEX IF NOT EXISTS ix_user_audit_logs_sid_created_at
                ON public.user_audit_logs (sid, created_at DESC)
            """);

            execCritical("""
                CREATE TABLE IF NOT EXISTS public.user_devices (
                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                    user_id UUID NOT NULL,

                    session_id VARCHAR(255),
                    ip         VARCHAR(255),
                    user_agent TEXT,

                    first_seen_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                    last_seen_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                    banned BOOLEAN NOT NULL DEFAULT FALSE,
                    banned_at TIMESTAMPTZ,
                    banned_reason TEXT,

                    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                    device_key VARCHAR(255)
                )
            """);

            execOptional("ALTER TABLE public.user_devices ALTER COLUMN id SET DEFAULT gen_random_uuid()");

            execOptional("ALTER TABLE public.user_devices ADD COLUMN IF NOT EXISTS device_key VARCHAR(255)");
            execOptional("ALTER TABLE public.user_devices ALTER COLUMN device_key TYPE VARCHAR(255)");

            // фикс banned=null
            execOptional("ALTER TABLE public.user_devices ALTER COLUMN banned SET DEFAULT FALSE");
            execOptional("UPDATE public.user_devices SET banned = FALSE WHERE banned IS NULL");

            execOptional("ALTER TABLE public.user_devices ALTER COLUMN first_seen_at SET DEFAULT NOW()");
            execOptional("ALTER TABLE public.user_devices ALTER COLUMN last_seen_at  SET DEFAULT NOW()");
            execOptional("ALTER TABLE public.user_devices ALTER COLUMN created_at    SET DEFAULT NOW()");
            execOptional("ALTER TABLE public.user_devices ALTER COLUMN updated_at    SET DEFAULT NOW()");

            execOptional("UPDATE public.user_devices SET first_seen_at = NOW() WHERE first_seen_at IS NULL");
            execOptional("UPDATE public.user_devices SET last_seen_at  = NOW() WHERE last_seen_at  IS NULL");
            execOptional("UPDATE public.user_devices SET created_at    = NOW() WHERE created_at    IS NULL");
            execOptional("UPDATE public.user_devices SET updated_at    = NOW() WHERE updated_at    IS NULL");

            execOptional("""
                DO $$
                BEGIN
                    BEGIN
                        ALTER TABLE public.user_devices ALTER COLUMN banned SET NOT NULL;
                    EXCEPTION WHEN others THEN
                    END;
                END $$;
            """);

            execOptional("""
                DO $$
                BEGIN
                    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_user_devices_user_id') THEN
                        ALTER TABLE public.user_devices
                        ADD CONSTRAINT fk_user_devices_user_id
                        FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;
                    END IF;
                END $$;
            """);

            execOptional("CREATE INDEX IF NOT EXISTS idx_user_devices_user_id    ON public.user_devices(user_id)");
            execOptional("CREATE INDEX IF NOT EXISTS idx_user_devices_session_id ON public.user_devices(session_id)");
            execOptional("CREATE INDEX IF NOT EXISTS idx_user_devices_banned     ON public.user_devices(banned)");
            execOptional("CREATE INDEX IF NOT EXISTS idx_user_devices_device_key ON public.user_devices(device_key)");

            execOptional("""
                DO $$
                BEGIN
                    IF NOT EXISTS (
                        SELECT 1 FROM pg_trigger
                        WHERE tgrelid = 'public.user_devices'::regclass
                          AND tgname  = 'trg_user_devices_set_updated_at'
                    ) THEN
                        CREATE TRIGGER trg_user_devices_set_updated_at
                        BEFORE UPDATE ON public.user_devices
                        FOR EACH ROW
                        EXECUTE FUNCTION public.set_updated_at();
                    END IF;
                END $$;
            """);

            execCritical("""
                CREATE TABLE IF NOT EXISTS public.attendance_event_log (
                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                    event_id VARCHAR(64) NOT NULL,
                    type     VARCHAR(32) NOT NULL,

                    record_id UUID NULL,
                    user_id UUID NULL,
                    user_public_id VARCHAR(64) NOT NULL,

                    work_date DATE NULL,
                    occurred_at TIMESTAMPTZ NULL,
                    received_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                    actor_public_id VARCHAR(64),
                    actor_role      VARCHAR(64)
                )
            """);

            execOptional("""
                DO $$
                BEGIN
                    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_attendance_event_id') THEN
                        ALTER TABLE public.attendance_event_log
                        ADD CONSTRAINT uk_attendance_event_id UNIQUE (event_id);
                    END IF;
                END $$;
            """);

            execOptional("CREATE INDEX IF NOT EXISTS ix_att_event_user_id         ON public.attendance_event_log (user_id)");
            execOptional("CREATE INDEX IF NOT EXISTS ix_att_event_user_public_id  ON public.attendance_event_log (user_public_id)");
            execOptional("CREATE INDEX IF NOT EXISTS ix_att_event_work_date       ON public.attendance_event_log (work_date)");
            execOptional("CREATE INDEX IF NOT EXISTS ix_att_event_type            ON public.attendance_event_log (type)");
            execOptional("CREATE INDEX IF NOT EXISTS ix_att_event_occurred_at     ON public.attendance_event_log (occurred_at DESC)");
            execOptional("CREATE INDEX IF NOT EXISTS ix_att_event_received_at     ON public.attendance_event_log (received_at DESC)");

            execOptional("""
                DO $$
                BEGIN
                    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_attendance_event_log_user_id') THEN
                        ALTER TABLE public.attendance_event_log
                        ADD CONSTRAINT fk_attendance_event_log_user_id
                        FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE SET NULL;
                    END IF;
                END $$;
            """);

            log.info("DB init: done");
        });
    }

    private void execCritical(String sql) {
        try {
            jdbc.execute(sql);
        } catch (Exception e) {
            log.error("DB init CRITICAL failed. SQL={}", shortSql(sql), e);
            throw e;
        }
    }

    private void execOptional(String sql) {
        try {
            jdbc.execute(sql);
        } catch (Exception e) {
            log.warn("DB init optional failed (ignored). SQL={} ; reason={}", shortSql(sql), e.getMessage());
        }
    }

    private String shortSql(String sql) {
        String s = (sql == null ? "" : sql.trim().replaceAll("\\s+", " "));
        return s.length() > 220 ? s.substring(0, 220) + " ..." : s;
    }
}