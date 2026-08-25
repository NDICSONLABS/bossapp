-- src/main/resources/db/migration/postgresql/V1__init.sql

CREATE TABLE institution (
                             id UUID PRIMARY KEY,
                             name VARCHAR(255) NOT NULL,
                             code VARCHAR(255) NOT NULL UNIQUE,
                             active BOOLEAN NOT NULL DEFAULT TRUE,
                             created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE department (
                            id UUID PRIMARY KEY,
                            institution_id UUID NOT NULL REFERENCES institution(id),
                            code VARCHAR(255) NOT NULL UNIQUE,
                            name VARCHAR(255) NOT NULL,
                            type VARCHAR(100),
                            active BOOLEAN NOT NULL DEFAULT TRUE,
                            created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE app_user (
                          id UUID PRIMARY KEY,
                          username VARCHAR(100) NOT NULL UNIQUE,
                          password_hash VARCHAR(255) NOT NULL,
                          full_name VARCHAR(255),
                          active BOOLEAN NOT NULL DEFAULT TRUE,
                          department_id UUID REFERENCES department(id),
                          created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE role (
                      id UUID PRIMARY KEY,
                      name VARCHAR(100) NOT NULL UNIQUE,
                      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE user_role (
                           user_id UUID NOT NULL REFERENCES app_user(id),
                           role_id UUID NOT NULL REFERENCES role(id),
                           PRIMARY KEY (user_id, role_id)
);

CREATE TABLE student (
                         id UUID PRIMARY KEY,
                         student_number VARCHAR(100) NOT NULL UNIQUE,
                         full_name VARCHAR(255) NOT NULL,
                         department_id UUID NOT NULL REFERENCES department(id),
                         created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE student_charge (
                                id UUID PRIMARY KEY,
                                student_id UUID NOT NULL REFERENCES student(id),
                                department_id UUID NOT NULL REFERENCES department(id),
                                charge_date DATE,
                                due_date DATE,
                                amount NUMERIC(19,4) NOT NULL,
                                paid_amount NUMERIC(19,4) NOT NULL DEFAULT 0,
                                status VARCHAR(50) NOT NULL,
                                created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE supplier (
                          id UUID PRIMARY KEY,
                          code VARCHAR(100) NOT NULL UNIQUE,
                          name VARCHAR(255) NOT NULL,
                          active BOOLEAN NOT NULL DEFAULT TRUE,
                          created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE supplier_invoice (
                                  id UUID PRIMARY KEY,
                                  supplier_id UUID NOT NULL REFERENCES supplier(id),
                                  department_id UUID NOT NULL REFERENCES department(id),
                                  invoice_number VARCHAR(100) NOT NULL UNIQUE,
                                  invoice_date DATE,
                                  due_date DATE,
                                  total_amount NUMERIC(19,4) NOT NULL,
                                  paid_amount NUMERIC(19,4) NOT NULL DEFAULT 0,
                                  status VARCHAR(50) NOT NULL,
                                  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE payment (
                         id UUID PRIMARY KEY,
                         payment_number VARCHAR(100) NOT NULL UNIQUE,
                         payment_date DATE,
                         amount NUMERIC(19,4) NOT NULL,
                         unallocated_amount NUMERIC(19,4) NOT NULL DEFAULT 0,
                         direction VARCHAR(10),
                         payer_or_payee VARCHAR(255),
                         method VARCHAR(100),
                         status VARCHAR(50),
                         department_id UUID NOT NULL REFERENCES department(id),
                         created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE payment_allocation (
                                    id UUID PRIMARY KEY,
                                    payment_id UUID NOT NULL REFERENCES payment(id),
                                    target_type VARCHAR(100),
                                    target_id UUID,
                                    amount NUMERIC(19,4) NOT NULL,
                                    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_department_institution ON department(institution_id);
CREATE INDEX idx_app_user_department ON app_user(department_id);
CREATE INDEX idx_student_department ON student(department_id);
CREATE INDEX idx_student_charge_student ON student_charge(student_id);
CREATE INDEX idx_student_charge_department ON student_charge(department_id);
CREATE INDEX idx_student_charge_due_date ON student_charge(due_date);
CREATE INDEX idx_student_charge_status ON student_charge(status);
CREATE INDEX idx_supplier_invoice_supplier ON supplier_invoice(supplier_id);
CREATE INDEX idx_supplier_invoice_department ON supplier_invoice(department_id);
CREATE INDEX idx_supplier_invoice_due_date ON supplier_invoice(due_date);
CREATE INDEX idx_supplier_invoice_status ON supplier_invoice(status);
CREATE INDEX idx_payment_department ON payment(department_id);
CREATE INDEX idx_payment_payment_date ON payment(payment_date);
CREATE INDEX idx_payment_allocation_payment ON payment_allocation(payment_id);
CREATE INDEX idx_payment_allocation_target ON payment_allocation(target_type, target_id);