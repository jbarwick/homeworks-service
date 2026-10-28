#!/bin/bash
set -euo pipefail

ImageName="lutron_qs_exporter"
Registry="monster-jj.jvj28.com:9092"
ImageVersion=$(sed -n 's/^[[:space:]]*version = "\(.*\)"/\1/p' pyproject.toml | head -n 1)

if [ -z "$ImageVersion" ]; then
  echo "No version field found in pyproject.toml" >&2
  exit 1
fi

echo "Building ${ImageName}:${ImageVersion}"
docker build -t "${ImageName}:${ImageVersion}" .

docker tag "${ImageName}:${ImageVersion}" "${Registry}/${ImageName}:${ImageVersion}"
docker tag "${ImageName}:${ImageVersion}" "${Registry}/${ImageName}:latest"

echo "Tagged ${Registry}/${ImageName}:${ImageVersion} and :latest"
echo "Push with ./docker-publish.sh after NEXUS_USERNAME and NEXUS_PASSWORD are set."
echo "compose.yaml must use the same tag before you deploy."
