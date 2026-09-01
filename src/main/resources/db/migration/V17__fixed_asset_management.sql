-- src/main/resources/db/migration/postgresql/V17__fixed_asset_management.sql

INSERT INTO privilege (id, code, name, description)
VALUES (
           gen_random_uuid(),
           'ASSET_MANAGE',
           'Fixed Asset Management',
           'Allows managing fixed assets, running depreciation, and processing disposals.'
       )
    ON CONFLICT (code) DO NOTHING;

INSERT INTO role_privilege (role_id, privilege_id)
SELECT r.id, p.id
FROM role r
         JOIN privilege p ON p.code = 'ASSET_MANAGE'
WHERE r.name IN (
                 'SYSTEM_ADMINISTRATOR',
                 'CENTRAL_ACCOUNTING_MANAGER',
                 'DEPARTMENT_FINANCE_OFFICER'
    )
    ON CONFLICT DO NOTHING;

INSERT INTO account_code (id, code, name, account_type, normal_balance, active)
VALUES
    (gen_random_uuid(), '1500', 'Fixed Assets - Cost', 'ASSET', 'DEBIT', TRUE),
    (gen_random_uuid(), '1550', 'Accumulated Depreciation', 'ASSET', 'CREDIT', TRUE),
    (gen_random_uuid(), '5400', 'Depreciation Expense', 'EXPENSE', 'DEBIT', TRUE),
    (gen_random_uuid(), '5500', 'Loss on Asset Disposal', 'EXPENSE', 'DEBIT', TRUE),
    (gen_random_uuid(), '4500', 'Gain on Asset Disposal', 'REVENUE', 'CREDIT', TRUE)
    ON CONFLICT (code) DO NOTHING;

INSERT INTO account_mapping (id, mapping_type, account_code_id, active)
SELECT gen_random_uuid(), 'ASSET_COST', id, TRUE FROM account_code WHERE code = '1500' ON CONFLICT (mapping_type) DO NOTHING;
INSERT INTO account_mapping (id, mapping_type, account_code_id, active)
SELECT gen_random_uuid(), 'ACCUMULATED_DEPRECIATION', id, TRUE FROM account_code WHERE code = '1550' ON CONFLICT (mapping_type) DO NOTHING;
INSERT INTO account_mapping (id, mapping_type, account_code_id, active)
SELECT gen_random_uuid(), 'DEPRECIATION_EXPENSE', id, TRUE FROM account_code WHERE code = '5400' ON CONFLICT (mapping_type) DO NOTHING;
INSERT INTO account_mapping (id, mapping_type, account_code_id, active)
SELECT gen_random_uuid(), 'LOSS_ON_DISPOSAL', id, TRUE FROM account_code WHERE code = '5500' ON CONFLICT (mapping_type) DO NOTHING;
INSERT INTO account_mapping (id, mapping_type, account_code_id, active)
SELECT gen_random_uuid(), 'GAIN_ON_DISPOSAL', id, TRUE FROM account_code WHERE code = '4500' ON CONFLICT (mapping_type) DO NOTHING;

