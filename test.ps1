$ErrorActionPreference = 'Stop'
& (Join-Path $PSScriptRoot 'run.ps1') -BuildOnly

if ($env:JAVA_HOME -and (Test-Path -LiteralPath (Join-Path $env:JAVA_HOME 'bin\javac.exe'))) {
    $compiler = Join-Path $env:JAVA_HOME 'bin\javac.exe'
} else {
    $compiler = (Get-Command javac.exe -ErrorAction Stop).Source
}
$java = Join-Path (Split-Path -Parent $compiler) 'java.exe'
$outputDirectory = Join-Path $PSScriptRoot 'out'
$testDirectory = Join-Path $PSScriptRoot '.build\tests'
New-Item -ItemType Directory -Path $testDirectory -Force | Out-Null
$testFiles = @(Get-ChildItem -LiteralPath (Join-Path $PSScriptRoot 'tests') -Recurse -Filter '*.java' -File |
    ForEach-Object FullName)
& $compiler -encoding UTF-8 --release 17 -cp $outputDirectory -d $testDirectory @testFiles
if ($LASTEXITCODE -ne 0) {
    throw 'Test compilation failed.'
}
& $java -cp "$outputDirectory;$testDirectory" StartupRegressionTest suite (Join-Path $PSScriptRoot 'data\ObjectPlus.data')
if ($LASTEXITCODE -ne 0) {
    throw 'Startup regression tests failed.'
}
