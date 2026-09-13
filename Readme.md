# CareSync

Sistema de agendamento hospitalar desenvolvido para o Tech Challenge da Fase 3
(Arquitetura e Desenvolvimento Java - Pós Tech). Permite que médicos,
enfermeiros e pacientes interajam com o agendamento de consultas, cada um
com um nível de acesso apropriado ao seu perfil, e notifica pacientes
automaticamente quando uma consulta é criada ou atualizada.

## Arquitetura

O sistema é composto por dois serviços independentes que se comunicam de
forma assíncrona via RabbitMQ:

```
┌──────────────────────┐         RabbitMQ          ┌───────────────────────┐
│  agendamento-service  │  ───(evento consulta)──▶  │  notificacao-service  │
│                       │                            │                       │
│  - REST API           │                            │  - Consome eventos    │
│  - GraphQL             │                            │  - Processa lembretes │
│  - Spring Security     │                            │                       │
│  - H2 (em memória)     │                            │                       │
└──────────────────────┘                            └───────────────────────┘
        :8080                                                :8081
```

- **agendamento-service**: responsável por toda a lógica de negócio de
  consultas — CRUD via REST, consultas de histórico via GraphQL, autenticação
  e autorização por perfil de usuário. Publica um evento no RabbitMQ sempre
  que uma consulta é criada ou editada.
- **notificacao-service**: consome os eventos publicados pelo
  agendamento-service e processa o lembrete ao paciente (simulado via log
  formatado no console, representando onde entraria uma integração real de
  e-mail/SMS/push).

## Stack técnica

- Java 21
- Spring Boot 4.1.1
- Spring Security 7.1 (autenticação HTTP Basic)
- Spring for GraphQL
- Spring AMQP / RabbitMQ
- Spring Data JPA + H2 (banco em memória)
- Lombok
- Docker / Docker Compose

## Perfis de usuário e permissões

| Perfil | Visualizar consultas | Criar consulta | Editar consulta |
|---|---|---|---|
| **Médico** | Todas | Sim | Sim |
| **Enfermeiro** | Todas | Sim | Sim |
| **Paciente** | Apenas as próprias | Não | Não |

> **Nota de interpretação:** o PDF do desafio tem uma pequena ambiguidade
> entre a seção "Níveis de Acesso" (que sugere só o enfermeiro registrar
> consultas) e a seção de GraphQL/Agendamento (que diz "médicos e
> enfermeiros poderão registrar e modificar consultas"). Optamos pela leitura
> mais completa: ambos os perfis podem criar e editar.

### Usuários de teste (criados automaticamente ao subir a aplicação)

| Login | Senha | Perfil |
|---|---|---|
| `medico1` | `123456` | MEDICO |
| `enfermeiro1` | `123456` | ENFERMEIRO |
| `paciente1` | `123456` | PACIENTE |

Autenticação via **HTTP Basic Auth** — envie o header `Authorization: Basic
<login:senha em base64>` em cada requisição (o Postman faz isso
automaticamente pela aba "Authorization" de cada request).

## Endpoints REST (agendamento-service, porta 8080)

| Método | Rota | Quem pode acessar | Descrição |
|---|---|---|---|
| `GET` | `/consultas` | Médico, Enfermeiro, Paciente | Lista consultas (paciente vê só as próprias) |
| `POST` | `/consultas` | Médico, Enfermeiro | Cria uma nova consulta |
| `PATCH` | `/consultas/{id}` | Médico, Enfermeiro | Atualiza parcialmente (dataHora, observações e/ou status) |

**Exemplo de body do `POST /consultas`:**
```json
{
  "pacienteId": 1,
  "profissionalId": 1,
  "dataHora": "2026-09-20T10:00:00",
  "observacoes": "Consulta de rotina"
}
```

**Exemplo de body do `PATCH /consultas/{id}`** (todos os campos são opcionais):
```json
{
  "status": "CANCELADA"
}
```

## GraphQL (agendamento-service, porta 8080)

Endpoint: `POST /graphql` · Interface interativa: `/graphiql`

```graphql
query {
  consultasPorPaciente(pacienteId: 1) {
    id
    pacienteNome
    profissionalNome
    dataHora
    status
    observacoes
  }
}

query {
  consultasFuturas(pacienteId: 1) {
    id
    dataHora
    status
  }
}
```

Um paciente autenticado só pode consultar o próprio `pacienteId` — qualquer
tentativa de consultar outro paciente retorna erro `FORBIDDEN`. Médicos e
enfermeiros podem consultar o histórico de qualquer paciente.

## Console H2

`http://localhost:8080/h2-console`

| Campo | Valor |
|---|---|
| JDBC URL | `jdbc:h2:mem:caresync` |
| Usuário | `sa` |
| Senha | *(em branco)* |

## Como executar

### Opção 1 — Para avaliação (recomendado para o professor)

Sobe os dois serviços já compilados e o RabbitMQ com um único comando,
sem necessidade de instalar Java, Maven ou IDE:

```bash
docker-compose up --build
```

- `agendamento-service` fica disponível em `http://localhost:8080`
- `notificacao-service` fica disponível em `http://localhost:8081`
- RabbitMQ Management UI em `http://localhost:15672` (usuário/senha: `guest`/`guest`)

### Opção 2 — Ambiente de desenvolvimento (dev container)

Requer VS Code com a extensão **Dev Containers** e Docker instalado.

1. Abra a pasta do projeto no VS Code.
2. `Ctrl+Shift+P` → `Dev Containers: Reopen in Container`.
3. O ambiente já sobe com Java 21, Maven e RabbitMQ prontos.
4. Em terminais separados dentro do container, rode:
   ```bash
   cd agendamento-service && mvn spring-boot:run
   ```
   ```bash
   cd notificacao-service && mvn spring-boot:run
   ```

## Testando o fluxo assíncrono

1. Autentique-se como `medico1` ou `enfermeiro1` e crie uma consulta
   (`POST /consultas`).
2. Observe o console/log do `notificacao-service` — deve aparecer uma linha
   como:
   ```
   [LEMBRETE] Consulta #1 agendada! Paciente: Carlos Lima | Profissional: Dra. Ana Souza | Data/Hora: 2026-09-20T10:00
   ```

## Collection do Postman

Disponível em `docs/CareSync.postman_collection.json`, com requisições
prontas para os 3 perfis de usuário, incluindo os cenários de sucesso e de
bloqueio por permissão (403 Forbidden).