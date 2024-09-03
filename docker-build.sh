#!/bin/bash

# Define image name
IMAGE_NAME="lutron_qs_exporter"

echo "Running poetry build..."
poetry build -f wheel

echo "Poetry build completed."

echo "Building Docker image: $IMAGE_NAME"

# Build the Docker image
docker build -t $IMAGE_NAME .

echo "Image $IMAGE_NAME built successfully."

