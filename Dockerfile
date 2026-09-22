# ============================================================
# Dockerfile — Cidades ESG Inteligentes (InovaGAB_API)
# Multi-stage build:
#   Stage 1 (builder): compila com JDK 24 + Maven wrapper
#   Stage 2 (runtime): imagem mínima com JRE 24 e usuário não-root
# ============================================================

# ---------- Stage 1: BUILD ----------
FROM eclipse-temurin:24-jdk AS builder
WORKDIR /app

# Copia apenas o necessário para baixar dependências primeiro
# (aproveita cache de camadas Docker em builds repetidos)
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

# Copia o código-fonte e gera o artefato (testes rodam no CI)
COPY src src
RUN ./mvnw -B -DskipTests package

# ---------- Stage 2: RUNTIME ----------
FROM eclipse-temurin:24-jre AS runtime

# Usuário não-root (boas práticas de segurança)
RUN groupadd -r app && useradd -r -g app -d /app app

WORKDIR /app

# curl para healthcheck dentro do container
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/target/*.jar app.jar

# Porta padrão da aplicação (sobrescrevível via SERVER_PORT)
EXPOSE 8080

# Variáveis de ambiente lidas pela aplicação.
# Apenas valores não sensíveis ficam fixos na imagem.
# Credenciais (MONGO_PASSWORD, JWT_SECRET, ...) DEVEM ser injetadas
# em runtime via compose/--env-file (ver .env.example).
ENV SERVER_PORT=8080 \
    SPRING_PROFILES_ACTIVE=local \
    MONGO_HOST=localhost \
    MONGO_PORT=27017 \
    MONGO_DATABASE=inovagab_db \
    MONGO_USERNAME=root

# MONGO_PASSWORD, MONGO_AUTH_DB e JWT_SECRET:
# injetados em runtime (compose/.env/--env-file)

USER app

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD curl -fsS http://localhost:${SERVER_PORT:-8080}/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]