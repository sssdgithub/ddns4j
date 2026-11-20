# 07 — 最小闭环与验收

## 1. 目标

用最小代价证明三件事：

1. 统一 manifest/契约能同时**描述**宿主 Module 与端点 Module；v1 只**执行**宿主 Module，端点 Module 留到 v1.1。
2. 可逆 effect + inject 在真实进程里成立（重载不泄漏、依赖变化能级联）。
3. Endpoint 的 desired/reported 双状态能闭环（下发 → 应用 → 回报 → 漂移可算）。

## 2. 组成

| 组件 | 形态 | 说明 |
|---|---|---|
| `foundation-host` | Java 21 + Spring Boot 4 + PF4J | 内核、REST API、四页极简管理页 |
| `plugin-demo-greeting` | 服务端插件 jar | `provides: demo.greeting`，注册一个 HTTP 路由 |
| `endpoint-cli` | Java 21 CLI（测试替身） | 实现 enroll / register / heartbeat / twin / report；CLI 暴露 enroll / twin / report 三个子命令 |
| `app-ddns4j-lite` | app-bundle manifest | 引用 `plugin-demo-greeting` + 一个页面 |

> 端点侧的正式实现（Rust）不在本期；`endpoint-cli` 只用于验证协议，不代表边缘技术选型。

## 3. 场景

```
1. 启动 host → /readyz 200
2. host 扫描 `foundation.plugins.dir`（默认 `./plugins`）→ 注册 Plugin；POST /api/v1/modules（target.host=true，config.greeting="hello"）→ 宿主 Module ACTIVE
3. GET /api/v1/demo/greeting → 200 + 插件返回值（值来自模块 config）
4. 创建 Enroll Token → endpoint-cli enroll → endpointId + token
5. endpoint-cli register → 上报 profile=edge / platform / version / capabilities
6. endpoint-cli heartbeat → 返回 desiredRevision=1（端点创建时 desired 初始化为 revision 1）
7. endpoint-cli GET /endpoint/v1/twin → 拿到 desired
8. endpoint-cli 应用后 POST /twin/report → reported.revision=1
9. 控制台 PATCH twin（If-Match=1）→ revision 2 → endpoint-cli 拉取 → 回报 revision 2
10. 控制台 stop module → /api/v1/demo/greeting 返回 503 + MODULE_INACTIVE
11. 控制台 start module → 恢复 200；连续 reload 100 次仍稳定
12. 控制台打开四页：登录 / 端点 / 模块 / 审计
```

## 4. 验收清单

| # | 验收项 | 判定 |
|---|---|---|
| A1 | host 冷启动到 `/readyz` 200 | 同一台机器、JDK21、4C/8G 下 3 次中位数 ≤ 3s；记录每次原始值 |
| A2 | 插件被发现并 ACTIVE | `/api/v1/plugins` 与 `/api/v1/modules` 可见，且扫描来源为配置的本地插件目录 |
| A3 | greeting 端点可用 | 返回模块 config 中的值，宿主无同名字符串常量 |
| A4 | 模块 stop 后服务消失 | 返回 503 + `MODULE_INACTIVE`；`provides` 不再可解析 |
| A5 | 模块 start 后恢复 | 返回 200 |
| A6 | 连续 100 次 reload | 全部成功；每次后 `context.getEffects()` 为空；无 ERROR 日志 |
| A7 | endpoint enroll | 返回 endpointId + token；同一 Token 在有效期内重复 enroll 返回同一 endpointId，过期后 401 |
| A8 | endpoint register | 上报后 `/api/v1/endpoints/{id}` 能查到 profile、platform、version、capabilities |
| A9 | heartbeat | 返回 desiredRevision 与 serverTime |
| A10 | twin 下发与回报 | 初始 desired.revision=1；PATCH 后递增；reported.revision 跟上；漂移为 0 |
| A11 | 上报去重 | `/endpoint/v1/report` 相同 `Idempotency-Key` 只入库一次 |
| A12 | 四页可用 | 登录、端点列表/详情、模块启停、审计列表均可访问；端点详情显示 desired/reported 差异 |
| A13 | 构建 | 一条构建脚本产出 host、插件、CLI；`java -jar` 可启动 |
| A14 | 凭据一次性交付 | 端点在线时可交付一次；重复交付 409；数据库与日志查不到明文；端点离线 409 |
| A15 | 端点 CLI | `enroll` / `twin` / `report` 可独立执行；退出码：0 成功、1 网络/服务端错误、2 参数错误、3 本地状态错误 |
| A16 | 漂移可算 | 阻断一次 `/endpoint/v1/twin/report` 回报后，控制台显示 desired ≠ reported 与最后错误 |

## 5. 明确不做

- 真实 DNS 调用、真实云厂商凭据（只用 demo 密钥验证交付路径）；
- 完整 RBAC/多租户（只用单一管理员账号）；
- 制品库、强制签名、灰度（v1 只校验 sha256）；
- Rust 端点实现、桌面/移动客户端（三个 profile 只保留字段）；
- 端点 Module 的实际执行（v1 只验证宿主 Module，端点 Module 留到 v1.1）；
- 多端点批量下发与 UI（模型支持，但只验收 1 个）；
- 命令下发与升级。

## 6. 运行步骤（参考）

> `reference/` 为 v1 实施时创建的参考实现目录，当前仓库尚未生成。

```bash
# 1. 准备 JDK21 与 Maven（工作区已带 tools/jdk21）
.\tools\mvn21.ps1 -v

# 2. 构建
.\tools\mvn21.ps1 -q -f reference/pom.xml package

# 3. 启动
.\tools\jdk21\bin\java.exe -jar reference/host/target/foundation-host.jar

# 4. 验证
curl -s localhost:8080/readyz
curl -s localhost:8080/api/v1/demo/greeting
```

## 7. 记录

实测值写入 `docs/baseline.md`：JDK 版本、机器规格、启动时间（3 次）、100 次 reload 总耗时、twin 往返耗时、构建产物大小。

## 8. 通过后的下一步

- 引入 Rust `edge` 端点包（enroll/register/heartbeat/twin/report）；
- 接入 ddns4j 应用包（v1.1 先接阿里云一个服务商）；
- 再把制品库/签名/灰度纳入。
