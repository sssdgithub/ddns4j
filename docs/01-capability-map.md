# 01 — 能力面、分层与兼容性

## 1. 能力地图

| 能力面 | 子能力 | v1 | 说明 |
|---|---|---|---|
| **插件运行时** | 发现与装配 | ✓ | 从包目录/manifest 加载 |
| | 生命周期状态机 | ✓ | installed → resolved → active → stopped → uninstalled |
| | 可逆 effect | ✓ | LIFO、幂等、异步等待 |
| | inject 依赖驱动 | ✓ | AND 语义、变化重算 |
| | 服务注册与解析 | ✓ | provide/get/set，所有权校验 |
| | 作用域 | ✓ 规范 | extend / isolate / intercept 语义 |
| | 模块部署 | ✓ | 宿主 Module 与端点 Module 共用部署语义 |
| | 多版本共存 | — | 同一 Plugin 只允许一个 active 版本 |
| **控制台** | 总览 | ✓ | 端点在线率、模块状态 |
| | 端点管理 | ✓ | 列表、详情、标签、Token 轮换 |
| | 模块与插件 | ✓ | 安装、启停、重载、配置 |
| | 应用 | ✓ | 安装、启停、升级 |
| | 包与版本 | ✓ | 上传、通道、灰度、回滚 |
| | 审计查询 | ✓ | 按 actor/target/时间过滤 |
| **治理** | 用户与角色 | ✓ | owner/admin/operator/viewer |
| | 租户 | ✓ 规范 | Core 单租户；Enterprise 多租户 |
| | Token 与 Scope | ✓ | Endpoint Token、API Token |
| | 审计事件 | ✓ | 追加写、不可改 |
| | 配额 | — | v2 |
| **接入与分发** | OpenAPI | ✓ | `/api/v1` |
| | Endpoint 协议 | ✓ | 注册、心跳、twin、配置、命令、升级 |
| | SDK | ✓ 规范 | HTTP 级约定；语言 SDK 后续 |
| | Webhook | 规范占位 | v1 不实现 |
| | 包签名与灰度 | 规范 ✓ / v1 仅 sha256 | Core 可选 Ed25519，Enterprise 强制；灰度属 Enterprise |

## 2. 版本分层

| 层 | 内容 |
|---|---|
| **Core（Apache-2.0）** | 内核、控制台、单租户、本地包目录、基础审计、Endpoint 协议 |
| **Enterprise（商业插件）** | 多租户、SSO/LDAP、RBAC 细化、集群/HA、灰度编排增强、制品库对接、合规报表、KMS |

Enterprise 能力以**服务端插件**形式提供，通过 `requires` 声明对 Core 服务的依赖，不修改内核。

## 3. Profile 与能力

| Profile | 典型载体 | 独有能力 | 默认 runtime |
|---|---|---|---|
| `edge` | 路由器、NAS、软路由、机器人主控 | OTA、离线队列、本地凭据、资源上限 | Rust（静态单文件） |
| `desktop` | Windows/macOS/Linux 客户端 | 用户会话、托盘、本地文件选择 | Tauri / 原生 |
| `mobile` | iOS/Android | 推送、后台限制、扫码 | 原生 / 跨端 |
| `relay` | 中继/网关 | 流量转发、连接保持 | Rust / Go |

Profile 只决定**允许出现的字段与策略**，不决定语言。任何 profile 都必须实现同一套 Endpoint 协议。

> v1 只实现 `edge`；`desktop` / `mobile` / `relay` 仅保留规范字段与扩展位，不写协议实现、不做客户端。

## 4. 兼容性

- 契约版本：`foundation/v1`；manifest 必填 `apiVersion`。
- 宿主拒绝加载 `apiVersion` 主版本不匹配的插件。
- Plugin 版本遵循 SemVer；manifest 记录 `minHost` / `maxHost`。
- Endpoint 协议版本独立演进，通过 `X-Foundation-Protocol` 协商。
- Twin 文档带 `schemaVersion`；未知字段必须忽略，不得报错。

## 5. 关键约束

1. 内核不得依赖任何业务包名（如 `ddns4j.*`）。
2. 应用包只能依赖 `foundation/api`，不得直接依赖内核实现类。
3. 所有服务必须由模块 `provide`；内核只提供最小基础服务（配置、时钟、事件、日志）；HTTP 客户端与密钥读取由基础插件提供。
4. 控制台页面必须由 manifest 声明，不允许硬编码在宿主里。
5. 所有可下发对象必须有 desired/reported 两份状态。
