-- src/main/resources/db/migration/postgresql/V4__phase3_reporting_control.sql

CREATE TABLE audit_log (
    id UUID PRIMARY KEY,
    username VARCHAR(100),
    department_id UUID REFERENCES department(id),
    entity_type VARCHAR(100),
    entity_id VARCHAR(100),
    action VARCHAR(100),
    reason TEXT,
    before_value TEXT,
    after_value TEXT,
    ip_address VARCHAR(100),
    user_agent TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE report_template (
    id UUID PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    report_category VARCHAR(100),
    jasper_resource VARCHAR(255) NOT NULL,
    data_source_type VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE report_run (
    id UUID PRIMARY KEY,
    report_template_id UUID NOT NULL REFERENCES report_template(id),
    run_by_username VARCHAR(100),
    parameters TEXT,
    output_format VARCHAR(20),
    status VARCHAR(50),
    message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE cash_flow_adjustment (
    id UUID PRIMARY KEY,
    department_id UUID REFERENCES department(id),
    adjustment_date DATE NOT NULL,
    description VARCHAR(255) NOT NULL,
    direction VARCHAR(10) NOT NULL,
    amount NUMERIC(19,4) NOT NULL,
    currency VARCHAR(10),
    confidence VARCHAR(50),
    notes TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'APPROVED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_log_created_at ON audit_log(created_at DESC);
CREATE INDEX idx_audit_log_entity ON audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_log_username ON audit_log(username);

CREATE INDEX idx_report_template_code ON report_template(code);
CREATE INDEX idx_report_template_active ON report_template(active);

CREATE INDEX idx_report_run_template ON report_run(report_template_id);
CREATE INDEX idx_report_run_created_at ON report_run(created_at DESC);

CREATE INDEX idx_cash_flow_adjustment_date ON cash_flow_adjustment(adjustment_date);
CREATE INDEX idx_cash_flow_adjustment_status ON cash_flow_adjustment(status);

INSERT INTO report_template (
    id,
    code,
    name,
    description,
    report_category,
    jasper_resource,
    data_source_type,
    active
)
VALUES
    (
        gen_random_uuid(),
        'STUDENT_FEE_AGING',
        'Student Fee Aging',
        'Aging report for outstanding student fees.',
        'EDUCATION',
        '/reports/aging_report.jrxml',
        'AGING',
        TRUE
    ),
    (
        gen_random_uuid(),
        'PATIENT_DEBT_AGING',
        'Patient Debt Aging',
        'Aging report for outstanding patient charges.',
        'HEALTH',
        '/reports/aging_report.jrxml',
        'AGING',
        TRUE
    ),
    (
        gen_random_uuid(),
        'SUPPLIER_CREDIT_AGING',
        'Supplier Credit Aging',
        'Aging report for outstanding supplier invoices and credits.',
        'PROCUREMENT',
        '/reports/aging_report.jrxml',
        'AGING',
        TRUE
    ),
    (
        gen_random_uuid(),
        'CASH_FLOW_FORECAST',
        'Cash Flow Forecast',
        'Forecast of expected inflows and outflows based on outstanding balances and manual adjustments.',
        'FINANCE',
        '/reports/cash_flow_forecast.jrxml',
        'CASH_FLOW',
        TRUE
    );

INSERT INTO cash_flow_adjustment (
    id,
    department_id,
    adjustment_date,
    description,
    direction,
    amount,
    currency,
    confidence,
    notes,
    status
)
SELECT
    gen_random_uuid(),
    d.id,
    CURRENT_DATE + 10,
    'Sample institutional grant receipt',
    'INFLOW',
    1000000.0000,
    'USD',
    'HIGH',
    'Development sample cash-flow adjustment.',
    'APPROVED'
FROM department d
LIMIT 1;