# 🏃 Runner Circle — API em Spring Boot

<p align="center">
  <img src="https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 25" />
  <img src="https://img.shields.io/badge/Spring_Boot-4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot 4" />
  <img src="https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white" alt="Spring Security" />
  <img src="https://img.shields.io/badge/PostgreSQL-17-4169E1?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL 17" />
  <img src="https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white" alt="JWT" />
  <br />
  <img src="https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black" alt="Swagger" />
  <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker" />
  <img src="https://img.shields.io/badge/JUnit_5-25A162?style=for-the-badge&logo=junit5&logoColor=white" alt="JUnit 5" />
  <img src="https://img.shields.io/badge/Mockito-78A641?style=for-the-badge" alt="Mockito" />
  <img src="https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" alt="Maven" />
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-green?style=for-the-badge" alt="Licença MIT" /></a>
</p>

API REST do **Runner Circle**, uma rede social de treinos de corrida e caminhada, construída com **Java 25 e Spring Boot 4**. Ela cuida de cadastro e login com JWT, treinos com foto, curtidas, comentários, perfil e um endpoint administrativo protegido por role.

Sobe com **um comando** via Docker, tem **documentação Swagger** com botão *Authorize* para testar as rotas protegidas e uma suíte de testes que roda **sem precisar de banco**.

