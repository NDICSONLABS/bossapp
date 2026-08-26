-- src/main/resources/db/migration/postgresql/V7__education_finance_enhancement.sql

CREATE TABLE academic_year (
                               id UUID PRIMARY KEY,
                               code VARCHAR(100) NOT NULL UNIQUE,
                               name VARCHAR(255) NOT NULL,
                               start_date DATE,
                               end_date DATE,
                               active BOOLEAN NOT NULL DEFAULT TRUE,
                               created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE academic_term (
                               id UUID PRIMARY KEY,
                               academic_year_id UUID NOT NULL REFERENCES academic_year(id),
                               code VARCHAR(100) NOT NULL,
                               name VARCHAR(255) NOT NULL,
                               start_date DATE,
                               end_date DATE,
                               active BOOLEAN NOT NULL DEFAULT TRUE,
                               created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                               CONSTRAINT uq_academic_term UNIQUE (academic_year_id, code)
);

CREATE TABLE student_enrollment (
                                    id UUID PRIMARY KEY,
                                    student_id UUID NOT NULL REFERENCES student(id),
                                    academic_year_id UUID NOT NULL REFERENCES academic_year(id),
                                    term_id UUID REFERENCES academic_term(id),
                                    department_id UUID NOT NULL REFERENCES department(id),
                                    program_or_class VARCHAR(255),
                                    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
                                    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE fee_schedule (
                              id UUID PRIMARY KEY,
                              department_id UUID NOT NULL REFERENCES department(id),
                              academic_year_id UUID NOT NULL REFERENCES academic_year(id),
                              term_id UUID REFERENCES academic_term(id),
                              program_or_class VARCHAR(255),
                              student_category VARCHAR(100),
                              fee_type VARCHAR(255) NOT NULL,
                              amount NUMERIC(19,4) NOT NULL,
                              currency VARCHAR(10),
                              due_date DATE,
                              installment_number INTEGER NOT NULL DEFAULT 1,
                              mandatory BOOLEAN NOT NULL DEFAULT TRUE,
                              active BOOLEAN NOT NULL DEFAULT TRUE,
                              created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE student_charge
    ADD COLUMN service_category VARCHAR(255),
    ADD COLUMN academic_year_id UUID REFERENCES academic_year(id),
    ADD COLUMN term_id UUID REFERENCES academic_term(id),
    ADD COLUMN fee_schedule_id UUID REFERENCES fee_schedule(id),
    ADD COLUMN original_amount NUMERIC(19,4),
    ADD COLUMN discount_amount NUMERIC(19,4) NOT NULL DEFAULT 0,
    ADD COLUMN scholarship_amount NUMERIC(19,4) NOT NULL DEFAULT 0,
    ADD COLUMN waiver_amount NUMERIC(19,4) NOT NULL DEFAULT 0,
    ADD COLUMN net_amount NUMERIC(19,4);

UPDATE student_charge
SET original_amount = amount
WHERE original_amount IS NULL;

UPDATE student_charge
SET net_amount = amount
WHERE net_amount IS NULL;

ALTER TABLE student_charge
    ALTER COLUMN original_amount SET NOT NULL,
ALTER COLUMN net_amount SET NOT NULL;

CREATE TABLE student_charge_adjustment (
                                           id UUID PRIMARY KEY,
                                           student_charge_id UUID NOT NULL REFERENCES student_charge(id),
                                           adjustment_type VARCHAR(50) NOT NULL,
                                           amount NUMERIC(19,4) NOT NULL,
                                           reason TEXT,
                                           approved_by VARCHAR(100),
                                           approved_at TIMESTAMPTZ,
                                           status VARCHAR(50) NOT NULL DEFAULT 'APPROVED',
                                           created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE student_payment_plan (
                                      id UUID PRIMARY KEY,
                                      student_id UUID NOT NULL REFERENCES student(id),
                                      department_id UUID NOT NULL REFERENCES department(id),
                                      total_debt NUMERIC(19,4) NOT NULL,
                                      down_payment NUMERIC(19,4) NOT NULL DEFAULT 0,
                                      installment_amount NUMERIC(19,4) NOT NULL,
                                      frequency VARCHAR(50) NOT NULL DEFAULT 'MONTHLY',
                                      first_due_date DATE NOT NULL,
                                      number_of_installments INTEGER NOT NULL,
                                      responsible_officer VARCHAR(255),
                                      approval_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
                                      notes TEXT,
                                      status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
                                      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE payment_plan_installment (
                                          id UUID PRIMARY KEY,
                                          payment_plan_id UUID NOT NULL REFERENCES student_payment_plan(id),
                                          installment_number INTEGER NOT NULL,
                                          due_date DATE NOT NULL,
                                          amount NUMERIC(19,4) NOT NULL,
                                          paid_amount NUMERIC(19,4) NOT NULL DEFAULT 0,
                                          status VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED',
                                          created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE student_receipt (
                                 id UUID PRIMARY KEY,
                                 receipt_number VARCHAR(100) NOT NULL UNIQUE,
                                 payment_id UUID REFERENCES payment(id),
                                 student_id UUID NOT NULL REFERENCES student(id),
                                 department_id UUID NOT NULL REFERENCES department(id),
                                 received_date DATE NOT NULL,
                                 amount NUMERIC(19,4) NOT NULL,
                                 payment_method VARCHAR(100),
                                 payer VARCHAR(255),
                                 cashier VARCHAR(100),
                                 status VARCHAR(50) NOT NULL DEFAULT 'POSTED',
                                 created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE cashier_session (
                                 id UUID PRIMARY KEY,
                                 department_id UUID NOT NULL REFERENCES department(id),
                                 session_date DATE NOT NULL,
                                 cashier_username VARCHAR(100) NOT NULL,
                                 opening_balance NUMERIC(19,4) NOT NULL DEFAULT 0,
                                 expected_closing_balance NUMERIC(19,4),
                                 actual_closing_balance NUMERIC(19,4),
                                 variance NUMERIC(19,4),
                                 explanation TEXT,
                                 status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
                                 approved_by VARCHAR(100),
                                 approved_at TIMESTAMPTZ,
                                 created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                 CONSTRAINT uq_cashier_session UNIQUE (department_id, session_date)
);

CREATE TABLE cashier_transaction (
                                     id UUID PRIMARY KEY,
                                     cashier_session_id UUID NOT NULL REFERENCES cashier_session(id),
                                     payment_id UUID REFERENCES payment(id),
                                     payment_method VARCHAR(100),
                                     direction VARCHAR(10) NOT NULL,
                                     amount NUMERIC(19,4) NOT NULL,
                                     description VARCHAR(255),
                                     created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_academic_term_year ON academic_term(academic_year_id);
CREATE INDEX idx_student_enrollment_student ON student_enrollment(student_id);
CREATE INDEX idx_student_enrollment_year ON student_enrollment(academic_year_id);
CREATE INDEX idx_student_enrollment_department ON student_enrollment(department_id);
CREATE INDEX idx_fee_schedule_department ON fee_schedule(department_id);
CREATE INDEX idx_fee_schedule_year ON fee_schedule(academic_year_id);
CREATE INDEX idx_fee_schedule_term ON fee_schedule(term_id);
CREATE INDEX idx_student_charge_year ON student_charge(academic_year_id);
CREATE INDEX idx_student_charge_term ON student_charge(term_id);
CREATE INDEX idx_student_charge_fee_schedule ON student_charge(fee_schedule_id);
CREATE INDEX idx_student_charge_adjustment_charge ON student_charge_adjustment(student_charge_id);
CREATE INDEX idx_student_payment_plan_student ON student_payment_plan(student_id);
CREATE INDEX idx_payment_plan_installment_plan ON payment_plan_installment(payment_plan_id);
CREATE INDEX idx_student_receipt_student ON student_receipt(student_id);
CREATE INDEX idx_student_receipt_payment ON student_receipt(payment_id);
CREATE INDEX idx_cashier_session_department ON cashier_session(department_id);
CREATE INDEX idx_cashier_session_date ON cashier_session(session_date);
CREATE INDEX idx_cashier_transaction_session ON cashier_transaction(cashier_session_id);

INSERT INTO academic_year (id, code, name, start_date, end_date, active)
SELECT
    gen_random_uuid(),
    'AY-2026',
    'Academic Year 2026',
    CURRENT_DATE,
    CURRENT_DATE + INTERVAL '1 year',
    TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM academic_year WHERE code = 'AY-2026'
);

INSERT INTO academic_term (id, academic_year_id, code, name, start_date, end_date, active)
SELECT
    gen_random_uuid(),
    ay.id,
    'TERM-1',
    'Term 1',
    ay.start_date,
    ay.start_date + INTERVAL '4 months',
    TRUE
FROM academic_year ay
WHERE ay.code = 'AY-2026'
  AND NOT EXISTS (
    SELECT 1
    FROM academic_term t
    WHERE t.academic_year_id = ay.id
      AND t.code = 'TERM-1'
);

INSERT INTO student_enrollment (
    id,
    student_id,
    academic_year_id,
    term_id,
    department_id,
    program_or_class,
    status
)
SELECT
    gen_random_uuid(),
    s.id,
    ay.id,
    at.id,
    d.id,
    'Grade 1',
    'ACTIVE'
FROM student s
         JOIN department d ON d.code = 'SCH-001'
         JOIN academic_year ay ON ay.code = 'AY-2026'
         JOIN academic_term at ON at.academic_year_id = ay.id AND at.code = 'TERM-1'
WHERE s.student_number = 'STU-0001'
  AND NOT EXISTS (
    SELECT 1
    FROM student_enrollment e
    WHERE e.student_id = s.id
      AND e.academic_year_id = ay.id
      AND e.term_id = at.id
);

INSERT INTO fee_schedule (
    id,
    department_id,
    academic_year_id,
    term_id,
    program_or_class,
    student_category,
    fee_type,
    amount,
    currency,
    due_date,
    installment_number,
    mandatory,
    active
)
SELECT
    gen_random_uuid(),
    d.id,
    ay.id,
    at.id,
    'Grade 1',
    'ALL',
    'TUITION',
    150000.0000,
    'USD',
    CURRENT_DATE + 30,
    1,
    TRUE,
    TRUE
FROM department d
         JOIN academic_year ay ON ay.code = 'AY-2026'
         JOIN academic_term at ON at.academic_year_id = ay.id AND at.code = 'TERM-1'
WHERE d.code = 'SCH-001'
  AND NOT EXISTS (
    SELECT 1
    FROM fee_schedule fs
    WHERE fs.department_id = d.id
      AND fs.academic_year_id = ay.id
      AND fs.term_id = at.id
      AND fs.fee_type = 'TUITION'
      AND fs.program_or_class = 'Grade 1'
);