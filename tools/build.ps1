# 构建 Foundation 参考实现，并组装可运行目录 reference/dist
# 用法：.\tools\build.ps1
$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot
$env:JAVA_HOME = Join-Path $PSScriptRoot 'jdk21'
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

$reference = Join-Path $root 'reference'
Write-Host "==> mvn package ($reference)"
$ErrorActionPreference = 'Continue'
& mvn -q -f (Join-Path $reference 'pom.xml') clean package
$mavenExit = $LASTEXITCODE
$ErrorActionPreference = 'Stop'
if ($mavenExit -ne 0) {
    throw "Maven build failed with exit code $mavenExit"
}

$dist = Join-Path $reference 'dist'
Remove-Item $dist -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path (Join-Path $dist 'plugins'), (Join-Path $dist 'apps') | Out-Null

Copy-Item (Join-Path $reference 'host\target\foundation-host.jar') (Join-Path $dist 'foundation-host.jar')
Copy-Item (Join-Path $reference 'plugins\demo-greeting\target\demo-greeting-plugin.jar') (Join-Path $dist 'plugins')
Copy-Item (Join-Path $reference 'endpoint-cli\target\endpoint-cli-jar-with-dependencies.jar') (Join-Path $dist 'endpoint-cli.jar')
Copy-Item (Join-Path $reference 'apps\ddns4j-lite.yaml') (Join-Path $dist 'apps')

Write-Host ''
Write-Host '==> artifacts'
Get-ChildItem -Recurse $dist -File | ForEach-Object {
    $size = [math]::Round($_.Length / 1KB, 1)
    Write-Host ("  {0,-58} {1,10} KB" -f $_.FullName.Substring($dist.Length + 1), $size)
}
Write-Host ''
Write-Host "Run:  cd $dist; ..\..\tools\jdk21\bin\java.exe -jar foundation-host.jar"
