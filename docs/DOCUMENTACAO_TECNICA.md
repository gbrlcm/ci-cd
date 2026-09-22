# Cidades ESG Inteligentes — Implementação DevOps

**Documentação técnica de entrega** — InovaGAB_API

---

## 1. Identificação

| Campo | Valor |
|---|---|
| Projeto | Cidades ESG Inteligentes (InovaGAB API) |
| Repositório | https://github.com/KaioBraga-b/InovaGAB_API |
| Disciplina | DevOps (atividade acadêmica) |
| Aluno/Equipe | _[preencher nomes]_ |
| Data | _[preencher]_ |

---

## 2. Visão geral

A **InovaGAB** é uma API REST em Java Spring Boot para gestão da inovação em cidades ESG inteligentes: grupos de usuários, projetos, estratégias, ideias, transações financeiras, dashboard e notificações, com autenticação JWT e isolamento de dados por grupo.

Esta atividade adaptou o projeto existente para **simular um ambiente de produção real** com práticas DevOps: containerização, orquestração, banco containerizado, CI/CD, registry de imagens, dois ambientes isolados (staging e produção) e deploy automatizado. O código de negócio original foi preservado.

---

## 3. Arquitetura

```
GitHub (main / develop)
      │  push
      ▼
GitHub Actions
      │  CI: build + testes → artefato .jar
      │  CD: build da imagem → GHCR → deploy
      ▼
Build (Maven / Maven Wrapper)
      ▼
Testes automatizados (9 testes — MongoDB containerizado)
      ▼
Docker Build (multi-stage: JDK 24 → JRE 24, usuário não-root)
      ▼
GHCR — ghcr.io/<usuario>/<repo>:<sha> + :latest
      ▼
      ├──> STAGING   (branch develop — porta 8081, banco cidades_esg_staging)
      │
      └──> PRODUÇÃO  (branch main — porta 8080, banco cidades_esg)
```

**Tecnologias:** Java 24, Spring Boot 4.1, MongoDB 7.0, Maven, Docker, Docker Compose, GitHub Actions, GHCR, SSH.

---

## 4. Pipeline

### 4.1 CI — `.github/workflows/ci.yml`

| Passo | Ação |
|---|---|
| 1 | Checkout do código |
| 2 | Instalação Java 24 (Temurin) |
| 3 | Build + testes (`./mvnw test`) com MongoDB em service container |
| 4 | Empacotamento e publicação do `.jar` como artefato |
| 5 | Build da imagem Docker |
| 6 | Publicação no GHCR (`:<sha>` e `:latest`) |

*Triggens:* todo push e pull request.

### 4.2 CD — `.github/workflows/cd.yml`

| Passo | Ação |
|---|---|
| 1 | Determinação do ambiente pela branch (`develop`→staging, `main`→produção) |
| 2 | Build + publicação da imagem no GHCR |
| 3 | Configuração do agente SSH |
| 4 | `scripts/deploy-remote.sh`: envio do bundle ao servidor |
| 5 | `scripts/remote-apply.sh`: `docker compose pull && up -d` (banco preservado) |
| 6 | Validação do healthcheck `/actuator/health` e resultado no pipeline |

Com `SERVER_HOST`/`SERVER_USER`/`SERVER_SSH_KEY` cadastrados como secrets, o CD executa o deploy real; sem eles, o CD publica a imagem e orienta explicitamente o cadastro.

---

## 5. Docker

### 5.1 Dockerfile (multi-stage)

- **builder** (`eclipse-temurin:24-jdk`): resolve dependências (cacheia camadas), compila com `./mvnw` e gera o `.jar`;
- **runtime** (`eclipse-temurin:24-jre`): imagem mínima, inclui `curl` (healthcheck), roda com usuário não-root (`app`);
- `HEALTHCHECK` consultando `/actuator/health`.

### 5.2 Imagem criada

```bash
docker build -t cidades-esg:latest .
docker images | grep cidades-esg    # evidência
```

### 5.3 Containers, volumes e networks

| O que | Nome (compose raiz) |
|---|---|
| Container app | `cidades-esg-app` |
| Container banco | `cidades-esg-mongodb` |
| Volume (persistência) | `cidades-esg-mongo-data` |
| Network (bridge) | `cidades-esg-network` |

---

## 6. Docker Compose

Serviços orquestrados no `docker-compose.yml` raiz:

- `app`: imagem da aplicação (build local ou imagem pronta), porta `8080`, healthcheck via actuator;
- `mongodb`: `mongo:7.0`, porta `27017`, volume nomeado, healthcheck via `mongosh`;

A app espera o banco **saudável** (`depends_on.condition: service_healthy`) e conecta pelo **nome do serviço** (`mongodb`), não por `localhost`. Volumes, rede, portas e credenciais são parametrizados por variáveis de ambiente.

```bash
docker compose up -d --build    # sobe tudo
docker compose down             # derruba
```

---

## 7. Staging

| Característica | Valor |
|---|---|
| Gatilho | push em `develop` |
| Porta | 8081 |
| Banco | `cidades_esg_staging` |
| Diretório no servidor | `/opt/cidades-esg/staging` |
| Arquivos | `deploy/staging/docker-compose.yml` + `.env.example` |

Demonstração local:

