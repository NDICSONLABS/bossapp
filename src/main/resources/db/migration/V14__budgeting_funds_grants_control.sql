-- src/main/resources/db/migration/postgresql/V14__budgeting_funds_grants_control.sql

INSERT INTO privilege (id, code, name, description)
VALUES (
           gen_random_uuid(),
           'BUDGET_MANAGE',
           'Budget Management',
           'Allows budget creation, approval, locking, and adjustment.'
       )
    ON CONFLICT (code) DO NOTHING;

INSERT INTO role_privilege (role_id, privilege_id)
SELECT r.id, p.id
FROM role r
         JOIN privilege p ON p.code = 'BUDGET_MANAGE'
WHERE r.name IN (
                 'SYSTEM_ADMINISTRATOR',
                 'CENTRAL_ACCOUNTING_MANAGER'
    )
    ON CONFLICT DO NOTHING;

CREATE TABLE donor (
                       id UUID PRIMARY KEY,
                       code VARCHAR(100) NOT NULL UNIQUE,
                       name VARCHAR(255) NOT NULL,
                       donor_type VARCHAR(100),
                       active BOOLEAN NOT NULL DEFAULT TRUE,
                       created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE fund (
                      id UUID PRIMARY KEY,
                      code VARCHAR(100) NOT NULL UNIQUE,
                      name VARCHAR(255) NOT NULL,
                      donor_id UUID REFERENCES donor(id),
                      active BOOLEAN NOT NULL DEFAULT TRUE,
                      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE grant_award (
                             id UUID PRIMARY KEY,
                             code VARCHAR(100) NOT NULL UNIQUE,
                             name VARCHAR(255) NOT NULL,
                             donor_id UUID NOT NULL REFERENCES donor(id),
                             fund_id UUID REFERENCES fund(id),
                             start_date DATE,
                             end_date DATE,
                             total_amount NUMERIC(19,4),
                             currency VARCHAR(10),
                             status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
                             active BOOLEAN NOT NULL DEFAULT TRUE,
                             created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE program (
                         id UUID PRIMARY KEY,
                         code VARCHAR(100) NOT NULL UNIQUE,
                         name VARCHAR(255) NOT NULL,
                         active BOOLEAN NOT NULL DEFAULT TRUE,
                         created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE project (
                         id UUID PRIMARY KEY,
                         program_id UUID REFERENCES program(id),
                         code VARCHAR(100) NOT NULL UNIQUE,
                         name VARCHAR(255) NOT NULL,
                         start_date DATE,
                         end_date DATE,
                         budget_amount NUMERIC(19,4),
                         status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
                         active BOOLEAN NOT NULL DEFAULT TRUE,
                         created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE cost_center (
                             id UUID PRIMARY KEY,
                             department_id UUID REFERENCES department(id),
                             code VARCHAR(100) NOT NULL UNIQUE,
                             name VARCHAR(255) NOT NULL,
                             active BOOLEAN NOT NULL DEFAULT TRUE,
                             created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE budget_header (
                               id UUID PRIMARY KEY,
                               fiscal_year INTEGER NOT NULL,
                               fund_id UUID NOT NULL REFERENCES fund(id),
                               grant_award_id UUID REFERENCES grant_award(id),
                               department_id UUID NOT NULL REFERENCES department(id),
                               program_id UUID REFERENCES program(id),
                               project_id UUID REFERENCES project(id),
                               cost_center_id UUID REFERENCES cost_center(id),
                               description TEXT,
                               status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
                               total_amount NUMERIC(19,4) NOT NULL DEFAULT 0,
                               created_by VARCHAR(100),
                               submitted_by VARCHAR(100),
                               submitted_at TIMESTAMPTZ,
                               approved_by VARCHAR(100),
                               approved_at TIMESTAMPTZ,
                               locked_by VARCHAR(100),
                               locked_at TIMESTAMPTZ,
                               created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE budget_line (
                             id UUID PRIMARY KEY,
                             budget_header_id UUID NOT NULL REFERENCES budget_header(id),
                             account_code_id UUID REFERENCES account_code(id),
                             expense_category VARCHAR(255),
                             description TEXT,
                             original_amount NUMERIC(19,4) NOT NULL DEFAULT 0,
                             adjusted_amount NUMERIC(19,4) NOT NULL DEFAULT 0,
                             reserved_amount NUMERIC(19,4) NOT NULL DEFAULT 0,
                             spent_amount NUMERIC(19,4) NOT NULL DEFAULT 0,
                             active BOOLEAN NOT NULL DEFAULT TRUE,
                             created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE budget_adjustment (
                                   id UUID PRIMARY KEY,
                                   budget_line_id UUID NOT NULL REFERENCES budget_line(id),
                                   amount NUMERIC(19,4) NOT NULL,
                                   reason TEXT,
                                   status VARCHAR(50) NOT NULL DEFAULT 'APPROVED',
                                   approved_by VARCHAR(100),
                                   approved_at TIMESTAMPTZ,
                                   created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE purchase_order
    ADD COLUMN budget_line_id UUID REFERENCES budget_line(id),
    ADD COLUMN invoiced_amount NUMERIC(19,4) NOT NULL DEFAULT 0;

ALTER TABLE supplier_invoice
    ADD COLUMN budget_line_id UUID REFERENCES budget_line(id);

ALTER TABLE accounting_entry_line
    ADD COLUMN budget_line_id UUID REFERENCES budget_line(id);

CREATE INDEX idx_fund_donor ON fund(donor_id);
CREATE INDEX idx_grant_award_donor ON grant_award(donor_id);
CREATE INDEX idx_grant_award_fund ON grant_award(fund_id);
CREATE INDEX idx_project_program ON project(program_id);
CREATE INDEX idx_cost_center_department ON cost_center(department_id);
CREATE INDEX idx_budget_header_fund ON budget_header(fund_id);
CREATE INDEX idx_budget_header_grant ON budget_header(grant_award_id);
CREATE INDEX idx_budget_header_department ON budget_header(department_id);
CREATE INDEX idx_budget_header_status ON budget_header(status);
CREATE INDEX idx_budget_line_header ON budget_line(budget_header_id);
CREATE INDEX idx_budget_line_account ON budget_line(account_code_id);
CREATE INDEX idx_budget_adjustment_line ON budget_adjustment(budget_line_id);
CREATE INDEX idx_purchase_order_budget_line ON purchase_order(budget_line_id);
CREATE INDEX idx_supplier_invoice_budget_line ON supplier_invoice(budget_line_id);
CREATE INDEX idx_accounting_entry_line_budget_line ON accounting_entry_line(budget_line_id);

INSERT INTO donor (id, code, name, donor_type, active)
VALUES (
           gen_random_uuid(),
           'INST',
           'Institutional Funding',
           'INSTITUTION',
           TRUE
       )
    ON CONFLICT (code) DO NOTHING;

INSERT INTO fund (id, code, name, donor_id, active)
SELECT
    gen_random_uuid(),
    'GENERAL',
    'General Fund',
    d.id,
    TRUE
FROM donor d
WHERE d.code = 'INST'
    ON CONFLICT (code) DO NOTHING;

INSERT INTO grant_award (
    id,
    code,
    name,
    donor_id,
    fund_id,
    start_date,
    end_date,
    total_amount,
    currency,
    status,
    active
)
SELECT
    gen_random_uuid(),
    'GRANT-2026',
    'General Operational Grant',
    d.id,
    f.id,
    CURRENT_DATE,
    CURRENT_DATE + INTERVAL '1 year',
    100000000.0000,
    'USD',
    'ACTIVE',
    TRUE
FROM donor d
         JOIN fund f ON f.code = 'GENERAL'
WHERE d.code = 'INST'
    ON CONFLICT (code) DO NOTHING;