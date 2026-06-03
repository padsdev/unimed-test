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
