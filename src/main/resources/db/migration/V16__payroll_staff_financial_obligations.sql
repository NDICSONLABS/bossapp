-- src/main/resources/db/migration/postgresql/V16__payroll_staff_financial_obligations.sql

INSERT INTO privilege (id, code, name, description)
VALUES (
           gen_random_uuid(),
           'PAYROLL_MANAGE',
           'Payroll Management',
           'Allows managing employees, payroll periods, payroll calculation, approval, and payment.'
       )
    ON CONFLICT (code) DO NOTHING;

INSERT INTO role_privilege (role_id, privilege_id)
SELECT r.id, p.id
FROM role r
         JOIN privilege p ON p.code = 'PAYROLL_MANAGE'
WHERE r.name IN (
                 'SYSTEM_ADMINISTRATOR',
                 'CENTRAL_ACCOUNTING_MANAGER',
                 'DEPARTMENT_FINANCE_OFFICER'
    )
    ON CONFLICT DO NOTHING;

INSERT INTO account_code (id, code, name, account_type, normal_balance, active)
VALUES
    (gen_random_uuid(), '2200', 'Payroll Liabilities', 'LIABILITY', 'CREDIT', TRUE),
    (gen_random_uuid(), '2300', 'Salaries Payable', 'LIABILITY', 'CREDIT', TRUE),
    (gen_random_uuid(), '5300', 'Salary Expense', 'EXPENSE', 'DEBIT', TRUE)
    ON CONFLICT (code) DO NOTHING;

INSERT INTO account_mapping (id, mapping_type, account_code_id, active)
SELECT gen_random_uuid(), 'PAYROLL_EXPENSE', id, TRUE
FROM account_code
WHERE code = '5300'
    ON CONFLICT (mapping_type) DO NOTHING;

INSERT INTO account_mapping (id, mapping_type, account_code_id, active)
SELECT gen_random_uuid(), 'PAYROLL_LIABILITY', id, TRUE
FROM account_code
WHERE code = '2200'
    ON CONFLICT (mapping_type) DO NOTHING;

INSERT INTO account_mapping (id, mapping_type, account_code_id, active)
SELECT gen_random_uuid(), 'PAYROLL_PAYABLE', id, TRUE
FROM account_code
WHERE code = '2300'
    ON CONFLICT (mapping_type) DO NOTHING;

INSERT INTO account_mapping (id, mapping_type, account_code_id, active)
SELECT gen_random_uuid(), 'PAYROLL_CASH', id, TRUE
FROM account_code
WHERE code = '1000'
    ON CONFLICT (mapping_type) DO NOTHING;

