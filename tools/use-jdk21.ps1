# 仅在本工作区启用 JDK 21
# 用法：. .\tools\use-jdk21.ps1
$env:JAVA_HOME = Join-Path $PSScriptRoot 'jdk21'
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
Write-Host "JAVA_HOME=$env:JAVA_HOME"
& "$env:JAVA_HOME\bin\java.exe" -version
