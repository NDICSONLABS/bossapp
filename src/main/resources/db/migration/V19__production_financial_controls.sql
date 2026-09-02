-- src/main/resources/db/migration/postgresql/V19__production_financial_controls.sql

INSERT INTO privilege (id, code, name, description)
VALUES
    (
        gen_random_uuid(),
        'PERIOD_SOFT_CLOSE_OVERRIDE',
        'Period Soft Close Override',
        'Allows posting into a soft-closed accounting period.'
    ),
    (
        gen_random_uuid(),
        'FINANCIAL_VALIDATION',
        'Financial Validation',
        'Allows running formal financial statement validations.'
    ),
    (
        gen_random_uuid(),
        'AUTO_REVERSAL_MANAGE',
        'Automatic Reversal Management',
        'Allows managing automatic reversal detection and execution.'
    )
    ON CONFLICT (code) DO NOTHING;

INSERT INTO role_privilege (role_id, privilege_id)
SELECT r.id, p.id
FROM role r
         JOIN privilege p ON p.code IN (
                                        'PERIOD_SOFT_CLOSE_OVERRIDE',
                                        'FINANCIAL_VALIDATION',
                                        'AUTO_REVERSAL_MANAGE'
    )
WHERE r.name IN (
                 'SYSTEM_ADMINISTRATOR',
                 'CENTRAL_ACCOUNTING_MANAGER'
    )
    ON CONFLICT DO NOTHING;

