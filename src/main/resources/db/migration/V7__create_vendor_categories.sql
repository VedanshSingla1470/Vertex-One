CREATE TABLE app.vendor_categories (
                                       id UUID PRIMARY KEY,
                                       organization_id UUID NOT NULL,

                                       name VARCHAR(100) NOT NULL,
                                       description VARCHAR(255),

                                       created_at TIMESTAMPTZ NOT NULL,
                                       updated_at TIMESTAMPTZ NOT NULL,

                                       CONSTRAINT fk_vendor_categories_organization
                                           FOREIGN KEY (organization_id)
                                               REFERENCES app.organizations(id),

                                       CONSTRAINT uk_vendor_categories_org_name
                                           UNIQUE (organization_id, name)
);

CREATE INDEX idx_vendor_categories_organization
    ON app.vendor_categories(organization_id);