CREATE TABLE asset_category (
                                id UUID PRIMARY KEY,
                                code VARCHAR(100) NOT NULL UNIQUE,
                                name VARCHAR(255) NOT NULL,
                                useful_life_months INTEGER NOT NULL,
                                depreciation_method VARCHAR(50) NOT NULL DEFAULT 'STRAIGHT_LINE',
                                salvage_value_percent NUMERIC(9,4) NOT NULL DEFAULT 0,
                                capitalization_threshold NUMERIC(19,4) NOT NULL DEFAULT 0,
                                active BOOLEAN NOT NULL DEFAULT TRUE,
                                created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE fixed_asset (
                             id UUID PRIMARY KEY,
                             asset_number VARCHAR(100) NOT NULL UNIQUE,
                             description VARCHAR(255) NOT NULL,
                             category_id UUID NOT NULL REFERENCES asset_category(id),
                             department_id UUID NOT NULL REFERENCES department(id),
                             custodian_employee_id UUID REFERENCES employee(id),
                             physical_location VARCHAR(255),
                             serial_number VARCHAR(255),
                             acquisition_date DATE NOT NULL,
                             capitalized_date DATE,
                             original_cost NUMERIC(19,4) NOT NULL DEFAULT 0,
                             salvage_value NUMERIC(19,4) NOT NULL DEFAULT 0,
                             accumulated_depreciation NUMERIC(19,4) NOT NULL DEFAULT 0,
                             net_book_value NUMERIC(19,4) NOT NULL DEFAULT 0,
                             depreciation_method VARCHAR(50) NOT NULL,
                             useful_life_months INTEGER NOT NULL,
                             status VARCHAR(50) NOT NULL DEFAULT 'ACQUIRED',
                             purchase_order_id UUID REFERENCES purchase_order(id),
                             supplier_invoice_id UUID REFERENCES supplier_invoice(id),
                             created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE asset_depreciation_run (
                                        id UUID PRIMARY KEY,
                                        run_date DATE NOT NULL,
                                        period_year INTEGER NOT NULL,
                                        period_month INTEGER NOT NULL,
                                        total_depreciation NUMERIC(19,4) NOT NULL DEFAULT 0,
                                        status VARCHAR(50) NOT NULL DEFAULT 'COMPLETED',
                                        posted_by VARCHAR(100),
                                        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                        CONSTRAINT uq_depreciation_run UNIQUE (period_year, period_month)
);

CREATE TABLE asset_depreciation_line (
                                         id UUID PRIMARY KEY,
                                         run_id UUID NOT NULL REFERENCES asset_depreciation_run(id),
                                         asset_id UUID NOT NULL REFERENCES fixed_asset(id),
                                         department_id UUID NOT NULL REFERENCES department(id),
                                         depreciation_amount NUMERIC(19,4) NOT NULL DEFAULT 0,
                                         created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE asset_disposal (
                                id UUID PRIMARY KEY,
                                asset_id UUID NOT NULL REFERENCES fixed_asset(id),
                                disposal_date DATE NOT NULL,
                                disposal_type VARCHAR(50) NOT NULL,
                                proceeds NUMERIC(19,4) NOT NULL DEFAULT 0,
                                net_book_value_at_disposal NUMERIC(19,4) NOT NULL DEFAULT 0,
                                gain_or_loss NUMERIC(19,4) NOT NULL DEFAULT 0,
                                reason TEXT,
                                approved_by VARCHAR(100),
                                created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_asset_category_active ON asset_category(active);
CREATE INDEX idx_fixed_asset_category ON fixed_asset(category_id);
CREATE INDEX idx_fixed_asset_department ON fixed_asset(department_id);
CREATE INDEX idx_fixed_asset_custodian ON fixed_asset(custodian_employee_id);
CREATE INDEX idx_fixed_asset_status ON fixed_asset(status);
CREATE INDEX idx_depreciation_line_run ON asset_depreciation_line(run_id);
CREATE INDEX idx_depreciation_line_asset ON asset_depreciation_line(asset_id);
CREATE INDEX idx_asset_disposal_asset ON asset_disposal(asset_id);

INSERT INTO asset_category (id, code, name, useful_life_months, depreciation_method, salvage_value_percent, capitalization_threshold)
VALUES
    (gen_random_uuid(), 'IT_EQUIPMENT', 'IT Equipment & Computers', 36, 'STRAIGHT_LINE', 0, 1000.0000),
    (gen_random_uuid(), 'MEDICAL_EQUIPMENT', 'Medical & Hospital Equipment', 60, 'STRAIGHT_LINE', 5.0000, 5000.0000),
    (gen_random_uuid(), 'FURNITURE', 'Furniture & Fixtures', 120, 'STRAIGHT_LINE', 0, 500.0000),
    (gen_random_uuid(), 'VEHICLES', 'Motor Vehicles & Ambulances', 60, 'DECLINING_BALANCE', 10.0000, 20000.0000),
    (gen_random_uuid(), 'BUILDINGS', 'Buildings & Infrastructure', 480, 'STRAIGHT_LINE', 10.0000, 100000.0000)
    ON CONFLICT (code) DO NOTHING;