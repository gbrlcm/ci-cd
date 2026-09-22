#!/usr/bin/env bash
# ============================================================
# remote-apply.sh — executa NO SERVIDOR (VPS / host remoto)
# Garante .env, baixa a nova imagem do GHCR, atualiza os
# containers (preservando o volume do banco), valida o health.
#
# Uso (no servidor, já dentro do diretório do ambiente):
#   IMAGE=ghcr.io/usuario/repo:SHA bash remote-apply.sh <staging|production>
# ============================================================
set -euo pipefail

ENV_NAME="${1:?Uso: remote-apply.sh <staging|production>}"
IMAGE="${IMAGE:?Defina IMAGE}"

# Detecta se docker exige sudo
DOCKER="docker"
if ! docker info >/dev/null 2>&1; then
  if sudo -n docker info >/dev/null 2>&1; then
    DOCKER="sudo docker"
  else
    echo "ERRO: usuário sem permissão ao Docker. Use um usuário com acesso" >&2
    echo "ou adicione o usuário ao grupo docker (ou rode como root)." >&2
    exit 1
  fi
fi

echo "==> [remote-apply] Ambiente: ${ENV_NAME} | Imagem: ${IMAGE}"

# 1. Garante .env (mantém credenciais do ambiente; não sobrescreve)
if [ ! -f .env ]; then
  cp .env.example .env
  chmod 600 .env
  echo "!! .env criado a partir de .env.example — EDITE as credenciais agora." >&2
  echo "   Dica: nano ${PWD}/.env  e depois rode este script novamente." >&2
  echo "   (para demonstração, valores padrão podem ser usados sem edição)" >&2
fi

PORT="$(grep -E '^APP_PORT=' .env | tail -1 | cut -d= -f2-)"
PORT="${PORT:-8080}"

# 2. Obtém a nova imagem (registro GHCR)
export IMAGE
${DOCKER} compose pull

# 3. Atualiza os containers (banco persiste no volume mongo_data)
${DOCKER} compose up -d

# 4. Healthcheck: espera o /actuator/health da aplicação
echo "==> [remote-apply] Aguardando healthcheck em localhost:${PORT}/actuator/health"
sleep 25
for i in $(seq 1 12); do
  if curl -fsS "http://localhost:${PORT}/actuator/health" | grep -q '"status":"UP"'; then
    echo "==> [remote-apply] DEPLOY OK em ${ENV_NAME} (health UP)"
    echo "==> [remote-apply] http://localhost:${PORT}"
    exit 0
  fi
  echo "   ...ainda não UP (${i}/12), aguardando 5s"
  sleep 5
done

echo "==> [remote-apply] DEPLOY FALHOU — /actuator/health não ficou UP" >&2
${DOCKER} compose ps 2>&1 || true
${DOCKER} compose logs --tail 100 2>&1 || true
exit 1