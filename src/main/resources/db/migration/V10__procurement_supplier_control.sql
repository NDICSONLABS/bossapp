-- src/main/resources/db/migration/postgresql/V10__procurement_supplier_control.sql

ALTER TABLE supplier
    ADD COLUMN category VARCHAR(100),
    ADD COLUMN payment_terms_days INTEGER;

CREATE TABLE purchase_request (
                                  id UUID PRIMARY KEY,
                                  department_id UUID NOT NULL REFERENCES department(id),
                                  requested_by VARCHAR(255),
                                  request_date DATE,
                                  needed_by DATE,
                                  description TEXT,
                                  estimated_amount NUMERIC(19,4),
                                  status VARCHAR(50) NOT NULL DEFAULT 'SUBMITTED',
                                  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE purchase_order (
                                id UUID PRIMARY KEY,
                                po_number VARCHAR(100) NOT NULL UNIQUE,
                                supplier_id UUID NOT NULL REFERENCES supplier(id),
                                department_id UUID NOT NULL REFERENCES department(id),
                                order_date DATE,
                                expected_delivery_date DATE,
                                currency VARCHAR(10),
                                status VARCHAR(50) NOT NULL DEFAULT 'APPROVED',
                                total_amount NUMERIC(19,4) NOT NULL DEFAULT 0,
                                approved_by VARCHAR(100),
                                approved_at TIMESTAMPTZ,
                                created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE purchase_order_line (
                                     id UUID PRIMARY KEY,
                                     purchase_order_id UUID NOT NULL REFERENCES purchase_order(id),
                                     description VARCHAR(255),
                                     quantity NUMERIC(19,4) NOT NULL DEFAULT 0,
                                     unit_price NUMERIC(19,4) NOT NULL DEFAULT 0,
                                     tax_percent NUMERIC(9,4) NOT NULL DEFAULT 0,
                                     line_total NUMERIC(19,4) NOT NULL DEFAULT 0,
                                     received_quantity NUMERIC(19,4) NOT NULL DEFAULT 0,
                                     accepted_quantity NUMERIC(19,4) NOT NULL DEFAULT 0,
                                     created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE goods_receipt (
                               id UUID PRIMARY KEY,
                               grn_number VARCHAR(100) NOT NULL UNIQUE,
                               supplier_id UUID NOT NULL REFERENCES supplier(id),
                               department_id UUID NOT NULL REFERENCES department(id),
                               purchase_order_id UUID NOT NULL REFERENCES purchase_order(id),
                               delivery_date DATE,
                               received_by VARCHAR(255),
                               status VARCHAR(50) NOT NULL DEFAULT 'RECEIVED',
                               created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE goods_receipt_line (
                                    id UUID PRIMARY KEY,
                                    goods_receipt_id UUID NOT NULL REFERENCES goods_receipt(id),
                                    purchase_order_line_id UUID NOT NULL REFERENCES purchase_order_line(id),
                                    quantity_ordered NUMERIC(19,4) NOT NULL DEFAULT 0,
                                    quantity_received NUMERIC(19,4) NOT NULL DEFAULT 0,
                                    accepted_quantity NUMERIC(19,4) NOT NULL DEFAULT 0,
                                    rejected_quantity NUMERIC(19,4) NOT NULL DEFAULT 0,
                                    rejection_reason TEXT,
                                    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE supplier_invoice
    ADD COLUMN purchase_order_id UUID REFERENCES purchase_order(id),
    ADD COLUMN goods_receipt_id UUID REFERENCES goods_receipt(id),
    ADD COLUMN match_status VARCHAR(50) NOT NULL DEFAULT 'UNMATCHED',
    ADD COLUMN procurement_notes TEXT;

CREATE TABLE procurement_match_issue (
                                         id UUID PRIMARY KEY,
                                         supplier_invoice_id UUID NOT NULL REFERENCES supplier_invoice(id),
                                         purchase_order_id UUID REFERENCES purchase_order(id),
                                         goods_receipt_id UUID REFERENCES goods_receipt(id),
                                         issue_type VARCHAR(100) NOT NULL,
                                         severity VARCHAR(50) NOT NULL,
                                         message TEXT,
                                         amount NUMERIC(19,4),
                                         created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE supplier_statement_reconciliation (
                                                   id UUID PRIMARY KEY,
                                                   supplier_id UUID NOT NULL REFERENCES supplier(id),
                                                   statement_date DATE NOT NULL,
                                                   supplier_balance NUMERIC(19,4) NOT NULL DEFAULT 0,
                                                   system_balance NUMERIC(19,4) NOT NULL DEFAULT 0,
                                                   variance NUMERIC(19,4) NOT NULL DEFAULT 0,
                                                   status VARCHAR(50) NOT NULL DEFAULT 'VARIANCE',
                                                   notes TEXT,
                                                   created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_purchase_request_department ON purchase_request(department_id);
CREATE INDEX idx_purchase_order_supplier ON purchase_order(supplier_id);
CREATE INDEX idx_purchase_order_department ON purchase_order(department_id);
CREATE INDEX idx_purchase_order_status ON purchase_order(status);
CREATE INDEX idx_purchase_order_line_po ON purchase_order_line(purchase_order_id);
CREATE INDEX idx_goods_receipt_po ON goods_receipt(purchase_order_id);
CREATE INDEX idx_goods_receipt_supplier ON goods_receipt(supplier_id);
CREATE INDEX idx_goods_receipt_line_grn ON goods_receipt_line(goods_receipt_id);
CREATE INDEX idx_goods_receipt_line_po_line ON goods_receipt_line(purchase_order_line_id);
CREATE INDEX idx_supplier_invoice_po ON supplier_invoice(purchase_order_id);
CREATE INDEX idx_supplier_invoice_grn ON supplier_invoice(goods_receipt_id);
CREATE INDEX idx_supplier_invoice_match_status ON supplier_invoice(match_status);
CREATE INDEX idx_procurement_match_issue_invoice ON procurement_match_issue(supplier_invoice_id);
CREATE INDEX idx_supplier_statement_supplier ON supplier_statement_reconciliation(supplier_id);
CREATE INDEX idx_supplier_statement_date ON supplier_statement_reconciliation(statement_date);

UPDATE supplier
SET category = 'GENERAL'
WHERE category IS NULL;