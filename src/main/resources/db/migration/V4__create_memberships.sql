CREATE TABLE app.memberships (
                                 id UUID PRIMARY KEY,

                                 user_id UUID NOT NULL,
                                 organization_id UUID NOT NULL,

                                 role VARCHAR(50) NOT NULL,
                                 status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',

                                 created_at TIMESTAMPTZ NOT NULL,
                                 updated_at TIMESTAMPTZ NOT NULL,

                                 CONSTRAINT fk_memberships_user
                                     FOREIGN KEY (user_id)
                                         REFERENCES app.users(id),

                                 CONSTRAINT fk_memberships_organization
                                     FOREIGN KEY (organization_id)
                                         REFERENCES app.organizations(id),

                                 CONSTRAINT uk_memberships_user_organization
                                     UNIQUE (user_id, organization_id)
);

CREATE INDEX idx_memberships_user_id
    ON app.memberships(user_id);

CREATE INDEX idx_memberships_organization_id
    ON app.memberships(organization_id);