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
| D7 | 端点模型 | Endpoint + profile(edge/desktop/mobile/relay) + capability + module | 对齐 AWS/Azure/KubeEdge/Intune |
| D8 | 状态模型 | desired/reported 双状态 | 能回答"实际应用了什么" |
| D9 | 作用域 | extend/isolate/intercept 纳入规范；Java 用 Spring 父子容器实现 | 多租户/多实例需要 |
| D10 | 依赖与生命周期 | 自研薄 Kernel（effect/inject）+ PF4J 只做类加载 | PF4J 不提供可逆 effect |
| D11 | 控制台 | v1 = OpenAPI + 极简管理页 | 先跑通能力，不引入前端工程 |
| D12 | 凭据 | BYOC，不托管 | 安全责任最小 |
| D13 | 分发 | Core 本地目录；Enterprise 接制品库 | 兼顾开箱即用与企业治理 |
| D14 | 许可 | Apache-2.0 + CLA | 企业友好，保留双许可可能 |
| D15 | 仓库 | 新目录 `e:\self\foundation`，`git init`，分支 `feat/foundation` | 不拉旧代码，从零写 |

## 2. 已知假设

| # | 假设 | 影响 |
|---|---|---|
| A1 | Java 21 与 Maven 在当前机器未安装，实施前需安装 | 不安装则最小闭环 BLOCKED |
| A2 | Spring Boot 4.x 与 PF4J 3.16 可共存（PF4J 不依赖 Spring） | 若不兼容，需降级 Boot 或隔离类加载 |
| A3 | Edge 默认实现为 Rust（≤8MB） | 若体积不达标，需裁剪服务商或改方案 |
| A4 | `profile` 只决定字段与策略，不决定语言 | 若未来出现强绑定，需扩展规范 |
| A5 | 同一 Plugin 只允许一个 active 版本 | 多版本共存留到 v2 |
| A6 | 审计保留 90 天为默认值 | 合规场景需 Enterprise 配置 |

## 3. 与旧方案的关系

旧 `ddns4j-architecture-v2.md` 与 `manifest-20261119-ddns4j-p0.md`：

- 技术基线 JDK17 + Spring Boot 3：**作废**，改为 Java 21 + Boot 4；
- 概念 `Node`：**更名** `Endpoint`；
- `configRevision`：**升级**为 desired/reported twin；
- PF4J 热重载验证：保留为最小闭环的 A6 项，但不再单独作为 P0。

## 4. 遗留问题（下一轮讨论）

1. **Endpoint 协议是否要兼容现有生态**（No-IP `/nic/update`、DuckDNS 风格）？兼容会增加一个 ingress adapter。
2. **多租户在 Core 是否完全不实现**，还是保留 `tenantId` 字段但不暴露 UI？
3. **模块的调度归属**：定时任务由宿主模块提供，还是由端点本地执行？影响离线行为。
4. **Twin 的合并语义**：整体替换还是按 key 合并（Azure 的 desired properties 是合并）？
5. **作用域的 Java 实现细节**：Spring 父子容器能否满足 `isolate` 的"同服务多实例"，还是需要自研 registry？
6. **包签名是否 v1 强制**：Core 默认警告、Enterprise 强制，是否足够？
7. **控制台极简页的技术选型**：Thymeleaf / HTMX / 纯静态 + API。
8. **ddns4j 应用包的首批模块清单**：4 个 DNS 服务商是否都进 v1。

## 5. 风险

| 风险 | 说明 | 缓解 |
|---|---|---|
| 规范过度设计 | 平台能力面过大，实施拖延 | 最小闭环只覆盖 A1–A12 |
| Java 生态与 Cordis 语义错位 | 显式 Context 传递导致插件 API 冗长 | Java adapter 内部用 Spring 容器，插件只依赖 `foundation/api` |
| 作用域实现复杂 | isolate 需要多实例 registry | v1 只做接口与单租户路径，EE 再落多实例 |
| Boot 4 稳定性 | 新主版本生态适配未知 | 参考实现先跑最小闭环，不引入重依赖 |
| 端点协议被滥用 | 公网 enroll/report 可能被刷 | 一次性 Token、频控、租户配额 |
