-- src/main/resources/db/migration/postgresql/V9__gl_operational_integration.sql

ALTER TABLE student_charge
    ADD COLUMN gl_status VARCHAR(50) NOT NULL DEFAULT 'NOT_POSTED',
    ADD COLUMN gl_error TEXT,
    ADD COLUMN gl_posted_at TIMESTAMPTZ;

ALTER TABLE patient_charge
    ADD COLUMN gl_status VARCHAR(50) NOT NULL DEFAULT 'NOT_POSTED',
    ADD COLUMN gl_error TEXT,
    ADD COLUMN gl_posted_at TIMESTAMPTZ;

ALTER TABLE supplier_invoice
    ADD COLUMN gl_status VARCHAR(50) NOT NULL DEFAULT 'NOT_POSTED',
    ADD COLUMN gl_error TEXT,
    ADD COLUMN gl_posted_at TIMESTAMPTZ;

ALTER TABLE payment
    ADD COLUMN gl_status VARCHAR(50) NOT NULL DEFAULT 'NOT_POSTED',
    ADD COLUMN gl_error TEXT,
    ADD COLUMN gl_posted_at TIMESTAMPTZ;

CREATE TABLE gl_integration_log (
                                    id UUID PRIMARY KEY,
                                    source_type VARCHAR(100) NOT NULL,
                                    source_id UUID NOT NULL,
                                    action VARCHAR(100) NOT NULL,
                                    status VARCHAR(50) NOT NULL,
                                    message TEXT,
                                    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE gl_reconciliation (
                                   id UUID PRIMARY KEY,
                                   reconciliation_date DATE NOT NULL,
                                   source_type VARCHAR(100) NOT NULL,
                                   subledger_amount NUMERIC(19,4) NOT NULL DEFAULT 0,
                                   gl_amount NUMERIC(19,4) NOT NULL DEFAULT 0,
                                   variance NUMERIC(19,4) NOT NULL DEFAULT 0,
                                   status VARCHAR(50) NOT NULL,
                                   notes TEXT,
                                   created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE gl_setting (
                            setting_key VARCHAR(100) PRIMARY KEY,
                            setting_value VARCHAR(255),
                            description TEXT,
                            updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_gl_integration_log_source ON gl_integration_log(source_type, source_id);
CREATE INDEX idx_gl_integration_log_created_at ON gl_integration_log(created_at DESC);
CREATE INDEX idx_gl_reconciliation_date ON gl_reconciliation(reconciliation_date);
CREATE INDEX idx_gl_reconciliation_source ON gl_reconciliation(source_type);

INSERT INTO gl_setting (setting_key, setting_value, description)
VALUES
    ('AUTO_POST_STUDENT_CHARGE', 'TRUE', 'Automatically post student charges to GL.'),
    ('AUTO_POST_PATIENT_CHARGE', 'TRUE', 'Automatically post patient charges to GL.'),
    ('AUTO_POST_SUPPLIER_INVOICE', 'TRUE', 'Automatically post supplier invoices to GL.'),
    ('AUTO_POST_PAYMENT', 'TRUE', 'Automatically post payments to GL.'),
    ('AUTO_POST_ON_SUBMISSION_ACCEPT', 'TRUE', 'Automatically post submission transactions when central accounting accepts a submission.')
    ON CONFLICT (setting_key) DO NOTHING;