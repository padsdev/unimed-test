# Plano de Execucao por Milestones (Frontend-First)

## Diagnostico rapido do estado atual

Com base no `pad.md` e no estado atual do repositorio:

- Estrutura de entrega ja alinhada com `/backend`, `/frontend` e `README.md`.
- Frontend base ja funcional com App Router, React Query e identidade visual clinica.
- Backend ainda em bootstrap, sem DDD completo e sem persistencia dual pronta.
- `docker-compose.yml` e scripts SQL de inicializacao ainda nao implementados.

## Premissa oficial do plano

Este projeto passa a seguir **frontend-first** de forma explicita:

- UX e fluxo de telas vem primeiro.
- Backend evolui em paralelo para cumprir contratos que o frontend ja usa.
- Mocks tipados sao usados cedo e trocados progressivamente por API real.
- Swagger/OpenAPI e atualizado incrementalmente conforme endpoints ficam viaveis.

---

## Milestone 0 - Foundation de repositorio e UX base (concluida)

### Objetivo

Criar base unica de projeto, visual e stack tecnica para acelerar as proximas entregas.

### Entregas

- Backend migrado para `backend/` (Spring Boot + Gradle).
- Frontend funcional em `frontend/` com Next.js + TypeScript + App Router + pnpm.
- React Query desde o inicio.
- UI base clinica com tokens, tipografia e shell responsivo.
- Convencoes iniciais de serializacao/data no backend.

### Criterios de aceite

- Build basico de backend e frontend funcionando.
- Navegacao inicial entre modulos existente.

### Checkpoints de commit

- **M0-CP1**: estrutura monorepo organizada (`/backend`, `/frontend`) e scripts basicos funcionando.
- **M0-CP2**: bootstrap backend (Gradle/Spring) + bootstrap frontend (Next/TypeScript/pnpm) sem erro.
- **M0-CP3**: shell de UX base com tokens visuais e navegacao inicial entre modulos.

---

## Milestone 1 - UX de fluxos principais com contrato estavel (mockado)

### Objetivo

Consolidar a experiencia ponta a ponta no frontend antes da API completa.

### Entregas

- Telas de `Pacientes`, `Atendimentos`, `Procedimentos` e `Historico` com layout final base.
- Estados de loading, erro, vazio e sucesso padronizados.
- Formularios iniciais com validacao no frontend (regras espelho do `pad.md`).
- Camada `services/` com contratos tipados que representem a API alvo.
- Politica de tamanho fixo da plataforma com scroll interno por componente (desktop), mantendo responsividade mobile.

### Criterios de aceite

- Fluxos principais navegaveis sem depender de backend real.
- Contratos de dados do frontend definidos e estaveis para orientar backend.

### Checkpoints de commit

- **M1-CP1**: contratos OpenAPI/tipos frontend definidos para Pacientes, Atendimentos, Procedimentos e Historico.
- **M1-CP2**: CRUD mockado com paginação, filtros, estados de loading/erro/vazio/sucesso.
- **M1-CP3**: padrao UX final aplicado (drawer/sheet, menu de acoes, feedback visual, historico hierarquico).
- **M1-CP4**: validacao final de lint/build e ajuste de responsividade + tamanhos fixos desktop.

---

## Milestone 2 - Infra de dados e Docker Compose completo

### Objetivo

Garantir ambiente reproduzivel por um unico comando.

### Entregas

- `docker-compose.yml` com 4 servicos: `backend`, `frontend`, `postgres`, `mysql`.
- Init SQL de PostgreSQL com tabela `pacientes` conforme enunciado.
- Init SQL de MySQL com tabelas `atendimentos` e `procedimentos` conforme enunciado.
- Startup sem configuracao manual adicional.
- Health checks e dependencias de subida para estabilidade.

### Criterios de aceite

- `docker-compose up --build` sobe ambiente completo.
- Bancos iniciados com schema esperado.

### Checkpoints de commit

- **M2-CP1**: `docker-compose.yml` com servicos e rede base.
- **M2-CP2**: scripts init SQL em PostgreSQL e MySQL com tabelas exigidas.
- **M2-CP3**: health checks, dependencias e startup ordenado sem passo manual.

---

## Milestone 3 - Arquitetura DDD backend e contratos REST base

