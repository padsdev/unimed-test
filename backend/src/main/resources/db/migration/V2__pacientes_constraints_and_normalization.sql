UPDATE pacientes
SET cpf = regexp_replace(cpf, '\D', '', 'g');

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pacientes
        WHERE length(cpf) <> 11
    ) THEN
        RAISE EXCEPTION 'Flyway V2 abortado: existem CPFs com formato invalido apos normalizacao (esperado 11 digitos).';
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT cpf
        FROM pacientes
        GROUP BY cpf
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Flyway V2 abortado: existem CPFs duplicados apos normalizacao.';
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pacientes
        WHERE email <> btrim(email)
    ) THEN
        RAISE EXCEPTION 'Flyway V2 abortado: existem emails com espacos nas extremidades.';
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pacientes
        WHERE btrim(email) = ''
    ) THEN
        RAISE EXCEPTION 'Flyway V2 abortado: existem emails vazios.';
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT lower(email)
        FROM pacientes
        GROUP BY lower(email)
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Flyway V2 abortado: existem emails duplicados ignorando case.';
    END IF;
END $$;

ALTER TABLE pacientes
    ALTER COLUMN cpf TYPE CHAR(11) USING cpf::CHAR(11);

ALTER TABLE pacientes
    DROP CONSTRAINT IF EXISTS chk_pacientes_cpf_digits,
    DROP CONSTRAINT IF EXISTS chk_pacientes_cpf_not_blank,
    DROP CONSTRAINT IF EXISTS chk_pacientes_email_trimmed,
    DROP CONSTRAINT IF EXISTS chk_pacientes_email_not_blank;

ALTER TABLE pacientes
    ADD CONSTRAINT chk_pacientes_cpf_digits CHECK (cpf ~ '^[0-9]{11}$'),
    ADD CONSTRAINT chk_pacientes_cpf_not_blank CHECK (btrim(cpf) <> ''),
    ADD CONSTRAINT chk_pacientes_email_trimmed CHECK (email = btrim(email)),
    ADD CONSTRAINT chk_pacientes_email_not_blank CHECK (btrim(email) <> '');

DROP INDEX IF EXISTS uq_pacientes_email_lower;

CREATE UNIQUE INDEX uq_pacientes_email_lower ON pacientes (lower(email));

ALTER TABLE pacientes
    DROP COLUMN IF EXISTS status;
