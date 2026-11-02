param([switch]$BuildOnly)

$ErrorActionPreference = 'Stop'
$projectRoot = [System.IO.Path]::GetFullPath($PSScriptRoot)
$outputDirectory = Join-Path $projectRoot 'out'
$buildDirectory = Join-Path $projectRoot ('.build\compile-' + [guid]::NewGuid().ToString('N'))

# Every deleted path must remain inside this project.
function Assert-ProjectPath([string]$Path) {
    $resolvedPath = [System.IO.Path]::GetFullPath($Path)
    $projectPrefix = $projectRoot.TrimEnd('\') + '\'
    if (-not $resolvedPath.StartsWith($projectPrefix, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "Path is outside the project: $resolvedPath"
    }
}

if ($env:JAVA_HOME -and (Test-Path -LiteralPath (Join-Path $env:JAVA_HOME 'bin\javac.exe'))) {
    $compiler = Join-Path $env:JAVA_HOME 'bin\javac.exe'
} else {
    $compilerCommand = Get-Command javac.exe -ErrorAction SilentlyContinue
    if (-not $compilerCommand) {
        throw 'Install JDK 17 or newer and set JAVA_HOME or add its bin directory to PATH.'
    }
    $compiler = $compilerCommand.Source
}
$java = Join-Path (Split-Path -Parent $compiler) 'java.exe'
$sourceFiles = @(Get-ChildItem -LiteralPath (Join-Path $projectRoot 'src') -Recurse -Filter '*.java' -File |
    ForEach-Object FullName)
if ($sourceFiles.Count -eq 0) {
    throw 'No Java source files found in src.'
}

Assert-ProjectPath $buildDirectory
Assert-ProjectPath $outputDirectory
New-Item -ItemType Directory -Path $buildDirectory -Force | Out-Null
try {
    & $compiler -encoding UTF-8 --release 17 -d $buildDirectory @sourceFiles
    if ($LASTEXITCODE -ne 0) {
        throw 'Compilation failed. The previous output was preserved.'
    }

    New-Item -ItemType Directory -Path $outputDirectory -Force | Out-Null
    Get-ChildItem -LiteralPath $outputDirectory -Recurse -Filter '*.class' -File | ForEach-Object {
        Assert-ProjectPath $_.FullName
        Remove-Item -LiteralPath $_.FullName -Force
    }
    Get-ChildItem -LiteralPath $buildDirectory | ForEach-Object {
        Copy-Item -LiteralPath $_.FullName -Destination $outputDirectory -Recurse -Force
    }
    Write-Host "Compiled $($sourceFiles.Count) source files."
} finally {
    Assert-ProjectPath $buildDirectory
    Remove-Item -LiteralPath $buildDirectory -Recurse -Force
}

if (-not $BuildOnly) {
    & $java -cp $outputDirectory app.gui.GuiMain
    if ($LASTEXITCODE -ne 0) {
        throw "Application exited with code $LASTEXITCODE."
    }
}
