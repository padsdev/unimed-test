# unimed-test

Aplicacao full stack para gestao de pacientes, atendimentos e procedimentos, seguindo os requisitos de dupla persistencia (PostgreSQL e MySQL) e arquitetura em camadas.

## Estrutura

- `backend/`: API Kotlin + Spring Boot
- `frontend/`: Next.js + TypeScript (App Router)
- `docker-compose.yml`: orquestracao de backend, frontend, PostgreSQL e MySQL

## Milestone 0 (estado atual)

- Backend migrado para `backend/`
- Frontend funcional em `frontend/`
- UI base clinica com tokens de design
- React Query ativo desde o inicio com camada de servicos tipada

## Executando localmente

### Backend

```bash
cd backend
./gradlew bootRun
```

API em `http://localhost:8080`

### Frontend

```bash
cd frontend
pnpm dev
```

App em `http://localhost:3000`

## Executando com Docker Compose (Milestone 2)

1. (Opcional) copie variaveis padrao:

```bash
cp .env.example .env
```

2. Suba toda stack:

```bash
docker-compose up --build
```

Servicos:

- Frontend: `http://localhost:3000`
- Backend: `http://localhost:8080`
- Health backend: `http://localhost:8080/actuator/health`

Notas:

- PostgreSQL usa Flyway para criar schema de `pacientes`.
- MySQL inicializa schema de `atendimentos` e `procedimentos` via `docker/mysql/init`.
- Volumes nomeados preservam dados entre reinicios (`postgres_data`, `mysql_data`).

## Decisoes tecnicas da base

- Java 21 (LTS)
- Spring Boot 3.5.x
- Serializacao JSON com datas em ISO
- Fuso padrao UTC na API
- Tratamento de erro via Problem Details (RFC 7807)
- Frontend-first com mocks tipados para acelerar UX

## Flyway V2 (diagnostico rapido)

Se a migration `V2__pacientes_constraints_and_normalization.sql` falhar, rode estes SQLs no PostgreSQL para identificar dados legados invalidos antes de tentar novamente.

```sql
-- CPF invalido apos normalizacao (esperado 11 digitos)
SELECT id, nome, cpf, regexp_replace(cpf, '\\D', '', 'g') AS cpf_normalizado
FROM pacientes
WHERE length(regexp_replace(cpf, '\\D', '', 'g')) <> 11;

-- CPF duplicado apos normalizacao
SELECT regexp_replace(cpf, '\\D', '', 'g') AS cpf_normalizado, COUNT(*)
FROM pacientes
GROUP BY regexp_replace(cpf, '\\D', '', 'g')
HAVING COUNT(*) > 1;

-- Email com espaco em extremidade
SELECT id, email
FROM pacientes
WHERE email <> btrim(email);

-- Email vazio
SELECT id, email
FROM pacientes
WHERE btrim(email) = '';

-- Email duplicado ignorando case
SELECT lower(email) AS email_normalizado, COUNT(*)
FROM pacientes
GROUP BY lower(email)
HAVING COUNT(*) > 1;
```

Correcao deve ser manual e explicita nos dados legados. A migration nao faz auto-correcao de duplicidade nem inventa valores.
