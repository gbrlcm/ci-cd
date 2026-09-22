# Projeto - Cidades ESG Inteligentes

API de gestão da inovação entre grupos de uma cidade ESG inteligente — **InovaGAB**. O projeto permite gerenciar projetos, estratégias, ideias, transações financeiras, notificações e grupos de usuários, com autenticação via JWT e controle de acesso por grupo.

Este repositório entrega o projeto original adaptado para **simular um ambiente de produção real**, com práticas completas de DevOps:

- Containerização com Docker (multi-stage build);
- Orquestração com Docker Compose;
- Banco de dados containerizado (MongoDB) com persistência;
- Integração Contínua (CI) e Entrega Contínua (CD) com GitHub Actions;
- Publicação de imagem no GitHub Container Registry (GHCR);
- Dois ambientes isolados: **staging** e **produção**;
- Deploy automatizado por SSH + Docker Compose.

---

## Descrição

A **InovaGAB** é uma API REST (Java Spring Boot) voltada à gestão da inovação em cidades ESG inteligentes. Funcionalidades principais:

- Autenticação e autorização com JWT (login, registro, perfil);
- Gestão de grupos e delegação de papéis (GESTOR, LIDER, OPERADOR);
- CRUD de projetos, estratégias, ideias, transações financeiras;
- Isolamento de dados por grupo (`groupId`) — proteção contra IDOR;
- Votação e comentários em ideias;
- Dashboard com resumo (projetos ativos, no prazo, lucro, investimento);
- Notificações por papel;
- Swagger/OpenAPI em `/swagger-ui/index.html`.

---

## Arquitetura

```
                        +-------------------+
                        |    GitHub Repo    |
                        |  (main / develop) |
                        +---------+---------+
                                  | push
                                  v
                     +--------------------------+
                     |     GitHub Actions       |
                     |  CI  -> build + testes   |
                     |  CD  -> imagem + deploy  |
                     +------------+-------------+
                                  | docker build & push
                                  v
                        +---------------------+
                        | GHCR (ghcr.io)      |
                        |  <repo>:<sha>       |
                        |  <repo>:latest      |
                        +----------+----------+
                                   |
              +--------------------+--------------------+
              v                                         v
+-----------------------------+          +-----------------------------+
| STAGING  (branch develop)   |          | PRODUÇÃO   (branch main)    |
| porta 8081                  |          | porta 8080                 |
| banco  cidades_esg_staging  |          | banco  cidades_esg         |
| vol/network próprios        |          | vol/network próprios       |
| /opt/cidades-esg/staging    |          | /opt/cidades-esg/production|
+-----------------------------+          +-----------------------------+
```

**Componentes:**

| Camada | Tecnologia |
|---|---|
| Aplicação | Java 24 + Spring Boot 4.1 + Spring Security + JWT |
| Banco de dados | MongoDB 7.0 (container) |
| Build | Maven (`mvnw`) |
| Container | Docker (multi-stage) + Docker Compose |
| CI/CD | GitHub Actions (`.github/workflows/ci.yml` e `cd.yml`) |
| Registry | GitHub Container Registry (GHCR) |
| Deploy | SSH + Docker Compose (staging e produção isolados) |
| Documentação da API | springdoc-openapi (Swagger UI) |

---

## Como executar localmente

### Pré-requisitos

- JDK 24 (ou 25) + Maven 3.9+ (ou usar o wrapper `./mvnw`);
- Docker + Docker Compose funcionando.

