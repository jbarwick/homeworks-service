#!/bin/bash
# Push lutron_qs_exporter:<version> from pyproject.toml.
# PUBLISH_TARGET=nexus (default), dockerhub, or both.
# SKIP_LATEST=1 pushes only the version tag.
#
# Nexus: NEXUS_USERNAME and NEXUS_PASSWORD
# Docker Hub: DOCKERHUB_USERNAME and DOCKERHUB_TOKEN
set -euo pipefail

ImageName="lutron_qs_exporter"
NexusRegistry="monster-jj.jvj28.com:9092"
Target="${PUBLISH_TARGET:-nexus}"
ImageVersion=$(sed -n 's/^[[:space:]]*version = "\(.*\)"/\1/p' pyproject.toml | head -n 1)
LocalImage="${ImageName}:${ImageVersion}"

if [ -z "$ImageVersion" ]; then
  echo "No version field found in pyproject.toml" >&2
  exit 1
fi

if ! docker image inspect "$LocalImage" >/dev/null 2>&1; then
  echo "Local image $LocalImage was not found. Run ./docker-build.sh first." >&2
  exit 1
fi

login_registry() {
  local registry="$1"
  local username="$2"
  local secret="$3"
  if [ -z "$username" ] || [ -z "$secret" ]; then
    echo "Missing credentials for $registry" >&2
    exit 1
  fi
  printf '%s' "$secret" | docker login "$registry" --username "$username" --password-stdin
}

publish_image() {
  local remote="$1"
  docker tag "$LocalImage" "${remote}:${ImageVersion}"
  echo "Pushing ${remote}:${ImageVersion}"
  docker push "${remote}:${ImageVersion}"
  if [ "${SKIP_LATEST:-}" = "1" ]; then
    return
  fi
  docker tag "$LocalImage" "${remote}:latest"
  echo "Pushing ${remote}:latest"
  docker push "${remote}:latest"
}

echo "Publishing $LocalImage to $Target"

case "$Target" in
  nexus|Nexus)
    login_registry "$NexusRegistry" "${NEXUS_USERNAME:-}" "${NEXUS_PASSWORD:-}"
    publish_image "${NexusRegistry}/${ImageName}"
    ;;
  dockerhub|DockerHub)
    login_registry "docker.io" "${DOCKERHUB_USERNAME:-}" "${DOCKERHUB_TOKEN:-}"
    publish_image "docker.io/${DOCKERHUB_USERNAME}/${ImageName}"
    ;;
  both|Both)
    login_registry "$NexusRegistry" "${NEXUS_USERNAME:-}" "${NEXUS_PASSWORD:-}"
    publish_image "${NexusRegistry}/${ImageName}"
    login_registry "docker.io" "${DOCKERHUB_USERNAME:-}" "${DOCKERHUB_TOKEN:-}"
    publish_image "docker.io/${DOCKERHUB_USERNAME}/${ImageName}"
    ;;
  *)
    echo "PUBLISH_TARGET must be nexus, dockerhub, or both" >&2
    exit 1
    ;;
esac

echo "Publish finished for $LocalImage"
