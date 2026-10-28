# Push lutron_qs_exporter:<version> from pyproject.toml.
# Default target is the private Nexus registry used by compose.yaml.
# Docker Hub is optional and does not change the compose image.
#
#   .\docker-publish.ps1
#   .\docker-publish.ps1 -Target DockerHub
#   .\docker-publish.ps1 -Target Both
#   .\docker-publish.ps1 -SkipLatest
#
# Nexus: NEXUS_USERNAME and NEXUS_PASSWORD
# Docker Hub: DOCKERHUB_USERNAME and DOCKERHUB_TOKEN (an access token, not the account password)

param(
    [ValidateSet("Nexus", "DockerHub", "Both")]
    [string]$Target = "Nexus",

    [switch]$SkipLatest
)

$ErrorActionPreference = "Stop"
$ImageName = "lutron_qs_exporter"
$NexusRegistry = "monster-jj.jvj28.com:9092"

function Invoke-Docker {
    & docker @args
    if ($LASTEXITCODE -ne 0) {
        throw "docker $($args[0]) failed with exit code $LASTEXITCODE"
    }
}

function Get-ProjectVersion {
    $match = Select-String -Path .\pyproject.toml -Pattern '^\s*version\s*=\s*"([^"]+)"' | Select-Object -First 1
    if (-not $match) {
        throw "No version field found in pyproject.toml"
    }
    return $match.Matches[0].Groups[1].Value
}

function Assert-LocalImage {
    param([string]$Reference)
    & docker image inspect $Reference 1>$null 2>$null
    if ($LASTEXITCODE -ne 0) {
        throw "Local image $Reference was not found. Run .\docker-build.ps1 first."
    }
}

function Connect-Registry {
    param(
        [string]$Registry,
        [string]$Username,
        [string]$Secret
    )
    if ([string]::IsNullOrWhiteSpace($Username) -or [string]::IsNullOrWhiteSpace($Secret)) {
        throw "Missing credentials for $Registry"
    }
    # password-stdin keeps the secret out of the process command line
    $Secret | & docker login $Registry --username $Username --password-stdin
    if ($LASTEXITCODE -ne 0) {
        throw "docker login to $Registry failed"
    }
}

function Publish-Image {
    param(
        [string]$Remote,
        [string]$Version,
        [string]$LocalImage
    )
    Invoke-Docker tag $LocalImage "${Remote}:${Version}"
    Write-Output "Pushing ${Remote}:${Version}"
    Invoke-Docker push "${Remote}:${Version}"
    if ($SkipLatest) {
        return
    }
    Invoke-Docker tag $LocalImage "${Remote}:latest"
    Write-Output "Pushing ${Remote}:latest"
    Invoke-Docker push "${Remote}:latest"
}

$ImageVersion = Get-ProjectVersion
$LocalImage = "${ImageName}:${ImageVersion}"
Assert-LocalImage $LocalImage
Write-Output "Publishing $LocalImage to $Target"

if ($Target -eq "Nexus" -or $Target -eq "Both") {
    Connect-Registry -Registry $NexusRegistry -Username $env:NEXUS_USERNAME -Secret $env:NEXUS_PASSWORD
    Publish-Image -Remote "$NexusRegistry/$ImageName" -Version $ImageVersion -LocalImage $LocalImage
}

if ($Target -eq "DockerHub" -or $Target -eq "Both") {
    Connect-Registry -Registry "docker.io" -Username $env:DOCKERHUB_USERNAME -Secret $env:DOCKERHUB_TOKEN
    Publish-Image -Remote "docker.io/$($env:DOCKERHUB_USERNAME)/$ImageName" -Version $ImageVersion -LocalImage $LocalImage
}

Write-Output "Publish finished for $LocalImage"
