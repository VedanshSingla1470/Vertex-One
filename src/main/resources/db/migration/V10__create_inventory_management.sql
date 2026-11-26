
-- ============================================
-- ADD COMPOSITE PRODUCT KEY
-- ============================================

ALTER TABLE app.products
    ADD CONSTRAINT uk_products_org_id
        UNIQUE (organization_id, id);


-- ============================================
-- WAREHOUSES
-- ============================================

CREATE TABLE app.warehouses (
                                id UUID PRIMARY KEY,
                                organization_id UUID NOT NULL,

                                name VARCHAR(150) NOT NULL,
                                code VARCHAR(50) NOT NULL,
                                description VARCHAR(255),
                                address TEXT,

                                status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

                                created_at TIMESTAMPTZ NOT NULL,
                                updated_at TIMESTAMPTZ NOT NULL,

                                CONSTRAINT fk_warehouses_organization
                                    FOREIGN KEY (organization_id)
                                        REFERENCES app.organizations(id),

                                CONSTRAINT uk_warehouses_org_code
                                    UNIQUE (organization_id, code),

                                CONSTRAINT uk_warehouses_org_id
                                    UNIQUE (organization_id, id),

                                CONSTRAINT chk_warehouses_status
                                    CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE INDEX idx_warehouses_organization_id
    ON app.warehouses(organization_id);


-- ============================================
-- INVENTORY BALANCES
-- ============================================

CREATE TABLE app.inventory_balances (
                                        id UUID PRIMARY KEY,

                                        organization_id UUID NOT NULL,
                                        warehouse_id UUID NOT NULL,
                                        product_id UUID NOT NULL,

                                        quantity_on_hand NUMERIC(19, 4) NOT NULL DEFAULT 0,
                                        reorder_level NUMERIC(19, 4) NOT NULL DEFAULT 0,

                                        created_at TIMESTAMPTZ NOT NULL,
                                        updated_at TIMESTAMPTZ NOT NULL,

                                        CONSTRAINT fk_inventory_balances_warehouse
                                            FOREIGN KEY (organization_id, warehouse_id)
                                                REFERENCES app.warehouses(organization_id, id),

                                        CONSTRAINT fk_inventory_balances_product
                                            FOREIGN KEY (organization_id, product_id)
                                                REFERENCES app.products(organization_id, id),

                                        CONSTRAINT uk_inventory_balance_warehouse_product
                                            UNIQUE (warehouse_id, product_id),

                                        CONSTRAINT chk_inventory_quantity
                                            CHECK (quantity_on_hand >= 0),

                                        CONSTRAINT chk_inventory_reorder_level
                                            CHECK (reorder_level >= 0)
);

CREATE INDEX idx_inventory_balances_organization_id
    ON app.inventory_balances(organization_id);

CREATE INDEX idx_inventory_balances_product_id
    ON app.inventory_balances(product_id);


-- ============================================
-- STOCK MOVEMENTS
-- ============================================

CREATE TABLE app.stock_movements (
                                     id UUID PRIMARY KEY,

                                     organization_id UUID NOT NULL,
                                     product_id UUID NOT NULL,

                                     source_warehouse_id UUID,
                                     destination_warehouse_id UUID,

                                     movement_type VARCHAR(30) NOT NULL,
                                     quantity NUMERIC(19, 4) NOT NULL,

                                     reference_number VARCHAR(100),
                                     reason VARCHAR(500),

                                     performed_by_user_id UUID NOT NULL,

                                     created_at TIMESTAMPTZ NOT NULL,

                                     CONSTRAINT fk_stock_movements_product
                                         FOREIGN KEY (organization_id, product_id)
                                             REFERENCES app.products(organization_id, id),

                                     CONSTRAINT fk_stock_movements_source_warehouse
                                         FOREIGN KEY (organization_id, source_warehouse_id)
                                             REFERENCES app.warehouses(organization_id, id),

                                     CONSTRAINT fk_stock_movements_destination_warehouse
                                         FOREIGN KEY (organization_id, destination_warehouse_id)
                                             REFERENCES app.warehouses(organization_id, id),

                                     CONSTRAINT fk_stock_movements_user
                                         FOREIGN KEY (performed_by_user_id)
                                             REFERENCES app.users(id),

                                     CONSTRAINT chk_stock_movement_quantity
                                         CHECK (quantity > 0),

                                     CONSTRAINT chk_stock_movement_type
                                         CHECK (
                                             movement_type IN (
                                                               'RECEIPT',
                                                               'ISSUE',
                                                               'ADJUSTMENT_IN',
                                                               'ADJUSTMENT_OUT',
                                                               'TRANSFER'
                                                 )
                                             ),

                                     CONSTRAINT chk_stock_movement_warehouses
                                         CHECK (
                                             (
                                                 movement_type IN ('RECEIPT', 'ADJUSTMENT_IN')
                                                     AND source_warehouse_id IS NULL
                                                     AND destination_warehouse_id IS NOT NULL
                                                 )
                                                 OR
                                             (
                                                 movement_type IN ('ISSUE', 'ADJUSTMENT_OUT')
                                                     AND source_warehouse_id IS NOT NULL
                                                     AND destination_warehouse_id IS NULL
                                                 )
                                                 OR
                                             (
                                                 movement_type = 'TRANSFER'
                                                     AND source_warehouse_id IS NOT NULL
                                                     AND destination_warehouse_id IS NOT NULL
                                                     AND source_warehouse_id <> destination_warehouse_id
                                                 )
                                             )
);

CREATE INDEX idx_stock_movements_organization_id
    ON app.stock_movements(organization_id);

CREATE INDEX idx_stock_movements_product_id
    ON app.stock_movements(product_id);

CREATE INDEX idx_stock_movements_created_at
    ON app.stock_movements(created_at);

CREATE INDEX idx_stock_movements_source_warehouse
    ON app.stock_movements(source_warehouse_id);

CREATE INDEX idx_stock_movements_destination_warehouse
    ON app.stock_movements(destination_warehouse_id);
