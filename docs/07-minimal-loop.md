# 07 — 最小闭环与验收

## 1. 目标

用最小代价证明三件事：

1. 统一契约能同时承载**宿主模块**与**端点模块**；
2. 可逆 effect + inject 在真实进程里成立（重载不泄漏、依赖变化能级联）；
3. Endpoint 的 desired/reported 双状态能闭环（下发 → 应用 → 回报 → 漂移可算）。

## 2. 组成

| 组件 | 形态 | 说明 |
|---|---|---|
| `foundation-host` | Java 21 + Spring Boot 4 + PF4J | 内核、REST API、极简管理页 |
| `plugin-demo-greeting` | 服务端插件 jar | `provides: demo.greeting`，一个 HTTP 端点 |
| `endpoint-sim` | Java 21 CLI（测试替身） | 实现 enroll / heartbeat / twin / report |
| `app-ddns4j-lite` | app-bundle manifest | 引用 `plugin-demo-greeting` + 一个页面 |

> 端点侧的正式实现（Rust）不在本期；`endpoint-sim` 只用于验证协议，不代表边缘技术选型。

## 3. 场景

```
1. 启动 host → /readyz 200
2. host 发现 plugin-demo-greeting → 部署为宿主 Module → ACTIVE
3. GET /api/v1/demo/greeting → 200 + 插件返回值
4. 创建 Enroll Token → endpoint-sim enroll → 获得 endpointId + token
5. endpoint-sim heartbeat → 返回 desiredRevision=1
6. endpoint-sim GET /endpoint/v1/twin → 拿到 desired
7. endpoint-sim 应用后 POST /twin/report → reported.revision=1
8. 控制台 PUT twin（revision 2）→ endpoint-sim 拉取 → 回报 revision 2
9. 控制台 stop module → /api/v1/demo/greeting 返回 503
10. 控制台 start module → 恢复 200；连续 reload 100 次仍稳定
```

## 4. 验收清单

| # | 验收项 | 判定 |
|---|---|---|
| A1 | host 冷启动到 `/readyz` 200 | 3 次中位数 ≤ 3s |
| A2 | 插件被发现并 ACTIVE | `/api/v1/plugins` 与 `/api/v1/modules` 可见 |
| A3 | greeting 端点可用 | 返回插件定义值，宿主无同名字符串常量 |
| A4 | 模块 stop 后服务消失 | 返回 503，`provides` 不再可解析 |
| A5 | 模块 start 后恢复 | 返回 200 |
| A6 | 连续 100 次 reload | 全部成功；无 effect 残留；无不稳定错误 |
| A7 | endpoint enroll | 返回 endpointId + token，重复 enroll 幂等 |
| A8 | heartbeat | 返回 desiredRevision 与 serverTime |
| A9 | twin 下发与回报 | desired.revision 递增；reported.revision 跟上；漂移为 0 |
| A10 | 上报去重 | 相同 `Idempotency-Key` 只入库一次 |
| A11 | 极简页可用 | 登录后能看到端点列表与模块状态 |
| A12 | 构建 | `mvn -q package` 与 `java -jar` 全部通过 |
| A13 | 凭据一次性交付 | 端点在线时可交付一次；重复交付被拒；服务端数据库与日志中查不到明文；端点离线返回 409 |

## 5. 明确不做

- 真实 DNS 调用、真实云厂商凭据（只用 demo 密钥验证交付路径）；
- 完整 RBAC/多租户（只用单一管理员账号）；
- 制品库、强制签名、灰度（v1 只校验 sha256）；
- Rust 端点实现、桌面/移动客户端（三个 profile 只保留字段）；
- 多端点批量下发与 UI（模型支持，但只验收 1 个）；
- 命令下发与升级。

## 6. 运行步骤（参考）

```bash
# 1. 准备 JDK21 与 Maven
java -version   # 21.x
mvn -v          # Java 21

# 2. 构建
mvn -q -f reference/host/pom.xml package
mvn -q -f reference/plugins/demo-greeting/pom.xml package

# 3. 启动
java -jar reference/host/target/foundation-host.jar

# 4. 验证
curl -s localhost:8080/readyz
curl -s localhost:8080/api/v1/demo/greeting
```

## 7. 记录

实测值写入 `docs/baseline.md`：启动时间、reload 次数与耗时、twin 往返耗时、构建产物大小。

## 8. 通过后的下一步

- 引入 Rust `edge` 端点包（enroll/heartbeat/twin/report 四接口）；
- 接入 ddns4j 应用包（一个真实 DNS 插件 + 一个 edge 模块）；
- 再把制品库/签名/灰度纳入。
