# Define image and container name
$ImageName = "lutron_qs_exporter"
# Delete 'dist' folder if it exists
if (Test-Path -Path .\dist -PathType Container) {
    Remove-Item -Path .\dist -Recurse -Force
}

# Run poetry build
Write-Output "Running poetry build..."
poetry config warnings.export false
poetry build
Write-Output "Poetry build completed."

# I wanna do a nexusiq scan
poetry export -f requirements.txt --without-hashes --output requirements.txt

Write-Output "Building Docker image: $ImageName"

# Build the Docker image
docker build -t $ImageName .

Write-Output "Image $ImageName built successfully."