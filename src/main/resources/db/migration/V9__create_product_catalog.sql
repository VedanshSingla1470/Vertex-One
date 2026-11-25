
-- ============================================
-- PRODUCT CATEGORIES
-- ============================================

CREATE TABLE app.product_categories (
                                        id UUID PRIMARY KEY,
                                        organization_id UUID NOT NULL,
                                        name VARCHAR(100) NOT NULL,
                                        description VARCHAR(255),

                                        created_at TIMESTAMPTZ NOT NULL,
                                        updated_at TIMESTAMPTZ NOT NULL,

                                        CONSTRAINT fk_product_categories_organization
                                            FOREIGN KEY (organization_id)
                                                REFERENCES app.organizations(id),

                                        CONSTRAINT uk_product_categories_org_name
                                            UNIQUE (organization_id, name),

    -- Supports tenant-safe composite foreign keys.
                                        CONSTRAINT uk_product_categories_org_id
                                            UNIQUE (organization_id, id)
);

CREATE INDEX idx_product_categories_organization_id
    ON app.product_categories(organization_id);


-- ============================================
-- PRODUCTS
-- ============================================

CREATE TABLE app.products (
                              id UUID PRIMARY KEY,
                              organization_id UUID NOT NULL,
                              category_id UUID,

                              sku VARCHAR(100) NOT NULL,
                              name VARCHAR(150) NOT NULL,
                              description TEXT,

                              unit_of_measure VARCHAR(30) NOT NULL,
                              status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

                              created_at TIMESTAMPTZ NOT NULL,
                              updated_at TIMESTAMPTZ NOT NULL,

                              CONSTRAINT fk_products_organization
                                  FOREIGN KEY (organization_id)
                                      REFERENCES app.organizations(id),

    -- Ensures a product cannot use another
    -- organization's category.
                              CONSTRAINT fk_products_category
                                  FOREIGN KEY (organization_id, category_id)
                                      REFERENCES app.product_categories(organization_id, id),

                              CONSTRAINT uk_products_org_sku
                                  UNIQUE (organization_id, sku),

                              CONSTRAINT chk_products_status
                                  CHECK (status IN ('ACTIVE', 'INACTIVE'))
);


-- ============================================
-- INDEXES
-- ============================================

CREATE INDEX idx_products_organization_id
    ON app.products(organization_id);

CREATE INDEX idx_products_category_id
    ON app.products(category_id);

CREATE INDEX idx_products_name
    ON app.products(name);

CREATE INDEX idx_products_status
    ON app.products(status);
