# Build the image and tag it with the version field in pyproject.toml.
$ErrorActionPreference = "Stop"

function Invoke-Docker {
    & docker @args
    if ($LASTEXITCODE -ne 0) {
        throw "docker $($args[0]) failed with exit code $LASTEXITCODE"
    }
}

$match = Select-String -Path .\pyproject.toml -Pattern '^\s*version\s*=\s*"([^"]+)"' | Select-Object -First 1
if (-not $match) {
    throw "No version field found in pyproject.toml"
}
$ImageVersion = $match.Matches[0].Groups[1].Value
$ImageName = "lutron_qs_exporter"
$Registry = "monster-jj.jvj28.com:9092"
$LocalImage = "${ImageName}:${ImageVersion}"

Write-Output "Building $LocalImage"
Invoke-Docker build -t $LocalImage .

Invoke-Docker tag $LocalImage "${Registry}/${ImageName}:${ImageVersion}"
Invoke-Docker tag $LocalImage "${Registry}/${ImageName}:latest"

Write-Output "Tagged ${Registry}/${ImageName}:${ImageVersion} and :latest"
Write-Output "Push with .\docker-publish.ps1 (Nexus) or .\docker-publish.ps1 -Target DockerHub."
Write-Output "compose.yaml must use the same tag before you deploy."
