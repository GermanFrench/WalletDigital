-- V6: Número de cuenta único (tipo CVU), alias y campos extra en transacciones

-- Agregar número de cuenta y alias a accounts
ALTER TABLE accounts
    ADD COLUMN account_number VARCHAR(22),
    ADD COLUMN alias          VARCHAR(100);

-- Poblar número de cuenta para filas existentes (formato CVU: 22 dígitos)
WITH ranked AS (
    SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS rn FROM accounts
)
UPDATE accounts a
SET account_number = LPAD((1000000000000000000 + r.rn)::TEXT, 22, '0')
FROM ranked r
WHERE a.id = r.id;

-- Poblar alias para filas existentes (prefijo de email + 4 chars del UUID)
UPDATE accounts a
SET alias = LOWER(SPLIT_PART(u.email, '@', 1)) || '.' || SUBSTRING(REPLACE(a.id::TEXT, '-', ''), 1, 4)
FROM users u
WHERE a.user_id = u.id;

-- Índices únicos parciales (permiten NULL pero no duplicados en valores no-NULL)
CREATE UNIQUE INDEX idx_accounts_account_number ON accounts (account_number) WHERE account_number IS NOT NULL;
CREATE UNIQUE INDEX idx_accounts_alias         ON accounts (alias)          WHERE alias          IS NOT NULL;

-- Agregar descripción y cuenta relacionada a wallet_transactions
ALTER TABLE wallet_transactions
    ADD COLUMN description        VARCHAR(255),
    ADD COLUMN related_account_id UUID REFERENCES accounts(id);

CREATE INDEX idx_wallet_transactions_related_account ON wallet_transactions (related_account_id)
    WHERE related_account_id IS NOT NULL;
