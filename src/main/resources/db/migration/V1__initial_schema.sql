CREATE TABLE users (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE auth_users (
    id UUID PRIMARY KEY,
    email VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    enabled BOOLEAN NOT NULL,
    CONSTRAINT uk_auth_users_email UNIQUE (email)
);

CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    balance NUMERIC(19,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_accounts_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE wallet_transactions (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    executed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_transactions_account FOREIGN KEY (account_id) REFERENCES accounts (id)
);

CREATE INDEX idx_accounts_user_id ON accounts (user_id);
CREATE INDEX idx_wallet_transactions_account_id ON wallet_transactions (account_id);
CREATE INDEX idx_wallet_transactions_executed_at ON wallet_transactions (executed_at);