CREATE TABLE IF NOT EXISTS currency (
                                        id UUID PRIMARY KEY,
                                        code VARCHAR(10) NOT NULL UNIQUE,
                                        name VARCHAR(255) NOT NULL,
                                        decimal_precision INTEGER NOT NULL DEFAULT 2,
                                        active BOOLEAN NOT NULL DEFAULT TRUE,
                                        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS exchange_rate (
                                             id UUID PRIMARY KEY,
                                             from_currency VARCHAR(10) NOT NULL,
                                             to_currency VARCHAR(10) NOT NULL,
                                             rate_date DATE NOT NULL,
                                             rate NUMERIC(19,8) NOT NULL,
                                             created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                             CONSTRAINT uq_exchange_rate UNIQUE (from_currency, to_currency, rate_date)
);

CREATE TABLE IF NOT EXISTS tax_code (
                                        id UUID PRIMARY KEY,
                                        code VARCHAR(100) NOT NULL UNIQUE,
                                        name VARCHAR(255) NOT NULL,
                                        rate NUMERIC(9,4) NOT NULL DEFAULT 0,
                                        tax_type VARCHAR(50) NOT NULL DEFAULT 'EXCLUSIVE',
                                        sales_account_code_id UUID REFERENCES account_code(id),
                                        purchase_account_code_id UUID REFERENCES account_code(id),
                                        active BOOLEAN NOT NULL DEFAULT TRUE,
                                        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE accounting_entry
    ADD COLUMN IF NOT EXISTS transaction_currency VARCHAR(10),
    ADD COLUMN IF NOT EXISTS base_currency VARCHAR(10),
    ADD COLUMN IF NOT EXISTS exchange_rate NUMERIC(19,8),
    ADD COLUMN IF NOT EXISTS original_entry_id UUID REFERENCES accounting_entry(id),
    ADD COLUMN IF NOT EXISTS reversed_by_entry_id UUID REFERENCES accounting_entry(id),
    ADD COLUMN IF NOT EXISTS reversal_reason TEXT,
    ADD COLUMN IF NOT EXISTS auto_reversed BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE accounting_entry_line
    ADD COLUMN IF NOT EXISTS currency VARCHAR(10),
    ADD COLUMN IF NOT EXISTS exchange_rate NUMERIC(19,8),
    ADD COLUMN IF NOT EXISTS debit_currency NUMERIC(19,4),
    ADD COLUMN IF NOT EXISTS credit_currency NUMERIC(19,4),
    ADD COLUMN IF NOT EXISTS tax_code_id UUID REFERENCES tax_code(id),
    ADD COLUMN IF NOT EXISTS tax_basis NUMERIC(19,4),
    ADD COLUMN IF NOT EXISTS tax_amount NUMERIC(19,4);

ALTER TABLE accounting_period
    ADD COLUMN IF NOT EXISTS soft_closed_by VARCHAR(100),
    ADD COLUMN IF NOT EXISTS soft_closed_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS closed_by VARCHAR(100),
    ADD COLUMN IF NOT EXISTS closed_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS reopen_reason TEXT;

ALTER TABLE payment_allocation
    ADD COLUMN IF NOT EXISTS currency VARCHAR(10),
    ADD COLUMN IF NOT EXISTS exchange_rate NUMERIC(19,8),
    ADD COLUMN IF NOT EXISTS base_amount NUMERIC(19,4),
    ADD COLUMN IF NOT EXISTS accounting_status VARCHAR(50) NOT NULL DEFAULT 'NOT_POSTED';

CREATE TABLE IF NOT EXISTS financial_statement_validation (
                                                              id UUID PRIMARY KEY,
                                                              accounting_period_id UUID NOT NULL REFERENCES accounting_period(id),
                                                              validation_code VARCHAR(100) NOT NULL,
                                                              status VARCHAR(50) NOT NULL,
                                                              message TEXT,
                                                              numeric_value NUMERIC(19,4),
                                                              created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS gl_reconciliation_line (
                                                      id UUID PRIMARY KEY,
                                                      gl_reconciliation_id UUID NOT NULL REFERENCES gl_reconciliation(id),
                                                      account_code_id UUID REFERENCES account_code(id),
                                                      source_type VARCHAR(100),
                                                      source_id UUID,
                                                      accounting_entry_id UUID REFERENCES accounting_entry(id),
                                                      amount NUMERIC(19,4) NOT NULL DEFAULT 0,
                                                      status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
                                                      notes TEXT,
                                                      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS operational_reversal (
                                                    id UUID PRIMARY KEY,
                                                    source_type VARCHAR(100) NOT NULL,
                                                    source_id UUID NOT NULL,
                                                    accounting_entry_id UUID REFERENCES accounting_entry(id),
                                                    reversal_entry_id UUID REFERENCES accounting_entry(id),
                                                    reason TEXT,
                                                    status VARCHAR(50) NOT NULL DEFAULT 'DETECTED',
                                                    detected_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                                    reversed_at TIMESTAMPTZ,
                                                    reversed_by VARCHAR(100),
                                                    automatic BOOLEAN NOT NULL DEFAULT FALSE,
                                                    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS reconciliation_job_run (
                                                      id UUID PRIMARY KEY,
                                                      job_code VARCHAR(100) NOT NULL,
                                                      status VARCHAR(50) NOT NULL,
                                                      message TEXT,
                                                      started_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                                      completed_at TIMESTAMPTZ,
                                                      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO currency (id, code, name, decimal_precision, active)
VALUES
    (gen_random_uuid(), 'USD', 'US Dollar', 2, TRUE),
    (gen_random_uuid(), 'EUR', 'Euro', 2, TRUE),
    (gen_random_uuid(), 'GBP', 'British Pound', 2, TRUE)
    ON CONFLICT (code) DO NOTHING;

INSERT INTO account_code (id, code, name, account_type, normal_balance, active)
VALUES
    (gen_random_uuid(), '1110', 'Student Receivables', 'ASSET', 'DEBIT', TRUE),
    (gen_random_uuid(), '1120', 'Patient Receivables', 'ASSET', 'DEBIT', TRUE),
    (gen_random_uuid(), '1130', 'Insurance Receivables', 'ASSET', 'DEBIT', TRUE),
    (gen_random_uuid(), '1140', 'Government Receivables', 'ASSET', 'DEBIT', TRUE),
    (gen_random_uuid(), '1150', 'General Receivables', 'ASSET', 'DEBIT', TRUE),
    (gen_random_uuid(), '2500', 'Unallocated Funds', 'LIABILITY', 'CREDIT', TRUE),
    (gen_random_uuid(), '2600', 'Sales Tax Payable', 'LIABILITY', 'CREDIT', TRUE),
    (gen_random_uuid(), '2610', 'Purchase Tax Recoverable', 'LIABILITY', 'DEBIT', TRUE),
    (gen_random_uuid(), '4800', 'Discounts', 'REVENUE', 'DEBIT', TRUE),
    (gen_random_uuid(), '4810', 'Scholarships', 'REVENUE', 'DEBIT', TRUE),
    (gen_random_uuid(), '4820', 'Waivers', 'REVENUE', 'DEBIT', TRUE),
    (gen_random_uuid(), '4830', 'Subsidies', 'REVENUE', 'DEBIT', TRUE)
    ON CONFLICT (code) DO NOTHING;

INSERT INTO account_mapping (id, mapping_type, account_code_id, active)
SELECT gen_random_uuid(), 'STUDENT_AR', id, TRUE FROM account_code WHERE code = '1110'
    ON CONFLICT (mapping_type) DO NOTHING;

INSERT INTO account_mapping (id, mapping_type, account_code_id, active)
SELECT gen_random_uuid(), 'PATIENT_AR', id, TRUE FROM account_code WHERE code = '1120'
    ON CONFLICT (mapping_type) DO NOTHING;

INSERT INTO account_mapping (id, mapping_type, account_code_id, active)
SELECT gen_random_uuid(), 'INSURANCE_AR', id, TRUE FROM account_code WHERE code = '1130'
    ON CONFLICT (mapping_type) DO NOTHING;

INSERT INTO account_mapping (id, mapping_type, account_code_id, active)
SELECT gen_random_uuid(), 'GOVERNMENT_RECEIVABLE', id, TRUE FROM account_code WHERE code = '1140'
    ON CONFLICT (mapping_type) DO NOTHING;

INSERT INTO account_mapping (id, mapping_type, account_code_id, active)
SELECT gen_random_uuid(), 'GENERAL_AR', id, TRUE FROM account_code WHERE code = '1150'
    ON CONFLICT (mapping_type) DO NOTHING;

INSERT INTO account_mapping (id, mapping_type, account_code_id, active)
SELECT gen_random_uuid(), 'UNALLOCATED_FUNDS', id, TRUE FROM account_code WHERE code = '2500'
    ON CONFLICT (mapping_type) DO NOTHING;

INSERT INTO account_mapping (id, mapping_type, account_code_id, active)
SELECT gen_random_uuid(), 'SALES_TAX_PAYABLE', id, TRUE FROM account_code WHERE code = '2600'
    ON CONFLICT (mapping_type) DO NOTHING;

INSERT INTO account_mapping (id, mapping_type, account_code_id, active)
SELECT gen_random_uuid(), 'PURCHASE_TAX_RECOVERABLE', id, TRUE FROM account_code WHERE code = '2610'
    ON CONFLICT (mapping_type) DO NOTHING;

INSERT INTO account_mapping (id, mapping_type, account_code_id, active)
SELECT gen_random_uuid(), 'DISCOUNT_CONTRA_REVENUE', id, TRUE FROM account_code WHERE code = '4800'
    ON CONFLICT (mapping_type) DO NOTHING;

INSERT INTO account_mapping (id, mapping_type, account_code_id, active)
SELECT gen_random_uuid(), 'SCHOLARSHIP_EXPENSE', id, TRUE FROM account_code WHERE code = '4810'
    ON CONFLICT (mapping_type) DO NOTHING;

INSERT INTO account_mapping (id, mapping_type, account_code_id, active)
SELECT gen_random_uuid(), 'WAIVER_EXPENSE', id, TRUE FROM account_code WHERE code = '4820'
    ON CONFLICT (mapping_type) DO NOTHING;

INSERT INTO account_mapping (id, mapping_type, account_code_id, active)
SELECT gen_random_uuid(), 'SUBSIDY_EXPENSE', id, TRUE FROM account_code WHERE code = '4830'
    ON CONFLICT (mapping_type) DO NOTHING;

INSERT INTO tax_code (id, code, name, rate, tax_type, active)
VALUES
    (gen_random_uuid(), 'TAX-0', 'Zero Rated', 0, 'EXCLUSIVE', TRUE),
    (gen_random_uuid(), 'TAX-EX', 'Tax Exempt', 0, 'EXEMPT', TRUE),
    (gen_random_uuid(), 'TAX-STD', 'Standard Tax', 15, 'EXCLUSIVE', TRUE)
    ON CONFLICT (code) DO NOTHING;