-- =========================================================
-- PURCHASE REQUISITIONS
-- =========================================================

CREATE TABLE app.purchase_requisitions (
                                           id UUID PRIMARY KEY,
                                           organization_id UUID NOT NULL,
                                           requisition_number VARCHAR(50) NOT NULL,
                                           title VARCHAR(200) NOT NULL,
                                           description TEXT,
                                           status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
                                           requested_by_user_id UUID NOT NULL,
                                           reviewed_by_user_id UUID,
                                           reviewed_at TIMESTAMPTZ,
                                           rejection_reason VARCHAR(500),
                                           created_at TIMESTAMPTZ NOT NULL,
                                           updated_at TIMESTAMPTZ NOT NULL,

                                           CONSTRAINT fk_requisitions_organization
                                               FOREIGN KEY (organization_id)
                                                   REFERENCES app.organizations(id),

                                           CONSTRAINT fk_requisitions_requested_by
                                               FOREIGN KEY (requested_by_user_id)
                                                   REFERENCES app.users(id),

                                           CONSTRAINT fk_requisitions_reviewed_by
                                               FOREIGN KEY (reviewed_by_user_id)
                                                   REFERENCES app.users(id),

                                           CONSTRAINT uk_requisitions_org_number
                                               UNIQUE (organization_id, requisition_number),

                                           CONSTRAINT uk_requisitions_org_id
                                               UNIQUE (organization_id, id),

                                           CONSTRAINT chk_requisition_status
                                               CHECK (status IN (
                                                                 'DRAFT', 'SUBMITTED', 'APPROVED',
                                                                 'REJECTED', 'CANCELLED'
                                                   ))
);

CREATE INDEX idx_requisitions_organization
    ON app.purchase_requisitions(organization_id);

CREATE INDEX idx_requisitions_status
    ON app.purchase_requisitions(organization_id, status);

CREATE INDEX idx_requisitions_requested_by
    ON app.purchase_requisitions(requested_by_user_id);


-- =========================================================
-- PURCHASE REQUISITION ITEMS
-- =========================================================

CREATE TABLE app.purchase_requisition_items (
                                                id UUID PRIMARY KEY,
                                                organization_id UUID NOT NULL,
                                                requisition_id UUID NOT NULL,
                                                product_id UUID NOT NULL,
                                                requested_quantity NUMERIC(19, 4) NOT NULL,
                                                approved_quantity NUMERIC(19, 4),
                                                notes VARCHAR(500),
                                                created_at TIMESTAMPTZ NOT NULL,
                                                updated_at TIMESTAMPTZ NOT NULL,

                                                CONSTRAINT fk_requisition_items_requisition
                                                    FOREIGN KEY (organization_id, requisition_id)
                                                        REFERENCES app.purchase_requisitions(organization_id, id),

                                                CONSTRAINT fk_requisition_items_product
                                                    FOREIGN KEY (product_id)
                                                        REFERENCES app.products(id),

                                                CONSTRAINT chk_requisition_requested_quantity
                                                    CHECK (requested_quantity > 0),

                                                CONSTRAINT chk_requisition_approved_quantity
                                                    CHECK (
                                                        approved_quantity IS NULL
                                                            OR (
                                                            approved_quantity > 0
                                                                AND approved_quantity <= requested_quantity
                                                            )
                                                        )
);

CREATE INDEX idx_requisition_items_requisition
    ON app.purchase_requisition_items(requisition_id);

CREATE INDEX idx_requisition_items_product
    ON app.purchase_requisition_items(product_id);


-- =========================================================
-- PURCHASE ORDERS
-- =========================================================

