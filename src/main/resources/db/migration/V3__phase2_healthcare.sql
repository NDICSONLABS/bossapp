-- src/main/resources/db/migration/postgresql/V3__phase2_healthcare.sql

CREATE TABLE insurance_provider (
                                    id UUID PRIMARY KEY,
                                    code VARCHAR(100) NOT NULL UNIQUE,
                                    name VARCHAR(255) NOT NULL,
                                    active BOOLEAN NOT NULL DEFAULT TRUE,
                                    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE patient_account (
                                 id UUID PRIMARY KEY,
                                 patient_number VARCHAR(100) NOT NULL UNIQUE,
                                 full_name VARCHAR(255) NOT NULL,
                                 department_id UUID NOT NULL REFERENCES department(id),
                                 insurance_provider_id UUID REFERENCES insurance_provider(id),
                                 active BOOLEAN NOT NULL DEFAULT TRUE,
                                 created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE patient_encounter (
                                   id UUID PRIMARY KEY,
                                   patient_account_id UUID NOT NULL REFERENCES patient_account(id),
                                   department_id UUID NOT NULL REFERENCES department(id),
                                   encounter_type VARCHAR(100),
                                   encounter_date DATE,
                                   status VARCHAR(50),
                                   created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE patient_charge (
                                id UUID PRIMARY KEY,
                                patient_account_id UUID NOT NULL REFERENCES patient_account(id),
                                patient_encounter_id UUID REFERENCES patient_encounter(id),
                                department_id UUID NOT NULL REFERENCES department(id),
                                service_category VARCHAR(255),
                                charge_date DATE,
                                due_date DATE,
                                amount NUMERIC(19,4) NOT NULL,
                                paid_amount NUMERIC(19,4) NOT NULL DEFAULT 0,
                                status VARCHAR(50) NOT NULL,
                                created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE insurance_claim (
                                 id UUID PRIMARY KEY,
                                 claim_number VARCHAR(100) NOT NULL UNIQUE,
                                 patient_account_id UUID NOT NULL REFERENCES patient_account(id),
                                 patient_encounter_id UUID REFERENCES patient_encounter(id),
                                 insurance_provider_id UUID NOT NULL REFERENCES insurance_provider(id),
                                 claim_date DATE,
                                 amount NUMERIC(19,4) NOT NULL,
                                 approved_amount NUMERIC(19,4),
                                 paid_amount NUMERIC(19,4) NOT NULL DEFAULT 0,
                                 status VARCHAR(50) NOT NULL,
                                 created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_patient_account_department ON patient_account(department_id);
CREATE INDEX idx_patient_account_insurer ON patient_account(insurance_provider_id);
CREATE INDEX idx_patient_encounter_patient ON patient_encounter(patient_account_id);
CREATE INDEX idx_patient_encounter_department ON patient_encounter(department_id);
CREATE INDEX idx_patient_charge_patient ON patient_charge(patient_account_id);
CREATE INDEX idx_patient_charge_department ON patient_charge(department_id);
CREATE INDEX idx_patient_charge_due_date ON patient_charge(due_date);
CREATE INDEX idx_patient_charge_status ON patient_charge(status);
CREATE INDEX idx_insurance_claim_patient ON insurance_claim(patient_account_id);
CREATE INDEX idx_insurance_claim_insurer ON insurance_claim(insurance_provider_id);
CREATE INDEX idx_insurance_claim_status ON insurance_claim(status);