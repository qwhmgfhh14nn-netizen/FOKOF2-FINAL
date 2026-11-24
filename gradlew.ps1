# FOKOF2 checksum-pinned Gradle bootstrapper for Windows PowerShell.
# Reads the pinned distribution URL and SHA-256 from gradle-wrapper.properties.
$ErrorActionPreference = 'Stop'
$AppHome = Split-Path -Parent $MyInvocation.MyCommand.Path
$PropertiesPath = Join-Path $AppHome 'gradle\wrapper\gradle-wrapper.properties'
if (-not (Test-Path -LiteralPath $PropertiesPath)) { throw "Missing $PropertiesPath" }

$Properties = @{}
Get-Content -LiteralPath $PropertiesPath | ForEach-Object {
    if ($_ -match '^([^#=]+)=(.*)$') { $Properties[$matches[1]] = $matches[2].Replace('\:', ':').Replace('\\', '\') }
}
$DistributionUrl = [string]$Properties['distributionUrl']
$ExpectedSha = ([string]$Properties['distributionSha256Sum']).ToLowerInvariant()
$UrlMatch = [regex]::Match($DistributionUrl, '^https://services\.gradle\.org/distributions/(gradle-[0-9.]+-bin\.zip)$')
if (-not $UrlMatch.Success) { throw 'Unexpected Gradle distribution URL.' }
if ($ExpectedSha -notmatch '^[0-9a-f]{64}$') { throw 'Invalid distributionSha256Sum.' }
$ArchiveName = $UrlMatch.Groups[1].Value
$DistributionName = [IO.Path]::GetFileNameWithoutExtension($ArchiveName)
$GradleUserHome = if ($env:GRADLE_USER_HOME) { $env:GRADLE_USER_HOME } else { Join-Path $HOME '.gradle' }
$CacheDir = Join-Path $GradleUserHome "wrapper\dists\$DistributionName\fokof2-bootstrap"
$GradleHome = Join-Path $CacheDir $DistributionName
$GradleExe = Join-Path $GradleHome 'bin\gradle.bat'

if (-not (Test-Path -LiteralPath $GradleExe)) {
    New-Item -ItemType Directory -Force -Path $CacheDir | Out-Null
    $ZipPath = Join-Path $CacheDir $ArchiveName
    $TempDir = Join-Path $CacheDir ('unpack-' + [guid]::NewGuid().ToString('N'))
    try {
        Write-Host "Downloading $DistributionName from the official Gradle distribution service..."
        Invoke-WebRequest -Uri $DistributionUrl -OutFile $ZipPath
        $ActualSha = (Get-FileHash -LiteralPath $ZipPath -Algorithm SHA256).Hash.ToLowerInvariant()
        if ($ActualSha -ne $ExpectedSha) { throw 'Gradle archive SHA-256 mismatch. Download rejected.' }
        Expand-Archive -LiteralPath $ZipPath -DestinationPath $TempDir -Force
        $ExtractedGradle = Join-Path $TempDir $DistributionName
        if (-not (Test-Path -LiteralPath (Join-Path $ExtractedGradle 'bin\gradle.bat'))) { throw 'Unexpected Gradle archive structure.' }
        if (Test-Path -LiteralPath $GradleHome) { Remove-Item -Recurse -Force $GradleHome }
        Move-Item -LiteralPath $ExtractedGradle -Destination $GradleHome
    }
    finally {
        if (Test-Path -LiteralPath $ZipPath) { Remove-Item -Force $ZipPath }
        if (Test-Path -LiteralPath $TempDir) { Remove-Item -Recurse -Force $TempDir }
    }
}
& $GradleExe @args
exit $LASTEXITCODE
