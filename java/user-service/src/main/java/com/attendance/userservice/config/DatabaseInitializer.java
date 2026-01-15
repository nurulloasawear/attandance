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

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS users (
                id UUID PRIMARY KEY,
                public_id VARCHAR(8) NOT NULL UNIQUE,
                username VARCHAR(100) NOT NULL UNIQUE,
                password VARCHAR(255) NOT NULL,
                email VARCHAR(200) NOT NULL UNIQUE,
                first_name VARCHAR(100),
                last_name VARCHAR(100),
                role VARCHAR(50) NOT NULL,
                is_active BOOLEAN NOT NULL DEFAULT TRUE
            )
        """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS refresh_tokens (
                id UUID PRIMARY KEY,
                user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                token_hash VARCHAR(255) NOT NULL UNIQUE,
                expires_at TIMESTAMPTZ NOT NULL,
                revoked BOOLEAN NOT NULL DEFAULT FALSE,
                created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
            )
        """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS user_faces (
                id UUID PRIMARY KEY,
                user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
                face_data BYTEA NOT NULL,
                content_type VARCHAR(100) NOT NULL,
                size_bytes BIGINT NOT NULL,
                created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
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

        jdbcTemplate.execute("ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS user_id UUID");
        jdbcTemplate.execute("ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS token_hash VARCHAR(255)");
        jdbcTemplate.execute("ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS expires_at TIMESTAMPTZ");
        jdbcTemplate.execute("ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS revoked BOOLEAN NOT NULL DEFAULT FALSE");
        jdbcTemplate.execute("ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()");

        jdbcTemplate.execute("ALTER TABLE user_faces ADD COLUMN IF NOT EXISTS user_id UUID");
        jdbcTemplate.execute("ALTER TABLE user_faces ADD COLUMN IF NOT EXISTS face_data BYTEA");
        jdbcTemplate.execute("ALTER TABLE user_faces ADD COLUMN IF NOT EXISTS content_type VARCHAR(100)");
        jdbcTemplate.execute("ALTER TABLE user_faces ADD COLUMN IF NOT EXISTS size_bytes BIGINT");
        jdbcTemplate.execute("ALTER TABLE user_faces ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()");
        jdbcTemplate.execute("ALTER TABLE user_faces ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()");
    }
}
