-- src/main/resources/db/migration/postgresql/V12__inventory_stock_financial_integration.sql

INSERT INTO account_code (id, code, name, account_type, normal_balance, active)
VALUES
    (gen_random_uuid(), '1200', 'Inventory Asset', 'ASSET', 'DEBIT', TRUE),
    (gen_random_uuid(), '2100', 'Goods Received Not Invoiced', 'LIABILITY', 'CREDIT', TRUE),
    (gen_random_uuid(), '5100', 'Cost of Goods Consumed', 'EXPENSE', 'DEBIT', TRUE),
    (gen_random_uuid(), '5200', 'Inventory Write-off', 'EXPENSE', 'DEBIT', TRUE)
    ON CONFLICT (code) DO NOTHING;

INSERT INTO account_mapping (id, mapping_type, account_code_id, active)
SELECT gen_random_uuid(), 'INVENTORY_ASSET', id, TRUE
FROM account_code
WHERE code = '1200'
    ON CONFLICT (mapping_type) DO NOTHING;

INSERT INTO account_mapping (id, mapping_type, account_code_id, active)
SELECT gen_random_uuid(), 'INVENTORY_RECEIPT_CLEARING', id, TRUE
FROM account_code
WHERE code = '2100'
    ON CONFLICT (mapping_type) DO NOTHING;

INSERT INTO account_mapping (id, mapping_type, account_code_id, active)
SELECT gen_random_uuid(), 'COST_OF_GOODS_CONSUMED', id, TRUE
FROM account_code
WHERE code = '5100'
    ON CONFLICT (mapping_type) DO NOTHING;

INSERT INTO account_mapping (id, mapping_type, account_code_id, active)
SELECT gen_random_uuid(), 'INVENTORY_WRITE_OFF', id, TRUE
FROM account_code
WHERE code = '5200'
    ON CONFLICT (mapping_type) DO NOTHING;

