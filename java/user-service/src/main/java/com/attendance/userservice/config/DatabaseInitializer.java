package com.attendance.userservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
@RequiredArgsConstructor
public class DatabaseInitializer {

    private final JdbcTemplate jdbc;
    private final TransactionTemplate tx;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        tx.executeWithoutResult(status -> {
            safeExec("CREATE EXTENSION IF NOT EXISTS pgcrypto");

            safeExec("""
                CREATE TABLE IF NOT EXISTS users (
                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                    public_id VARCHAR(8) NOT NULL,
                    username VARCHAR(100) NOT NULL,
                    password VARCHAR(255) NOT NULL,
                    email VARCHAR(200) NOT NULL,

                    first_name VARCHAR(100),
                    last_name VARCHAR(100),
                    role VARCHAR(50) NOT NULL,
                    is_active BOOLEAN NOT NULL DEFAULT TRUE,

                    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                    deleted_at TIMESTAMPTZ NULL,

                    created_by VARCHAR(8),
                    updated_by VARCHAR(8)
                )
            """);

            safeExec("ALTER TABLE users ADD COLUMN IF NOT EXISTS created_by VARCHAR(8)");
            safeExec("ALTER TABLE users ADD COLUMN IF NOT EXISTS updated_by VARCHAR(8)");
            safeExec("ALTER TABLE users ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ NULL");

            safeExec("""
                DO $$
                DECLARE c RECORD;
                BEGIN
                    FOR c IN
                        SELECT conname
                        FROM pg_constraint con
                        JOIN pg_class rel ON rel.oid = con.conrelid
                        JOIN pg_namespace nsp ON nsp.oid = rel.relnamespace
                        WHERE rel.relname = 'users'
                          AND nsp.nspname = 'public'
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

            safeExec("""
                CREATE UNIQUE INDEX IF NOT EXISTS ux_users_public_id_active
                ON users (public_id)
                WHERE deleted_at IS NULL
            """);
            safeExec("""
                CREATE UNIQUE INDEX IF NOT EXISTS ux_users_username_active
                ON users (username)
                WHERE deleted_at IS NULL
            """);
            safeExec("""
                CREATE UNIQUE INDEX IF NOT EXISTS ux_users_email_active
                ON users (email)
                WHERE deleted_at IS NULL
            """);

            safeExec("CREATE INDEX IF NOT EXISTS ix_users_deleted_at ON users (deleted_at)");
            safeExec("CREATE INDEX IF NOT EXISTS ix_users_is_active ON users (is_active)");
            safeExec("CREATE INDEX IF NOT EXISTS ix_users_created_at ON users (created_at DESC)");
            safeExec("""
                CREATE INDEX IF NOT EXISTS ix_users_username_active_lookup
                ON users (username)
                WHERE deleted_at IS NULL
            """);
            safeExec("""
                CREATE INDEX IF NOT EXISTS ix_users_email_active_lookup
                ON users (email)
                WHERE deleted_at IS NULL
            """);

            safeExec("""
                CREATE OR REPLACE FUNCTION set_updated_at()
                RETURNS TRIGGER AS $$
                BEGIN
                    NEW.updated_at = NOW();
                    RETURN NEW;
                END;
                $$ LANGUAGE plpgsql;
            """);

            safeExec("""
                DO $$
                BEGIN
                    IF NOT EXISTS (
                        SELECT 1 FROM pg_trigger WHERE tgname = 'trg_users_set_updated_at'
                    ) THEN
                        CREATE TRIGGER trg_users_set_updated_at
                        BEFORE UPDATE ON users
                        FOR EACH ROW
                        EXECUTE FUNCTION set_updated_at();
                    END IF;
                END $$;
            """);

            safeExec("""
                CREATE TABLE IF NOT EXISTS refresh_tokens (
                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                    user_id UUID NOT NULL,
                    token_hash VARCHAR(255) NOT NULL UNIQUE,
                    expires_at TIMESTAMPTZ NOT NULL,
                    revoked BOOLEAN NOT NULL DEFAULT FALSE,
                    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
                )
            """);

            safeExec("""
                DO $$
                BEGIN
                    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_refresh_tokens_user_id') THEN
                        ALTER TABLE refresh_tokens
                        ADD CONSTRAINT fk_refresh_tokens_user_id
                        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
                    END IF;
                END $$;
            """);

            safeExec("CREATE INDEX IF NOT EXISTS ix_refresh_tokens_user_id ON refresh_tokens (user_id)");
            safeExec("CREATE INDEX IF NOT EXISTS ix_refresh_tokens_expires_at ON refresh_tokens (expires_at)");
            safeExec("CREATE INDEX IF NOT EXISTS ix_refresh_tokens_revoked ON refresh_tokens (revoked)");

            safeExec("""
                CREATE TABLE IF NOT EXISTS user_faces (
                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                    user_id UUID NOT NULL,

                    face_data BYTEA NOT NULL,
                    format VARCHAR(50) NOT NULL DEFAULT 'FACE_TEMPLATE_V1',
                    size_bytes BIGINT NOT NULL,

                    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
                )
            """);

            safeExec("""
                DO $$
                BEGIN
                    IF EXISTS (
                        SELECT 1 FROM information_schema.columns
                        WHERE table_schema='public'
                          AND table_name='user_faces'
                          AND column_name='content_type'
                    )
                    AND NOT EXISTS (
                        SELECT 1 FROM information_schema.columns
                        WHERE table_schema='public'
                          AND table_name='user_faces'
                          AND column_name='format'
                    )
                    THEN
                        ALTER TABLE public.user_faces RENAME COLUMN content_type TO format;
                    END IF;

                    IF EXISTS (
                        SELECT 1 FROM information_schema.columns
                        WHERE table_schema='public'
                          AND table_name='user_faces'
                          AND column_name='content_type'
                    )
                    AND EXISTS (
                        SELECT 1 FROM information_schema.columns
                        WHERE table_schema='public'
                          AND table_name='user_faces'
                          AND column_name='format'
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

            safeExec("""
                UPDATE user_faces
                SET format = 'FACE_TEMPLATE_V1'
                WHERE format IS NULL OR trim(format) = ''
            """);
            safeExec("ALTER TABLE user_faces ALTER COLUMN format SET DEFAULT 'FACE_TEMPLATE_V1'");
            safeExec("""
                DO $$
                BEGIN
                    BEGIN
                        ALTER TABLE user_faces ALTER COLUMN format SET NOT NULL;
                    EXCEPTION WHEN others THEN
                    END;
                END $$;
            """);

            safeExec("""
                DO $$
                BEGIN
                    IF NOT EXISTS (
                        SELECT 1 FROM pg_constraint WHERE conname = 'ck_user_faces_format_allowed'
                    ) THEN
                        ALTER TABLE user_faces
                        ADD CONSTRAINT ck_user_faces_format_allowed
                        CHECK (format IN ('FACE_TEMPLATE_V1'));
                    END IF;
                END $$;
            """);

            safeExec("""
                CREATE UNIQUE INDEX IF NOT EXISTS ux_user_faces_user_id
                ON user_faces (user_id)
            """);

            safeExec("""
                DO $$
                BEGIN
                    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_user_faces_user_id') THEN
                        ALTER TABLE user_faces
                        ADD CONSTRAINT fk_user_faces_user_id
                        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
                    END IF;
                END $$;
            """);

            safeExec("CREATE INDEX IF NOT EXISTS ix_user_faces_user_id ON user_faces (user_id)");

            safeExec("""
                DO $$
                BEGIN
                    IF NOT EXISTS (
                        SELECT 1 FROM pg_trigger WHERE tgname = 'trg_user_faces_set_updated_at'
                    ) THEN
                        CREATE TRIGGER trg_user_faces_set_updated_at
                        BEFORE UPDATE ON user_faces
                        FOR EACH ROW
                        EXECUTE FUNCTION set_updated_at();
                    END IF;
                END $$;
            """);

            safeExec("""
                CREATE TABLE IF NOT EXISTS user_audit_logs (
                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                    user_id UUID NULL,
                    user_public_id VARCHAR(50),

                    action VARCHAR(50),
                    actor VARCHAR(100),
                    sid VARCHAR(100),

                    ip VARCHAR(100),
                    device VARCHAR(255),

                    details TEXT,
                    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
                )
            """);

            safeExec("""
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

            safeExec("""
                DO $$
                BEGIN
                    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_user_audit_logs_user_id') THEN
                        ALTER TABLE user_audit_logs
                        ADD CONSTRAINT fk_user_audit_logs_user_id
                        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;
                    END IF;
                END $$;
            """);

            safeExec("""
                CREATE INDEX IF NOT EXISTS ix_user_audit_logs_user_id_created_at
                ON user_audit_logs (user_id, created_at DESC)
            """);
            safeExec("""
                CREATE INDEX IF NOT EXISTS ix_user_audit_logs_action_created_at
                ON user_audit_logs (action, created_at DESC)
            """);
            safeExec("""
                CREATE INDEX IF NOT EXISTS ix_user_audit_logs_user_public_id_created_at
                ON user_audit_logs (user_public_id, created_at DESC)
            """);
            safeExec("""
                CREATE INDEX IF NOT EXISTS ix_user_audit_logs_sid_created_at
                ON user_audit_logs (sid, created_at DESC)
            """);

            safeExec("""
                CREATE TABLE IF NOT EXISTS user_devices (
                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                    user_id UUID NOT NULL,

                    session_id VARCHAR(100),
                    ip VARCHAR(100),
                    user_agent TEXT,

                    first_seen_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                    last_seen_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                    banned BOOLEAN NOT NULL DEFAULT FALSE,
                    banned_at TIMESTAMPTZ,
                    banned_reason TEXT,

                    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
                )
            """);

            safeExec("""
                DO $$
                BEGIN
                    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_user_devices_user_id') THEN
                        ALTER TABLE user_devices
                        ADD CONSTRAINT fk_user_devices_user_id
                        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
                    END IF;
                END $$;
            """);

            safeExec("CREATE INDEX IF NOT EXISTS idx_user_devices_user_id ON user_devices(user_id)");
            safeExec("CREATE INDEX IF NOT EXISTS idx_user_devices_session_id ON user_devices(session_id)");
            safeExec("CREATE INDEX IF NOT EXISTS idx_user_devices_banned ON user_devices(banned)");

            safeExec("""
                DO $$
                BEGIN
                    IF NOT EXISTS (
                        SELECT 1 FROM pg_trigger WHERE tgname = 'trg_user_devices_set_updated_at'
                    ) THEN
                        CREATE TRIGGER trg_user_devices_set_updated_at
                        BEFORE UPDATE ON user_devices
                        FOR EACH ROW
                        EXECUTE FUNCTION set_updated_at();
                    END IF;
                END $$;
            """);
        });
    }

    private void safeExec(String sql) {
        jdbc.execute(sql);
    }
}