CREATE TABLE employee (
                          id UUID PRIMARY KEY,
                          employee_number VARCHAR(100) NOT NULL UNIQUE,
                          full_name VARCHAR(255) NOT NULL,
                          department_id UUID NOT NULL REFERENCES department(id),
                          job_title VARCHAR(255),
                          tax_id VARCHAR(100),
                          bank_name VARCHAR(255),
                          bank_account_number VARCHAR(100),
                          hire_date DATE,
                          termination_date DATE,
                          active BOOLEAN NOT NULL DEFAULT TRUE,
                          created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE payroll_component (
                                   id UUID PRIMARY KEY,
                                   code VARCHAR(100) NOT NULL UNIQUE,
                                   name VARCHAR(255) NOT NULL,
                                   component_type VARCHAR(50) NOT NULL,
                                   calculation_type VARCHAR(50) NOT NULL,
                                   default_amount NUMERIC(19,4),
                                   default_percent NUMERIC(9,4),
                                   taxable BOOLEAN NOT NULL DEFAULT FALSE,
                                   statutory BOOLEAN NOT NULL DEFAULT FALSE,
                                   active BOOLEAN NOT NULL DEFAULT TRUE,
                                   created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE employee_salary_component (
                                           id UUID PRIMARY KEY,
                                           employee_id UUID NOT NULL REFERENCES employee(id),
                                           payroll_component_id UUID NOT NULL REFERENCES payroll_component(id),
                                           amount NUMERIC(19,4),
                                           percentage NUMERIC(9,4),
                                           effective_date DATE,
                                           end_date DATE,
                                           active BOOLEAN NOT NULL DEFAULT TRUE,
                                           created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                           CONSTRAINT uq_employee_salary_component UNIQUE (employee_id, payroll_component_id)
);

CREATE TABLE payroll_period (
                                id UUID PRIMARY KEY,
                                fiscal_year INTEGER NOT NULL,
                                period_number INTEGER NOT NULL,
                                start_date DATE NOT NULL,
                                end_date DATE NOT NULL,
                                fund_id UUID REFERENCES fund(id),
                                grant_award_id UUID REFERENCES grant_award(id),
                                budget_line_id UUID REFERENCES budget_line(id),
                                total_gross NUMERIC(19,4) NOT NULL DEFAULT 0,
                                total_deductions NUMERIC(19,4) NOT NULL DEFAULT 0,
                                total_net NUMERIC(19,4) NOT NULL DEFAULT 0,
                                status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
                                payment_reference VARCHAR(100),
                                prepared_by VARCHAR(100),
                                approved_by VARCHAR(100),
                                approved_at TIMESTAMPTZ,
                                paid_at TIMESTAMPTZ,
                                created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                CONSTRAINT uq_payroll_period UNIQUE (fiscal_year, period_number)
);

CREATE TABLE employee_payroll_run (
                                      id UUID PRIMARY KEY,
                                      payroll_period_id UUID NOT NULL REFERENCES payroll_period(id),
                                      employee_id UUID NOT NULL REFERENCES employee(id),
                                      department_id UUID NOT NULL REFERENCES department(id),
                                      fund_id UUID REFERENCES fund(id),
                                      grant_award_id UUID REFERENCES grant_award(id),
                                      budget_line_id UUID REFERENCES budget_line(id),
                                      gross_amount NUMERIC(19,4) NOT NULL DEFAULT 0,
                                      total_deductions NUMERIC(19,4) NOT NULL DEFAULT 0,
                                      net_amount NUMERIC(19,4) NOT NULL DEFAULT 0,
                                      status VARCHAR(50) NOT NULL DEFAULT 'CALCULATED',
                                      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                      CONSTRAINT uq_employee_payroll_run UNIQUE (payroll_period_id, employee_id)
);

CREATE TABLE payroll_run_line (
                                  id UUID PRIMARY KEY,
                                  employee_payroll_run_id UUID NOT NULL REFERENCES employee_payroll_run(id),
                                  payroll_component_id UUID NOT NULL REFERENCES payroll_component(id),
                                  line_type VARCHAR(50) NOT NULL,
                                  amount NUMERIC(19,4) NOT NULL DEFAULT 0,
                                  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_employee_department ON employee(department_id);
CREATE INDEX idx_employee_active ON employee(active);
CREATE INDEX idx_payroll_component_type ON payroll_component(component_type);
CREATE INDEX idx_employee_salary_component_employee ON employee_salary_component(employee_id);
CREATE INDEX idx_employee_salary_component_component ON employee_salary_component(payroll_component_id);
CREATE INDEX idx_payroll_period_status ON payroll_period(status);
CREATE INDEX idx_employee_payroll_run_period ON employee_payroll_run(payroll_period_id);
CREATE INDEX idx_employee_payroll_run_employee ON employee_payroll_run(employee_id);
CREATE INDEX idx_payroll_run_line_run ON payroll_run_line(employee_payroll_run_id);
CREATE INDEX idx_payroll_run_line_component ON payroll_run_line(payroll_component_id);

INSERT INTO payroll_component (
    id,
    code,
    name,
    component_type,
    calculation_type,
    default_amount,
    default_percent,
    taxable,
    statutory,
    active
)
VALUES
    (
        gen_random_uuid(),
        'BASIC',
        'Basic Salary',
        'EARNING',
        'FIXED',
        0,
        NULL,
        TRUE,
        FALSE,
        TRUE
    ),
    (
        gen_random_uuid(),
        'HOUSING_ALLOWANCE',
        'Housing Allowance',
        'EARNING',
        'FIXED',
        0,
        NULL,
        TRUE,
        FALSE,
        TRUE
    ),
    (
        gen_random_uuid(),
        'TRANSPORT_ALLOWANCE',
        'Transport Allowance',
        'EARNING',
        'FIXED',
        0,
        NULL,
        TRUE,
        FALSE,
        TRUE
    ),
    (
        gen_random_uuid(),
        'PAYE',
        'Pay As You Earn Tax',
        'DEDUCTION',
        'PERCENTAGE_OF_BASIC',
        NULL,
        10.0000,
        FALSE,
        TRUE,
        TRUE
    ),
    (
        gen_random_uuid(),
        'PENSION',
        'Pension Contribution',
        'DEDUCTION',
        'PERCENTAGE_OF_BASIC',
        NULL,
        5.0000,
        FALSE,
        TRUE,
        TRUE
    ),
    (
        gen_random_uuid(),
        'HEALTH_INSURANCE',
        'Health Insurance Contribution',
        'DEDUCTION',
        'FIXED',
        0,
        NULL,
        FALSE,
        FALSE,
        TRUE
    )
    ON CONFLICT (code) DO NOTHING;