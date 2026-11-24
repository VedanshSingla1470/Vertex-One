ALTER TABLE app.vendors
    ADD COLUMN category_id UUID;

ALTER TABLE app.vendors
    ADD CONSTRAINT fk_vendors_category
        FOREIGN KEY (category_id)
            REFERENCES app.vendor_categories(id)
            ON DELETE SET NULL;

CREATE INDEX idx_vendors_category
    ON app.vendors(category_id);

CREATE TABLE app.vendor_contacts (
                                     id UUID PRIMARY KEY,
                                     vendor_id UUID NOT NULL,

                                     name VARCHAR(120) NOT NULL,
                                     email VARCHAR(254),
                                     phone VARCHAR(30),
                                     designation VARCHAR(100),

                                     is_primary BOOLEAN NOT NULL DEFAULT FALSE,

                                     created_at TIMESTAMPTZ NOT NULL,
                                     updated_at TIMESTAMPTZ NOT NULL,

                                     CONSTRAINT fk_vendor_contacts_vendor
                                         FOREIGN KEY (vendor_id)
                                             REFERENCES app.vendors(id)
                                             ON DELETE CASCADE
);

CREATE INDEX idx_vendor_contacts_vendor
    ON app.vendor_contacts(vendor_id);

CREATE UNIQUE INDEX uk_vendor_contacts_primary
    ON app.vendor_contacts(vendor_id)
    WHERE is_primary = TRUE;