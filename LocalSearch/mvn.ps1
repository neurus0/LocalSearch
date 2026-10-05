$ErrorActionPreference = 'Stop'
if (-not $env:JAVA_HOME) {
    $javaCmd = Get-Command java -ErrorAction SilentlyContinue
    if ($javaCmd) {
        $env:JAVA_HOME = (Get-Item $javaCmd.Source).Directory.Parent.FullName
    }
}
$mvnw = Join-Path $PSScriptRoot "mvnw.cmd"
& $mvnw $args
