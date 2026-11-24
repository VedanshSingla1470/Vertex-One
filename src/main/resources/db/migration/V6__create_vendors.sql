
CREATE TABLE app.vendors (
                             id UUID PRIMARY KEY,

                             organization_id UUID NOT NULL,

                             name VARCHAR(150) NOT NULL,
                             email VARCHAR(254),
                             phone VARCHAR(30),
                             website VARCHAR(255),
                             address TEXT,
                             tax_id VARCHAR(50),

                             status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

                             created_at TIMESTAMPTZ NOT NULL,
                             updated_at TIMESTAMPTZ NOT NULL,

                             CONSTRAINT fk_vendors_organization
                                 FOREIGN KEY (organization_id)
                                     REFERENCES app.organizations(id),

                             CONSTRAINT uk_vendors_organization_tax_id
                                 UNIQUE (organization_id, tax_id),

                             CONSTRAINT chk_vendors_status
                                 CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE INDEX idx_vendors_organization_id
    ON app.vendors(organization_id);

CREATE INDEX idx_vendors_name
    ON app.vendors(name);

CREATE INDEX idx_vendors_status
    ON app.vendors(status);
