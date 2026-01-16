package com.attendance.userservice.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DatabaseInitializer {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    @Transactional
    public void init() {

        jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS pgcrypto");


        jdbcTemplate.execute("""
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

        jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS public_id VARCHAR(8)");
        jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS username VARCHAR(100)");
        jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS password VARCHAR(255)");
        jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS email VARCHAR(200)");
        jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS first_name VARCHAR(100)");
        jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS last_name VARCHAR(100)");
        jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(50)");
        jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE");

        jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()");
        jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()");
        jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ NULL");
        jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS created_by VARCHAR(8)");
        jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS updated_by VARCHAR(8)");

        jdbcTemplate.execute("""
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

        jdbcTemplate.execute("""
            CREATE UNIQUE INDEX IF NOT EXISTS ux_users_public_id_active
            ON users (public_id)
            WHERE deleted_at IS NULL
        """);
        jdbcTemplate.execute("""
            CREATE UNIQUE INDEX IF NOT EXISTS ux_users_username_active
            ON users (username)
            WHERE deleted_at IS NULL
        """);
        jdbcTemplate.execute("""
            CREATE UNIQUE INDEX IF NOT EXISTS ux_users_email_active
            ON users (email)
            WHERE deleted_at IS NULL
        """);

        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS ix_users_deleted_at ON users (deleted_at)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS ix_users_is_active ON users (is_active)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS ix_users_created_at ON users (created_at DESC)");
        jdbcTemplate.execute("""
            CREATE INDEX IF NOT EXISTS ix_users_username_active_lookup
            ON users (username)
            WHERE deleted_at IS NULL
        """);
        jdbcTemplate.execute("""
            CREATE INDEX IF NOT EXISTS ix_users_email_active_lookup
            ON users (email)
            WHERE deleted_at IS NULL
        """);

        jdbcTemplate.execute("""
            CREATE OR REPLACE FUNCTION set_updated_at()
            RETURNS TRIGGER AS $$
            BEGIN
                NEW.updated_at = NOW();
                RETURN NEW;
            END;
            $$ LANGUAGE plpgsql;
        """);

        jdbcTemplate.execute("""
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


        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS refresh_tokens (
                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                user_id UUID NOT NULL,
                token_hash VARCHAR(255) NOT NULL UNIQUE,
                expires_at TIMESTAMPTZ NOT NULL,
                revoked BOOLEAN NOT NULL DEFAULT FALSE,
                created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
            )
        """);

        jdbcTemplate.execute("ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS user_id UUID");
        jdbcTemplate.execute("ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS token_hash VARCHAR(255)");
        jdbcTemplate.execute("ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS expires_at TIMESTAMPTZ");
        jdbcTemplate.execute("ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS revoked BOOLEAN NOT NULL DEFAULT FALSE");
        jdbcTemplate.execute("ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()");


        jdbcTemplate.execute("""
            DO $$
            BEGIN
                IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_refresh_tokens_user_id') THEN
                    ALTER TABLE refresh_tokens
                    ADD CONSTRAINT fk_refresh_tokens_user_id
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
                END IF;
            END $$;
        """);

        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS ix_refresh_tokens_user_id ON refresh_tokens (user_id)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS ix_refresh_tokens_expires_at ON refresh_tokens (expires_at)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS ix_refresh_tokens_revoked ON refresh_tokens (revoked)");


        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS user_faces (
                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                user_id UUID NOT NULL,

                face_data BYTEA NOT NULL,
                content_type VARCHAR(100) NOT NULL,
                size_bytes BIGINT NOT NULL,

                created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
            )
        """);

        jdbcTemplate.execute("ALTER TABLE user_faces ADD COLUMN IF NOT EXISTS user_id UUID");
        jdbcTemplate.execute("ALTER TABLE user_faces ADD COLUMN IF NOT EXISTS face_data BYTEA");
        jdbcTemplate.execute("ALTER TABLE user_faces ADD COLUMN IF NOT EXISTS content_type VARCHAR(100)");
        jdbcTemplate.execute("ALTER TABLE user_faces ADD COLUMN IF NOT EXISTS size_bytes BIGINT");
        jdbcTemplate.execute("ALTER TABLE user_faces ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()");
        jdbcTemplate.execute("ALTER TABLE user_faces ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()");

        jdbcTemplate.execute("""
            DO $$
            DECLARE c RECORD;
            BEGIN
                FOR c IN
                    SELECT conname
                    FROM pg_constraint con
                    JOIN pg_class rel ON rel.oid = con.conrelid
                    JOIN pg_namespace nsp ON nsp.oid = rel.relnamespace
                    WHERE rel.relname = 'user_faces'
                      AND nsp.nspname = 'public'
                      AND con.contype = 'u'
                      AND pg_get_constraintdef(con.oid) ILIKE '%(user_id)%'
                LOOP
                    EXECUTE format('ALTER TABLE public.user_faces DROP CONSTRAINT %I', c.conname);
                END LOOP;
            END $$;
        """);

        jdbcTemplate.execute("""
            CREATE UNIQUE INDEX IF NOT EXISTS ux_user_faces_user_id
            ON user_faces (user_id)
        """);

        jdbcTemplate.execute("""
            DO $$
            BEGIN
                IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_user_faces_user_id') THEN
                    ALTER TABLE user_faces
                    ADD CONSTRAINT fk_user_faces_user_id
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
                END IF;
            END $$;
        """);

        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS ix_user_faces_user_id ON user_faces (user_id)");


        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS user_audit_logs (
                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                user_id UUID NULL,
                action VARCHAR(50) NOT NULL,

                actor_public_id VARCHAR(8),
                session_id VARCHAR(100),
                ip VARCHAR(100),
                device VARCHAR(200),

                message VARCHAR(500),
                created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
            )
        """);

        jdbcTemplate.execute("""
            DO $$
            BEGIN
                IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_user_audit_logs_user_id') THEN
                    ALTER TABLE user_audit_logs
                    ADD CONSTRAINT fk_user_audit_logs_user_id
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;
                END IF;
            END $$;
        """);

        jdbcTemplate.execute("""
            CREATE INDEX IF NOT EXISTS ix_user_audit_logs_user_id_created_at
            ON user_audit_logs (user_id, created_at DESC)
        """);
        jdbcTemplate.execute("""
            CREATE INDEX IF NOT EXISTS ix_user_audit_logs_action_created_at
            ON user_audit_logs (action, created_at DESC)
        """);
    }
}
