# 04 — HTTP API 约定

机器可读定义：[`openapi/foundation-v1.yaml`](../openapi/foundation-v1.yaml)。

## 1. 通用约定

| 项 | 规定 |
|---|---|
| Base URL | 控制面 `/api/v1`；端点 `/endpoint/v1` |
| 内容类型 | `application/json; charset=utf-8` |
| 鉴权 | 控制面 `Authorization: Bearer <session/api-token>`；端点 `Bearer <endpoint-token>` |
| 租户 | `X-Foundation-Tenant`；缺省为默认租户 |
| 幂等 | 写操作支持 `Idempotency-Key`，24h 去重 |
| 分页 | `?cursor=&limit=`，返回 `nextCursor`；limit 默认 20，最大 100 |
| 排序 | `?sort=field` / `?sort=-field` |
| 时间 | RFC 3339 UTC |
| 追踪 | 响应头 `X-Trace-Id` |

## 2. 响应信封

```json
{ "code": "OK", "message": "", "data": {}, "traceId": "01HX..." }
```

错误：

```json
{ "code": "ENDPOINT_NOT_FOUND", "message": "endpoint not found",
  "details": { "endpointId": "ep_01HX" }, "traceId": "01HX..." }
```

### 错误码

| HTTP | code | 场景 |
|---|---|---|
| 400 | `INVALID_ARGUMENT` | 参数/校验失败 |
| 401 | `UNAUTHENTICATED` | Token 缺失或过期 |
| 403 | `FORBIDDEN` | 权限不足 |
| 404 | `NOT_FOUND` | 资源不存在 |
| 409 | `CONFLICT` | 版本冲突、重复注册 |
| 412 | `REVISION_MISMATCH` | twin 版本冲突 |
| 429 | `RATE_LIMITED` | 限流 |
| 500 | `INTERNAL` | 未预期错误 |
| 503 | `DEPENDENCY_UNAVAILABLE` | 依赖服务未就绪 |
| 503 | `MODULE_INACTIVE` | 模块未激活或已停用 |

## 3. 认证

```
POST /api/v1/auth/login
{ "username": "...", "password": "..." }
→ { "token": "...", "expiresAt": "...", "user": {...} }
```

- Token 默认 12h，支持 `POST /api/v1/auth/refresh`。
- API Token 由用户创建，带 scope（`endpoints:read`、`plugins:write` 等）。
- Endpoint Token 只能访问 `/endpoint/v1/*`。

## 4. 端点

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/v1/endpoints` | 列表；过滤 `status`、`profile`、`label` |
| GET | `/api/v1/endpoints/{id}` | 详情（含 desired/reported 摘要） |
| PATCH | `/api/v1/endpoints/{id}` | 改名、改标签 |
| DELETE | `/api/v1/endpoints/{id}` | 移除（软删） |
| GET | `/api/v1/endpoints/{id}/twin` | 读取 desired 与 reported |
| PATCH | `/api/v1/endpoints/{id}/twin` | 写入 desired（revision +1） |
| POST | `/api/v1/endpoints/{id}/token:rotate` | 轮换 Endpoint Token |
| POST | `/api/v1/enroll-tokens` | 创建一次性 Enroll Token |

Twin 写入统一使用 `PATCH` + `Content-Type: application/merge-patch+json` + `If-Match: <revision>`；`config` 递归合并，`modules` 数组整体替换；冲突返回 `412`。

> OpenAPI 只列出路径与核心字段；`X-Foundation-Tenant`、`X-Trace-Id`、`Idempotency-Key`、`sort` 等公共头/参数以本节约定为准，未在每个 path 上重复声明。

## 5. 模块

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/v1/modules?endpointId=` | 模块列表 |
| POST | `/api/v1/modules` | 部署模块（pluginId + version + target） |
| GET | `/api/v1/modules/{id}` | 详情（含 effect 树、twin） |
| DELETE | `/api/v1/modules/{id}` | 卸载 |
| POST | `/api/v1/modules/{id}:start` | 激活 |
| POST | `/api/v1/modules/{id}:stop` | 停用并回滚 |
| POST | `/api/v1/modules/{id}:reload` | 卸载后重建 |
| GET/PATCH | `/api/v1/modules/{id}/config` | 读取/更新配置 |
| GET | `/api/v1/modules/{id}/twin` | 读取 desired/reported |
| PATCH | `/api/v1/modules/{id}/twin` | 写入 desired |
| POST | `/api/v1/modules/{id}:deliver-credential` | 一次性交付凭据（单次、60s、不落库；端点离线或已交付返回 409） |

## 6. 插件（包目录）

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/v1/plugins` | 插件包列表（版本、capabilities、依赖） |
| GET | `/api/v1/plugins/{id}` | 插件包详情（manifest） |
| GET | `/api/v1/plugins/{id}/versions` | 已安装插件包版本 |

> 启停/重载针对 Module，不针对 Plugin。插件包只做安装/卸载。

## 7. 应用

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/v1/apps` | 应用包列表 |
| POST | `/api/v1/apps` | 安装应用包 |
| GET | `/api/v1/apps/{id}` | 详情（含模块与页面） |
| POST | `/api/v1/apps/{id}:disable` | 停用整个应用 |
| POST | `/api/v1/apps/{id}:upgrade` | 升级 |

## 8. 包与分发

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/v1/packages` | 过滤 `type`、`channel`、`platform` |
| POST | `/api/v1/packages` | 上传（multipart 或 URL 导入） |
| GET | `/api/v1/packages/{id}` | 详情 |
| POST | `/api/v1/packages/{id}:rollout` | 灰度（targetLabels、batchPercent） |

## 9. 审计

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/v1/audit` | 过滤 `actor`、`action`、`target`、`from`、`to` |

审计事件只追加，不修改、不删除；仅支持查询与导出。

## 10. 健康

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/healthz` | 存活 |
| GET | `/readyz` | 就绪（内核已初始化） |

## 11. 端点协议

见 [`03-endpoint-contract.md`](./03-endpoint-contract.md)。固定端点：

```
POST /endpoint/v1/enroll
POST /endpoint/v1/register
POST /endpoint/v1/heartbeat
GET  /endpoint/v1/twin
POST /endpoint/v1/twin/report
POST /endpoint/v1/report
GET  /endpoint/v1/commands
POST /endpoint/v1/ack
POST /endpoint/v1/upgrade/plan
```

## 12. 版本与限流

- 路径版本 `/api/v1`；破坏性变更升级 `/api/v2`。
- 新增字段向后兼容；客户端忽略未知字段。
- 弃用：`Deprecation: true` + `Sunset: <date>`。

| 对象 | 默认限流 |
|---|---|
| 控制面用户 | 60 req/min |
| Endpoint 心跳 | 4 req/min/端点 |
| Endpoint 上报 | 30 req/min/端点 |
| Enroll | 10 req/min/租户 |

超限返回 `429` + `Retry-After`。
