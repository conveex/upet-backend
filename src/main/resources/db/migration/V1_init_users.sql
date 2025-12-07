CREATE EXTENSION IF NOT EXISTS "pgcrypto";

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_status') THEN
       CREATE TYPE user_status AS ENUM ('ACTIVE', 'PENDING_APPROVAL', 'INACTIVE');
    END IF;
END$$;

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,

    phone VARCHAR(50),
    main_address VARCHAR(255),
    photo_url VARCHAR(255),

    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    fcm_token VARCHAR(255),

    is_client BOOLEAN NOT NULL DEFAULT FALSE,
    is_walker BOOLEAN NOT NULL DEFAULT FALSE,
    is_admin BOOLEAN NOT NULL DEFAULT FALSE,

    status user_status NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);