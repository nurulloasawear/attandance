package com.groupservice.groupservice.config;

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
            log.info("DB init (group-service): start");

            // ✅ pgcrypto for gen_random_uuid()
            execCritical("CREATE EXTENSION IF NOT EXISTS pgcrypto");

            // ✅ shared trigger function
            execCritical("""
                CREATE OR REPLACE FUNCTION public.set_updated_at()
                RETURNS TRIGGER AS $$
                BEGIN
                    NEW.updated_at = NOW();
                    RETURN NEW;
                END;
                $$ LANGUAGE plpgsql;
            """);

            // =========================
            // groups
            // =========================
            execCritical("""
                CREATE TABLE IF NOT EXISTS public.groups (
                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                    group_public_id VARCHAR(32) NOT NULL,
                    name            VARCHAR(200) NOT NULL,
                    department      VARCHAR(200),

                    leader_user_public_id VARCHAR(64),

                    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
                )
            """);

            // unique constraints as indexes (safe with IF NOT EXISTS)
            execCritical("""
                CREATE UNIQUE INDEX IF NOT EXISTS ux_groups_group_public_id
                ON public.groups (group_public_id)
            """);

            // ✅ one leader -> one group (leader can be null)
            execCritical("""
                CREATE UNIQUE INDEX IF NOT EXISTS ux_groups_leader_user_public_id
                ON public.groups (leader_user_public_id)
                WHERE leader_user_public_id IS NOT NULL
            """);

            execOptional("CREATE INDEX IF NOT EXISTS ix_groups_department ON public.groups (department)");

            // trigger for updated_at
            execOptional("""
                DO $$
                BEGIN
                    IF NOT EXISTS (
                        SELECT 1 FROM pg_trigger
                        WHERE tgrelid = 'public.groups'::regclass
                          AND tgname  = 'trg_groups_set_updated_at'
                    ) THEN
                        CREATE TRIGGER trg_groups_set_updated_at
                        BEFORE UPDATE ON public.groups
                        FOR EACH ROW
                        EXECUTE FUNCTION public.set_updated_at();
                    END IF;
                END $$;
            """);

            // =========================
            // group_members
            // =========================
            execCritical("""
                CREATE TABLE IF NOT EXISTS public.group_members (
                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                    group_id UUID NOT NULL,
                    user_public_id VARCHAR(64) NOT NULL,

                    joined_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
                )
            """);

            // FK group_members -> groups
            execOptional("""
                DO $$
                BEGIN
                    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_group_members_group_id') THEN
                        ALTER TABLE public.group_members
                        ADD CONSTRAINT fk_group_members_group_id
                        FOREIGN KEY (group_id) REFERENCES public.groups(id) ON DELETE CASCADE;
                    END IF;
                END $$;
            """);

            // ✅ worker only ONE group
            execCritical("""
                CREATE UNIQUE INDEX IF NOT EXISTS ux_group_members_user_public_id
                ON public.group_members (user_public_id)
            """);

            execOptional("CREATE INDEX IF NOT EXISTS ix_group_members_group_id ON public.group_members (group_id)");

            log.info("DB init (group-service): done");
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