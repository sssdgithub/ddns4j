# 01 — 概览

## 定位

Foundation 是一个语言无关的插件化数字底座：内核负责装配、生命周期、服务解析与治理；业务能力以 Plugin / Module / App 接入。ddns4j 是第一个 App，不是平台本身。

## 原则

1. **一切皆插件**：服务端能力、控制台页面、端点能力共用一份 manifest。
2. **包与部署分离**：Plugin 是可分发的包，Module 是宿主或 Endpoint 上的部署单元。
3. **副作用可逆**：所有登记返回 `Disposable`，卸载按 LIFO 回滚。
4. **依赖声明式**：`requires` 齐备才激活，变化自动重算。
5. **内核最小**：内核不含业务概念；业务只在 App。
6. **能力优先**：按 Service/Capability 查找，不 import 实现。
7. **Endpoint 统一**：边缘/桌面/移动/中继都是 Endpoint，差异由 `profile` 表达。
8. **双状态**：每个可下发对象都有 desired/reported Twin。
9. **规范先于实现**：契约用 HTTP/JSON/YAML，语言无关。
10. **默认安全**：不托管用户凭据；Endpoint 使用最小权限 Token。

## 核心术语

完整表见 [GLOSSARY.md](../GLOSSARY.md)。

| 术语 | 一句话 |
|---|---|
| Plugin | 可分发的包（manifest + 入口） |
| Module | Plugin 在宿主或 Endpoint 上的部署单元 |
| App | 聚合型 Plugin，声明模块、页面、默认配置 |
| Endpoint | 被纳管的端；profile = edge/desktop/mobile/relay |
| Twin | 同一对象的 desired 与 reported 两份文档 |
| Capability | 对外声明的一项能力，与实现解耦 |

> `Endpoint` 不是 Node.js；旧文档中的 `Node` 一律指 Endpoint。

## 分层

```
接入层  HTTP API · Endpoint 协议 · SDK · Webhook
治理层  身份 · 角色 · 租户 · 审计 · 配额
控制面  控制台 · App/Module/Plugin/Package · 期望状态下发
内核    Contract · Runtime · Scope · Assembly
```

依赖只允许自上而下；内核不得引用控制面与治理层类型。

## v1 能力面

| 能力 | v1 |
|---|---|
| 插件运行时 | 统一 manifest、生命周期、effect、inject、作用域语义 |
| 部署 | 宿主 Module 可执行；Endpoint Module 仅契约 |
| Endpoint | 只实现 `edge`；register/heartbeat/twin/report |
| 控制台 | Thymeleaf 四页：登录 / 端点 / 模块 / 审计 |
| 治理 | 单管理员 + API Token + 审计；RBAC/多租户留 Enterprise |
| 分发 | 本地目录 + sha256；签名可选；制品库/灰度留 Enterprise |
| 业务 | 只做 demo 插件；真实 DNS 留 v1.1 |

## 边界（v1 不做）

1. 进程级插件隔离与恶意插件防护。
2. 分布式调度、多集群、多租户。
3. 计费、市场交易、内建 UI 组件库。
4. 托管用户云厂商凭据。
5. 桌面/移动/relay 的协议实现。
6. No-IP / DuckDNS 兼容。
7. 管理侧 CLI。

## 质量目标（建议值）

| 指标 | 目标 | 测量 |
|---|---|---|
| 内核冷启动 | ≤ 3s | `/readyz` 首次 200，3 次中位数 |
| 单个 Module 激活 | ≤ 200ms | 不含自身初始化 |
| 卸载回滚 | 100 次重载无残留 effect | `context.getEffects()` 为空 |
| 控制面 API P95 | ≤ 200ms | 本地单机 |
| Endpoint 心跳 | 30s ± 20% | 离线判定 3 周期 |

实测统一写入 [baseline.md](./baseline.md)。

## 兼容性

- 契约版本 `foundation/v1`；manifest 必填 `apiVersion`，主版本不匹配拒绝加载。
- 产品版本 `0.x` 不承诺 API 稳定，`1.0` 才承诺。
- Endpoint 协议通过 `X-Foundation-Protocol` 协商；新增字段向后兼容，客户端忽略未知字段。
