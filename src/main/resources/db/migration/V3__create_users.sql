
CREATE TABLE app.users (
                           id UUID PRIMARY KEY,
                           email VARCHAR(150) NOT NULL,
                           password_hash VARCHAR(255) NOT NULL,
                           first_name VARCHAR(80) NOT NULL,
                           last_name VARCHAR(80) NOT NULL,
                           enabled BOOLEAN NOT NULL DEFAULT TRUE,
                           created_at TIMESTAMPTZ NOT NULL,
                           updated_at TIMESTAMPTZ NOT NULL,

                           CONSTRAINT uk_users_email UNIQUE (email)
);
