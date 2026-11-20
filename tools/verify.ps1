# Foundation v1 最小闭环验收脚本（A1–A16）
# 前置：.\tools\build.ps1 已生成 reference\dist
# 用法：.\tools\verify.ps1 [-Port 18080]
param(
    [int]$Port = 18080
)

$ErrorActionPreference = 'Continue'
$root = Split-Path -Parent $PSScriptRoot
$dist = Join-Path $root 'reference\dist'
$java = Join-Path $PSScriptRoot 'jdk21\bin\java.exe'
$cli = Join-Path $dist 'endpoint-cli.jar'
$work = Join-Path $root 'reference\verify'

if (-not (Test-Path (Join-Path $dist 'foundation-host.jar'))) {
    throw "reference\dist is missing, run .\tools\build.ps1 first"
}

Remove-Item $work -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path $work | Out-Null

$script:results = New-Object System.Collections.Generic.List[object]
function Check([string]$id, [string]$desc, [bool]$ok, [string]$detail = '') {
    $script:results.Add([pscustomobject]@{ Id = $id; Desc = $desc; Ok = $ok; Detail = $detail })
    $mark = if ($ok) { 'PASS' } else { 'FAIL' }
    Write-Host ("[{0}] {1} {2} {3}" -f $mark, $id, $desc, $(if ($detail) { "-> $detail" } else { '' }))
}

function Http {
    param(
        [string]$Method,
        [string]$Url,
        [string]$Body,
        [hashtable]$Headers = @{},
        $WebSession = $null
    )
    $params = @{
        Uri = $Url
        Method = $Method
        UseBasicParsing = $true
        TimeoutSec = 30
    }
    if ($Headers.Count -gt 0) { $params['Headers'] = $Headers }
    if (-not [string]::IsNullOrEmpty($Body)) {
        $params['Body'] = $Body
        $params['ContentType'] = 'application/json'
    }
    if ($null -ne $WebSession) { $params['WebSession'] = $WebSession }
    try {
        $response = Invoke-WebRequest @params
        return @{ status = [int]$response.StatusCode; body = $response.Content }
    } catch {
        $response = $_.Exception.Response
        $text = $null
        if ($null -ne $_.ErrorDetails -and $_.ErrorDetails.Message) {
            $text = $_.ErrorDetails.Message
        }
        if ([string]::IsNullOrEmpty($text) -and $null -ne $response) {
            try {
                $stream = $response.GetResponseStream()
                $reader = New-Object System.IO.StreamReader($stream)
                $text = $reader.ReadToEnd()
            } catch {
            }
        }
        if ($null -ne $response) {
            return @{ status = [int]$response.StatusCode; body = $text }
        }
        return @{ status = 0; body = $_.Exception.Message }
    }
}

