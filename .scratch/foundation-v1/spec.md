# Foundation v1 — Spec

> 目标 issue 标题：`[Foundation] v1：插件化数字底座最小闭环`
> 追踪：Gitee Issues（网页创建，标签 `ready-for-agent`）

## Problem Statement

ddns4j 目前是 Java 8 + Spring Boot 2.7 的单体：DNS 服务商、调度、存储全部硬编码在内核，新增能力要改主程序；同时没有端点概念，路由器/NAS 用户无法被统一纳管。团队想转向"底座 + 应用包"的结构，但没有证据证明三件事：插件能否安全卸载（可逆副作用）、端点能否自治运行、期望状态与实际状态能否闭环。缺少这份证据时，任何大规模重构都可能白做。

## Solution

构建 Foundation：一个语言无关的插件化数字底座。内核负责插件装配、生命周期、服务解析与治理；ddns4j 作为第一个 App（应用包）接入。v1 用最小闭环证明契约成立：一个宿主、一个 `edge` Endpoint、一个 demo 插件、一个 App，跑通"部署 → 下发 → 应用 → 回报 → 漂移可见"。

## User Stories

1. 作为平台维护者，我希望插件通过同一份 manifest 描述，这样服务端与端点能力不需要两套扩展机制。
2. 作为平台维护者，我希望插件卸载时自动回滚副作用，这样热重载不会泄漏线程、监听器或缓存。
3. 作为平台维护者，我希望依赖用 `requires` 声明，这样启动顺序由依赖图决定而不是人工编排。
4. 作为平台维护者，我希望依赖消失时下游自动停用，这样不会出现"依赖已死、插件还活着"的幽灵状态。
5. 作为平台维护者，我希望同一服务可以被替换，这样存储、调度、通知都能插件化。
6. 作为平台维护者，我希望作用域支持隔离，这样未来多租户可以各用一份服务。
7. 作为平台维护者，我希望配置可以拦截覆盖，这样不改插件代码就能调整行为。
8. 作为运维人员，我希望在控制台看到所有 Endpoint 的在线状态，这样能快速定位失联设备。
9. 作为运维人员，我希望把 Plugin 部署为宿主或端点上的 Module，这样同一份包可以复用在不同位置。
10. 作为运维人员，我希望启停或重载 Module 后立刻看到服务是否恢复，这样能判断回滚是否成功。
11. 作为运维人员，我希望看到每个 Module 的 Effect 树，这样能知道卸载时什么会被撤销。
12. 作为运维人员，我希望 Endpoint 断开后仍按最后一次配置运行，这样控制面故障不影响业务。
13. 作为运维人员，我希望恢复连接后自动对齐 revision，这样不需要手工重新下发。
14. 作为运维人员，我希望控制台显示 desired 与 reported 的差异，这样能发现"发了但没生效"。
15. 作为运维人员，我希望看到最后一次错误，这样漂移有可定位的原因。
16. 作为安全负责人，我希望平台不保存云厂商凭据，这样一次泄露不会波及全部用户。
17. 作为安全负责人，我希望凭据可以一次性交付给在线端点，这样新设备不必人工登录配置。
18. 作为安全负责人，我希望交付过的凭据不能重复获取，这样抓包重放拿不到明文。
19. 作为安全负责人，我希望审计记录"谁向哪个模块交付了哪类凭据"，但不记录值。
20. 作为端点开发者，我希望用 CLI 完成 enroll、拉 twin、上报，这样调试不需要写脚本。
21. 作为端点开发者，我希望协议只有 HTTP/JSON，这样任何语言都能实现。
22. 作为端点开发者，我希望包按平台/架构分发，这样边缘设备能选到正确产物。
23. 作为端点开发者，我希望升级校验 sha256，这样下载损坏时不会替换掉可用版本。
24. 作为应用开发者，我希望 App 声明包含的模块与页面，这样安装一次就能得到完整能力。
25. 作为应用开发者，我希望只依赖 `foundation/api`，这样不用绑定内核实现。
26. 作为产品负责人，我希望 v1 只做一个 `edge` profile，这样不会被未验证的桌面/移动需求拖平。
27. 作为产品负责人，我希望 0.x 不承诺 API 稳定，这样还能根据真实使用调整契约。
28. 作为产品负责人，我希望第二个应用候选是"设备巡检与告警"，这样可以验证内核是否真的与 DNS 解耦。

