-- src/main/resources/db/migration/postgresql/V6__period_privileges.sql

CREATE TABLE privilege (
                           id UUID PRIMARY KEY,
                           code VARCHAR(100) NOT NULL UNIQUE,
                           name VARCHAR(255) NOT NULL,
                           description TEXT,
                           created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE role_privilege (
                                role_id UUID NOT NULL REFERENCES role(id),
                                privilege_id UUID NOT NULL REFERENCES privilege(id),
                                PRIMARY KEY (role_id, privilege_id)
);

INSERT INTO privilege (id, code, name, description)
VALUES
    (gen_random_uuid(), 'ACCOUNTING_PERIOD_OPEN', 'Open Accounting Period', 'Allows opening or reopening accounting periods.'),
    (gen_random_uuid(), 'ACCOUNTING_PERIOD_LOCK', 'Lock Accounting Period', 'Allows locking accounting periods.'),
    (gen_random_uuid(), 'EDUCATION_FINANCE_MANAGE', 'Manage Education Finance', 'Allows managing education finance adjustments, fee schedules, receipts, and related operations.'),
    (gen_random_uuid(), 'CASHIER_RECONCILE', 'Cashier Reconciliation', 'Allows opening, closing, and approving daily cashier sessions.')
    ON CONFLICT (code) DO NOTHING;

INSERT INTO role_privilege (role_id, privilege_id)
SELECT r.id, p.id
FROM role r
         JOIN privilege p ON p.code IN (
                                        'ACCOUNTING_PERIOD_OPEN',
                                        'ACCOUNTING_PERIOD_LOCK',
                                        'EDUCATION_FINANCE_MANAGE',
                                        'CASHIER_RECONCILE'
    )
WHERE r.name IN (
                 'SYSTEM_ADMINISTRATOR',
                 'CENTRAL_ACCOUNTING_MANAGER'
    )
    ON CONFLICT DO NOTHING;

INSERT INTO role_privilege (role_id, privilege_id)
SELECT r.id, p.id
FROM role r
         JOIN privilege p ON p.code IN (
                                        'EDUCATION_FINANCE_MANAGE',
                                        'CASHIER_RECONCILE'
    )
WHERE r.name IN (
                 'SCHOOL_FEES_OFFICER',
                 'DEPARTMENT_FINANCE_OFFICER'
    )
    ON CONFLICT DO NOTHING;

ALTER TABLE accounting_period
    ADD COLUMN reopened_by VARCHAR(100),
    ADD COLUMN reopened_at TIMESTAMPTZ,
    ADD COLUMN reopen_reason TEXT;