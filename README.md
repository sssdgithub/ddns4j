# Foundation — 语言无关的插件化数字底座

> 状态：v1 规范（完整草案，待评审）
> 分支：`feat/foundation`
> 参考实现：Java 21（LTS） + Spring Boot 4.x + PF4J 3.16.x
> 许可证：Apache-2.0 + CLA

## 这是什么

Foundation 是一个通用插件平台：内核只负责**装配、生命周期、服务解析与治理**，业务能力以插件/应用包的形式接入。ddns4j 是它的第一个应用包，不是平台本身。

五句话概括：

1. **一切皆插件**：服务端能力、控制台页面、端点能力都通过同一份 manifest 描述。
2. **插件是制品，模块是实例**：Plugin 是可分发的代码包；Module 是它部署在宿主或端点上的运行实例。
3. **一切副作用可回滚**：注册即副作用，卸载时逆序撤销。
4. **依赖声明式**：服务就绪才激活，依赖变化自动重算。
5. **端点统一建模**：边缘/桌面/移动/中继都是 Endpoint，差异由 `profile` 与 capability 表达；状态用 desired/reported 双状态描述。

## v1 范围

| 能力面 | v1 包含 |
|---|---|
| 插件运行时 | 统一契约、生命周期、可逆 effect、inject、作用域语义 |
| 控制台 | 端点/模块/应用/插件/包/审计/设置的 API 与极简管理页 |
| 身份与治理 | 用户、角色、租户、Token、审计事件 |
| 开放接入与分发 | OpenAPI、Endpoint 协议、包 manifest、签名与灰度 |

不包含：具体 DNS 业务实现、计费、AI 能力、多集群调度、插件市场交易。

## 目录

```
GLOSSARY.md                      术语表（Endpoint/Plugin/Module/Twin…）
docs/
  00-principles.md               定位、原则、术语、边界
  01-capability-map.md           能力面、分层、兼容性
  02-plugin-contract.md          统一插件契约（核心）
  03-endpoint-contract.md        Endpoint 模型、profile、模块、双状态与接入协议
  04-http-api.md                 HTTP API 约定与端点
  05-console-and-governance.md   控制台 IA、RBAC、多租户、审计
  06-package-and-distribution.md 包类型、manifest、签名、灰度
  07-minimal-loop.md             最小闭环与验收
  08-decisions.md                决策记录、假设、遗留问题
  adr/
    0001-foundation-is-independent.md
    0002-twin-and-endpoint-autonomy.md
    0003-credential-handoff.md
openapi/
  foundation-v1.yaml             机器可读 API
schemas/
  plugin-manifest.schema.json    插件/模块 manifest
  package-manifest.schema.json   分发包 manifest
  endpoint.schema.json           Endpoint、Module、Twin
```

## v1 收敛范围（拷问结论）

- 只实现 `edge` profile；desktop/mobile/relay 仅保留字段。
- 业务只做 demo 插件，不接真实 DNS。
- 验收 1 个端点；模型支持多端点，但不做批量 UI。
- 控制台用 Thymeleaf 四页；Core 只强制 sha256，签名可选。
- 周期任务由端点本地调度；跨端只走 Twin 与命令；凭据一次性下发、不落库。

## 阅读顺序

1. `00-principles.md`：边界与术语（先确认 Endpoint 不是 Node.js）。
2. `02-plugin-contract.md` + `03-endpoint-contract.md`：全部实现的基础。
3. `04`–`06`：API、控制台与治理、包分发。
4. `07`：最小闭环；`08`：取舍与遗留问题。

## 与旧方案的关系

`ddns4j-architecture-v2.md` 是"DDNS 控制面"方案（JDK17 + Spring Boot 3 + Node 概念）。本规范将其取代：改用 Java 21 + Spring Boot 4，Node 更名 Endpoint，DDNS 收敛为应用包。差异见 `08-decisions.md`。
