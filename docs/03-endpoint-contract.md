# 03 — Endpoint 模型与接入协议

> 本文件里的 **Endpoint 是"被纳管的端"**，与 Node.js 无关。旧文档中的 `Node` 一律更名 `Endpoint`。

## 1. 模型

```json
{
  "id": "ep_01HX...",
  "name": "home-router-01",
  "profile": "edge",
  "platform": { "os": "linux", "arch": "arm64", "runtime": "musl" },
  "version": "0.1.0",
  "capabilities": [
    { "id": "ip.resolve", "modes": ["interface", "http"] },
    { "id": "dns.update", "providers": ["aliyun", "cloudflare"] }
  ],
  "labels": { "site": "home", "tier": "edge" },
  "status": "online",
  "lastSeen": "2026-11-19T10:00:00Z",
  "tenantId": "t_default",
  "desired": { "revision": 7, "config": {} },
  "reported": { "revision": 7, "config": {}, "appliedAt": "2026-11-19T09:59:40Z" }
}
```

### Profile

| profile | 典型载体 | 独有约束 |
|---|---|---|
| `edge` | 路由器/NAS/软路由/机器人主控 | OTA、离线队列、本地凭据、资源上限 |
| `desktop` | Windows/macOS/Linux 客户端 | 用户会话、托盘、文件访问 |
| `mobile` | iOS/Android | 推送、后台执行限制 |
| `relay` | 中继/网关 | 连接保持、转发配额 |

Profile 决定允许的字段与策略，不决定语言。`edge` 的默认实现是 Rust 静态单文件。

## 2. 模块

Endpoint 上的模块是 Plugin 的实例：

```json
{
  "moduleId": "mod_01HX",
  "endpointId": "ep_01HX",
  "pluginId": "foundation.dns.aliyun",
  "version": "1.0.0",
  "state": "active",
  "desired": { "revision": 3, "config": { "provider": "aliyun" } },
  "reported": { "revision": 3, "state": "ok", "lastError": null }
}
```

- 模块由控制台下发的 deployment 决定，不由端点自选；
- 模块的 desired/reported 与 Endpoint 的 twin 同构；
- 模块失败不阻塞端点其他模块；端点级 `reported.state` 聚合为 `ok/degraded/error`。

## 3. 接入流程

```
1. 控制台创建 Enroll Token（一次性、绑定租户/标签、默认 24h）
2. Endpoint  POST /endpoint/v1/enroll      → nodeId 改为 endpointId + endpointToken
3. Endpoint  POST /endpoint/v1/register    → 上报 profile、platform、version、capabilities
4. 周期      POST /endpoint/v1/heartbeat   → 返回 desiredRevision、serverTime
5. 需要时    GET  /endpoint/v1/twin?rev=   → 拉取 desired（ETag）
6. 周期      POST /endpoint/v1/report      → 上报 reported、IP、健康、任务结果
7. 下行      GET  /endpoint/v1/commands    → 长轮询；或 WSS /endpoint/v1/stream
8. 升级      POST /endpoint/v1/upgrade/plan → 包地址 + sha256 + 签名
```

## 4. 接口

| 接口 | 方法 | 鉴权 | 幂等 |
|---|---|---|---|
| `/endpoint/v1/enroll` | POST | Enroll Token | 是（同 token 返回同一 endpointId） |
| `/endpoint/v1/register` | POST | Endpoint Token | 是 |
| `/endpoint/v1/heartbeat` | POST | Endpoint Token | 是 |
| `/endpoint/v1/twin` | GET | Endpoint Token | 是（ETag） |
| `/endpoint/v1/twin/report` | POST | Endpoint Token | 是（`Idempotency-Key`） |
| `/endpoint/v1/report` | POST | Endpoint Token | 是 |
| `/endpoint/v1/commands` | GET | Endpoint Token | 否（拉取即确认） |
| `/endpoint/v1/ack` | POST | Endpoint Token | 是 |
| `/endpoint/v1/upgrade/plan` | POST | Endpoint Token | 是 |

统一信封：`{ code, message, data, traceId }`。

## 5. Twin（desired / reported）

