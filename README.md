# unimed-test

Aplicacao full stack para gestao clinica de pacientes, atendimentos e procedimentos com dupla persistencia (PostgreSQL + MySQL), arquitetura DDD em camadas e frontend React.

## Pre-requisitos

- Docker 24+ com Compose v2
- Java 21 (apenas para desenvolvimento local)
- pnpm 9+ (apenas para desenvolvimento local)

## Quick Start

```bash
docker compose up --build
```

Apos inicializacao (30-60s), acesse:

| Servico | URL                       |
|---------|---------------------------|
| Frontend  | http://localhost:3000       |
| Backend   | http://localhost:8080       |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs   |
| Health    | http://localhost:8080/actuator/health |

## Exemplos de chamadas

### Pacientes

```bash
# Criar paciente
curl -sf -X POST http://localhost:8080/api/pacientes \
  -H "Content-Type: application/json" \
  -d '{"nome":"Joao Silva","cpf":"12345678901","dataNascimento":"1990-05-15","telefone":"11988887777","email":"joao@email.com"}'

# Listar pacientes (paginado)
curl -s "http://localhost:8080/api/pacientes?page=0&size=10&sort=nome,asc"

# Buscar por ID
curl -s http://localhost:8080/api/pacientes/1

# Atualizar
curl -sf -X PUT http://localhost:8080/api/pacientes/1 \
  -H "Content-Type: application/json" \
  -d '{"nome":"Joao Silva Atualizado","dataNascimento":"1990-05-15","telefone":"11988887777","email":"joao.novo@email.com"}'

# Excluir (409 se houver vinculos)
curl -s -o /dev/null -w "%{http_code}" -X DELETE http://localhost:8080/api/pacientes/1
```

### Atendimentos

```bash
# Criar atendimento
curl -sf -X POST http://localhost:8080/api/atendimentos \
  -H "Content-Type: application/json" \
  -d '{"pacienteId":1,"dataAtendimento":"2026-06-01T10:00:00Z","medico":"Dr. Carlos","observacoes":"Consulta geral"}'

# Listar (com filtro opcional por paciente)
curl -s "http://localhost:8080/api/atendimentos?page=0&size=10&sort=data_atendimento,desc&pacienteId=1"
```

### Procedimentos

```bash
# Criar procedimento
curl -sf -X POST http://localhost:8080/api/procedimentos \
  -H "Content-Type: application/json" \
  -d '{"atendimentoId":1,"nome":"Eletrocardiograma","valor":150.00}'

# Listar (com filtro opcional por atendimento)
curl -s "http://localhost:8080/api/procedimentos?page=0&size=10&sort=nome,asc&atendimentoId=1"
```

### Historico consolidado

```bash
# Historico completo do paciente (atendimentos + procedimentos)
curl -s "http://localhost:8080/api/pacientes/1/historico?sortOrder=desc"
```

### Validacao rapida pos-subida

```bash
curl -sf http://localhost:8080/api/pacientes?page=0\&size=1 | jq .
```

## Estrutura do projeto

```
/
├── backend/                          # Kotlin + Spring Boot
│   ├── src/main/kotlin/com/devpads/unimed/
│   │   ├── domain/                   # Entidades de dominio
│   │   ├── application/             # Casos de uso (services + ports)
│   │   └── infrastructure/          # Adapters (web, persistence)
│   ├── src/main/resources/db/migration/  # Flyway (PostgreSQL)
│   └── build.gradle.kts             # Gradle Kotlin DSL
├── frontend/                         # Next.js App Router + TypeScript
│   ├── src/components/              # Componentes UI (shadcn)
│   ├── src/hooks/                   # React Query hooks
│   ├── src/services/                # Camada de servicos (mock/API)
│   └── src/types/                   # Contratos tipados
├── docker/                           # Config Docker
│   └── mysql/init/                   # Schema inicial MySQL
├── docs/openapi/                     # OpenAPI contract
├── scripts/
│   └── smoke-test.sh                # Smoke test via curl
└── docker-compose.yml
```

## Decisoes tecnicas

- **Java 21 + Spring Boot 3.5.x** — LTS estavel
- **Dupla persistencia** — PostgreSQL (Flyway) p/ pacientes; MySQL (SQL init) p/ atendimentos/procedimentos
- **Arquitetura DDD** — `domain/ → application/ → infrastructure/` por modulo
- **Erros RFC 7807** — `GlobalExceptionHandler` com `ProblemDetail` + `details[]`
- **Frontend-first** — contratos tipados definidos antes da implementacao
- **React Query** — data fetching padronizado com `useQuery`/`useMutation`
- **Modulo status** — cada feature pode estar em `mock`, `hybrid` ou `api` (`frontend/src/services/module-status.ts`)

## Resolucao de problemas

### Flyway V2 falha

A migration `V2__pacientes_constraints_and_normalization.sql` normaliza CPF p/ 11 digitos e exige CPF/email unicos. Se falhar, use os SQLs abaixo p/ diagnosticar dados legados invalidos:

```sql
-- CPF invalido (diferente de 11 digitos apos normalizacao)
SELECT id, nome, cpf, regexp_replace(cpf, '\\D', '', 'g') AS cpf_normalizado
FROM pacientes
WHERE length(regexp_replace(cpf, '\\D', '', 'g')) <> 11;

-- CPF duplicado apos normalizacao
SELECT regexp_replace(cpf, '\\D', '', 'g') AS cpf_normalizado, COUNT(*)
FROM pacientes
GROUP BY regexp_replace(cpf, '\\D', '', 'g')
HAVING COUNT(*) > 1;

-- Email duplicado (case-insensitive)
SELECT lower(email) AS email_normalizado, COUNT(*)
FROM pacientes
GROUP BY lower(email)
HAVING COUNT(*) > 1;
```

A migration **nao** faz auto-correcao — corrija manualmente os dados legados e reexecute o container.

### Docker veth error (Linux)

Em ambientes Linux com restricao de virtualizacao, `docker compose up --build` pode falhar com `operation not supported`. Solucoes:

1. Tentar `docker compose up --build -d` com `DOCKER_DEFAULT_PLATFORM=linux/amd64`
2. Executar backend/frontend localmente (ver secao "Desenvolvimento local")
3. Usar ambiente com suporte a Docker Desktop ou WSL2

## Desenvolvimento local (sem Docker)

### Backend

```bash
cd backend
./gradlew bootRun
```

API em `http://localhost:8080`

Nota: requer PostgreSQL em `localhost:5432` e MySQL em `localhost:3306` (ou ajustar `application.properties`).

### Frontend

```bash
cd frontend
pnpm install
pnpm dev
```

App em `http://localhost:3000`

O frontend usa `http://localhost:8080` como API (definido em `.env.local`).
