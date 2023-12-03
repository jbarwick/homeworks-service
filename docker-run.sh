#!/bin/bash

ImageName="lutron_qs_exporter"
ContainerName="lutron_qs_exporter_container"

currentDirectory=$(pwd)

# Run the Docker container
docker run -d -p 9992:9992 \
   -v "$currentDirectory/static/data.json:/app/static/data.json" \
   -v "$currentDirectory/config.json:/app/config.json" \
   --name $ContainerName \
   $ImageName

echo "Container $ContainerName running successfully."