## Implementation Decisions

- 定位：通用插件平台；ddns4j 只是第一个 App。内核禁止出现 `ddns4j` 或 DNS 业务词汇。
- 命名：`Endpoint` 取代 `Node`；profile 为 `edge`/`desktop`/`mobile`/`relay`，v1 只实现 `edge`。
- 模型：Plugin 是可分发的包；Module 是部署单元；App 是聚合型 Plugin，声明 modules 与 pages。
- 状态：Endpoint 与 Module 都有 desired/reported Twin；写入用 `PATCH` + JSON Merge Patch + `If-Match`；`config` 递归合并，`modules` 数组整体替换。
- 生命周期：installed → resolved → active → stopped → uninstalled（另有 failed）；依赖不齐备停在 resolved；重载是卸载后重建。
- 副作用：所有登记返回 `Disposable`，卸载 LIFO 回滚，幂等、可异步；`getEffects()` 提供诊断树。
- 服务：`provide/get/set`，所有权校验；Service 只在进程内可见。
- 依赖：`requires` 为 AND 语义；变化触发重算；`get()` 是逃生口，需声明 `escapeHatch`。
- 作用域：`extend`/`isolate`/`intercept` 进入规范；`isolate` 实现推迟到 Enterprise 多租户。
- 调度：周期任务由 Endpoint 本地执行；宿主只下发 desired 与一次性命令。
- 跨端：只走 Endpoint 协议（register/heartbeat/twin/command/report/upgrade），不做跨进程服务调用。
- 凭据：BYOC；一次性交付单次有效、60s TTL、仅内存、不落库不落日志；同时支持本地配置。
- 分发：包 manifest 含 sha256、size、channel、platform；Core 校验 sha256、签名可选，Enterprise 强制签名；platform 用 `os-arch` 匹配。
- 控制台：Thymeleaf 四页（登录/端点/模块/审计）；包上传、灰度、用户与租户管理只提供 API。
- 版本：契约 v1；产品 0.x 不承诺 API 稳定，1.0 才承诺。
- 技术基线：Java 21 LTS + Spring Boot 4.x + PF4J 3.16.x；工作区自带 `tools/jdk21` 与 `tools/mvn21.ps1`。
- 许可：Apache-2.0 + CLA。

## Testing Decisions

- 只测外部行为：HTTP 状态与响应体、CLI 退出码、Twin 差异、Effect 树是否为空；不测私有方法。
- 宿主测试：插件发现→注册→部署 Module→调用路由→停用→恢复；连续 100 次 reload 后 `getEffects()` 为空。
- 端点测试：enroll→register→heartbeat→拉 twin→回报；相同 Idempotency-Key 只入库一次；阻断回报后漂移可见。
- 凭据测试：在线端点交付一次成功、第二次 409、离线 409；数据库与日志扫描不到明文。
- 控制台测试：四页可访问；端点详情显示 desired/reported 差异。
- 构建测试：一条脚本产出 host、插件、CLI；`java -jar` 可启动；OpenAPI 与三份 JSON Schema 解析通过。
- 基线：`docs/baseline.md` 记录启动时间、reload 耗时、twin 往返、产物大小。

## Out of Scope

- 真实 DNS 调用与云厂商凭据；v1.1 只接阿里云一个。
- desktop/mobile/relay 的实现；端点 Module 的实际执行。
- 多端点批量下发与灰度 UI。
- 用户/角色/租户管理页面与完整 RBAC。
- 制品库、强制签名、命令下发与升级（除契约占位）。
- No-IP/DuckDNS 兼容；CLI 的管理侧命令。

## Further Notes

- 旧方案 `ddns4j-architecture-v2.md` 与 P0 manifest 已作废；差异见 `docs/07-decisions.md`。
- 术语以 `GLOSSARY.md` 为准；决策记录见 `docs/adr/`。
- v1 完成的判定以 `docs/06-minimal-loop.md` 的 A1–A16 为准。