> Esta é uma implementação em Spring Boot do mesmo domínio do meu projeto [React-Running-circle](https://github.com/Fredsongomes/React-Running-circle) (front-end em React + API em NestJS). As rotas e os campos são diferentes (`/treinos`, campos em português), então **esta API não substitui diretamente** a API usada por aquele front-end.

---

## ✨ Funcionalidades

- **Cadastro e login com JWT**: senha com hash BCrypt, token com expiração (2 horas, ou 30 dias com `lembrarMe`)
- **Roles `USER` e `ADMIN`**: a role vai no token e protege `GET /users` com `@PreAuthorize`
- **Treinos** com foto opcional (`multipart/form-data`, jpg/png/webp até 5 MB)
- **Listagem paginada** do mais recente para o mais antigo, com busca na descrição
- **Regras de autoria**: só quem criou o treino pode editá-lo ou removê-lo; um comentário pode ser removido pela pessoa que o escreveu ou pela autora do treino
- **Curtidas e comentários** feitos sempre em nome da pessoa do token
- **Perfil** (`PUT /users/me`): atualiza só a pessoa autenticada, sem receber id
- **Erros padronizados** em JSON, inclusive nos `401` e `403` do Spring Security
- **Swagger / OpenAPI** com esquema Bearer
- **Health check** com Spring Boot Actuator

---

## 🧱 Stack e arquitetura

| Camada | Tecnologia |
| --- | --- |
| Linguagem | Java 25 (DTOs com `record`) |
| Framework | Spring Boot 4 (Web MVC, Data JPA, Validation, Security, Actuator) |
| Banco | PostgreSQL 17 (H2 em memória nos testes) |
| Auth | Spring Security + JWT ([jjwt](https://github.com/jwtk/jjwt)) + BCrypt |
| Docs | springdoc-openapi (Swagger UI) |
| Testes | JUnit 5, Mockito, MockMvc, AssertJ |
| Infra | Docker + Docker Compose, GitHub Actions (CI) |

**Arquitetura em camadas**: `controller` → `service` → `repository`, com `dto` e `mapper` separando o que entra e sai da API das entidades JPA (`model`). A autenticação fica em `security` (filtro JWT e respostas de 401/403) e `config` (Spring Security, OpenAPI e arquivos estáticos).

### Decisões de design

- **Erros sempre no mesmo formato.** Toda falha, de validação, de regra de negócio ou de autenticação, responde com `{ timestamp, status, mensagem, caminho }`. Erros inesperados são logados no servidor e chegam ao cliente só como `"erro interno no servidor"`, sem stack trace.
- **JWT stateless.** Sem sessão nem cookie (por isso o CSRF fica desligado). O token guarda o **id** da pessoa (`sub`) e a **role**, que vira a authority `ROLE_USER`/`ROLE_ADMIN`.
- **A pessoa autenticada vem sempre do token.** Criar treino, comentar, curtir e editar o perfil nunca recebem o id de quem faz a ação pela requisição.
- **401 e 403 diferentes.** Sem token ou com token inválido: `401`. Autenticado, mas sem permissão (role ou autoria): `403`.
- **Segredos fora do código.** O `JWT_SECRET` vem de variável de ambiente, e a aplicação **não sobe** sem ele (ou com menos de 32 bytes). A senha nunca aparece em resposta nenhuma.
- **Perfis `dev`, `prod` e `test`.** Em `dev` o Hibernate cria e atualiza as tabelas sozinho; em `prod` ele só **valida** o schema (`ddl-auto=validate`) e nunca altera o banco; em `test` tudo roda em H2 em memória.
- **Actuator mínimo.** Só o `/actuator/health` é exposto, e sem detalhes internos.

---

## 🚀 Como rodar

### Opção 1: Docker (recomendado)

Pré-requisito: [Docker](https://www.docker.com/) instalado.

```bash
git clone https://github.com/Fredsongomes/runner-circle-api.git
cd runner-circle-api
cp .env.example .env    # opcional: defina JWT_SECRET e as portas
docker compose up --build
```

O build da aplicação acontece dentro do container, então não é preciso ter Java instalado.

- API: <http://localhost:8080>
- Swagger: <http://localhost:8080/swagger-ui.html>
- Health: <http://localhost:8080/actuator/health>

Para parar mantendo os dados, use `docker compose down`. Para apagar o banco e as imagens enviadas também, use `docker compose down -v`.

> Se a porta 5432 já estiver em uso (por um Postgres instalado, por exemplo), defina `DB_PORT=5433` no `.env`.

### Opção 2: local

Pré-requisitos: **Java 25** e um **PostgreSQL** com um banco chamado `runnercircle`. Se preferir, suba só o banco pelo Docker com `docker compose up -d postgres`.

Defina as variáveis obrigatórias e rode a API:

```bash
# Linux/macOS
export JWT_SECRET=$(openssl rand -base64 48)
export DB_PASSWORD=postgres
./mvnw spring-boot:run

# Windows (PowerShell)
$env:JWT_SECRET="um-segredo-com-pelo-menos-32-bytes-0123456789"
$env:DB_PASSWORD="postgres"
./mvnw spring-boot:run
```

Sem `SPRING_PROFILES_ACTIVE`, a API sobe no perfil `dev` e conecta em `localhost:5432` com o usuário `postgres`.

### Variáveis de ambiente

Veja também o [`.env.example`](./.env.example).

| Variável | Descrição | Padrão |
| --- | --- | --- |
| `JWT_SECRET` | Segredo de assinatura do JWT (mínimo 32 bytes) | **obrigatória** (no Docker há um padrão só para uso local) |
| `DB_PASSWORD` | Senha do Postgres | **obrigatória** |
| `DB_URL` | URL JDBC do banco | `jdbc:postgresql://localhost:5432/runnercircle` em `dev` |
| `DB_USER` | Usuário do banco | `postgres` em `dev` |
| `SERVER_PORT` | Porta da API | `8080` |
| `UPLOAD_DIR` | Pasta das imagens enviadas | `uploads` |
| `SPRING_PROFILES_ACTIVE` | Perfil (`dev` ou `prod`) | `dev` |
| `DB_PORT` / `API_PORT` | Portas no host, só no Docker Compose | `5432` / `8080` |

> **Perfil `prod`:** `DB_URL`, `DB_USER` e `DB_PASSWORD` não têm valor padrão, e o Hibernate só valida o schema. Como o projeto ainda não usa migrations (Flyway), as tabelas precisam existir antes do primeiro deploy.

### Criando uma pessoa ADMIN

O cadastro sempre cria a role `USER`, e de propósito não existe endpoint para promover alguém. Para testar o `GET /users`, cadastre uma pessoa e promova direto no banco:

```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'admin@exemplo.com';
```

Depois **faça login de novo**: a role é gravada no token no momento do login.

---

## 📚 Endpoints

A documentação completa e testável está no **Swagger** (`/swagger-ui.html`). Faça login, copie o `token` da resposta (sem as aspas) e cole no botão **Authorize**. Resumo:

| Método | Rota | Auth | Descrição |
| --- | --- | --- | --- |
| `POST` | `/auth/register` | público | Cadastro (role `USER`) |
| `POST` | `/auth/login` | público | Login, devolve `{ token, role, usuario }` |
| `GET` | `/treinos` | público | Feed paginado: `?busca&page&size` |
| `POST` | `/treinos` | 🔒 | Cria um treino (multipart: parte `dados` em JSON + `imagem` opcional) |
| `GET` | `/treinos/{id}` | 🔒 | Busca um treino |
| `PUT` | `/treinos/{id}` | 🔒 autora | Edita o treino |
| `DELETE` | `/treinos/{id}` | 🔒 autora | Remove o treino |
| `POST` | `/treinos/{id}/curtir` | 🔒 | Curte o treino |
| `DELETE` | `/treinos/{id}/curtir` | 🔒 | Remove a curtida |
| `GET` | `/treinos/{id}/comentarios` | 🔒 | Lista os comentários |
| `POST` | `/treinos/{id}/comentarios` | 🔒 | Comenta |
| `DELETE` | `/treinos/{id}/comentarios/{comentarioId}` | 🔒 autora do comentário ou do treino | Remove o comentário |
| `PUT` | `/users/me` | 🔒 | Edita o próprio perfil (`username`, `nome`, `bio`) |
| `GET` | `/users/{id}/treinos` | público | Treinos de uma pessoa |
| `GET` | `/users` | 🔒 ADMIN | Lista todas as pessoas usuárias |
| `GET` | `/actuator/health` | público | Health check |

### Exemplo

```jsonc
// POST /auth/login
{ "email": "ana@exemplo.com", "senha": "senha12345", "lembrarMe": false }

// 200 OK
{
  "token": "eyJhbGciOiJIUzM4NCJ9...",
  "role": "USER",
  "usuario": { "id": 1, "nome": "Ana", "username": "ana", "email": "ana@exemplo.com", "bio": null }
}
```

### Formato de erro

```jsonc
// GET /treinos/9999 (com token)
// 404 Not Found
{
  "timestamp": "2026-10-05T14:32:10.123",
  "status": 404,
  "mensagem": "treino 9999 não encontrado",
  "caminho": "/treinos/9999"
}
```

---

## 🧪 Testes

```bash
./mvnw test
```

Os testes usam o perfil `test`, com **H2 em memória**, então não precisam de PostgreSQL nem de variáveis de ambiente. A mesma suíte roda no **GitHub Actions** a cada push.

| Tipo | Classe | O que cobre |
| --- | --- | --- |
| Unitário (Mockito) | `TreinoServiceTest` | busca, treino inexistente e regra de autoria na edição |
| Integração | `TreinoIntegrationTest` | criação de treino persistida no banco |
| Web (MockMvc) | `TreinoControllerTest` | `201`, `400`, `401`, `403` e `404` com JWT de verdade (cadastro + login) |

---

## 📁 Estrutura de pastas

```
runner-circle-api/
├── src/main/java/br/com/alura/runnercircleapi/
│   ├── config/        # Spring Security, OpenAPI (Swagger) e arquivos estáticos
│   ├── controller/    # Endpoints REST
│   ├── dto/           # Records de entrada e saída da API
│   ├── exception/     # Exceções de negócio e tratamento global de erros
│   ├── mapper/        # Conversão entre entidades e DTOs
│   ├── model/         # Entidades JPA e enums
│   ├── repository/    # Acesso a dados (Spring Data JPA)
│   ├── security/      # Filtro JWT e respostas de 401/403
│   └── service/       # Regras de negócio
├── src/main/resources/
│   ├── application.properties        # configuração comum
│   ├── application-dev.properties    # desenvolvimento (padrão)
│   └── application-prod.properties   # produção
├── src/test/          # Testes (perfil test com H2)
├── .github/workflows/ # CI
├── .env.example
├── docker-compose.yml
├── Dockerfile
└── pom.xml
```

---

## 📚 Contexto

O projeto nasceu no curso **"Spring Boot: construindo uma API profissional (Runner Circle)"** da [Alura](https://www.alura.com.br/), que forneceu o projeto de partida: um CRUD de treinos num pacote só. A partir dele, evoluí a API para a arquitetura em camadas e implementei o restante: autenticação com JWT e roles, regras de autoria, tratamento padronizado de erros, perfis de ambiente, testes, Docker e CI.

---

## 📄 Licença

[MIT](./LICENSE)

## 👤 Autor

Feito por **Fredson Gomes**.

[![LinkedIn](https://img.shields.io/badge/LinkedIn-0A66C2?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/fredson-gomes-a8082a338/)
[![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/Fredsongomes)
