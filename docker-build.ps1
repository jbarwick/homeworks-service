# Define image and container name
$ImageName = "lutron_qs_exporter"

# Delete 'dist' folder if it exists
if (Test-Path -Path .\dist -PathType Container) {
    Remove-Item -Path .\dist -Recurse -Force
}

# Run poetry build
Write-Output "Running poetry build..."
poetry build
Write-Output "Poetry build completed."

Write-Output "Building Docker image: $ImageName"

# Build the Docker image
docker build -t $ImageName .

Write-Output "Image $ImageName built successfully."