### Objetivo

Estabelecer separacao dominio/aplicacao/infraestrutura e preparar contratos reais para substituir mocks.

### Entregas

- Estrutura DDD no `backend/src/main/kotlin`:
  - `domain/`
  - `application/`
  - `infrastructure/persistence/postgres`
  - `infrastructure/persistence/mysql`
  - `infrastructure/web`
  - `infrastructure/config`
- Tratamento global de excecao com codigos RESTful e Problem Details (`application/problem+json`).
- Primeira versao de Swagger/OpenAPI para endpoints ja expostos nesta milestone.
- Definicao de padroes de data/hora (UTC + ISO-8601) e naming de payloads.

### Criterios de aceite

- Sem regra de negocio em controller.
- Contratos REST do backend compativeis com contratos tipados do frontend.

### Checkpoints de commit

- **M3-CP1**: estrutura DDD criada no backend (domain/application/infrastructure).
- **M3-CP2**: erro global padronizado com Problem Details e codigos RESTful.
- **M3-CP3**: OpenAPI backend inicial publicado para endpoints da milestone.

---

## Milestone 4 - CRUD de Pacientes (PostgreSQL com JPA/Hibernate)

### Objetivo

Entregar primeiro modulo real e substituir mock correspondente no frontend.

### Entregas

- Endpoints:
  - `GET /api/pacientes`
  - `GET /api/pacientes/{id}`
  - `POST /api/pacientes`
  - `PUT /api/pacientes/{id}`
  - `DELETE /api/pacientes/{id}`
- Validacoes:
  - nome obrigatorio
  - cpf obrigatorio e unico
  - email valido
- Swagger atualizado para modulo Pacientes.
- Frontend Pacientes trocando de mock para API real.

### Criterios de aceite

- Persistencia em PostgreSQL via JPA/Hibernate funcionando.
- Tela de Pacientes operando contra backend real.

### Checkpoints de commit

- **M4-CP1.1**: migracao Flyway V2 (CPF 11 digitos, email unique case-insensitive), OpenAPI atualizada, frontend sem status. (concluido)
- **M4-CP1.2**: PacienteRepositoryPort + PacienteRepositoryJpa (PostgreSQL), metodos existsByCpf/email. (concluido)
- **M4-CP1.3**: AtendimentoVinculoPort + MySQL adapter com JDBC puro, Testcontainers. (concluido)
- **M4-CP2.1**: PacienteService com create + update (validacao negocio em service). (concluido)
- **M4-CP2.2**: controller POST/PUT, DTOs com Bean Validation, testes unitarios service com mock. (concluido)
- **M4-CP3**: DELETE /api/pacientes/{id} com validacao de vinculos (409 se atendimentos existentes). (concluido)
- **M4-CP4**: frontend Pacientes migrado de mock para API real. (proximo)
- **M4-CP5**: seed Flyway V3 (5 pacientes reais ficticios) + testes de integracao fim a fim (Testcontainers, todos endpoints + validacoes).

---

## Milestone 5 - CRUD de Atendimentos e Procedimentos (MySQL via JDBC puro)

### Objetivo

Entregar modulos operacionais respeitando restricao tecnica de JDBC puro.

### Entregas

- Endpoints de Atendimentos (listar, obter, criar, atualizar, excluir).
- Endpoints de Procedimentos (listar, obter, criar, atualizar, excluir).
- Regras:
  - medico obrigatorio
  - dataAtendimento obrigatoria
  - paciente deve existir
  - nome obrigatorio
  - valor > 0
  - atendimento deve existir
- Persistencia MySQL usando `Connection`, `PreparedStatement`, `ResultSet` (sem JPA/JdbcTemplate).
- Swagger atualizado para Atendimentos e Procedimentos.
- Frontend desses modulos migrado de mock para API real.

### Criterios de aceite

- CRUDs funcionais em MySQL.
- Nenhum uso de tecnologia proibida para MySQL.

### Checkpoints de commit

- **M5-CP1**: camada JDBC pura para Atendimentos (Connection/PreparedStatement/ResultSet).
- **M5-CP2**: camada JDBC pura para Procedimentos com regras de negocio.
- **M5-CP3**: endpoints REST completos e documentados em Swagger.
- **M5-CP4**: frontend Atendimentos/Procedimentos migrado de mock para API real.

