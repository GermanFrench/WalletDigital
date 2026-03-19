-- V5__insert_seed_data.sql
-- Insert seed data for initial testing

-- Insert a seed user (using existing UUID for testing)
INSERT INTO users (id, name, email) 
VALUES ('550e8400-e29b-41d4-a716-446655440000'::UUID, 'Demo User', 'demo@example.com')
ON CONFLICT (email) DO NOTHING;

-- Insert a seed auth user with bcrypt hashed password
-- Password: 'password123' -> bcrypt hash
INSERT INTO auth_users (id, email, password_hash, role, enabled)
VALUES ('550e8400-e29b-41d4-a716-446655440000'::UUID, 'demo@example.com', '$2a$10$dXJ3SW6G7P50eS3xNsCyve', 'USER', true)
ON CONFLICT (email) DO NOTHING;

-- Insert a demo account for the seed user
INSERT INTO accounts (id, user_id, balance, currency, status, version)
SELECT '550e8400-e29b-41d4-a716-446655440001'::UUID, '550e8400-e29b-41d4-a716-446655440000'::UUID, 0.00, 'USD', 'ACTIVE', 0
WHERE NOT EXISTS (SELECT 1 FROM accounts WHERE id = '550e8400-e29b-41d4-a716-446655440001'::UUID);
