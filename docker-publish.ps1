
$ImageName = "lutron_qs_exporter"
$RawImageVersion = poetry version -s
$ImageVersion = $RawImageVersion -replace '[^a-zA-Z0-9_.-]', '_'
$NexusPushRepositoryURL = "monster-jj.jvj28.com:9092"
$Username = $env:NEXUS_USERNAME
$Password = $env:NEXUS_PASSWORD

Write-Output "Tagging Docker image for Nexus repository..."

# Tag the Docker image
docker tag $ImageName "$($NexusPushRepositoryURL)/$($ImageName):$($ImageVersion)"

Write-Output "Image tagged successfully."

Write-Output "Pushing Docker image to Nexus repository..."

# Log in to Docker
Write-Output $Password | docker login $NexusPushRepositoryURL --username $Username --password-stdin

# Push the Docker image
docker push "$($NexusPushRepositoryURL)/$($ImageName):$($ImageVersion)"

docker tag $ImageName "$($NexusPushRepositoryURL)/$($ImageName):latest"
docker push "$($NexusPushRepositoryURL)/$($ImageName):latest"

Write-Output "Image pushed to Nexus repository successfully."

