-- src/main/resources/db/migration/postgresql/V5__phase4_central_accounting.sql

CREATE TABLE accounting_period (
                                   id UUID PRIMARY KEY,
                                   fiscal_year INTEGER NOT NULL,
                                   period_number INTEGER NOT NULL,
                                   start_date DATE NOT NULL,
                                   end_date DATE NOT NULL,
                                   status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
                                   opened_by VARCHAR(100),
                                   closed_by VARCHAR(100),
                                   locked_date DATE,
                                   created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                   CONSTRAINT uq_accounting_period UNIQUE (fiscal_year, period_number)
);

CREATE TABLE department_submission (
                                       id UUID PRIMARY KEY,
                                       department_id UUID NOT NULL REFERENCES department(id),
                                       period_id UUID NOT NULL REFERENCES accounting_period(id),
                                       opening_ap_balance NUMERIC(19,4) NOT NULL DEFAULT 0,
                                       new_ap_amount NUMERIC(19,4) NOT NULL DEFAULT 0,
                                       ap_payments NUMERIC(19,4) NOT NULL DEFAULT 0,
                                       closing_ap_balance NUMERIC(19,4) NOT NULL DEFAULT 0,
                                       opening_ar_balance NUMERIC(19,4) NOT NULL DEFAULT 0,
                                       new_ar_amount NUMERIC(19,4) NOT NULL DEFAULT 0,
                                       ar_collections NUMERIC(19,4) NOT NULL DEFAULT 0,
                                       adjustments NUMERIC(19,4) NOT NULL DEFAULT 0,
                                       closing_ar_balance NUMERIC(19,4) NOT NULL DEFAULT 0,
                                       transaction_count INTEGER NOT NULL DEFAULT 0,
                                       created_by VARCHAR(100),
                                       submitted_by VARCHAR(100),
                                       submitted_at TIMESTAMPTZ,
                                       department_approved_by VARCHAR(100),
                                       department_approved_at TIMESTAMPTZ,
                                       central_reviewed_by VARCHAR(100),
                                       central_reviewed_at TIMESTAMPTZ,
                                       review_comments TEXT,
                                       status VARCHAR(50) NOT NULL,
                                       created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE submission_transaction (
                                        id UUID PRIMARY KEY,
                                        submission_id UUID NOT NULL REFERENCES department_submission(id),
                                        target_type VARCHAR(100) NOT NULL,
                                        target_id UUID NOT NULL,
                                        direction VARCHAR(50),
                                        transaction_date DATE,
                                        amount NUMERIC(19,4) NOT NULL,
                                        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE department_reconciliation (
                                           id UUID PRIMARY KEY,
                                           submission_id UUID NOT NULL REFERENCES department_submission(id),
                                           description VARCHAR(255) NOT NULL,
                                           expected_amount NUMERIC(19,4) NOT NULL DEFAULT 0,
                                           actual_amount NUMERIC(19,4) NOT NULL DEFAULT 0,
                                           variance NUMERIC(19,4) NOT NULL DEFAULT 0,
                                           explanation TEXT,
                                           status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
                                           created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE student_charge
    ADD COLUMN accounting_status VARCHAR(50) NOT NULL DEFAULT 'NOT_SUBMITTED';

ALTER TABLE patient_charge
    ADD COLUMN accounting_status VARCHAR(50) NOT NULL DEFAULT 'NOT_SUBMITTED';

ALTER TABLE supplier_invoice
    ADD COLUMN accounting_status VARCHAR(50) NOT NULL DEFAULT 'NOT_SUBMITTED';

ALTER TABLE payment
    ADD COLUMN accounting_status VARCHAR(50) NOT NULL DEFAULT 'NOT_SUBMITTED';

CREATE INDEX idx_accounting_period_dates ON accounting_period(start_date, end_date);
CREATE INDEX idx_accounting_period_status ON accounting_period(status);

CREATE INDEX idx_department_submission_department ON department_submission(department_id);
CREATE INDEX idx_department_submission_period ON department_submission(period_id);
CREATE INDEX idx_department_submission_status ON department_submission(status);

CREATE INDEX idx_submission_transaction_submission ON submission_transaction(submission_id);
CREATE INDEX idx_submission_transaction_target ON submission_transaction(target_type, target_id);

CREATE INDEX idx_department_reconciliation_submission ON department_reconciliation(submission_id);

CREATE INDEX idx_student_charge_accounting_status ON student_charge(accounting_status);
CREATE INDEX idx_patient_charge_accounting_status ON patient_charge(accounting_status);
CREATE INDEX idx_supplier_invoice_accounting_status ON supplier_invoice(accounting_status);
CREATE INDEX idx_payment_accounting_status ON payment(accounting_status);

INSERT INTO accounting_period (
    id,
    fiscal_year,
    period_number,
    start_date,
    end_date,
    status,
    opened_by
)
SELECT
    gen_random_uuid(),
    EXTRACT(YEAR FROM CURRENT_DATE)::int,
    n,
    (date_trunc('year', CURRENT_DATE) + make_interval(months => n - 1))::date,
    (date_trunc('year', CURRENT_DATE) + make_interval(months => n))::date - 1,
    'OPEN',
    'system'
FROM generate_series(1, 12) AS n
    ON CONFLICT (fiscal_year, period_number) DO NOTHING;