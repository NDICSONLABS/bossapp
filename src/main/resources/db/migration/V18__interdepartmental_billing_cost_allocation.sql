-- src/main/resources/db/migration/postgresql/V18__interdepartmental_billing_cost_allocation.sql

INSERT INTO privilege (id, code, name, description)
VALUES (
           gen_random_uuid(),
           'INTERNAL_BILLING_MANAGE',
           'Internal Billing Management',
           'Allows managing inter-departmental invoices, settlements, and cost allocations.'
       )
    ON CONFLICT (code) DO NOTHING;

INSERT INTO role_privilege (role_id, privilege_id)
SELECT r.id, p.id
FROM role r
         JOIN privilege p ON p.code = 'INTERNAL_BILLING_MANAGE'
WHERE r.name IN (
                 'SYSTEM_ADMINISTRATOR',
                 'CENTRAL_ACCOUNTING_MANAGER',
                 'DEPARTMENT_FINANCE_OFFICER'
    )
    ON CONFLICT DO NOTHING;

INSERT INTO account_code (id, code, name, account_type, normal_balance, active)
VALUES
    (gen_random_uuid(), '1300', 'Interdepartmental Receivable', 'ASSET', 'DEBIT', TRUE),
    (gen_random_uuid(), '2400', 'Interdepartmental Payable', 'LIABILITY', 'CREDIT', TRUE),
    (gen_random_uuid(), '4900', 'Internal Service Revenue', 'REVENUE', 'CREDIT', TRUE),
    (gen_random_uuid(), '5900', 'Internal Service Expense', 'EXPENSE', 'DEBIT', TRUE)
    ON CONFLICT (code) DO NOTHING;

INSERT INTO account_mapping (id, mapping_type, account_code_id, active)
SELECT gen_random_uuid(), 'INTERDEPT_RECEIVABLE', id, TRUE
FROM account_code
WHERE code = '1300'
    ON CONFLICT (mapping_type) DO NOTHING;

INSERT INTO account_mapping (id, mapping_type, account_code_id, active)
SELECT gen_random_uuid(), 'INTERDEPT_PAYABLE', id, TRUE
FROM account_code
WHERE code = '2400'
    ON CONFLICT (mapping_type) DO NOTHING;

INSERT INTO account_mapping (id, mapping_type, account_code_id, active)
SELECT gen_random_uuid(), 'INTERNAL_REVENUE', id, TRUE
FROM account_code
WHERE code = '4900'
    ON CONFLICT (mapping_type) DO NOTHING;

INSERT INTO account_mapping (id, mapping_type, account_code_id, active)
SELECT gen_random_uuid(), 'INTERNAL_EXPENSE', id, TRUE
FROM account_code
WHERE code = '5900'
    ON CONFLICT (mapping_type) DO NOTHING;

