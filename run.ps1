Param()
$ErrorActionPreference = "Stop"
$root = Split-Path $MyInvocation.MyCommand.Path -Parent
Set-Location $root

function HasCommand($name) { Get-Command $name -ErrorAction SilentlyContinue }

if (-not (HasCommand "java")) { Write-Error "Java not found on PATH"; exit 1 }
if (-not (HasCommand "javac")) { Write-Error "Javac not found on PATH"; exit 1 }

$lib = Join-Path $root "lib"
$out = Join-Path $root "out"
New-Item -ItemType Directory -Force -Path $lib | Out-Null
New-Item -ItemType Directory -Force -Path $out | Out-Null

$deps = @(
  @{ url = "https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.17.1/jackson-core-2.17.1.jar"; file = "jackson-core-2.17.1.jar" },
  @{ url = "https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.17.1/jackson-annotations-2.17.1.jar"; file = "jackson-annotations-2.17.1.jar" },
  @{ url = "https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.17.1/jackson-databind-2.17.1.jar"; file = "jackson-databind-2.17.1.jar" },
  @{ url = "https://repo1.maven.org/maven2/com/fasterxml/jackson/datatype/jackson-datatype-jsr310/2.17.1/jackson-datatype-jsr310-2.17.1.jar"; file = "jackson-datatype-jsr310-2.17.1.jar" }
)
${deps} += @{ url = "https://repo1.maven.org/maven2/org/jline/jline/3.23.0/jline-3.23.0.jar"; file = "jline-3.23.0.jar" }
${deps} += @{ url = "https://repo1.maven.org/maven2/org/jline/jline-reader/3.23.0/jline-reader-3.23.0.jar"; file = "jline-reader-3.23.0.jar" }
${deps} += @{ url = "https://repo1.maven.org/maven2/org/jline/jline-terminal/3.23.0/jline-terminal-3.23.0.jar"; file = "jline-terminal-3.23.0.jar" }
${deps} += @{ url = "https://repo1.maven.org/maven2/org/jline/jline-terminal-jna/3.23.0/jline-terminal-jna-3.23.0.jar"; file = "jline-terminal-jna-3.23.0.jar" }
${deps} += @{ url = "https://repo1.maven.org/maven2/org/jline/jline-terminal-jansi/3.23.0/jline-terminal-jansi-3.23.0.jar"; file = "jline-terminal-jansi-3.23.0.jar" }
${deps} += @{ url = "https://repo1.maven.org/maven2/net/java/dev/jna/jna/5.13.0/jna-5.13.0.jar"; file = "jna-5.13.0.jar" }
${deps} += @{ url = "https://repo1.maven.org/maven2/org/fusesource/jansi/jansi/2.4.0/jansi-2.4.0.jar"; file = "jansi-2.4.0.jar" }
foreach ($d in $deps) {
  $dest = Join-Path $lib $d.file
  if (-not (Test-Path $dest)) { Invoke-WebRequest -Uri $d.url -OutFile $dest }
}

$javaFiles = Get-ChildItem -Path (Join-Path $root "src\main\java") -Filter *.java -Recurse | ForEach-Object { $_.FullName }
& javac -cp "$lib\*" -d $out $javaFiles
& java -cp "$out;$lib\*" org.example.Main