| 字段 | 写入方 | 说明 |
|---|---|---|
| `desired.revision` | 控制面 | 单调递增；每次配置/模块变更 +1 |
| `desired.config` | 控制面 | 端点级配置（探测策略、通道、标签） |
| `desired.modules[]` | 控制面 | 期望部署的模块及配置 |
| `reported.revision` | Endpoint | 已应用到的 desired 版本 |
| `reported.config` | Endpoint | 实际生效配置（脱敏后） |
| `reported.modules[]` | Endpoint | 每模块状态与错误 |
| `reported.appliedAt` | Endpoint | 最近应用时间 |

规则：

1. 端点拉取 twin 用 `GET /endpoint/v1/twin?rev=N`；未变化返回 304。
2. Endpoint 应用后必须回报 `reported.revision`；不等于 desired 即为"漂移"。
3. 控制台必须展示漂移（desired ≠ reported）与最后错误。
4. 冲突以控制面为准，但不得静默覆盖：失败保留旧值并记审计。
5. 写入使用 JSON Merge Patch（`application/merge-patch+json`）+ `If-Match`；`config` 递归合并，`modules` 数组整体替换。

### 5.1 端点自治

- 周期任务由端点按 desired 中的 cron **本地调度**；控制面不承担调度。
- 控制面只下发 desired 与一次性命令；命令带 `commandId` 与 `expiresAt`。
- 端点离线时按最后一次 desired 继续运行；恢复后按 revision 对齐。
- 端点不得跨进程调用宿主服务；跨端只走 Twin 与命令。

## 6. 心跳与离线

- 默认 30s，±20% 抖动；服务端可下发覆盖。
- 连续 3 周期未心跳 → `offline`；恢复立即 `online`。
- 心跳响应含 `serverTime`、`desiredRevision`、`intervalSec`。

## 7. 上报

```json
{
  "endpointId": "ep_01HX",
  "reportedAt": "2026-11-19T10:00:00Z",
  "publicIp": { "v4": "2.2.2.2", "v6": "2408:..." },
  "health": { "state": "ok", "uptimeSec": 86400 },
  "metrics": { "cpu": 0.12, "memMb": 18 },
  "results": [{ "taskId": "t1", "status": "success", "detail": "..." }]
}
```

- 必须带 `Idempotency-Key`；服务端按 `(endpointId, key)` 去重 24h。
- 禁止出现凭据字段；服务端做敏感字段扫描。
- 单次 ≤ 64KB；超出拆分或走对象存储。

## 8. 下行命令

| 命令 | 说明 |
|---|---|
| `twin.apply` | 立即拉取 twin |
| `module.install` / `module.remove` | 模块部署 |
| `task.run` | 触发一次任务 |
| `upgrade.start` | 开始升级 |
| `diagnose.collect` | 采集诊断 |

命令带 `commandId` 与 `expiresAt`；执行后 `ack`，超时重投最多 3 次。

## 9. 升级（profile=edge）

```json
{ "endpointId": "ep_01HX", "currentVersion": "0.1.0", "channel": "stable",
  "platform": { "os": "linux", "arch": "arm64" } }
```

响应：`packageId`、`version`、`download`、`sha256`、`signature`、`minHost`。

规则：

- 校验 sha256（签名可选但推荐）；失败不替换。
- 下载到临时目录 → 校验 → 原子重命名 → 重启；失败回滚旧文件。
- 灰度按 labels 分批，端点无需感知批次。

## 10. 安全

| 项 | 规定 |
|---|---|
| Enroll Token | 一次性、默认 24h、绑定租户与标签 |
| Endpoint Token | 只可访问 `/endpoint/v1/*`；可轮换、可吊销 |
| 传输 | 强制 HTTPS；Enterprise 可启 mTLS |
| 凭据 | 只下发引用；明文不入库、不上报 |
| 一次性交付 | 控制台经 TLS 转发给在线端点，单次有效、60s TTL、仅内存、不落库、不落日志；端点离线时返回冲突 |
| 审计 | 注册、吊销、升级、命令下发全部留痕 |

## 11. 最小实现要求

- 单二进制、无运行时依赖；`edge` 目标 ≤ 8MB（建议值）。
- 最小闭环只需 enroll / heartbeat / twin / report 四个接口。
- 命令与升级可返回 `501`，由 v1.1 补齐。
