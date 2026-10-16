#!/usr/bin/env bash
# Build, publish and deploy a new release of the app to the VM.
#
# Required environment:
#   DEPLOY_HOST      ssh target, e.g. user@host        (not stored in the repo)
# Optional environment:
#   DEPLOY_SSH_KEY   path to the ssh private key       (default: ssh's own default)
#   DEPLOY_ENV_FILE  env file ON THE VM with the app's runtime config
#                    (default: /root/url-shortener.env). Its contents are never
#                    read, printed or copied by this script.
#   IMAGE_REPO       default: darshit12/url-shortener
#   CONTAINER        default: competent_roentgen
#   PUBLIC_URL       if set, checked after deploy, e.g. https://just.darshit.dev
#
# Usage: DEPLOY_HOST=user@host scripts/deploy.sh [--yes]
#
# Flow: tests -> build -> push -> pull on VM -> keep the old container as
# <name>_prev -> start new -> readiness check -> automatic rollback on failure.
set -euo pipefail

: "${DEPLOY_HOST:?set DEPLOY_HOST, e.g. user@host}"
IMAGE_REPO="${IMAGE_REPO:-darshit12/url-shortener}"
CONTAINER="${CONTAINER:-competent_roentgen}"
DEPLOY_ENV_FILE="${DEPLOY_ENV_FILE:-/root/url-shortener.env}"
ASSUME_YES=false
[ "${1:-}" = "--yes" ] && ASSUME_YES=true

cd "$(git rev-parse --show-toplevel)"

[ -z "$(git status --porcelain --untracked-files=no)" ] \
  || { echo "Uncommitted changes; commit first." >&2; exit 1; }

TAG="release-$(date -u +%Y%m%dT%H%M%SZ)-$(git rev-parse --short=12 HEAD)"
IMAGE="$IMAGE_REPO:$TAG"

ssh_vm() {
  ssh ${DEPLOY_SSH_KEY:+-i "$DEPLOY_SSH_KEY"} -o BatchMode=yes -o ConnectTimeout=10 "$DEPLOY_HOST" "$@"
}

echo "==> Running tests (throwaway Redis on 127.0.0.1:6379)"
docker run -d --rm --name deploy-test-redis -p 127.0.0.1:6379:6379 redis:7-alpine >/dev/null
trap 'docker rm -f deploy-test-redis >/dev/null 2>&1 || true' EXIT
sleep 2
./mvnw -q clean verify

echo "==> Building $IMAGE"
./mvnw -q -DskipTests -Djib.to.image="$IMAGE" compile jib:dockerBuild

if [ "$ASSUME_YES" != true ]; then
  read -r -p "Push $IMAGE and deploy to the VM? [y/N] " answer
  [ "$answer" = y ] || { echo "Aborted."; exit 1; }
fi

echo "==> Pushing"
docker push "$IMAGE"

echo "==> Deploying"
ssh_vm bash -s -- "$CONTAINER" "$IMAGE" "$DEPLOY_ENV_FILE" <<'REMOTE'
set -euo pipefail
name=$1 image=$2 env_file=$3
prev="${name}_prev"

sudo test -s "$env_file" || { echo "env file missing on VM: $env_file" >&2; exit 1; }
sudo docker pull "$image" >/dev/null

had_old=false
if sudo docker inspect "$name" >/dev/null 2>&1; then
  had_old=true
  sudo docker rm -f "$prev" >/dev/null 2>&1 || true
  sudo docker stop "$name" >/dev/null
  sudo docker rename "$name" "$prev"
fi

sudo docker run -d --name "$name" --env-file "$env_file" \
  --publish 127.0.0.1:8080:8080 \
  --read-only --tmpfs /tmp:rw,noexec,nosuid,size=64m \
  --cap-drop=ALL --security-opt=no-new-privileges \
  "$image" >/dev/null

for _ in $(seq 1 45); do
  if curl -fsS -o /dev/null http://127.0.0.1:8080/ui/index.html 2>/dev/null; then
    echo "ready: $name -> $image"
    exit 0
  fi
  sleep 2
done

echo "NOT READY; rolling back" >&2
sudo docker logs --tail 30 "$name" >&2 || true
sudo docker rm -f "$name" >/dev/null || true
if [ "$had_old" = true ]; then
  sudo docker rename "$prev" "$name"
  sudo docker start "$name" >/dev/null
fi
exit 1
REMOTE

if [ -n "${PUBLIC_URL:-}" ]; then
  echo "==> Checking $PUBLIC_URL"
  for path in / /ui/index.html /ui/app.js /ui/styles.css; do
    code=$(curl -s -o /dev/null -m 15 -w '%{http_code}' "$PUBLIC_URL$path")
    echo "$path -> $code"
    [ "$code" = 200 ] || { echo "public check failed; previous container is kept as ${CONTAINER}_prev" >&2; exit 1; }
  done
fi

echo "Deployed $IMAGE. Previous version kept as ${CONTAINER}_prev for rollback."