```bash
export IMAGE=cidades-esg:test
docker compose -f deploy/staging/docker-compose.yml --env-file deploy/staging/.env up -d
curl http://localhost:8081/actuator/health   # → {"status":"UP"}
```

**Evidência real (local):** _inserir screenshot do `docker compose ps` (staging healthy) e do healthcheck._

---

## 8. Produção

| Característica | Valor |
|---|---|
| Gatilho | push em `main` |
| Porta | 8080 |
| Banco | `cidades_esg` |
| Diretório no servidor | `/opt/cidades-esg/production` |
| Arquivos | `deploy/production/docker-compose.yml` + `.env.example` |

Demonstração local:

```bash
export IMAGE=cidades-esg:test
docker compose -f deploy/production/docker-compose.yml --env-file deploy/production/.env up -d
curl http://localhost:8080/actuator/health   # → {"status":"UP"}
```

**Evidência real (local):** _inserir screenshot da porta 8080 saudável e isolamento (redes/volumes separados)._

> Staging e produção foram validados simultaneamente nesta máquina com redes (`staging_cidades-esg-network` / `production_cidades-esg-network`), volumes e bancos separados — incluindo teste de registro do mesmo usuário em ambos os bancos com sucesso independente.

---

## 9. Pipeline executando

_Blocos para evidências reais — substituir pelo screenshot de cada etapa verde no GitHub._

- [ ] Build + testes (job `test`)
- [ ] Build + publicação da imagem (job `build-image`)
- [ ] Deploy staging (workflow CD, branch `develop`)
- [ ] Deploy produção (workflow CD, branch `main`)
- [ ] GHCR: lista de packages/imagens

---

## 10. Desafios encontrados

Todos os desafios abaixo ocorreram **de fato** durante a implementação:

1. **Compilação quebrada por BOM (`\ufeff`)** — 3 arquivos Java (`InovacaoController.java`, `InovacaoService.java`, `IdeiaRepository.java`) continham Byte Order Mark no início, fazendo o javac falhar com `illegal character: '\ufeff'`.
2. **`mongo:latest` incompatível com o kernel Docker da máquina** — erro `MongoDB cannot start: Linux kernel versions 6.19 and newer has a known incompatibility` (bug [SERVER-121912](https://jira.mongodb.org/browse/SERVER-121912)); o container saía imediatamente.
3. **Credenciais fixas no código** — `application.properties` e `application.yml` traziam `root/secret` e `JWT_SECRET` hardcoded.
4. **Healthcheck da aplicação bloqueado** — `/actuator/health` retornava `403` porque o Spring Security exigia autenticação.
5. **Interpolação `${IMAGE:?...}` quebrando o YAML do Compose** — a mensagem `Ex.: ghcr.io/...` continha `:` interpretado como mapeamento YAML (`mapping values are not allowed in this context`).
6. **Testes de integração exigem banco no ar** — `./mvnw test` falha sem MongoDB em `localhost:27017` (comportamento previsto, resolvido na documentação e no CI).

---

## 11. Soluções

1. **BOM:** removido com `perl -pi -e 's/^\xef\xbb\xbf//'` nos 3 arquivos; build voltou a compilar.
2. **MongoDB:** pinado `mongo:7.0` (versão compatível) em todos os ambientes (compose raiz e deploys). Validado com execução real.
3. **Credenciais:** todas movidas para variáveis de ambiente (`application.yml` com `${VAR:default}`), criado `.env.example`, perfis `local`/`staging`/`prod` e `.gitignore` para `.env`.
4. **Healthcheck:** liberado `/actuator/health/**` no `SecurityConfig` e expostos os endpoints `health,info` no Actuator → responde `200 {"status":"UP"}` e alimenta os healthchecks de Docker e do deploy.
5. **YAML:** valores de `image:` com mensagem passaram a ser **entre aspas** (`"${IMAGE:?...}"`); validado com `docker compose config`.
6. **Testes:** documentado que `mvn test` requer o banco (`docker compose up -d mongodb`); no CI, o MongoDB é iniciado automaticamente como service container.

---

## 12. Checklist de entrega

| Item | Status |
|---|---|
| Código-fonte funcional | ✔ |
| Dockerfile funcional | ✔ |
| docker-compose.yml (app + banco) | ✔ |
| Banco de dados containerizado | ✔ |
| Variáveis de ambiente / `.env.example` | ✔ |
| Volumes persistentes | ✔ |
| Redes Docker | ✔ |
| Pipeline CI/CD (GitHub Actions) | ✔ |
| Build automatizado | ✔ |
| Testes automatizados | ✔ (9 testes) |
| Deploy automatizado em staging | ☐ (local ✔ / remoto aguarda secrets) |
| Deploy automatizado em produção | ☐ (local ✔ / remoto aguarda secrets) |
| README.md completo | ✔ |
| Documentação técnica | ✔ (este documento) |
| Evidências reais da execução | em andamento |
| Checklist final da atividade | em andamento |

---

*Para gerar este documento em PDF:*

```bash
# com pandoc + LaTeX instalados
pandoc docs/DOCUMENTACAO_TECNICA.md -o docs/Documentacao_DevOps_Cidades_ESG.pdf \
  --pdf-engine=xelatex -V mainfont="Helvetica Neue"
```