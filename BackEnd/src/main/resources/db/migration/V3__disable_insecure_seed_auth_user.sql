UPDATE auth_users
SET enabled = FALSE
WHERE email = 'admin@wallet.local'
  AND password_hash LIKE '{noop}%';
