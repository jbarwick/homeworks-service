
$ImageName = "lutron_qs_exporter"
$RawImageVersion = poetry version -s
$ImageVersion = $RawImageVersion -replace '[^a-zA-Z0-9_.-]', '_'
$NexusPushRepositoryURL = "monster-jj.jvj28.com:9092"
$Username = $env:NEXUS_USERNAME
$Password = $env:NEXUS_PASSWORD
$DockerName = "$($NexusPushRepositoryURL)/$($ImageName)"


# Tag the Docker image

Write-Output "Image tagged successfully."


# Log in to Docker
Write-Output $Password | docker login $NexusPushRepositoryURL --username $Username --password-stdin

# Push the Docker image
Write-Output "Tagging image with tag: $($ImageVersion)..."
docker tag $ImageName "$($DockerName):$($ImageVersion)"
docker tag $ImageName "$($DockerName):latest"
Write-Output "Pushing Docker image to Nexus repository..."
docker push "$($DockerName):$($ImageVersion)"
Write-Output "Pushing Docker image $($ImageName):latest to Nexus repository."
docker push "$($DockerName):latest"

Write-Output "Image pushed to Nexus repository successfully."

