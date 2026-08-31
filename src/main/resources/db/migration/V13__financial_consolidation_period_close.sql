-- src/main/resources/db/migration/postgresql/V13__financial_consolidation_period_close.sql

INSERT INTO privilege (id, code, name, description)
VALUES (
           gen_random_uuid(),
           'PERIOD_CLOSE',
           'Period Close',
           'Allows soft close, final close, and period close checklist management.'
       )
    ON CONFLICT (code) DO NOTHING;

INSERT INTO role_privilege (role_id, privilege_id)
SELECT r.id, p.id
FROM role r
         JOIN privilege p ON p.code = 'PERIOD_CLOSE'
WHERE r.name IN (
                 'SYSTEM_ADMINISTRATOR',
                 'CENTRAL_ACCOUNTING_MANAGER'
    )
    ON CONFLICT DO NOTHING;

CREATE TABLE period_close_task (
                                   id UUID PRIMARY KEY,
                                   period_id UUID NOT NULL REFERENCES accounting_period(id),
                                   task_code VARCHAR(100) NOT NULL,
                                   description TEXT,
                                   required BOOLEAN NOT NULL DEFAULT TRUE,
                                   status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
                                   completed_by VARCHAR(100),
                                   completed_at TIMESTAMPTZ,
                                   notes TEXT,
                                   created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                   CONSTRAINT uq_period_close_task UNIQUE (period_id, task_code)
);

CREATE TABLE period_close_validation (
                                         id UUID PRIMARY KEY,
                                         period_id UUID NOT NULL REFERENCES accounting_period(id),
                                         validation_code VARCHAR(100) NOT NULL,
                                         status VARCHAR(50) NOT NULL,
                                         message TEXT,
                                         created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE report_pack_run (
                                 id UUID PRIMARY KEY,
                                 period_id UUID REFERENCES accounting_period(id),
                                 report_code VARCHAR(100) NOT NULL,
                                 output_format VARCHAR(20) NOT NULL,
                                 file_name VARCHAR(255),
                                 status VARCHAR(50) NOT NULL DEFAULT 'SUCCESS',
                                 message TEXT,
                                 generated_by VARCHAR(100),
                                 created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

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
        'STATEMENT_OF_FINANCIAL_POSITION',
        'Statement of Financial Position',
        'Formal balance sheet statement.',
        'FINANCIAL_STATEMENT',
        '/reports/statement_of_financial_position.jrxml',
        'STATEMENT',
        TRUE
    ),
    (
        gen_random_uuid(),
        'STATEMENT_OF_ACTIVITY',
        'Statement of Activity',
        'Formal revenue and expense activity statement.',
        'FINANCIAL_STATEMENT',
        '/reports/statement_of_activity.jrxml',
        'STATEMENT',
        TRUE
    )
    ON CONFLICT (code) DO NOTHING;

CREATE INDEX idx_period_close_task_period ON period_close_task(period_id);
CREATE INDEX idx_period_close_validation_period ON period_close_validation(period_id);
CREATE INDEX idx_report_pack_run_period ON report_pack_run(period_id);
CREATE INDEX idx_report_pack_run_report ON report_pack_run(report_code);