-- src/main/resources/db/migration/postgresql/V11__pharmacy_supplier_credit_control.sql

ALTER TABLE supplier
    ADD COLUMN credit_limit NUMERIC(19,4),
    ADD COLUMN supplier_subcategory VARCHAR(100),
    ADD COLUMN credit_hold BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE supplier_credit_control (
                                         id UUID PRIMARY KEY,
                                         supplier_id UUID NOT NULL UNIQUE REFERENCES supplier(id),
                                         credit_limit NUMERIC(19,4),
                                         credit_terms_days INTEGER,
                                         alert_threshold_days INTEGER NOT NULL DEFAULT 7,
                                         hold_on_limit_exceeded BOOLEAN NOT NULL DEFAULT FALSE,
                                         active BOOLEAN NOT NULL DEFAULT TRUE,
                                         created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE goods_receipt
    ADD COLUMN delivery_note_number VARCHAR(100),
    ADD COLUMN delivery_note_date DATE;

CREATE TABLE supplier_batch (
                                id UUID PRIMARY KEY,
                                supplier_id UUID NOT NULL REFERENCES supplier(id),
                                department_id UUID NOT NULL REFERENCES department(id),
                                purchase_order_line_id UUID REFERENCES purchase_order_line(id),
                                goods_receipt_line_id UUID REFERENCES goods_receipt_line(id),
                                batch_number VARCHAR(100) NOT NULL,
                                expiry_date DATE,
                                quantity NUMERIC(19,4),
                                unit_cost NUMERIC(19,4),
                                amount NUMERIC(19,4),
                                status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
                                created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE supplier_credit_alert (
                                       id UUID PRIMARY KEY,
                                       supplier_id UUID NOT NULL REFERENCES supplier(id),
                                       source_type VARCHAR(100),
                                       source_id UUID,
                                       alert_type VARCHAR(100) NOT NULL,
                                       severity VARCHAR(50) NOT NULL,
                                       message TEXT,
                                       due_date DATE,
                                       amount NUMERIC(19,4),
                                       acknowledged BOOLEAN NOT NULL DEFAULT FALSE,
                                       acknowledged_by VARCHAR(100),
                                       acknowledged_at TIMESTAMPTZ,
                                       created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE pharmacy_daily_reconciliation (
                                               id UUID PRIMARY KEY,
                                               department_id UUID NOT NULL REFERENCES department(id),
                                               reconciliation_date DATE NOT NULL,
                                               opening_supplier_credit NUMERIC(19,4) NOT NULL DEFAULT 0,
                                               new_supplier_invoices NUMERIC(19,4) NOT NULL DEFAULT 0,
                                               supplier_payments NUMERIC(19,4) NOT NULL DEFAULT 0,
                                               expected_closing_credit NUMERIC(19,4) NOT NULL DEFAULT 0,
                                               actual_closing_credit NUMERIC(19,4),
                                               variance NUMERIC(19,4),
                                               explanation TEXT,
                                               status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
                                               approved_by VARCHAR(100),
                                               approved_at TIMESTAMPTZ,
                                               created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                               CONSTRAINT uq_pharmacy_daily_reconciliation UNIQUE (department_id, reconciliation_date)
);

CREATE INDEX idx_supplier_credit_control_supplier ON supplier_credit_control(supplier_id);
CREATE INDEX idx_supplier_batch_supplier ON supplier_batch(supplier_id);
CREATE INDEX idx_supplier_batch_department ON supplier_batch(department_id);
CREATE INDEX idx_supplier_batch_expiry ON supplier_batch(expiry_date);
CREATE INDEX idx_supplier_credit_alert_supplier ON supplier_credit_alert(supplier_id);
CREATE INDEX idx_supplier_credit_alert_type ON supplier_credit_alert(alert_type);
CREATE INDEX idx_supplier_credit_alert_ack ON supplier_credit_alert(acknowledged);
CREATE INDEX idx_pharmacy_daily_recon_department ON pharmacy_daily_reconciliation(department_id);
CREATE INDEX idx_pharmacy_daily_recon_date ON pharmacy_daily_reconciliation(reconciliation_date);