CREATE TABLE internal_service_catalog (
                                          id UUID PRIMARY KEY,
                                          code VARCHAR(100) NOT NULL UNIQUE,
                                          name VARCHAR(255) NOT NULL,
                                          description TEXT,
                                          default_price NUMERIC(19,4),
                                          active BOOLEAN NOT NULL DEFAULT TRUE,
                                          created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE internal_invoice (
                                  id UUID PRIMARY KEY,
                                  invoice_number VARCHAR(100) NOT NULL UNIQUE,
                                  provider_department_id UUID NOT NULL REFERENCES department(id),
                                  receiver_department_id UUID NOT NULL REFERENCES department(id),
                                  service_id UUID REFERENCES internal_service_catalog(id),
                                  description TEXT,
                                  transaction_date DATE NOT NULL,
                                  due_date DATE,
                                  amount NUMERIC(19,4) NOT NULL,
                                  status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
                                  settlement_date DATE,
                                  posted_at TIMESTAMPTZ,
                                  settled_at TIMESTAMPTZ,
                                  created_by VARCHAR(100),
                                  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE cost_allocation_rule (
                                      id UUID PRIMARY KEY,
                                      name VARCHAR(255) NOT NULL,
                                      source_department_id UUID NOT NULL REFERENCES department(id),
                                      description TEXT,
                                      active BOOLEAN NOT NULL DEFAULT TRUE,
                                      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE cost_allocation_rule_target (
                                             id UUID PRIMARY KEY,
                                             rule_id UUID NOT NULL REFERENCES cost_allocation_rule(id),
                                             receiver_department_id UUID NOT NULL REFERENCES department(id),
                                             percentage NUMERIC(9,4) NOT NULL,
                                             created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                             CONSTRAINT uq_allocation_rule_target UNIQUE (rule_id, receiver_department_id)
);

CREATE TABLE cost_allocation_run (
                                     id UUID PRIMARY KEY,
                                     rule_id UUID NOT NULL REFERENCES cost_allocation_rule(id),
                                     period_year INTEGER NOT NULL,
                                     period_month INTEGER NOT NULL,
                                     total_amount NUMERIC(19,4) NOT NULL,
                                     status VARCHAR(50) NOT NULL DEFAULT 'POSTED',
                                     posted_by VARCHAR(100),
                                     created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                     CONSTRAINT uq_cost_allocation_run UNIQUE (rule_id, period_year, period_month)
);

CREATE TABLE cost_allocation_run_line (
                                          id UUID PRIMARY KEY,
                                          run_id UUID NOT NULL REFERENCES cost_allocation_run(id),
                                          receiver_department_id UUID NOT NULL REFERENCES department(id),
                                          percentage NUMERIC(9,4) NOT NULL,
                                          amount NUMERIC(19,4) NOT NULL,
                                          created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE internal_settlement (
                                     id UUID PRIMARY KEY,
                                     provider_department_id UUID NOT NULL REFERENCES department(id),
                                     receiver_department_id UUID NOT NULL REFERENCES department(id),
                                     settlement_date DATE NOT NULL,
                                     amount NUMERIC(19,4) NOT NULL,
                                     reference VARCHAR(255),
                                     status VARCHAR(50) NOT NULL DEFAULT 'POSTED',
                                     posted_by VARCHAR(100),
                                     created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_internal_service_active ON internal_service_catalog(active);
CREATE INDEX idx_internal_invoice_provider ON internal_invoice(provider_department_id);
CREATE INDEX idx_internal_invoice_receiver ON internal_invoice(receiver_department_id);
CREATE INDEX idx_internal_invoice_status ON internal_invoice(status);
CREATE INDEX idx_internal_invoice_date ON internal_invoice(transaction_date);
CREATE INDEX idx_cost_allocation_rule_source ON cost_allocation_rule(source_department_id);
CREATE INDEX idx_cost_allocation_target_rule ON cost_allocation_rule_target(rule_id);
CREATE INDEX idx_cost_allocation_run_rule ON cost_allocation_run(rule_id);
CREATE INDEX idx_cost_allocation_run_line_run ON cost_allocation_run_line(run_id);
CREATE INDEX idx_internal_settlement_provider ON internal_settlement(provider_department_id);
CREATE INDEX idx_internal_settlement_receiver ON internal_settlement(receiver_department_id);

INSERT INTO internal_service_catalog (id, code, name, description, default_price, active)
VALUES
    (gen_random_uuid(), 'IT_SUPPORT', 'IT Support Service', 'Central IT support charged to departments.', 500.0000, TRUE),
    (gen_random_uuid(), 'HR_PAYROLL_SERVICE', 'HR and Payroll Service', 'Central HR payroll processing service.', 300.0000, TRUE),
    (gen_random_uuid(), 'PROCUREMENT_SERVICE', 'Procurement Service', 'Central procurement processing service.', 250.0000, TRUE),
    (gen_random_uuid(), 'MEDICAL_CHECKUP', 'Student Medical Checkup', 'Hospital medical checkup service for schools.', 100.0000, TRUE),
    (gen_random_uuid(), 'TRANSPORT_SERVICE', 'Logistics Transport Service', 'Vehicle and transport services provided by logistics.', 150.0000, TRUE)
    ON CONFLICT (code) DO NOTHING;