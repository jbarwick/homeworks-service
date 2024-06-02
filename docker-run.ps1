
$ImageName = "lutron_qs_exporter"
$ContainerName = "lutron_qs_exporter_container"

$currentDirectory = $PWD.Path

# Run the Docker container
docker run -d -p 9992:9992 `
  -v $currentDirectory\config.json:/app/config.json `
  --name $ContainerName `
  -e LOG_LEVEL=INFO `
  $ImageName

Write-Output "Container $ContainerName running successfully."
