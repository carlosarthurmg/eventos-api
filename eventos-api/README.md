# 🎟️ API de Gestão de Eventos

API REST desenvolvida com **Spring Boot** para gerenciar **eventos**, **participantes** e **inscrições**, aplicando organização em camadas, DTOs, validações, tratamento centralizado de erros, filtros, paginação, regras de negócio e documentação com Swagger.

## 📌 Descrição e objetivo

O sistema permite cadastrar eventos (palestras, workshops, shows etc.), cadastrar participantes e inscrevê-los nos eventos, controlando vagas, duplicidade, status do evento e calculando automaticamente o valor do ingresso (estudantes pagam 50%).

### Modelo de dados

```
Evento (1) ────< (N) Inscricao (N) >──── (1) Participante
```

- **Evento**: nome, descrição, local, data início/fim, capacidade, preço, categoria, status (`ABERTO`, `ENCERRADO`, `CANCELADO`)
- **Participante**: nome, e-mail (único), telefone, estudante
- **Inscricao**: evento, participante, data da inscrição, valor pago, status (`ATIVA`, `CANCELADA`)

Relacionamentos mapeados com `@OneToMany` / `@ManyToOne`.

## 🛠️ Tecnologias

- Java 17
- Spring Boot 3.5 (Web, Data JPA, Validation)
- PostgreSQL
- Springdoc OpenAPI (Swagger UI)
- Lombok
- Maven

## 🧱 Organização em camadas

```
br.com.eventos
├── controller   # Endpoints REST (ResponseEntity + status HTTP)
├── service      # Regras de negócio
├── repository   # Spring Data JPA (+ Specifications para filtros)
├── model        # Entidades JPA e enums
├── dto          # DTOs de entrada/saída (records + Bean Validation)
├── mapper       # Conversão Entidade <-> DTO
├── exception    # Exceções de negócio + @RestControllerAdvice
└── config       # Configuração do OpenAPI
```

## ⚙️ Como configurar e executar

### Pré-requisitos
- JDK 17+
- Maven 3.9+ (ou o Maven da sua IDE)
- PostgreSQL + pgAdmin 4

### 1. Banco de dados
No **pgAdmin 4**: clique com o botão direito em *Databases → Create → Database…* e crie o banco:

```
eventos_db
```

Ou via SQL: `CREATE DATABASE eventos_db;`

As tabelas são criadas automaticamente pelo Hibernate (`ddl-auto=update`).

### 2. Credenciais
Edite `src/main/resources/application.properties` com seu usuário e senha do PostgreSQL, ou use variáveis de ambiente:

```
DB_URL=jdbc:postgresql://localhost:5432/eventos_db
DB_USER=postgres
DB_PASSWORD=sua_senha
```

### 3. Executar
```bash
mvn spring-boot:run
```
A API sobe em `http://localhost:8080`.

## 📖 Documentação Swagger

👉 **http://localhost:8080/swagger-ui.html**

JSON OpenAPI: http://localhost:8080/v3/api-docs

## 🔗 Endpoints

| Método | Rota | Descrição | Status |
|---|---|---|---|
| POST | `/eventos` | Cadastrar evento | 201 |
| GET | `/eventos` | Listar (filtros + paginação) | 200 |
| GET | `/eventos/{id}` | Buscar por id | 200 / 404 |
| PUT | `/eventos/{id}` | Atualizar | 200 / 404 / 409 |
| DELETE | `/eventos/{id}` | Excluir | 204 / 409 |
| PATCH | `/eventos/{id}/cancelar` | Cancelar evento | 200 / 409 |
| PATCH | `/eventos/{id}/encerrar` | Encerrar evento | 200 / 409 |
| POST | `/participantes` | Cadastrar participante | 201 / 409 |
| GET | `/participantes` | Listar (filtros) | 200 |
| GET | `/participantes/{id}` | Buscar por id | 200 / 404 |
| PUT | `/participantes/{id}` | Atualizar | 200 / 409 |
| DELETE | `/participantes/{id}` | Excluir | 204 / 409 |
| POST | `/inscricoes` | Inscrever participante | 201 / 409 |
| GET | `/inscricoes` | Listar (filtros) | 200 |
| GET | `/inscricoes/{id}` | Buscar por id | 200 / 404 |
| PATCH | `/inscricoes/{id}/cancelar` | Cancelar inscrição | 200 / 409 |
| DELETE | `/inscricoes/{id}` | Excluir (só canceladas) | 204 / 409 |

### Filtros

**Eventos** (`GET /eventos`): `nome`, `categoria`, `status`, `dataDe`, `dataAte` (yyyy-MM-dd), `precoMin`, `precoMax`, além de `page`, `size`, `sort`.
**Participantes** (`GET /participantes`): `nome`, `email`, `estudante`.
**Inscrições** (`GET /inscricoes`): `eventoId`, `participanteId`, `status`.

## 🧪 Exemplos de uso

**Criar evento**
```bash
curl -X POST http://localhost:8080/eventos -H "Content-Type: application/json" -d '{
  "nome": "Workshop de Spring Boot",
  "descricao": "Introdução prática a APIs REST",
  "local": "Auditório Central",
  "dataInicio": "2027-03-20T19:00:00",
  "dataFim": "2027-03-20T22:00:00",
  "capacidade": 2,
  "precoIngresso": 80.00,
  "categoria": "WORKSHOP"
}'
```

**Criar participante**
```bash
curl -X POST http://localhost:8080/participantes -H "Content-Type: application/json" -d '{
  "nome": "Maria Silva", "email": "maria@email.com", "telefone": "86999990000", "estudante": true
}'
```

**Inscrever** (estudante → valorPago = 40.00)
```bash
curl -X POST http://localhost:8080/inscricoes -H "Content-Type: application/json" -d '{"eventoId": 1, "participanteId": 1}'
```

**Filtrar eventos**
```bash
curl "http://localhost:8080/eventos?categoria=WORKSHOP&precoMax=100&dataDe=2027-01-01&sort=dataInicio,desc&page=0&size=5"
```

**Exemplo de erro (400)**
```json
{
  "timestamp": "2027-01-10T10:00:00",
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Dados inválidos. Verifique os campos informados.",
  "caminho": "/eventos",
  "campos": { "nome": "O nome é obrigatório" }
}
```

## 📏 Regras de negócio implementadas

1. Não permitir evento duplicado (mesmo nome e data/hora de início) → 409
2. Data de início deve ser futura e data de fim não pode ser anterior ao início → 409
3. Só é possível editar eventos com status `ABERTO` → 409
4. Capacidade não pode ser reduzida abaixo do nº de inscritos ativos → 409
5. Não excluir evento nem participante que possuam inscrições → 409
6. Cancelar evento cancela automaticamente todas as inscrições ativas
7. Inscrição só em evento `ABERTO` e que ainda não começou → 409
8. Não permitir inscrição duplicada do mesmo participante no mesmo evento → 409
9. Não permitir inscrição em evento lotado → 409
10. **Valor calculado**: estudante paga 50% do ingresso
11. E-mail de participante deve ser único → 409
12. Só é possível excluir inscrições já canceladas → 409

## ✅ Tratamento de erros

`@RestControllerAdvice` (`GlobalExceptionHandler`) padroniza as respostas: `400` (validação/JSON/parâmetro inválido), `404` (não encontrado), `409` (regra de negócio/integridade), `405`, `500`.

## 👥 Equipe

> Preencher com os nomes dos integrantes.
