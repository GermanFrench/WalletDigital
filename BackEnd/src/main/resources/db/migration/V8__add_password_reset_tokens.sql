-- V8: Tabla de tokens para recuperación de contraseña

CREATE TABLE password_reset_tokens (
    id          UUID         PRIMARY KEY,
    email       VARCHAR(150) NOT NULL,
    token_hash  VARCHAR(64)  NOT NULL UNIQUE,   -- SHA-256 hex del token real
    expires_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    used        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_password_reset_tokens_email      ON password_reset_tokens (email);
CREATE INDEX idx_password_reset_tokens_token_hash ON password_reset_tokens (token_hash);
