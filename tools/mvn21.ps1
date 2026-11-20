# 用工作区 JDK 21 跑 Maven，不修改系统环境
# 用法：.\tools\mvn21.ps1 -v
$env:JAVA_HOME = Join-Path $PSScriptRoot 'jdk21'
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
& mvn @args
