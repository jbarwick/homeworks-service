#!/bin/bash

ImageName="lutron_qs_exporter"
ImageVersion=$(cat version.txt)
NexusPushRepositoryURL="monster-jj.jvj28.com:9092"

echo "Pushing Docker image to Nexus repository..."

Username="admin"
SecretFilePath=".secret"

# Read password from the .secret file
Password=$(cat $SecretFilePath)

# Log in to Docker
echo "$Password" | docker login "$NexusPushRepositoryURL" --username $Username --password-stdin

# Push the Docker image
docker tag $ImageName "$($NexusPushRepositoryURL)/$($ImageName):$($ImageVersion)"
docker push "$($NexusPushRepositoryURL)/$($ImageName):$($ImageVersion)"

docker tag $ImageName "$($NexusPushRepositoryURL)/$($ImageName):latest"
docker push "$($NexusPushRepositoryURL)/$($ImageName):latest"

echo "Image pushed to Nexus repository successfully."