function Start-Host {
    param([int]$HostPort, [string]$Tag)
    $log = Join-Path $work "host-$Tag.log"
    $args = @(
        "-Djava.io.tmpdir=$work",
        '-jar', (Join-Path $dist 'foundation-host.jar'),
        "--server.port=$HostPort",
        "--foundation.plugins-dir=$(Join-Path $dist 'plugins')",
        "--foundation.apps-dir=$(Join-Path $dist 'apps')"
    )
    $watch = [System.Diagnostics.Stopwatch]::StartNew()
    $process = Start-Process -FilePath $java -ArgumentList $args -WorkingDirectory $work `
        -RedirectStandardOutput $log -RedirectStandardError "$log.err" -PassThru -NoNewWindow
    for ($i = 0; $i -lt 600; $i++) {
        Start-Sleep -Milliseconds 100
        if ($process.HasExited) { break }
        try {
            $probe = Invoke-WebRequest -Uri "http://127.0.0.1:$HostPort/readyz" -UseBasicParsing -TimeoutSec 2
            if ($probe.StatusCode -eq 200) {
                $watch.Stop()
                return @{ process = $process; ms = $watch.ElapsedMilliseconds; log = $log }
            }
        } catch {
        }
    }
    $watch.Stop()
    return @{ process = $process; ms = -1; log = $log }
}

Write-Host '==> A1: cold start (3 runs)'
$cold = @()
for ($run = 1; $run -le 3; $run++) {
    $started = Start-Host -HostPort (18100 + $run) -Tag "cold$run"
    $cold += $started.ms
    if ($null -ne $started.process -and -not $started.process.HasExited) {
        Stop-Process -Id $started.process.Id -Force
        Start-Sleep -Milliseconds 600
    }
}
$coldSorted = $cold | Sort-Object
$median = $coldSorted[1]
Check 'A1' 'host cold start to /readyz 200 (median <= 3000ms)' ($median -gt 0 -and $median -le 3000) `
    ("runs=" + ($cold -join ',') + " median=" + $median + "ms")

Write-Host '==> starting host for functional checks'
$hostInfo = Start-Host -HostPort $Port -Tag 'verify'
if ($hostInfo.ms -lt 0) {
    Write-Host 'host failed to start, tail of log:'
    Get-Content $hostInfo.log -Tail 40 | Write-Host
    throw 'host did not become ready'
}
$base = "http://127.0.0.1:$Port"
$hostLog = $hostInfo.log

try {
    # ---------- A2 / A3 ----------
    $plugins = Http GET "$base/api/v1/plugins"
    $created = Http POST "$base/api/v1/modules" '{"pluginId":"foundation.demo.greeting","target":{"kind":"host"},"config":{"greeting":"hello-from-config"}}'
    $moduleView = $created.body | ConvertFrom-Json
    $moduleId = $moduleView.data.moduleId
    Check 'A2' 'plugin discovered and host module ACTIVE' `
        (($plugins.body -match 'foundation.demo.greeting') -and $moduleView.data.state -eq 'active' -and $plugins.body -match 'demo-greeting-plugin.jar') `
        "module=$moduleId state=$($moduleView.data.state)"

    $greeting = Http GET "$base/api/v1/demo/greeting"
    Check 'A3' 'greeting route returns module config value' `
        ($greeting.status -eq 200 -and $greeting.body -match 'hello-from-config') "status=$($greeting.status)"

    # ---------- A4 / A5 ----------
    Http POST "$base/api/v1/modules/${moduleId}:stop" '{}' | Out-Null
    $closed = Http GET "$base/api/v1/demo/greeting"
    $servicesAfterStop = Http GET "$base/api/v1/services"
    Check 'A4' 'stop module -> 503 MODULE_INACTIVE and service unresolvable' `
        ($closed.status -eq 503 -and $closed.body -match 'MODULE_INACTIVE' -and $servicesAfterStop.body -notmatch 'demo.greeting') `
        "status=$($closed.status) body=$($closed.body)"

    Http POST "$base/api/v1/modules/${moduleId}:start" '{}' | Out-Null
    $reopened = Http GET "$base/api/v1/demo/greeting"
    Check 'A5' 'start module -> 200' ($reopened.status -eq 200) "status=$($reopened.status)"

    # ---------- A6 ----------
    $reloadWatch = [System.Diagnostics.Stopwatch]::StartNew()
    $reloadFailures = 0
    for ($i = 1; $i -le 100; $i++) {
        $reload = Http POST "$base/api/v1/modules/${moduleId}:reload" '{}'
        if ($reload.status -ne 200) { $reloadFailures++ }
    }
    $reloadWatch.Stop()
    $routes = Http GET "$base/api/v1/routes"
    $services = Http GET "$base/api/v1/services"
    $demoRoutes = ([regex]::Matches($routes.body, '/api/v1/demo/greeting')).Count
    $demoServices = ([regex]::Matches($services.body, '"serviceId":"demo.greeting"')).Count
    Http POST "$base/api/v1/modules/${moduleId}:stop" '{}' | Out-Null
    $stopped = Http GET "$base/api/v1/modules/$moduleId"
    $stoppedView = $stopped.body | ConvertFrom-Json
    $effectsEmpty = ($null -eq $stoppedView.data.effects) -or ($stoppedView.data.effects.children.Count -eq 0)
    Http POST "$base/api/v1/modules/${moduleId}:start" '{}' | Out-Null
    $logErrors = (Select-String -Path $hostLog -Pattern ' ERROR ' -SimpleMatch -Quiet)
    Check 'A6' '100 reloads, no route/service leak, effects empty after stop' `
        ($reloadFailures -eq 0 -and $demoRoutes -eq 1 -and $demoServices -eq 1 -and $effectsEmpty -and -not $logErrors) `
        "failures=$reloadFailures routes=$demoRoutes services=$demoServices effectsEmpty=$effectsEmpty totalMs=$($reloadWatch.ElapsedMilliseconds)"

    # ---------- A7 ----------
    $tokenData = (Http POST "$base/api/v1/enroll-tokens" '{"tenantId":"t_default","labels":{"site":"lab"}}').body | ConvertFrom-Json
    $enrollToken = $tokenData.data.token
    $enrolled = (Http POST "$base/endpoint/v1/enroll" "{`"token`":`"$enrollToken`",`"name`":`"lab-router`",`"profile`":`"edge`"}").body | ConvertFrom-Json
    $endpointId = $enrolled.data.endpointId
    $endpointToken = $enrolled.data.endpointToken
    $again = (Http POST "$base/endpoint/v1/enroll" "{`"token`":`"$enrollToken`"}").body | ConvertFrom-Json
    $expiredToken = ((Http POST "$base/api/v1/enroll-tokens" '{"ttlSeconds":1}').body | ConvertFrom-Json).data.token
    Start-Sleep -Milliseconds 1300
    $expired = Http POST "$base/endpoint/v1/enroll" "{`"token`":`"$expiredToken`"}"
    Check 'A7' 'enroll returns ids, same token -> same endpoint, expired -> 401' `
        ($endpointId -and $endpointToken -and $again.data.endpointId -eq $endpointId -and $expired.status -eq 401) `
        "endpoint=$endpointId repeat=$($again.data.endpointId) expired=$($expired.status)"

    # ---------- A8 ----------
    Http POST "$base/endpoint/v1/register" '{"profile":"edge","platform":{"os":"linux","arch":"arm64"},"version":"0.1.0","capabilities":[{"id":"ip.resolve"},{"id":"dns.update"}]}' @{ Authorization = "Bearer $endpointToken" } | Out-Null
    $endpoint = (Http GET "$base/api/v1/endpoints/$endpointId").body | ConvertFrom-Json
    Check 'A8' 'register stores profile/platform/version/capabilities' `
        ($endpoint.data.profile -eq 'edge' -and $endpoint.data.platform.arch -eq 'arm64' -and $endpoint.data.version -eq '0.1.0' -and $endpoint.data.capabilities.Count -eq 2) `
        "profile=$($endpoint.data.profile) arch=$($endpoint.data.platform.arch)"

    # ---------- A9 ----------
    $heartbeat = (Http POST "$base/endpoint/v1/heartbeat" '{}' @{ Authorization = "Bearer $endpointToken" }).body | ConvertFrom-Json
    Check 'A9' 'heartbeat returns desiredRevision and serverTime' `
        ($heartbeat.data.desiredRevision -eq 1 -and $heartbeat.data.serverTime) `
        "desiredRevision=$($heartbeat.data.desiredRevision)"

    # ---------- A10 ----------
    $twin = (Http GET "$base/endpoint/v1/twin" $null @{ Authorization = "Bearer $endpointToken" }).body | ConvertFrom-Json
    $patched = (Http PATCH "$base/api/v1/endpoints/$endpointId/twin" '{"config":{"probe":{"intervalSec":30}}}' @{ 'If-Match' = '1' }).body | ConvertFrom-Json
    $stale = Http PATCH "$base/api/v1/endpoints/$endpointId/twin" '{"config":{}}' @{ 'If-Match' = '1' }
    $statePath = Join-Path $work 'cli-state.json'
    & $java -jar $cli enroll --server $base --token $enrollToken --name lab-router --state $statePath | Out-Null
    $cliEnrollExit = $LASTEXITCODE
    & $java -jar $cli report --server $base --state $statePath --idempotency-key a10-1 | Out-Null
    $cliReportExit = $LASTEXITCODE
    $afterReport = (Http GET "$base/api/v1/endpoints/$endpointId").body | ConvertFrom-Json
    Check 'A10' 'twin desired 1 -> patch 2 -> reported follows, drift 0' `
        ($twin.data.revision -eq 1 -and $patched.data.revision -eq 2 -and $stale.status -eq 412 -and $afterReport.data.reportedRevision -eq 2 -and -not $afterReport.data.drift) `
        "desired=$($afterReport.data.desiredRevision) reported=$($afterReport.data.reportedRevision) drift=$($afterReport.data.drift) stale=$($stale.status) exits=$cliEnrollExit/$cliReportExit"

    # ---------- A11 ----------
    $reportBody = '{"reportedAt":"2026-11-20T00:00:00Z","health":{"state":"ok"}}'
    $firstReport = (Http POST "$base/endpoint/v1/report" $reportBody @{ Authorization = "Bearer $endpointToken"; 'Idempotency-Key' = 'idem-fixed' }).body | ConvertFrom-Json
    $secondReport = (Http POST "$base/endpoint/v1/report" $reportBody @{ Authorization = "Bearer $endpointToken"; 'Idempotency-Key' = 'idem-fixed' }).body | ConvertFrom-Json
    Check 'A11' 'same Idempotency-Key stored once' `
        ((-not $firstReport.data.duplicate) -and $secondReport.data.duplicate) `
        "first=$($firstReport.data.duplicate) second=$($secondReport.data.duplicate)"

    # ---------- A12 ----------
    $login = Invoke-WebRequest -Uri "$base/login" -Method POST -Body @{ username = 'admin'; password = 'admin' } `
        -SessionVariable consoleSession -UseBasicParsing
    $pageEndpoints = Invoke-WebRequest -Uri "$base/endpoints" -WebSession $consoleSession -UseBasicParsing
    $pageDetail = Invoke-WebRequest -Uri "$base/endpoints/$endpointId" -WebSession $consoleSession -UseBasicParsing
    $pageModules = Invoke-WebRequest -Uri "$base/modules" -WebSession $consoleSession -UseBasicParsing
    $pageAudit = Invoke-WebRequest -Uri "$base/audit" -WebSession $consoleSession -UseBasicParsing
    Check 'A12' 'four console pages reachable, detail shows desired/reported' `
        ($login.StatusCode -eq 200 -and $pageEndpoints.Content -match $endpointId -and $pageDetail.Content -match 'desired / reported' -and $pageModules.Content -match $moduleId -and $pageAudit.StatusCode -eq 200) `
        "login=$($login.StatusCode) endpoints=$($pageEndpoints.StatusCode) detail=$($pageDetail.StatusCode) modules=$($pageModules.StatusCode) audit=$($pageAudit.StatusCode)"

    # ---------- A14 ----------
    $endpointModule = (Http POST "$base/api/v1/modules" "{`"pluginId`":`"foundation.demo.greeting`",`"target`":{`"endpointId`":`"$endpointId`"},`"config`":{`"greeting`":`"hi`"}}").body | ConvertFrom-Json
    $endpointModuleId = $endpointModule.data.moduleId
    $secret = 'super-secret-123'
    $deliverBody = "{`"fields`":{`"accessKeyId`":`"AK-DEMO`",`"accessKeySecret`":`"$secret`"}}"
    $deliver = Http POST "$base/api/v1/modules/${endpointModuleId}:deliver-credential" $deliverBody
    $duplicate = Http POST "$base/api/v1/modules/${endpointModuleId}:deliver-credential" $deliverBody
    $commands = (Http GET "$base/endpoint/v1/commands" $null @{ Authorization = "Bearer $endpointToken" }).body | ConvertFrom-Json
    $offlineToken = ((Http POST "$base/api/v1/enroll-tokens" '{}').body | ConvertFrom-Json).data.token
    $offlineEndpoint = ((Http POST "$base/endpoint/v1/enroll" "{`"token`":`"$offlineToken`"}").body | ConvertFrom-Json).data.endpointId
    $offlineModule = (Http POST "$base/api/v1/modules" "{`"pluginId`":`"foundation.demo.greeting`",`"target`":{`"endpointId`":`"$offlineEndpoint`"}}").body | ConvertFrom-Json
    $offlineDeliver = Http POST "$base/api/v1/modules/$($offlineModule.data.moduleId)`:deliver-credential" $deliverBody
    $logHasSecret = Select-String -Path $hostLog -Pattern $secret -SimpleMatch -Quiet
    Check 'A14' 'credential delivered once, duplicate 409, offline 409, no plaintext in log' `
        ($deliver.status -eq 200 -and $duplicate.status -eq 409 -and $offlineDeliver.status -eq 409 -and $commands.data.Count -eq 1 -and -not $logHasSecret) `
        "first=$($deliver.status) duplicate=$($duplicate.status) offline=$($offlineDeliver.status) commands=$($commands.data.Count) logLeak=$logHasSecret"

    # ---------- A15 ----------
    & $java -jar $cli twin --server $base --state $statePath | Out-Null
    $twinExit = $LASTEXITCODE
    & $java -jar $cli enroll --server $base 2>$null | Out-Null
    $argsExit = $LASTEXITCODE
    & $java -jar $cli twin --server $base --state (Join-Path $work 'missing-state.json') 2>$null | Out-Null
    $stateExit = $LASTEXITCODE
    & $java -jar $cli twin --server 'http://127.0.0.1:1' --state $statePath 2>$null | Out-Null
    $networkExit = $LASTEXITCODE
    Check 'A15' 'CLI exit codes 0/2/3/1' `
        ($twinExit -eq 0 -and $argsExit -eq 2 -and $stateExit -eq 3 -and $networkExit -eq 1) `
        "ok=$twinExit args=$argsExit state=$stateExit network=$networkExit"

    # ---------- A16 ----------
    $currentRevision = ((Http GET "$base/api/v1/endpoints/$endpointId").body | ConvertFrom-Json).data.desiredRevision
    Http PATCH "$base/api/v1/endpoints/$endpointId/twin" '{"config":{"probe":{"intervalSec":45}}}' @{ 'If-Match' = "$currentRevision" } | Out-Null
    & $java -jar $cli report --server $base --state $statePath --fail 'apply blocked: probe schema mismatch' | Out-Null
    $drifted = (Http GET "$base/api/v1/endpoints/$endpointId").body | ConvertFrom-Json
    $driftedPage = Invoke-WebRequest -Uri "$base/endpoints/$endpointId" -WebSession $consoleSession -UseBasicParsing
    & $java -jar $cli report --server $base --state $statePath --idempotency-key a16-ok | Out-Null
    $healed = (Http GET "$base/api/v1/endpoints/$endpointId").body | ConvertFrom-Json
    Check 'A16' 'blocked report shows drift + lastError, next report clears it' `
        ($drifted.data.drift -and $drifted.data.lastError -match 'apply blocked' -and $driftedPage.Content -match 'drift' -and (-not $healed.data.drift)) `
        "drift=$($drifted.data.drift) lastError=$($drifted.data.lastError)"

    # ---------- A13 ----------
    $artifacts = @(
        (Join-Path $dist 'foundation-host.jar'),
        (Join-Path $dist 'plugins\demo-greeting-plugin.jar'),
        (Join-Path $dist 'endpoint-cli.jar')
    ) | ForEach-Object { Test-Path $_ }
    Check 'A13' 'build script produced host, plugin and CLI, java -jar starts' `
        (-not ($artifacts -contains $false)) "artifacts=$($artifacts -join ',')"
} finally {
    if ($null -ne $hostInfo.process -and -not $hostInfo.process.HasExited) {
        Stop-Process -Id $hostInfo.process.Id -Force
    }
}

$failed = $script:results | Where-Object { -not $_.Ok }
Write-Host ''
Write-Host ("==> {0} checks, {1} passed, {2} failed" -f $script:results.Count, ($script:results.Count - $failed.Count), $failed.Count)
if ($failed.Count -gt 0) {
    exit 1
}
exit 0