CREATE TABLE item_category (
                               id UUID PRIMARY KEY,
                               code VARCHAR(100) NOT NULL UNIQUE,
                               name VARCHAR(255) NOT NULL,
                               category_type VARCHAR(100),
                               active BOOLEAN NOT NULL DEFAULT TRUE,
                               created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE unit_of_measure (
                                 id UUID PRIMARY KEY,
                                 code VARCHAR(100) NOT NULL UNIQUE,
                                 name VARCHAR(255) NOT NULL,
                                 created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE inventory_item (
                                id UUID PRIMARY KEY,
                                code VARCHAR(100) NOT NULL UNIQUE,
                                name VARCHAR(255) NOT NULL,
                                category_id UUID NOT NULL REFERENCES item_category(id),
                                unit_of_measure_id UUID NOT NULL REFERENCES unit_of_measure(id),
                                description TEXT,
                                batch_controlled BOOLEAN NOT NULL DEFAULT FALSE,
                                expiry_controlled BOOLEAN NOT NULL DEFAULT FALSE,
                                standard_cost NUMERIC(19,4),
                                sale_price NUMERIC(19,4),
                                active BOOLEAN NOT NULL DEFAULT TRUE,
                                created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE inventory_location (
                                    id UUID PRIMARY KEY,
                                    code VARCHAR(100) NOT NULL UNIQUE,
                                    name VARCHAR(255) NOT NULL,
                                    department_id UUID NOT NULL REFERENCES department(id),
                                    location_type VARCHAR(100),
                                    active BOOLEAN NOT NULL DEFAULT TRUE,
                                    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE inventory_balance (
                                   id UUID PRIMARY KEY,
                                   item_id UUID NOT NULL REFERENCES inventory_item(id),
                                   location_id UUID NOT NULL REFERENCES inventory_location(id),
                                   batch_id UUID REFERENCES supplier_batch(id),
                                   quantity_on_hand NUMERIC(19,4) NOT NULL DEFAULT 0,
                                   average_cost NUMERIC(19,4) NOT NULL DEFAULT 0,
                                   created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE inventory_transaction (
                                       id UUID PRIMARY KEY,
                                       transaction_number VARCHAR(100) NOT NULL UNIQUE,
                                       item_id UUID NOT NULL REFERENCES inventory_item(id),
                                       location_id UUID NOT NULL REFERENCES inventory_location(id),
                                       batch_id UUID REFERENCES supplier_batch(id),
                                       movement_type VARCHAR(50) NOT NULL,
                                       quantity NUMERIC(19,4) NOT NULL,
                                       unit_cost NUMERIC(19,4) NOT NULL DEFAULT 0,
                                       amount NUMERIC(19,4) NOT NULL DEFAULT 0,
                                       reference_type VARCHAR(100),
                                       reference_id UUID,
                                       transaction_date DATE NOT NULL,
                                       status VARCHAR(50) NOT NULL DEFAULT 'POSTED',
                                       notes TEXT,
                                       created_by VARCHAR(100),
                                       created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE supplier_batch
    ADD COLUMN item_id UUID REFERENCES inventory_item(id);

ALTER TABLE supplier_batch
    ALTER COLUMN supplier_id DROP NOT NULL;

ALTER TABLE purchase_order_line
    ADD COLUMN item_id UUID REFERENCES inventory_item(id);

ALTER TABLE goods_receipt_line
    ADD COLUMN item_id UUID REFERENCES inventory_item(id);

CREATE INDEX idx_inventory_item_category ON inventory_item(category_id);
CREATE INDEX idx_inventory_item_unit ON inventory_item(unit_of_measure_id);
CREATE INDEX idx_inventory_location_department ON inventory_location(department_id);
CREATE INDEX idx_inventory_balance_item ON inventory_balance(item_id);
CREATE INDEX idx_inventory_balance_location ON inventory_balance(location_id);
CREATE INDEX idx_inventory_balance_batch ON inventory_balance(batch_id);
CREATE INDEX idx_inventory_transaction_item ON inventory_transaction(item_id);
CREATE INDEX idx_inventory_transaction_location ON inventory_transaction(location_id);
CREATE INDEX idx_inventory_transaction_date ON inventory_transaction(transaction_date);
CREATE INDEX idx_inventory_transaction_reference ON inventory_transaction(reference_type, reference_id);

INSERT INTO item_category (id, code, name, category_type, active)
VALUES
    (gen_random_uuid(), 'DRUG', 'Drugs', 'DRUG', TRUE),
    (gen_random_uuid(), 'MEDICAL_SUPPLY', 'Medical Supplies', 'MEDICAL_SUPPLY', TRUE),
    (gen_random_uuid(), 'EDUCATION_MATERIAL', 'Education Materials', 'EDUCATION_MATERIAL', TRUE),
    (gen_random_uuid(), 'GENERAL', 'General Items', 'GENERAL', TRUE)
    ON CONFLICT (code) DO NOTHING;

INSERT INTO unit_of_measure (id, code, name)
VALUES
    (gen_random_uuid(), 'EACH', 'Each'),
    (gen_random_uuid(), 'BOX', 'Box'),
    (gen_random_uuid(), 'PACK', 'Pack'),
    (gen_random_uuid(), 'BOTTLE', 'Bottle')
    ON CONFLICT (code) DO NOTHING;

INSERT INTO inventory_item (
    id,
    code,
    name,
    category_id,
    unit_of_measure_id,
    description,
    batch_controlled,
    expiry_controlled,
    standard_cost,
    sale_price,
    active
)
SELECT
    gen_random_uuid(),
    'ITEM-0001',
    'Paracetamol 500mg Tablet',
    c.id,
    u.id,
    'Sample drug item for pharmacy operations.',
    TRUE,
    TRUE,
    50.0000,
    100.0000,
    TRUE
FROM item_category c
         JOIN unit_of_measure u ON u.code = 'EACH'
WHERE c.code = 'DRUG'
    ON CONFLICT (code) DO NOTHING;

INSERT INTO inventory_location (id, code, name, department_id, location_type, active)
SELECT
    gen_random_uuid(),
    'LOC-PHARM-001',
    'Main Pharmacy Store',
    d.id,
    'PHARMACY',
    TRUE
FROM department d
LIMIT 1
    ON CONFLICT (code) DO NOTHING;