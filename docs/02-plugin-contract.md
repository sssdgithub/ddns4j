# 02 — 统一插件契约

> 服务端插件、端点模块、应用包遵循同一套 manifest 与生命周期语义；差异只在 runtime adapter。

## 1. 包与部署单元

| 概念 | 定义 | 例子 |
|---|---|---|
| **Plugin** | 可分发的包（manifest + 入口 + 依赖） | `foundation.dns.aliyun` 1.0.0 |
| **Module** | Plugin 在宿主或某个 Endpoint 上的一次部署单元 | 宿主上的 `aliyun-1`；endpoint `ep_01` 上的 `dns-1` |
| **App** | 聚合型 Plugin，声明包含哪些模块与页面 | `ddns4j` |

规则：

- 同一 Plugin 可部署为多个 Module（不同端点、不同租户）；
- Module 有自己的 `config` 与 desired/reported twin；
- 升级 Plugin 是换制品；升级 Module 是换实例版本，两者都留审计。

## 2. Manifest

每个插件根目录包含 `plugin.yaml`（或 `plugin.json`）。

```yaml
apiVersion: foundation/v1
id: foundation.dns.aliyun
name: Aliyun DNS Provider
version: 1.0.0
kind: server                  # server | endpoint | app
runtime:
  type: java                  # java | rust-native | tauri | shell | wasm
  entry: top.foundation.dns.aliyun.AliyunPlugin
provides:
  - dns.provider:aliyun
requires:
  - foundation.http.client
  - foundation.secret.reader
capabilities:
  - id: dns.update
    recordTypes: [A, AAAA]
    credentialFields:
      - { key: accessKeyId, label: AccessKey ID, required: true }
      - { key: accessKeySecret, label: AccessKey Secret, secret: true, required: true }
configSchema: ./config.schema.json
twin:
  desiredSchema: ./twin.desired.schema.json
  reportedSchema: ./twin.reported.schema.json
profiles: [edge, desktop]     # kind=endpoint 时声明可部署的 profile
compatibility:
  minHost: 0.1.0
  maxHost: 0.x
  apiVersion: foundation/v1
```

| 字段 | 必填 | 说明 |
|---|---|---|
| `apiVersion` | 是 | 契约版本，主版本不匹配拒绝加载 |
| `id` | 是 | 全局唯一，反向域名风格 |
| `version` | 是 | SemVer |
| `kind` | 是 | `server`（宿主）、`endpoint`（端上）、`app`（聚合） |
| `runtime` | 条件 | `server` / `endpoint` 必填；`app` 可省略 |
| `provides` / `requires` | 否 | 服务提供与依赖，AND 语义 |
| `capabilities` | 否 | 能力描述，驱动控制台与匹配 |
| `configSchema` | 否 | JSON Schema 2020-12 |
| `twin` | 否 | desired/reported 的 Schema |
| `profiles` | 条件 | `kind=endpoint` 必填 |
| `pages` / `modules` | 条件 | `kind=app` 必填，声明应用包含的页面与模块 |
| `compatibility` | 条件 | `server` / `endpoint` 必填；`app` 可省略 |

## 3. 生命周期

```
                 ┌──────────────┐
   安装制品 ─────▶ │  INSTALLED   │
                 └──────┬───────┘
                        │ 解析 manifest / 依赖 / 配置
                 ┌──────▼───────┐
                 │  RESOLVED    │◀────────────┐
                 └──────┬───────┘             │
                        │ 依赖齐备 & 配置合法   │ 依赖恢复
                 ┌──────▼───────┐             │
                 │   ACTIVE     │── 依赖变化/禁用/重载 ──▶ STOPPED
                 └──────┬───────┘                            │
                        │ 卸载                                │ 重新激活
                 ┌──────▼───────┐                            │
                 │ UNINSTALLED  │◀───────────────────────────┘
                 └──────────────┘
```

1. 依赖不齐备时停在 `RESOLVED`，**不执行** `install`，配置求值也推迟。
2. 依赖变化 → `deactivate`（逆序回滚全部 effect）→ 重新判定 → 必要时再次 `activate`。
3. 重载 = 卸载后重建，不是增量 patch；`install` 必须幂等、可重入。
4. 每次状态变化发 `internal/status` 事件（`moduleId`、`from`、`to`、`reason`）。
5. Endpoint 上的模块由端点 runtime 执行同一状态机；进程退出等价于 `STOPPED`，包替换等价于 `UNINSTALLED → INSTALLED`。

## 4. 入口契约

语言无关语义：

```
install(context, config) -> Disposable
```

- `context`：模块作用域上下文（服务查找、effect、事件、日志、时钟、twin 读写）。
- `config`：通过 `configSchema` 校验后的配置。
- 返回：撤销本次安装全部副作用的 `Disposable`，必须幂等。

可选钩子：

```
activate(context) / deactivate(context)
health() -> HealthReport
applyDesired(twinDesired) -> void      # 端点模块可选
```

## 5. Effect：可逆副作用

```
effect(label, execute) -> Disposable
```

| 语义 | 规定 |
|---|---|
| 执行时机 | 调用即同步执行 |
| 返回值 | `execute` 必须返回 disposer |
| 回滚顺序 | 同一模块内 **LIFO 逆序** |
| 触发 | 手动 dispose / 模块卸载，**先到先算** |
| 幂等 | 重复 dispose 为 no-op |
| 异步 | 允许异步清理；卸载等待全部完成 |
| 部分失败 | `execute` 中途抛错，已注册的子 effect 仍需回滚 |
| 已卸载后注册 | 抛 `INACTIVE_EFFECT` |
| 诊断 | `context.getEffects()` 返回树（label + children） |

所有"注册"必须走 `effect`：监听器、路由、线程池、定时任务、缓存、MBean、Metrics、文件锁、连接池。