CREATE TABLE app.purchase_orders (
                                     id UUID PRIMARY KEY,
                                     organization_id UUID NOT NULL,
                                     purchase_order_number VARCHAR(50) NOT NULL,
                                     requisition_id UUID,
                                     vendor_id UUID NOT NULL,
                                     warehouse_id UUID,
                                     status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
                                     order_date DATE NOT NULL,
                                     expected_delivery_date DATE,
                                     currency_code VARCHAR(3) NOT NULL DEFAULT 'INR',
                                     subtotal NUMERIC(19, 4) NOT NULL DEFAULT 0,
                                     tax_total NUMERIC(19, 4) NOT NULL DEFAULT 0,
                                     grand_total NUMERIC(19, 4) NOT NULL DEFAULT 0,
                                     notes TEXT,
                                     created_by_user_id UUID NOT NULL,
                                     approved_by_user_id UUID,
                                     approved_at TIMESTAMPTZ,
                                     sent_at TIMESTAMPTZ,
                                     created_at TIMESTAMPTZ NOT NULL,
                                     updated_at TIMESTAMPTZ NOT NULL,

                                     CONSTRAINT fk_purchase_orders_organization
                                         FOREIGN KEY (organization_id)
                                             REFERENCES app.organizations(id),

                                     CONSTRAINT fk_purchase_orders_requisition
                                         FOREIGN KEY (organization_id, requisition_id)
                                             REFERENCES app.purchase_requisitions(organization_id, id),

                                     CONSTRAINT fk_purchase_orders_vendor
                                         FOREIGN KEY (vendor_id)
                                             REFERENCES app.vendors(id),

                                     CONSTRAINT fk_purchase_orders_created_by
                                         FOREIGN KEY (created_by_user_id)
                                             REFERENCES app.users(id),

                                     CONSTRAINT fk_purchase_orders_approved_by
                                         FOREIGN KEY (approved_by_user_id)
                                             REFERENCES app.users(id),

                                     CONSTRAINT uk_purchase_orders_org_number
                                         UNIQUE (organization_id, purchase_order_number),

                                     CONSTRAINT uk_purchase_orders_org_id
                                         UNIQUE (organization_id, id),

                                     CONSTRAINT chk_purchase_order_status
                                         CHECK (status IN (
                                                           'DRAFT', 'PENDING_APPROVAL', 'APPROVED',
                                                           'SENT', 'ACKNOWLEDGED', 'PARTIALLY_RECEIVED',
                                                           'COMPLETED', 'CANCELLED'
                                             )),

                                     CONSTRAINT chk_purchase_order_amounts
                                         CHECK (
                                             subtotal >= 0
                                                 AND tax_total >= 0
                                                 AND grand_total >= 0
                                             ),

                                     CONSTRAINT chk_purchase_order_dates
                                         CHECK (
                                             expected_delivery_date IS NULL
                                                 OR expected_delivery_date >= order_date
                                             )
);

CREATE INDEX idx_purchase_orders_organization
    ON app.purchase_orders(organization_id);

CREATE INDEX idx_purchase_orders_vendor
    ON app.purchase_orders(organization_id, vendor_id);

CREATE INDEX idx_purchase_orders_status
    ON app.purchase_orders(organization_id, status);

CREATE INDEX idx_purchase_orders_requisition
    ON app.purchase_orders(requisition_id);


-- =========================================================
-- PURCHASE ORDER ITEMS
-- =========================================================

CREATE TABLE app.purchase_order_items (
                                          id UUID PRIMARY KEY,
                                          organization_id UUID NOT NULL,
                                          purchase_order_id UUID NOT NULL,
                                          product_id UUID NOT NULL,
                                          ordered_quantity NUMERIC(19, 4) NOT NULL,
                                          received_quantity NUMERIC(19, 4) NOT NULL DEFAULT 0,
                                          unit_price NUMERIC(19, 4) NOT NULL,
                                          tax_rate NUMERIC(7, 4) NOT NULL DEFAULT 0,
                                          line_subtotal NUMERIC(19, 4) NOT NULL,
                                          tax_amount NUMERIC(19, 4) NOT NULL,
                                          line_total NUMERIC(19, 4) NOT NULL,
                                          notes VARCHAR(500),
                                          created_at TIMESTAMPTZ NOT NULL,
                                          updated_at TIMESTAMPTZ NOT NULL,

                                          CONSTRAINT fk_purchase_order_items_order
                                              FOREIGN KEY (organization_id, purchase_order_id)
                                                  REFERENCES app.purchase_orders(organization_id, id),

                                          CONSTRAINT fk_purchase_order_items_product
                                              FOREIGN KEY (product_id)
                                                  REFERENCES app.products(id),

                                          CONSTRAINT chk_po_item_ordered_quantity
                                              CHECK (ordered_quantity > 0),

                                          CONSTRAINT chk_po_item_received_quantity
                                              CHECK (
                                                  received_quantity >= 0
                                                      AND received_quantity <= ordered_quantity
                                                  ),

                                          CONSTRAINT chk_po_item_unit_price
                                              CHECK (unit_price >= 0),

                                          CONSTRAINT chk_po_item_tax_rate
                                              CHECK (tax_rate >= 0 AND tax_rate <= 100),

                                          CONSTRAINT chk_po_item_amounts
                                              CHECK (
                                                  line_subtotal >= 0
                                                      AND tax_amount >= 0
                                                      AND line_total >= 0
                                                  )
);

CREATE INDEX idx_purchase_order_items_order
    ON app.purchase_order_items(purchase_order_id);

CREATE INDEX idx_purchase_order_items_product
    ON app.purchase_order_items(product_id);