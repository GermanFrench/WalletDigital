-- V7: Tabla de refresh tokens para rotación de sesión segura

CREATE TABLE refresh_tokens (
    id            UUID         PRIMARY KEY,
    auth_user_id  UUID         NOT NULL REFERENCES auth_users(id) ON DELETE CASCADE,
    token_hash    VARCHAR(64)  NOT NULL UNIQUE,   -- SHA-256 hex del token real
    expires_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_tokens_auth_user   ON refresh_tokens (auth_user_id);
CREATE INDEX idx_refresh_tokens_token_hash  ON refresh_tokens (token_hash);
