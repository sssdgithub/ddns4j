# 08 — 决策、假设与遗留问题

## 1. 已定决策

| # | 决策 | 结论 | 理由 |
|---|---|---|---|
| D1 | 定位 | 通用插件平台，ddns4j 是第一个应用包 | 避免平台被单一业务绑架 |
| D2 | 规范形态 | 语言无关（HTTP/JSON/YAML），参考实现 Java | 保留多语言生态 |
| D3 | 参考实现 | Java 21 LTS + Spring Boot 4.x + PF4J 3.16.x | 长期维护版 + 稳定框架 |
| D4 | 插件模型 | Plugin 制品 + Module 实例 | 升级/回滚/多实例可表达 |
| D5 | 契约 | 服务端与端点共用 manifest/capability/twin 语义，双 adapter | 避免第二套扩展机制 |
| D6 | 端点命名 | Endpoint（不是 Node） | 避开 Node.js 歧义 |
| D7 | 端点模型 | Endpoint + profile + capability + module | 对齐 AWS/Azure/KubeEdge/Intune |
| D8 | 状态模型 | desired/reported 双状态 | 能回答"实际应用了什么" |
| D9 | 作用域 | extend/isolate/intercept 纳入规范；Java 用 Spring 父子容器实现 | 多租户/多实例需要 |
| D10 | 依赖与生命周期 | 自研薄 Kernel（effect/inject）+ PF4J 只做类加载 | PF4J 不提供可逆 effect |
| D11 | 控制台 | v1 = OpenAPI + 极简管理页 | 先跑通能力，不引入前端工程 |
| D12 | 凭据 | BYOC，不托管 | 安全责任最小 |
| D13 | 分发 | Core 本地目录；Enterprise 接制品库 | 兼顾开箱即用与企业治理 |
| D14 | 许可 | Apache-2.0 + CLA | 企业友好，保留双许可可能 |
| D15 | 仓库 | 新目录 `e:\self\foundation`，`git init`，分支 `feat/foundation` | 不拉旧代码，从零写 |
| D16 | 平台独立性 | 独立平台、独立版本线；ddns4j 只做验证应用 | 见 ADR-0001 |
| D17 | v1 profile | 只实现 `edge`；desktop/mobile/relay 仅保留规范字段 | 唯一有真实载体的是 edge |
| D18 | v1 业务深度 | 只做 demo 插件，不接真实 DNS | 验证底座，不验证 DNS |
| D19 | Twin 写入语义 | JSON Merge Patch（RFC 7386）+ `If-Match`；`config` 递归合并，`modules` 数组整体替换 | 避免双方各实现一套合并 |
| D20 | 调度归属 | 端点按 desired 本地调度；宿主只下发配置与一次性命令 | 断网可自转，控制面不做单点 |
| D21 | 跨端边界 | Service 只在进程内可见；跨端只走 Twin 与命令 | 避免自建 RPC |
| D22 | 凭据下发 | 一次性：单次有效、60s TTL、仅内存、不落库不落日志；同时支持本地配置 | 见 ADR-0003 |
| D23 | 包签名 | Core 强制 sha256、签名可选；Enterprise 强制签名 | 降低个人用户门槛 |
| D24 | 控制台技术 | Thymeleaf 服务端渲染，v1 只做登录/端点/模块/审计四页 | 零前端构建 |
| D25 | v1 端点数 | 模型支持多端点，验收只跑 1 个，不做批量 UI | 不把数据模型写死 |
| D26 | 维护假设 | 按"1 人、非全职"裁剪 v1 | 见 ASM-A7 |
| D27 | 版本策略 | `0.x` 不承诺 API 稳定，破坏性变更记 CHANGELOG；`1.0` 才承诺 | 平台仍在找第二个应用 |
| D28 | 第二个应用 | 候选为"设备巡检与告警"：edge 定时探活 + 通知，不碰 DNS | 验证内核是否与业务解耦 |
| D29 | `isolate` 时机 | 只在 Enterprise 多租户时实现；v1 保留接口与单实例路径 | 没有真实多实例场景 |
| D30 | 旧协议兼容 | 不兼容 No-IP / DuckDNS；只支持自有 Endpoint 协议 | 兼容属获客手段，不是底座能力 |
| D31 | ddns4j 首批服务商 | v1.1 只接阿里云一个，纵向打通全链路 | 一个就能暴露凭据/限流/重试/漂移问题 |
| D32 | CLI | 只做端点侧 CLI（enroll / twin / report）；管理侧不做 | 端点调试刚需，管理侧有页面 |

## 2. 已知假设

| # | 假设 | 影响 |
|---|---|---|
| A1 | Java 21 与 Maven 在当前机器未安装，实施前需安装 | 不安装则最小闭环 BLOCKED |
| A2 | Spring Boot 4.0.x 要求 Java 17+，Java 21 可用；PF4J 运行时 Java 8+ | 官方文档已确认，无技术阻塞 |
| A3 | Edge 默认实现为 Rust（≤8MB） | 若体积不达标，需裁剪服务商或改方案 |
| A4 | `profile` 只决定字段与策略，不决定语言 | 若未来出现强绑定，需扩展规范 |
| A5 | 同一 Plugin 只允许一个 active 版本 | 多版本共存留到 v2 |
| A6 | 审计保留 90 天为默认值 | 合规场景需 Enterprise 配置 |
| A7 | 维护力量为 1 人、非全职 | v1 只保留最小闭环，其余降级为规范占位 |
| A8 | 一次性凭据下发要求端点在线 | 离线端点返回冲突，不排队保存 |

## 3. 与旧方案的关系

旧 `ddns4j-architecture-v2.md` 与 `manifest-20261119-ddns4j-p0.md`：

- 技术基线 JDK17 + Spring Boot 3：**作废**，改为 Java 21 + Boot 4；
- 概念 `Node`：**更名** `Endpoint`；
- `configRevision`：**升级**为 desired/reported twin；
- PF4J 热重载验证：保留为最小闭环的 A6 项，但不再单独作为 P0。

## 4. 遗留问题（已关闭）

| 原问题 | 结论 |
|---|---|
| 版本号与兼容承诺 | D27：0.x 不承诺，1.0 才承诺 |
| 第二个应用是什么 | D28：设备巡检与告警（候选） |
| `isolate` 实现时机 | D29：Enterprise 多租户时 |
| 是否兼容 No-IP / DuckDNS | D30：不做 |
| ddns4j 首批服务商数量 | D31：先接阿里云一个 |
| 是否需要 CLI | D32：只做端点侧 CLI |

> 本节不再新增问题；后续出现新的开放问题，另开"开放问题"小节，不回溯本表。

## 5. 风险

| 风险 | 说明 | 缓解 |
|---|---|---|
| 规范过度设计 | 平台能力面过大，实施拖延 | v1 只保留"一个宿主 + 一个 edge + 一个应用包" |
| 平台独立性缺少验证 | 只有 ddns4j 一个应用，通用性未证 | v1 后尽快引入第二个应用或明确候选 |
| 端点自治导致观测滞后 | 控制面看到的是 reported，可能落后于 desired | 漂移视图 + 最后错误 + 心跳 |
| 一次性下发依赖在线 | 端点离线无法收凭据 | 明确报错，提供本地配置兜底 |
| 作用域实现复杂 | isolate 需要多实例 registry | v1 只做接口与单租户路径 |
| Boot 4 稳定性 | 新主版本生态适配未知 | 参考实现先跑最小闭环，不引入重依赖 |
| 端点协议被滥用 | 公网 enroll/report 可能被刷 | 一次性 Token、频控、租户配额 |
