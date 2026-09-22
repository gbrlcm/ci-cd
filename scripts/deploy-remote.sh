#!/usr/bin/env bash
# ============================================================
# deploy-remote.sh — executa NA MÁQUINA DO CI/CD (runner)
# Copia o bundle do ambiente para o servidor e dispara a
# aplicação remota com a nova imagem GHCR.
#
# Uso:
#   SERVER_HOST=... SERVER_USER=... [SERVER_SSH_KEY=...] \
#     ./scripts/deploy-remote.sh <staging|production> <IMAGE>
#
# Variáveis exigidas (via GitHub Secrets quando no pipeline):
#   SERVER_HOST, SERVER_USER   (SERVER_PORT opcional, default 22)
#   SERVER_SSH_KEY             (caminho da chave privada, se não estiver
#                               configurada via ssh-agent)
# ============================================================
set -euo pipefail

ENV_NAME="${1:?Uso: deploy-remote.sh <staging|production> <IMAGE>}"
IMAGE="${2:?Uso: deploy-remote.sh <staging|production> <IMAGE>}"

SERVER_USER="${SERVER_USER:?Defina SERVER_USER}"
SERVER_HOST="${SERVER_HOST:?Defina SERVER_HOST}"
SSH_PORT="${SERVER_PORT:-22}"
APP_DIR="/opt/cidades-esg/${ENV_NAME}"

SSH_OPTS="-p ${SSH_PORT} -o StrictHostKeyChecking=accept-new"

# Chave privada opcional (o pipeline costuma usar ssh-agent)
if [ -n "${SERVER_SSH_KEY:-}" ]; then
  KEYFILE="$(mktemp)"
  trap 'rm -f "${KEYFILE}"' EXIT
  printf '%s\n' "${SERVER_SSH_KEY}" > "${KEYFILE}"
  chmod 600 "${KEYFILE}"
  SSH_OPTS="${SSH_OPTS} -i ${KEYFILE}"
fi

echo "==> [deploy-remote] ${ENV_NAME} -> ${SERVER_USER}@${SERVER_HOST}:${APP_DIR}"
echo "==> [deploy-remote] Imagem: ${IMAGE}"

# 1. Garante o diretório do ambiente no servidor
ssh ${SSH_OPTS} "${SERVER_USER}@${SERVER_HOST}" "mkdir -p ${APP_DIR}"

# 2. Envia bundle do ambiente + script de aplicação remota
scp ${SSH_OPTS} \
  "deploy/${ENV_NAME}/docker-compose.yml" \
  "deploy/${ENV_NAME}/.env.example" \
  "scripts/remote-apply.sh" \
  "${SERVER_USER}@${SERVER_HOST}:${APP_DIR}/"

# 3. Executa o deploy remoto (pull da imagem, up, healthcheck)
ssh ${SSH_OPTS} "${SERVER_USER}@${SERVER_HOST}" \
  "cd ${APP_DIR} && IMAGE=${IMAGE} bash remote-apply.sh ${ENV_NAME}"