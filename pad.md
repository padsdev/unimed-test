# TESTE TÉCNICO – DESENVOLVEDOR FULL STACK JÚNIOR

## Objetivo

Desenvolver uma aplicação para gerenciamento de pacientes e atendimentos médicos.

O objetivo deste teste é avaliar conhecimentos em:

* Kotlin
* Spring Boot
* PostgreSQL
* MySQL
* JDBC
* JPA/Hibernate
* Next.js
* Docker
* Boas práticas de desenvolvimento
* Arquitetura de software

---

# Tecnologias Obrigatórias

## Backend

* Kotlin
* Spring Boot 3+
* Maven ou Gradle

## Frontend

* Next.js
* TypeScript

## Banco de Dados

A aplicação deverá utilizar simultaneamente:

* PostgreSQL
* MySQL

---

# Requisitos Arquiteturais

O backend deverá seguir o padrão DDD (Domain-Driven Design).

A organização do projeto deverá refletir a separação adequada entre domínio, aplicação e infraestrutura.

---

# Regras de Persistência

## PostgreSQL

Todo acesso ao PostgreSQL deverá ser realizado utilizando:

* Spring Data JPA
* Hibernate

Não é permitido utilizar JDBC para operações relacionadas ao PostgreSQL.

---

## MySQL

Todo acesso ao MySQL deverá ser realizado utilizando:

* JDBC
* Connection
* PreparedStatement
* ResultSet

Não é permitido utilizar:

* Spring Data JPA
* Hibernate
* JdbcTemplate
* QueryDSL
* JOOQ
* MyBatis

para operações relacionadas ao MySQL.

---

# Cenário

Uma clínica médica possui um sistema legado dividido em dois bancos distintos.

Os dados cadastrais dos pacientes encontram-se armazenados em PostgreSQL.

Os dados operacionais dos atendimentos encontram-se armazenados em MySQL.

A aplicação deverá integrar essas informações para apresentar o histórico completo dos pacientes.

---

# Funcionalidades

## 1. CRUD de Pacientes

Persistido no PostgreSQL.

Campos:

| Campo          | Tipo   |
| -------------- | ------ |
| id             | Long   |
| nome           | String |
| cpf            | String |
| dataNascimento | Date   |
| telefone       | String |
| email          | String |

Operações:

* Criar paciente
* Buscar paciente por id
* Listar pacientes
* Atualizar paciente
* Remover paciente

---

## 2. CRUD de Atendimentos

Persistido no MySQL.

Campos:

| Campo           | Tipo     |
| --------------- | -------- |
| id              | Long     |
| pacienteId      | Long     |
| dataAtendimento | Datetime |
| medico          | String   |
| observacoes     | String   |

Operações:

* Criar atendimento
* Buscar atendimento por id
* Listar atendimentos
* Atualizar atendimento
* Remover atendimento

---

## 3. CRUD de Procedimentos

Persistido no MySQL.

Campos:

| Campo         | Tipo    |
| ------------- | ------- |
| id            | Long    |
| atendimentoId | Long    |
| nome          | String  |
| valor         | Decimal |

Operações:

* Criar procedimento
* Buscar procedimento por id
* Listar procedimentos
* Atualizar procedimento
* Remover procedimento

---

# Histórico Consolidado do Paciente

Criar o endpoint:

```http
GET /api/pacientes/{id}/historico
```

O endpoint deverá:

1. Buscar o paciente no PostgreSQL.
2. Buscar todos os atendimentos do paciente no MySQL.
3. Buscar todos os procedimentos vinculados aos atendimentos.
4. Consolidar os dados em uma única resposta.

Exemplo:

```json
{
  "id": 1,
  "nome": "João Silva",
  "cpf": "12345678900",
  "atendimentos": [
    {
      "id": 10,
      "dataAtendimento": "2026-05-01T09:00:00",
      "medico": "Dr. Carlos",
      "observacoes": "Paciente com dores no peito",
      "procedimentos": [
        {
          "id": 1,
          "nome": "Eletrocardiograma",
          "valor": 120.00
        },
        {
          "id": 2,
          "nome": "Consulta Cardiológica",
          "valor": 250.00
        }
      ]
    }
  ]
}
```

---

# API REST

A API deverá seguir boas práticas REST.

## Pacientes

```http
GET /api/pacientes

GET /api/pacientes/{id}

POST /api/pacientes

PUT /api/pacientes/{id}

DELETE /api/pacientes/{id}
```

---

## Atendimentos

```http
GET /api/atendimentos

GET /api/atendimentos/{id}

POST /api/atendimentos

PUT /api/atendimentos/{id}

DELETE /api/atendimentos/{id}
```

---

## Procedimentos

```http
GET /api/procedimentos

GET /api/procedimentos/{id}

POST /api/procedimentos

PUT /api/procedimentos/{id}

DELETE /api/procedimentos/{id}
```

---

# Validações

## Paciente

* Nome obrigatório
* CPF obrigatório
* CPF único
* Email válido

## Atendimento

* Médico obrigatório
* Data do atendimento obrigatória
* Paciente deve existir

## Procedimento

* Nome obrigatório
* Valor maior que zero
* Atendimento deve existir

---

# Tratamento de Erros

Implementar tratamento global de exceções.

Exemplos:

* Registro não encontrado
* Dados inválidos
* CPF duplicado
* Erros de integração
* Erros de banco de dados

---

# Frontend

Desenvolver uma interface utilizando Next.js.

---

## Tela de Pacientes

Funcionalidades:

* Listagem
* Cadastro
* Edição
* Exclusão

---

## Tela de Atendimentos

Funcionalidades:

* Listagem
* Cadastro
* Edição
* Exclusão

---

## Tela de Procedimentos

Funcionalidades:

* Listagem
* Cadastro
* Edição
* Exclusão

---

## Tela de Histórico

Permitir selecionar um paciente e visualizar:

* Dados cadastrais
* Lista de atendimentos
* Procedimentos realizados em cada atendimento

---

# PostgreSQL

Criar script de inicialização contendo:

```sql
CREATE TABLE pacientes (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    data_nascimento DATE NOT NULL,
    telefone VARCHAR(20),
    email VARCHAR(150)
);
```

---

# MySQL

Criar script de inicialização contendo:

```sql
CREATE TABLE atendimentos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    paciente_id BIGINT NOT NULL,
    data_atendimento DATETIME NOT NULL,
    medico VARCHAR(150) NOT NULL,
    observacoes TEXT
);
```

```sql
CREATE TABLE procedimentos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    atendimento_id BIGINT NOT NULL,
    nome VARCHAR(150) NOT NULL,
    valor DECIMAL(10,2) NOT NULL,

    CONSTRAINT fk_procedimento_atendimento
        FOREIGN KEY (atendimento_id)
        REFERENCES atendimentos(id)
);
```

---

# Docker

A aplicação deverá ser executada integralmente através de Docker Compose.

O ambiente deverá conter:

* Backend
* Frontend
* PostgreSQL
* MySQL

A execução deverá ocorrer através do comando:

```bash
docker-compose up --build
```

Nenhuma configuração manual adicional deverá ser necessária.

---

# Estrutura de Entrega

A solução deverá conter:

```text
/backend

/frontend

README.md

docker-compose.yml
```
---

# Diferenciais (Não Obrigatórios)

* Swagger/OpenAPI
* Testes unitários
* Testes de integração
* Testcontainers
* Paginação
* Filtros de pesquisa
* React Query
* Componentização avançada do frontend
* Flyway ou Liquibase
* Pipeline CI/CD