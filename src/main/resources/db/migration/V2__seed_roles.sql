-- src/main/resources/db/migration/postgresql/V2__seed_roles.sql

CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO role (id, name)
VALUES
    (gen_random_uuid(), 'SYSTEM_ADMINISTRATOR'),
    (gen_random_uuid(), 'CENTRAL_ACCOUNTING_MANAGER'),
    (gen_random_uuid(), 'CENTRAL_ACCOUNTING_OFFICER'),
    (gen_random_uuid(), 'DEPARTMENT_MANAGER'),
    (gen_random_uuid(), 'DEPARTMENT_FINANCE_OFFICER'),
    (gen_random_uuid(), 'SCHOOL_FEES_OFFICER'),
    (gen_random_uuid(), 'HOSPITAL_BILLING_OFFICER'),
    (gen_random_uuid(), 'PHARMACY_OR_MEDICAL_STORE_OFFICER'),
    (gen_random_uuid(), 'PROCUREMENT_OFFICER'),
    (gen_random_uuid(), 'AUDITOR'),
    (gen_random_uuid(), 'VIEWER');