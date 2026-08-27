-- src/main/resources/db/migration/postgresql/V8__double_entry_accounting.sql

CREATE TABLE account_code (
                              id UUID PRIMARY KEY,
                              code VARCHAR(100) NOT NULL UNIQUE,
                              name VARCHAR(255) NOT NULL,
                              account_type VARCHAR(50) NOT NULL,
                              normal_balance VARCHAR(10) NOT NULL,
                              active BOOLEAN NOT NULL DEFAULT TRUE,
                              created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE accounting_entry (
                                  id UUID PRIMARY KEY,
                                  entry_number VARCHAR(100) NOT NULL UNIQUE,
                                  entry_date DATE NOT NULL,
                                  accounting_period_id UUID NOT NULL REFERENCES accounting_period(id),
                                  department_id UUID REFERENCES department(id),
                                  description TEXT,
                                  source_type VARCHAR(100),
                                  source_id UUID,
                                  original_entry_id UUID REFERENCES accounting_entry(id),
                                  status VARCHAR(50) NOT NULL DEFAULT 'POSTED',
                                  posted_by VARCHAR(100),
                                  posted_at TIMESTAMPTZ,
                                  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE accounting_entry_line (
                                       id UUID PRIMARY KEY,
                                       entry_id UUID NOT NULL REFERENCES accounting_entry(id),
                                       account_code_id UUID NOT NULL REFERENCES account_code(id),
                                       debit NUMERIC(19,4) NOT NULL DEFAULT 0 CHECK (debit >= 0),
                                       credit NUMERIC(19,4) NOT NULL DEFAULT 0 CHECK (credit >= 0),
                                       department_id UUID REFERENCES department(id),
                                       description TEXT,
                                       created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE account_mapping (
                                 id UUID PRIMARY KEY,
                                 mapping_type VARCHAR(100) NOT NULL UNIQUE,
                                 account_code_id UUID NOT NULL REFERENCES account_code(id),
                                 active BOOLEAN NOT NULL DEFAULT TRUE,
                                 created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_account_code_code ON account_code(code);
CREATE INDEX idx_account_code_type ON account_code(account_type);
CREATE INDEX idx_accounting_entry_date ON accounting_entry(entry_date);
CREATE INDEX idx_accounting_entry_period ON accounting_entry(accounting_period_id);
CREATE INDEX idx_accounting_entry_source ON accounting_entry(source_type, source_id);
CREATE INDEX idx_accounting_entry_status ON accounting_entry(status);
CREATE INDEX idx_accounting_entry_line_entry ON accounting_entry_line(entry_id);
CREATE INDEX idx_accounting_entry_line_account ON accounting_entry_line(account_code_id);
CREATE INDEX idx_account_mapping_type ON account_mapping(mapping_type);

INSERT INTO account_code (
    id,
    code,
    name,
    account_type,
    normal_balance,
    active
)
VALUES
    (gen_random_uuid(), '1000', 'Cash and Bank', 'ASSET', 'DEBIT', TRUE),
    (gen_random_uuid(), '1100', 'Accounts Receivable', 'ASSET', 'DEBIT', TRUE),
    (gen_random_uuid(), '2000', 'Accounts Payable', 'LIABILITY', 'CREDIT', TRUE),
    (gen_random_uuid(), '4000', 'Education Revenue', 'REVENUE', 'CREDIT', TRUE),
    (gen_random_uuid(), '4100', 'Health Services Revenue', 'REVENUE', 'CREDIT', TRUE),
    (gen_random_uuid(), '5000', 'General Expense', 'EXPENSE', 'DEBIT', TRUE)
    ON CONFLICT (code) DO NOTHING;

INSERT INTO account_mapping (id, mapping_type, account_code_id, active)
SELECT gen_random_uuid(), 'STUDENT_CHARGE_AR', id, TRUE
FROM account_code
WHERE code = '1100'
    ON CONFLICT (mapping_type) DO NOTHING;

INSERT INTO account_mapping (id, mapping_type, account_code_id, active)
SELECT gen_random_uuid(), 'STUDENT_CHARGE_REVENUE', id, TRUE
FROM account_code
WHERE code = '4000'
    ON CONFLICT (mapping_type) DO NOTHING;

INSERT INTO account_mapping (id, mapping_type, account_code_id, active)
SELECT gen_random_uuid(), 'PATIENT_CHARGE_AR', id, TRUE
FROM account_code
WHERE code = '1100'
    ON CONFLICT (mapping_type) DO NOTHING;

INSERT INTO account_mapping (id, mapping_type, account_code_id, active)
SELECT gen_random_uuid(), 'PATIENT_CHARGE_REVENUE', id, TRUE
FROM account_code
WHERE code = '4100'
    ON CONFLICT (mapping_type) DO NOTHING;

INSERT INTO account_mapping (id, mapping_type, account_code_id, active)
SELECT gen_random_uuid(), 'SUPPLIER_INVOICE_EXPENSE', id, TRUE
FROM account_code
WHERE code = '5000'
    ON CONFLICT (mapping_type) DO NOTHING;

INSERT INTO account_mapping (id, mapping_type, account_code_id, active)
SELECT gen_random_uuid(), 'SUPPLIER_INVOICE_AP', id, TRUE
FROM account_code
WHERE code = '2000'
    ON CONFLICT (mapping_type) DO NOTHING;

INSERT INTO account_mapping (id, mapping_type, account_code_id, active)
SELECT gen_random_uuid(), 'PAYMENT_IN_CASH', id, TRUE
FROM account_code
WHERE code = '1000'
    ON CONFLICT (mapping_type) DO NOTHING;

INSERT INTO account_mapping (id, mapping_type, account_code_id, active)
SELECT gen_random_uuid(), 'PAYMENT_IN_AR', id, TRUE
FROM account_code
WHERE code = '1100'
    ON CONFLICT (mapping_type) DO NOTHING;

INSERT INTO account_mapping (id, mapping_type, account_code_id, active)
SELECT gen_random_uuid(), 'PAYMENT_OUT_CASH', id, TRUE
FROM account_code
WHERE code = '1000'
    ON CONFLICT (mapping_type) DO NOTHING;

INSERT INTO account_mapping (id, mapping_type, account_code_id, active)
SELECT gen_random_uuid(), 'PAYMENT_OUT_AP', id, TRUE
FROM account_code
WHERE code = '2000'
    ON CONFLICT (mapping_type) DO NOTHING;