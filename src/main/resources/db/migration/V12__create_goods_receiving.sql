
CREATE TABLE app.goods_receipts (
                                    id UUID PRIMARY KEY,
                                    organization_id UUID NOT NULL,
                                    purchase_order_id UUID NOT NULL,
                                    warehouse_id UUID NOT NULL,
                                    receipt_number VARCHAR(50) NOT NULL,
                                    delivery_note_number VARCHAR(100),
                                    received_by_user_id UUID NOT NULL,
                                    received_at TIMESTAMPTZ NOT NULL,
                                    notes TEXT,
                                    created_at TIMESTAMPTZ NOT NULL,

                                    CONSTRAINT fk_goods_receipts_organization
                                        FOREIGN KEY (organization_id)
                                            REFERENCES app.organizations(id),

                                    CONSTRAINT fk_goods_receipts_purchase_order
                                        FOREIGN KEY (purchase_order_id)
                                            REFERENCES app.purchase_orders(id),

                                    CONSTRAINT fk_goods_receipts_warehouse
                                        FOREIGN KEY (organization_id, warehouse_id)
                                            REFERENCES app.warehouses(organization_id, id),

                                    CONSTRAINT fk_goods_receipts_user
                                        FOREIGN KEY (received_by_user_id)
                                            REFERENCES app.users(id),

                                    CONSTRAINT uk_goods_receipts_org_number
                                        UNIQUE (organization_id, receipt_number)
);

CREATE INDEX idx_goods_receipts_org
    ON app.goods_receipts(organization_id);

CREATE INDEX idx_goods_receipts_purchase_order
    ON app.goods_receipts(purchase_order_id);

CREATE INDEX idx_goods_receipts_received_at
    ON app.goods_receipts(received_at);


CREATE TABLE app.goods_receipt_items (
                                         id UUID PRIMARY KEY,
                                         organization_id UUID NOT NULL,
                                         goods_receipt_id UUID NOT NULL,
                                         purchase_order_item_id UUID NOT NULL,
                                         product_id UUID NOT NULL,
                                         quantity_received NUMERIC(19, 4) NOT NULL,
                                         notes VARCHAR(500),
                                         created_at TIMESTAMPTZ NOT NULL,

                                         CONSTRAINT fk_goods_receipt_items_organization
                                             FOREIGN KEY (organization_id)
                                                 REFERENCES app.organizations(id),

                                         CONSTRAINT fk_goods_receipt_items_receipt
                                             FOREIGN KEY (goods_receipt_id)
                                                 REFERENCES app.goods_receipts(id),

                                         CONSTRAINT fk_goods_receipt_items_po_item
                                             FOREIGN KEY (purchase_order_item_id)
                                                 REFERENCES app.purchase_order_items(id),

                                         CONSTRAINT fk_goods_receipt_items_product
                                             FOREIGN KEY (product_id)
                                                 REFERENCES app.products(id),

                                         CONSTRAINT chk_goods_receipt_quantity
                                             CHECK (quantity_received > 0)
);

CREATE INDEX idx_goods_receipt_items_receipt
    ON app.goods_receipt_items(goods_receipt_id);

CREATE INDEX idx_goods_receipt_items_po_item
    ON app.goods_receipt_items(purchase_order_item_id);
