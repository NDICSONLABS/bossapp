-- src/main/resources/db/migration/postgresql/V15__treasury_cashbook_bank_reconciliation.sql

INSERT INTO privilege (id, code, name, description)
VALUES
    (
        gen_random_uuid(),
        'TREASURY_MANAGE',
        'Treasury Management',
        'Allows managing treasury accounts, cashbook entries, and bank statements.'
    ),
    (
        gen_random_uuid(),
        'BANK_RECONCILE',
        'Bank Reconciliation',
        'Allows preparing, matching, completing, and approving bank reconciliations.'
    )
    ON CONFLICT (code) DO NOTHING;

INSERT INTO role_privilege (role_id, privilege_id)
SELECT r.id, p.id
FROM role r
         JOIN privilege p ON p.code = 'TREASURY_MANAGE'
WHERE r.name IN (
                 'SYSTEM_ADMINISTRATOR',
                 'CENTRAL_ACCOUNTING_MANAGER',
                 'DEPARTMENT_FINANCE_OFFICER'
    )
    ON CONFLICT DO NOTHING;

INSERT INTO role_privilege (role_id, privilege_id)
SELECT r.id, p.id
FROM role r
         JOIN privilege p ON p.code = 'BANK_RECONCILE'
WHERE r.name IN (
                 'SYSTEM_ADMINISTRATOR',
                 'CENTRAL_ACCOUNTING_MANAGER',
                 'CENTRAL_ACCOUNTING_OFFICER'
    )
    ON CONFLICT DO NOTHING;

CREATE TABLE treasury_account (
                                  id UUID PRIMARY KEY,
                                  code VARCHAR(100) NOT NULL UNIQUE,
                                  name VARCHAR(255) NOT NULL,
                                  account_type VARCHAR(50) NOT NULL,
                                  currency VARCHAR(10),
                                  department_id UUID REFERENCES department(id),
                                  bank_name VARCHAR(255),
                                  account_number VARCHAR(100),
                                  iban VARCHAR(100),
                                  swift VARCHAR(100),
                                  opening_balance NUMERIC(19,4) NOT NULL DEFAULT 0,
                                  current_balance NUMERIC(19,4) NOT NULL DEFAULT 0,
                                  allow_negative BOOLEAN NOT NULL DEFAULT FALSE,
                                  active BOOLEAN NOT NULL DEFAULT TRUE,
                                  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE treasury_transaction (
                                      id UUID PRIMARY KEY,
                                      transaction_number VARCHAR(100) NOT NULL UNIQUE,
                                      treasury_account_id UUID NOT NULL REFERENCES treasury_account(id),
                                      payment_id UUID REFERENCES payment(id),
                                      direction VARCHAR(10) NOT NULL,
                                      amount NUMERIC(19,4) NOT NULL,
                                      currency VARCHAR(10),
                                      transaction_date DATE NOT NULL,
                                      value_date DATE,
                                      reference VARCHAR(255),
                                      description TEXT,
                                      status VARCHAR(50) NOT NULL DEFAULT 'UNCLEARED',
                                      source_type VARCHAR(100),
                                      source_id UUID,
                                      created_by VARCHAR(100),
                                      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE bank_statement (
                                id UUID PRIMARY KEY,
                                treasury_account_id UUID NOT NULL REFERENCES treasury_account(id),
                                statement_number VARCHAR(100) NOT NULL,
                                statement_date DATE NOT NULL,
                                opening_balance NUMERIC(19,4) NOT NULL DEFAULT 0,
                                closing_balance NUMERIC(19,4) NOT NULL DEFAULT 0,
                                currency VARCHAR(10),
                                status VARCHAR(50) NOT NULL DEFAULT 'UPLOADED',
                                uploaded_by VARCHAR(100),
                                created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                CONSTRAINT uq_bank_statement UNIQUE (treasury_account_id, statement_number)
);

CREATE TABLE bank_statement_line (
                                     id UUID PRIMARY KEY,
                                     bank_statement_id UUID NOT NULL REFERENCES bank_statement(id),
                                     line_number INTEGER NOT NULL,
                                     transaction_date DATE NOT NULL,
                                     amount NUMERIC(19,4) NOT NULL,
                                     direction VARCHAR(10) NOT NULL,
                                     reference VARCHAR(255),
                                     description TEXT,
                                     matched_treasury_transaction_id UUID REFERENCES treasury_transaction(id),
                                     status VARCHAR(50) NOT NULL DEFAULT 'UNMATCHED',
                                     ignore_reason TEXT,
                                     created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE bank_reconciliation (
                                     id UUID PRIMARY KEY,
                                     treasury_account_id UUID NOT NULL REFERENCES treasury_account(id),
                                     bank_statement_id UUID NOT NULL REFERENCES bank_statement(id),
                                     statement_date DATE NOT NULL,
                                     statement_closing_balance NUMERIC(19,4) NOT NULL DEFAULT 0,
                                     cashbook_balance NUMERIC(19,4) NOT NULL DEFAULT 0,
                                     adjusted_balance NUMERIC(19,4) NOT NULL DEFAULT 0,
                                     variance NUMERIC(19,4) NOT NULL DEFAULT 0,
                                     status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
                                     prepared_by VARCHAR(100),
                                     approved_by VARCHAR(100),
                                     approved_at TIMESTAMPTZ,
                                     notes TEXT,
                                     created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_treasury_account_department ON treasury_account(department_id);
CREATE INDEX idx_treasury_account_type ON treasury_account(account_type);
CREATE INDEX idx_treasury_transaction_account ON treasury_transaction(treasury_account_id);
CREATE INDEX idx_treasury_transaction_date ON treasury_transaction(transaction_date);
CREATE INDEX idx_treasury_transaction_status ON treasury_transaction(status);
CREATE INDEX idx_treasury_transaction_source ON treasury_transaction(source_type, source_id);
CREATE INDEX idx_bank_statement_account ON bank_statement(treasury_account_id);
CREATE INDEX idx_bank_statement_line_statement ON bank_statement_line(bank_statement_id);
CREATE INDEX idx_bank_statement_line_status ON bank_statement_line(status);
CREATE INDEX idx_bank_reconciliation_account ON bank_reconciliation(treasury_account_id);
CREATE INDEX idx_bank_reconciliation_statement ON bank_reconciliation(bank_statement_id);

INSERT INTO treasury_account (
    id,
    code,
    name,
    account_type,
    currency,
    department_id,
    opening_balance,
    current_balance,
    allow_negative,
    active
)
SELECT
    gen_random_uuid(),
    'CASH-HQ-001',
    'Headquarters Cash',
    'CASH',
    'USD',
    d.id,
    0,
    0,
    FALSE,
    TRUE
FROM department d
LIMIT 1
    ON CONFLICT (code) DO NOTHING;

INSERT INTO treasury_account (
    id,
    code,
    name,
    account_type,
    currency,
    department_id,
    bank_name,
    opening_balance,
    current_balance,
    allow_negative,
    active
)
SELECT
    gen_random_uuid(),
    'BANK-HQ-001',
    'Headquarters Main Bank Account',
    'BANK',
    'USD',
    d.id,
    'National Bank',
    0,
    0,
    FALSE,
    TRUE
FROM department d
LIMIT 1
    ON CONFLICT (code) DO NOTHING;