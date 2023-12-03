# Define image and container name
$ImageName = "lutron_qs_exporter"

Write-Output "Building Docker image: $ImageName"

# Build the Docker image
docker build -t $ImageName .

Write-Output "Image $ImageName built successfully."