---

## Milestone 6 - Historico consolidado do paciente

### Objetivo

Entregar principal caso de integracao entre os dois bancos e refletir isso na UX.

### Entregas

- Endpoint `GET /api/pacientes/{id}/historico` com consolidacao:
  1. paciente no PostgreSQL
  2. atendimentos no MySQL
  3. procedimentos por atendimento no MySQL
  4. resposta unica
- Estrategia para reduzir N+1 em MySQL (busca por lote quando aplicavel).
- Swagger atualizado para endpoint de historico.
- Tela de Historico consumindo backend real.

### Criterios de aceite

- Resposta conforme contrato do `pad.md`.
- Erro apropriado quando paciente nao existe.

### Checkpoints de commit

- **M6-CP1**: consolidacao backend paciente + atendimentos + procedimentos em resposta unica.
- **M6-CP2**: estrategia anti N+1 aplicada na consulta MySQL.
- **M6-CP3**: endpoint de historico documentado e frontend Historico consumindo API real.

---

## Milestone 7 - Robustez funcional e testes

### Objetivo

Aumentar confiabilidade da aplicacao com cobertura de cenarios criticos.

### Entregas

- Testes unitarios de regras de negocio backend.
- Testes de integracao para endpoints principais.
- Validacoes de fronteira no frontend para formularios criticos.
- Suite de smoke test frontend + backend + bancos em ambiente docker.

### Criterios de aceite

- Suite minima executa com sucesso.
- Fluxos obrigatorios do desafio passam em teste manual guiado.

### Checkpoints de commit

- **M7-CP1**: testes unitarios de dominio/backend para regras criticas.
- **M7-CP2**: testes de integracao dos endpoints principais.
- **M7-CP3**: smoke test de stack completa (frontend + backend + bancos) em Docker.
- **M7-CP4**: consolidacao de bugs encontrados e correcoes finais de estabilidade.

---

## Milestone 8 - Acabamento, observabilidade e entrega final

### Objetivo

Fechar entrega pronta para avaliacao tecnica sem ajustes manuais.

### Entregas

- Revisao final de `README.md` com:
  - pre-requisitos
  - comando unico de subida
  - URLs de frontend, backend e swagger
  - exemplos de chamadas
- Checklist de conformidade com todos os requisitos obrigatorios.
- Revisao de UX final (densidade, acessibilidade, consistencia visual entre telas).
- Hardening de logs e mensagens de erro para troubleshooting.

### Criterios de aceite

- Avaliador roda tudo do zero com `docker-compose up --build`.
- Requisitos obrigatorios atendidos explicitamente.

### Checkpoints de commit

- **M8-CP1**: README final com fluxo completo de execucao e validacao.
- **M8-CP2**: revisao de UX/acessibilidade e consistencia visual entre modulos.
- **M8-CP3**: hardening de logs/erros + checklist final de conformidade do desafio.

---

## Ordem ideal de execucao (resumo)

1. UX base e contratos frontend (mockados)
2. Docker + bancos + infra de execucao
3. DDD backend + contratos REST
4. CRUD Pacientes real
5. CRUD Atendimentos/Procedimentos real
6. Historico consolidado real
7. Testes e acabamento final

## Dependencias criticas

- Milestone 1 define os contratos que guiam milestones 3-6.
- Milestone 2 e pre-requisito para milestones 4-7.
- Milestone 6 depende da conclusao de 4 e 5.
- Milestone 8 depende do fechamento tecnico de todas anteriores.

## Riscos e mitigacoes (frontend-first)

- **Divergencia mock vs API real**: congelar contratos tipados e validar Swagger incrementalmente.
- **Atraso no backend quebrando UX**: priorizar entrega vertical por modulo (Pacientes -> Atendimentos/Procedimentos -> Historico).
- **Mistura indevida de persistencia**: separar pacotes/config por banco desde o inicio da camada infra.
- **Instabilidade local**: antecipar Docker completo na Milestone 2.

## Definicao de pronto (DoD) por milestone

Cada milestone so e concluida quando houver:

- codigo implementado
- validacao manual documentada
- testes minimos relacionados
- documentacao atualizada (incluindo Swagger quando aplicavel)
