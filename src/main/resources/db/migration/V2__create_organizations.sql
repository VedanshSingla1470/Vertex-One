
CREATE TABLE app.organizations (
                                   id UUID PRIMARY KEY,
                                   name VARCHAR(150) NOT NULL,
                                   slug VARCHAR(100) NOT NULL UNIQUE,
                                   description VARCHAR(255),
                                   created_at TIMESTAMPTZ NOT NULL,
                                   updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_organizations_name
    ON app.organizations(name);
