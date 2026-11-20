# 05 — 控制台、身份与审计

## 1. 信息架构

```
Foundation Console
├── 总览 Overview
│   ├── 端点在线率 / 离线列表
│   ├── 模块异常（failed / drifted）
│   └── 最近审计
├── 端点 Endpoints
│   ├── 列表（profile / status / labels 过滤）
│   ├── 详情：概览 · Twin · 模块 · 上报 · 事件
│   └── 动作：改标签、轮换 Token、下发 twin、升级
├── 模块 Modules
│   ├── 列表（按端点 / 状态）
│   └── 详情：配置 · Twin · Effect 树 · 日志
├── 应用 Apps
│   ├── 列表（版本、包含模块、页面）
│   └── 动作：安装、停用、升级
├── 插件 Plugins（制品目录）
│   ├── 列表（版本、capabilities、依赖）
│   └── 详情：manifest、版本、被哪些模块使用
├── 包 Packages
│   ├── 列表（type / channel / platform）
│   └── 动作：上传、灰度、回滚
├── 审计 Audit
│   └── 查询 / 导出
└── 设置 Settings
    ├── 用户与角色
    ├── 租户（Enterprise）
    ├── Token 与 Scope
    └── 系统信息（版本、插件目录、制品库）
```

## 2. 关键交互

| 场景 | 交互要求 |
|---|---|
| 插件重载 | 二次确认；展示将回滚的 effect 数；失败展示原因与回滚结果 |
| Twin 下发 | 展示 desired 与 reported 差异；写入需 `If-Match` revision；冲突提示刷新 |
| 端点升级 | 选择通道与目标标签；预览影响端点数量；展示批次进度 |
| Token 轮换 | 一次性展示新 Token；提示旧 Token 立即失效 |
| 删除端点 | 软删；提示模块与历史数据保留策略 |
| 秘密字段 | 只显示 `****`；编辑需重新输入；日志与审计脱敏 |

## 3. 角色与权限

| 角色 | 端点 | 模块 | 应用 | 包 | 用户 | 审计 | 设置 |
|---|---|---|---|---|---|---|---|
| owner | 读写 | 读写 | 读写 | 读写 | 读写 | 读 | 读写 |
| admin | 读写 | 读写 | 读写 | 读写 | 读 | 读 | 读 |
| operator | 读写 | 读写 | 读 | 读 | — | 读 | — |
| viewer | 读 | 读 | 读 | 读 | — | 读 | — |

- 权限以 `resource:action` 表达（如 `endpoints:write`、`packages:rollout`）。
- API Token 必须显式绑定 scope；不允许"全权限"默认值。
- 越权返回 `403 FORBIDDEN`，并写审计。

## 4. 多租户

- Core：单租户，`tenantId` 固定为 `t_default`。
- Enterprise：用户可属于多个租户；请求头 `X-Foundation-Tenant` 选择当前租户。
- 隔离边界：端点、模块、包、审计、Token 均按 `tenantId` 过滤；服务层强制校验，不依赖前端。
- 跨租户共享只允许"包制品"（显式标记 `shared: true`）。

## 5. 审计事件

```json
{
  "id": "aud_01HX",
  "actor": "user:u_01 / endpoint:ep_01 / system",
  "action": "module.reload",
  "target": "module:mod_01",
  "before": { "state": "active", "version": "1.0.0" },
  "after": { "state": "active", "version": "1.0.0" },
  "at": "2026-11-19T10:00:00Z",
  "ip": "10.0.0.5",
  "traceId": "01HX..."
}
```

规定：

1. 追加写，不允许更新/删除。
2. 必须覆盖：登录、Token 创建/吊销/轮换、端点注册/删除、twin 下发、模块部署/启停/重载、包上传/灰度、配置变更、角色变更。
3. `before/after` 中的秘密字段必须脱敏。
4. 默认保留 90 天；Enterprise 可配置更长与导出。

## 6. v1 控制台形态

参考实现采用 **Thymeleaf 服务端渲染**，零前端构建，只做四页：

- 登录；
- 端点列表 + 详情（twin 差异、模块状态）；
- 模块启停/重载；
- 审计列表。

包上传、灰度、用户管理与多租户只提供 OpenAPI，不进入 v1 页面。SPA 由后续版本或第三方实现。