> **Importante (macOS/Docker):** a imagem `mongo:latest` apresenta incompatibilidade com kernels Linux 6.19+ (veja [SERVER-121912](https://jira.mongodb.org/browse/SERVER-121912)). Por isso, o projeto usa **`mongo:7.0`** em todos os ambientes.

### 1. Clonar

```bash
git clone https://github.com/KaioBraga-b/InovaGAB_API.git
cd InovaGAB_API
```

### 2. Configurar variáveis de ambiente (opcional)

Preencha um `.env` a partir do exemplo:

```bash
cp .env.example .env
```

A aplicação já funciona com os valores padrão de desenvolvimento.

### 3. Subir a aplicação + banco — Docker Compose (recomendado)

```bash
docker compose up -d --build
```

Isso sobe dois serviços:

| Serviço | Container | Porta |
|---|---|---|
| Aplicação | `cidades-esg-app` | 8080 → 8080 |
| MongoDB | `cidades-esg-mongodb` | 27017 → 27017 |

Valide o healthcheck:

```bash
curl http://localhost:8080/actuator/health
# {"groups":["liveness","readiness"],"status":"UP"}
```

Acessos:

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- Health API: http://localhost:8080/actuator/health

Para derrubar:

```bash
docker compose down          # remove containers e rede
docker compose down -v       # remove também o volume do banco
```

### 4. Executar sem Docker (para desenvolvimento)

Suba apenas o banco:

```bash
docker compose up -d mongodb
```

Compile e rode a aplicação:

```bash
./mvnw spring-boot:run        # ou: mvn spring-boot:run
```

---

## Como executar os testes

Os testes de integração exigem o MongoDB acessível em `localhost:27017` (com as credenciais de desenvolvimento). Suba o banco e execute:

```bash
docker compose up -d mongodb   # garante o banco no ar
./mvnw test                    # ou: mvn test
```

Resultado esperado (medido neste projeto, 21/09/2026):

```
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

| Teste | Tipo | Cobertura |
|---|---|---|
| `InovagabApplicationTests` | Contexto | Sobe o contexto Spring |
| `GrupoServiceTest` (5) | Integração | Grupos, isolamento, IDOR, migração BCrypt |
| `JwtTokenProviderTest` (3) | Unitário | Geração/validação de tokens JWT |

> No CI, o MongoDB é iniciado automaticamente como **service container** (mesmo `mongo:7.0`), então `./mvnw test` roda sem nenhuma configuração adicional.

---

## Pipeline CI/CD

Tudo implementado com **GitHub Actions**, em dois arquivos legíveis e separados.

### `.github/workflows/ci.yml` — Integração Contínua

Dispara em **todo push e pull request**.

1. Checkout do código;
2. Instalação do **Java 24 (Temurin)**;
3. Build e **testes automatizados** (MongoDB real em service container);
4. Geração do artefato `.jar` e publicação como artifact;
5. **Build da imagem Docker** (multi-stage);
6. **Publicação no GHCR** com tags rastreáveis:
   - `ghcr.io/<usuario>/<repo>:<sha-do-commit>` (traceable)
   - `ghcr.io/<usuario>/<repo>:latest`

### `.github/workflows/cd.yml` — Entrega Contínua

Dispara apenas em push nas branches:

| Branch | Ambiente | Porta | Banco |
|---|---|---|---|
| `develop` | **staging** | 8081 | `cidades_esg_staging` |
| `main` | **produção** | 8080 | `cidades_esg` |

Para cada ambiente o CD:

1. Constrói e publica a imagem (mesma tag por SHA);
2. Configura agente SSH com a chave privada (`SERVER_SSH_KEY`);
3. Executa `scripts/deploy-remote.sh` → copia o bundle (`docker-compose.yml`, `.env.example`) para o servidor;
4. No servidor, `scripts/remote-apply.sh`: faz `docker compose pull`, `up -d` (preservando o volume do banco) e valida o **healthcheck** `/actuator/health`;
5. Informa **sucesso ou falha** no pipeline.

> **Sem secrets de servidor cadastrados**: o CD publica a imagem normalmente e registra um aviso explícito orientando a configuração (`SERVER_HOST`, `SERVER_USER`, `SERVER_SSH_KEY`). Ele **não inventa** um deploy.

---

## Containerização

### Dockerfile (multi-stage)

Estrutura:

- **Stage 1 `builder`** (`eclipse-temurin:24-jdk`): baixa dependências do Maven (cache de camadas), compila e gera o `.jar`;
- **Stage 2 `runtime`** (`eclipse-temurin:24-jre`): imagem mínima, com `curl` para o healthcheck e **usuário não-root** (`app`);
- `HEALTHCHECK` do container faz `curl` em `/actuator/health`.

```bash
docker build -t cidades-esg:latest .
docker run --rm -p 8080:8080 \
  -e MONGO_HOST=host.docker.internal \
  -e MONGO_PORT=27017 \
  -e MONGO_USERNAME=root \
  -e MONGO_PASSWORD=secret \
  -e JWT_SECRET=my-dev-secret-for-jwt-generation-which-should-be-changed-in-production \
  cidades-esg:latest
```

### docker-compose.yml (ambiente local)

- Serviços: `app` + `mongodb`;
- **Volume nomeado** `mongo_data` para persistência do banco;
- **Rede própria** `cidades-esg-network` (bridge);
- `depends_on` com `condition: service_healthy` — a app só inicia depois do banco saudável;
- A aplicação acessa o banco pelo **nome do serviço** (`mongodb`), não por `localhost`;
- Healthchecks em ambos os serviços;
- Portas e credenciais parametrizadas via variáveis de ambiente.

### Variáveis de ambiente

Todas as configurações sensíveis vêm de variáveis de ambiente (ver `.env.example`): `SERVER_PORT`, `SPRING_PROFILES_ACTIVE`, `JWT_SECRET`, `MONGO_HOST`, `MONGO_PORT`, `MONGO_DATABASE`, `MONGO_USERNAME`, `MONGO_PASSWORD`, `MONGO_AUTH_DB`.

Perfis Spring usados:

| Perfil | Uso |
|---|---|
| `local` | Desenvolvimento local (host `localhost`) |
| `staging` | Ambiente de staging (host `mongodb`) |
| `prod` | Ambiente de produção (host `mongodb`) |

---

## Deploy

### Staging

- **Gatilho:** push na branch `develop`;
- **Porta:** 8081;
- **Banco/volume/network:** `cidades_esg_staging` / `staging_mongo_data` / `staging_cidades-esg-network`;
- **Bundle no servidor:** `/opt/cidades-esg/staging/`.

Demonstração local (uma VPS na própria máquina):

```bash
cp deploy/staging/.env.example deploy/staging/.env   # edite se quiser
export IMAGE=cidades-esg:local                        # imagem da sua escolha
docker compose -f deploy/staging/docker-compose.yml --env-file deploy/staging/.env up -d
curl http://localhost:8081/actuator/health
```

### Produção

- **Gatilho:** push na branch `main`;
- **Porta:** 8080;
- **Banco/volume/network:** `cidades_esg` / `production_mongo_data` / `production_cidades-esg-network`;
- **Bundle no servidor:** `/opt/cidades-esg/production/`.

Demonstração local:

```bash
cp deploy/production/.env.example deploy/production/.env
export IMAGE=cidades-esg:local
docker compose -f deploy/production/docker-compose.yml --env-file deploy/production/.env up -d
curl http://localhost:8080/actuator/health
```

### Deploy real em VPS

Em produção real, os diretórios `/opt/cidades-esg/staging` (ou `production`) recebem os arquivos de `deploy/<ambiente>/` e devem ter um `.env` com credenciais reais. O pipeline SSH faz o resto (pull, up, healthcheck). Também é possível executar manualmente:

```bash
# no servidor, dentro de /opt/cidades-esg/staging
IMAGE=ghcr.io/seuusuario/cidades-esg:latest bash remote-apply.sh staging
```

> Ambos os ambientes **não compartilham** banco, volume, rede nem porta — isolamento total.

---

## Tecnologias utilizadas

- **Java 24** + **Spring Boot 4.1.1** (Web, Security, Data MongoDB, Validation, Actuator);
- **MongoDB 7.0** (containerizado);
- **JWT** (jjwt 0.12.6) + **BCrypt**;
- **springdoc-openapi** (Swagger UI);
- **Lombok**;
- **Maven** (wrapper `mvnw`, 3.9.16);
- **Docker** (multi-stage build) e **Docker Compose**;
- **GitHub Actions** (CI/CD);
- **GitHub Container Registry (GHCR)**;
- **SSH** para deploy.

---

## Secrets necessários

Nenhum segredo real está versionado. No GitHub, cadastre em *Settings → Secrets and variables → Actions*:

| Secret | Uso |
|---|---|
| `GITHUB_TOKEN` *(automático)* | Autenticação no GHCR |
| `SERVER_HOST` | IP/hostname da VPS (ex.: `192.168.0.10`) |
| `SERVER_USER` | Usuário SSH com acesso ao Docker (ex.: `root`) |
| `SERVER_PORT` | *(opcional)* Porta SSH (padrão 22) |
| `SERVER_SSH_KEY` | Chave privada SSH do servidor (formato OpenSSH) |

Localmente, os valores sensíveis da aplicação ficam no `.env` (não versionado). Troque ao menos `JWT_SECRET`, `MONGO_PASSWORD` em qualquer ambiente fora do desenvolvimento.

---

## Prints do funcionamento

> Os blocos abaixo devem ser preenchidos com **evidências reais** (screenshots da sua execução). Nenhum print falso deve ser adicionado. As seções 7 e 8 já listam comandos que geram as saídas reais para captura.

### 1. Aplicação local

_inserir print de: `curl http://localhost:8080/actuator/health` + Swagger UI aberto_

### 2. Docker / containers / volumes / networks

_inserir print de: `docker compose ps`, `docker volume ls`, `docker network ls`_

### 3. Imagem Docker

_inserir print de: `docker images | grep cidades-esg`_

### 4. Build e testes no CI

_inserir print do workflow CI no GitHub (jobs `test` e `build-image` verdes)_

### 5. Publicação da imagem (GHCR)

_inserir print dos packages do repositório em ghcr.io_

### 6. Deploy staging

_inserir print do workflow CD para `develop` + resultado do healthcheck na porta 8081_

### 7. Deploy produção

_inserir print do workflow CD para `main` + resultado do healthcheck na porta 8080_

### 8. Evidências geradas localmente (exemplo real)

Saídas reais capturadas durante o desenvolvimento:

```text
$ docker compose up -d --build
 Container cidades-esg-mongodb Healthy
 Container cidades-esg-app Started

$ curl -s http://localhost:8080/actuator/health
{"groups":["liveness","readiness"],"status":"UP"}

$ ./mvnw test
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS

$ docker compose -f deploy/staging/docker-compose.yml ... up -d     # porta 8081
$ docker compose -f deploy/production/docker-compose.yml ... up -d  # porta 8080
curl -s http://localhost:8081/actuator/health  -> UP
curl -s http://localhost:8080/actuator/health  -> UP
```

---

## Checklist de Entrega

| Item | Status |
|---|---|
| Projeto compactado em `.ZIP` com estrutura organizada | ☐ |
| Dockerfile funcional | ✔ (validado: `docker build` + container iniciando) |
| docker-compose.yml | ✔ (validado: app + MongoDB, healthchecks, volume, rede) |
| Pipeline com build, testes e deploy (GitHub Actions) | ✔ (arquivos criados; execução real depende de push) |
| README.md com instruções e prints | ☐ (preencher seção de prints com evidências) |
| Documentação técnica com evidências | ☐ (ver `docs/`) |
| Deploy realizado em staging | ☐ (local ✔ via Compose; remoto pendente de secrets) |
| Deploy realizado em produção | ☐ (local ✔ via Compose; remoto pendente de secrets) |

---

## Estrutura final do projeto

```
InovaGAB_API/
├── Dockerfile
├── docker-compose.yml
├── .env.example
├── .dockerignore
├── .github/workflows/
│   ├── ci.yml
│   └── cd.yml
├── deploy/
│   ├── staging/        (docker-compose.yml + .env.example)
│   └── production/     (docker-compose.yml + .env.example)
├── scripts/
│   ├── deploy-remote.sh
│   └── remote-apply.sh
├── docs/               (documentação técnica)
├── src/
├── pom.xml
└── mvnw
```