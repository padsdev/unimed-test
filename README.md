# unimed-test

Aplicacao full stack para gestao de pacientes, atendimentos e procedimentos, seguindo os requisitos de dupla persistencia (PostgreSQL e MySQL) e arquitetura em camadas.

## Estrutura

- `backend/`: API Kotlin + Spring Boot
- `frontend/`: Next.js + TypeScript (App Router)
- `docker-compose.yml`: sera adicionado na milestone de infraestrutura

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

## Decisoes tecnicas da base

- Java 21 (LTS)
- Spring Boot 3.5.x
- Serializacao JSON com datas em ISO
- Fuso padrao UTC na API
- Tratamento de erro via Problem Details (RFC 7807)
- Frontend-first com mocks tipados para acelerar UX