## 6. 服务注册与解析

| 操作 | 语义 |
|---|---|
| `provide(serviceId, instance)` | 归当前模块所有；ACTIVE 后对同作用域可见；返回 disposer；重名抛 `SERVICE_CONFLICT` |
| `get(serviceId, strict=true)` | strict 只返回提供方当前 ACTIVE 的实例 |
| `set(serviceId, value)` | 只有提供方有权调用，否则 `SERVICE_FORBIDDEN` |
| `accessor(serviceId, {get,set})` | 计算属性式服务，卸载即移除 |
| `mixin(serviceId, keys)` | 把成员挂到 context（语法糖，不改变所有权） |

服务 ID：`<域>.<能力>[:<变体>]`，如 `dns.provider:aliyun`、`storage.repo:mysql`。

## 7. Inject：依赖驱动

```yaml
requires:
  - foundation.event.bus
  - storage.repo
```

1. **AND 语义**：全部可用才激活。
2. 依赖消失 → 当前模块 `deactivate` 并回滚；依赖方在依赖方清理完成前仍可读。
3. 依赖恢复 → 重新 `install`，不恢复旧实例。
4. 默认无超时、无降级；需要超时由应用包自己用 `effect` 实现。
5. `get()` 是逃生口，必须在 manifest 标注 `escapeHatch: true` 并记录原因。

## 8. 作用域

| API | 语义 | 用途 |
|---|---|---|
| `extend(meta)` | 原型继承 + 元数据遮蔽 | 子树打标签（租户/区域/灰度） |
| `isolate(serviceId, label)` | 该服务在子树按新 label 解析，父级不可见；同 label 共享 | 同服务多实例 |
| `intercept(serviceId, config)` | 为下文启动的模块追加配置，祖先优先 | 不改代码调整行为 |

作用域必须显式传递：所有服务查找走 `context`，禁止全局静态单例。

### Java adapter 映射（参考实现）

| 规范语义 | Java 映射 |
|---|---|
| `extend` | 子 `ApplicationContext`（parent 指向当前） |
| `isolate` | `ScopeKey`（对象引用，非字符串）+ `ScopedServiceRegistry` 独立视图 |
| `intercept` | 配置合并链（祖先优先），`install` 前注入 |
| 副作用 | `Disposable` + `EffectStack`（LIFO） |
| 服务解析 | `ServiceRegistry`（不直接暴露 Spring BeanFactory） |

> Spring 只是实现手段；规范不要求实现语言具备 Spring。

## 9. 双适配

| 维度 | server adapter | endpoint adapter |
|---|---|---|
| 隔离单位 | 类加载器（PF4J ClassLoader） | 进程 / 沙箱 |
| 入口 | Java 类 | 可执行文件或脚本 |
| 服务来源 | 宿主 ServiceRegistry | 端点本地能力集 |
| 通信 | 进程内调用 | HTTP/WSS |
| 生命周期 | PF4J + Kernel 状态机 | 包安装 + 进程守护 |
| 共同点 | **同一 manifest、同一 capability、同一版本约束、同一 twin 语义** | 同左 |

端点模块不要求实现 effect 语义：进程退出 + 包替换即回滚，由端点 runtime 保证。

## 10. 配置与 Twin

- 配置优先级：`intercept` 链 > 应用包默认值 > 插件默认值。
- 写入方式：控制面 `PATCH /api/v1/endpoints/{id}/twin` 或 `.../modules/{id}/twin` 使用 **JSON Merge Patch**（`application/merge-patch+json`），必须带 `If-Match: <revision>`；`config` 递归合并，`modules` 数组整体替换。
- 冲突：revision 不匹配返回 `412`，不静默覆盖。
- 应用：模块应用后上报 `reported.revision` 与 `reported.config`；控制台以 desired ≠ reported 判定漂移。
- 失败：保留旧配置，记录 `lastError` 与审计，不阻塞其他模块。
- `secret: true` 字段控制台不回显、日志脱敏、API 返回掩码。
- 一次性凭据不进入 Twin：走 `POST /api/v1/modules/{id}:deliver-credential`，单次有效、60s TTL、仅内存、不落库、不落日志（见 ADR-0003）。

### 10.1 端点自治与跨端边界

- 周期任务由端点按 desired 中的 cron **本地调度**；宿主不承担调度，只下发配置与一次性命令。
- 宿主命令带 `commandId` 与 `expiresAt`，端点执行后 `ack`；超时重投最多 3 次。
- Service **只在进程内可见**，不跨端调用；跨端只能通过 Endpoint 协议（注册/心跳/Twin/命令/上报/升级）。
- 端点离线时按最后一次 desired 继续运行；恢复后按 revision 对齐，漂移可见。

## 11. 错误码

| 码 | 含义 |
|---|---|
| `INACTIVE_EFFECT` | 已卸载仍注册 effect |
| `SERVICE_CONFLICT` | 服务重名 |
| `SERVICE_FORBIDDEN` | 非所有者修改服务 |
| `DEPENDENCY_MISSING` | 依赖长期不可用（诊断用） |
| `CONFIG_INVALID` | 配置校验失败 |
| `TWIN_SCHEMA_INVALID` | twin 不符合 Schema |
| `API_VERSION_MISMATCH` | 契约主版本不匹配 |
| `MANIFEST_INVALID` | manifest 不符合 Schema |

## 12. 反模式

1. 在 `install` 做一次性全局初始化（重载会重复）。
2. 直接 `new Thread` / `Executors` 而不注册 effect。
3. 用静态变量保存服务实例。
4. 用 `get()` 绕过 `requires`。
5. 在 `stop()` 手写清理，而不让 effect 回滚。
6. 把端点语言写进平台概念（如"Rust 插件"作为 kind）。
