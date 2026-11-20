# 04 — API 约定

> 路径、请求体与响应字段以 [openapi/foundation-v1.yaml](../openapi/foundation-v1.yaml) 为唯一来源。本文件只写所有接口共享的约定。

## Base URL

- 控制面：`/api/v1`
- Endpoint：`/endpoint/v1`
- 健康：`/healthz`、`/readyz`

## 信封

```json
{ "code": "OK", "message": "", "data": {}, "traceId": "01HX..." }
```

错误：

```json
{ "code": "MODULE_INACTIVE", "message": "module not active",
  "details": {}, "traceId": "01HX..." }
```

## 错误码

| HTTP | code | 场景 |
|---|---|---|
| 400 | `INVALID_ARGUMENT` | 参数/校验失败 |
| 401 | `UNAUTHENTICATED` | Token 缺失或过期 |
| 403 | `FORBIDDEN` | 权限不足 |
| 404 | `NOT_FOUND` | 资源不存在 |
| 409 | `CONFLICT` | 版本冲突、重复注册/交付 |
| 412 | `REVISION_MISMATCH` | Twin revision 冲突 |
| 429 | `RATE_LIMITED` | 限流 |
| 500 | `INTERNAL` | 未预期错误 |
| 503 | `DEPENDENCY_UNAVAILABLE` | 依赖服务未就绪 |
| 503 | `MODULE_INACTIVE` | 模块未激活或已停用 |

## 认证

- 控制面：`Authorization: Bearer <session/api-token>`；登录 `POST /api/v1/auth/login`，刷新 `POST /api/v1/auth/refresh`。
- Endpoint：`Authorization: Bearer <endpoint-token>`，只能访问 `/endpoint/v1/*`。
- API Token 必须绑定 scope（如 `endpoints:read`、`packages:rollout`）。

## 公共约定

| 项 | 规定 |
|---|---|
| 租户 | `X-Foundation-Tenant`；缺省 `t_default` |
| 幂等 | 写操作支持 `Idempotency-Key`，24h 去重 |
| 分页 | `?cursor=&limit=`，返回 `nextCursor`；默认 20，最大 100 |
| 排序 | `?sort=field` / `?sort=-field` |
| 时间 | RFC 3339 UTC |
| 追踪 | 响应头 `X-Trace-Id` |
| 内容类型 | `application/json; charset=utf-8` |

## Twin 写入

`PATCH` + `Content-Type: application/merge-patch+json` + `If-Match: <revision>`：

- `config` 递归合并；
- `modules` 数组整体替换；
- revision 不匹配返回 `412`。

## 版本与限流

- 破坏性变更升级到 `/api/v2`；`v1` 至少保留一个主版本周期。
- 弃用：`Deprecation: true` + `Sunset: <date>`。

| 对象 | 默认限流 |
|---|---|
| 控制面用户 | 60 req/min |
| Endpoint 心跳 | 4 req/min/端点 |
| Endpoint 上报 | 30 req/min/端点 |
| Enroll | 10 req/min/租户 |
