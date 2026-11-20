# 05 — 控制台、治理与分发

## 1. 控制台（v1 四页）

参考实现用 Thymeleaf 服务端渲染，零前端构建：

| 页面 | 内容 |
|---|---|
| 登录 | 单管理员账号 |
| 端点 | 列表、详情、desired/reported 差异、Token 轮换 |
| 模块 | 启停、重载、配置、Effect 树 |
| 审计 | 按 actor/action/时间查询 |

包上传、灰度、用户/角色/租户管理只提供 OpenAPI，不进入 v1 页面。SPA 由后续版本或第三方实现。

## 2. 角色与权限

| 角色 | 范围 |
|---|---|
| owner | 全部读写 |
| admin | 端点/模块/应用/包读写，用户只读 |
| operator | 端点/模块读写，应用/包只读 |
| viewer | 全部只读 |

- 权限串：`resource:action`（如 `endpoints:write`）。
- v1 只有一个管理员；完整 RBAC 留 Enterprise。
- 越权返回 `403` 并写审计。

## 3. 审计

```json
{ "id": "aud_01HX", "actor": "user:u_01", "action": "module.reload",
  "target": "module:mod_01", "before": {}, "after": {},
  "at": "2026-11-20T10:00:00Z", "ip": "10.0.0.5", "traceId": "01HX..." }
```

- 追加写，不可修改/删除；默认保留 90 天。
- 必须覆盖：登录、Token 创建/吊销/轮换、Endpoint 注册/删除、Twin 下发、Module 部署/启停/重载、包上传/灰度、配置变更、角色变更、凭据交付。
- 凭据只记录"谁向哪个模块交付了哪类凭据"，不记录值；`before/after` 秘密字段脱敏。

## 4. 包与 manifest

| type | 内容 | 目标 |
|---|---|---|
| `server-plugin` | 服务端插件包 + `plugin.yaml` | Host |
| `endpoint-package` | 端点模块包 | Endpoint |
| `app-bundle` | 模块 + 页面 + 默认配置 | Host + Endpoint |
| `config-bundle` | 默认配置/策略集 | Host |

`package.yaml` 与 [schemas/package-manifest.schema.json](../schemas/package-manifest.schema.json) 一致：

```yaml
apiVersion: foundation/v1
id: foundation.dns.aliyun
type: server-plugin
version: 1.0.0
channel: stable
platform: any            # any | linux-amd64 | linux-arm64 | linux-armv7 | windows-amd64 | darwin-arm64
sha256: 9f2c...
size: 1843200
signature: ed25519:...   # 可选
minHost: 0.1.0
maxHost: 0.x
artifacts:
  - { name: plugin.jar, sha256: ..., url: https://... }
manifest:
  inline: ./plugin.yaml
contents:                # 仅 app-bundle
  modules: [{ pluginId: ..., target: host }]
  pages: [{ path: /, title: Demo }]
```

规则：

1. `sha256` 与 `size` 必填，下载必须校验。
2. 若提供 `signature`，Ed25519 校验失败拒绝安装；Core 签名可选，Enterprise 强制。
3. 同一 `id + version + platform` 不可重复发布。
4. `platform` 匹配：Endpoint 的 `{os, arch}` 拼成 `os-arch` 与包字符串比较；`any` 匹配所有；runtime 不参与 v1 匹配。
5. 包内禁止明文凭据；凭据只以配置字段引用。

## 5. 存储与灰度

| 部署 | 存储 |
|---|---|
| Core | 本地目录 / GitHub Release |
| Enterprise | Nexus OSS / Gitea Package Registry / S3 兼容 |
| 大规模端点 | Eclipse hawkBit + RAUC/SWUpdate（不自研 OTA） |

灰度（Enterprise）：按 `targetLabels` 选目标，`batchPercent` 分批，批次间观察 reported 与错误率，失败可回滚到上一版本。v1 只做本地目录与 sha256。

## 6. 安全

1. 包上传需 `packages:write`，灰度需 `packages:rollout`。
2. 制品与元数据分离：数据库存元数据，制品存对象存储。
3. 记录构建来源（commit、CI 运行号），可选 SBOM。
4. Endpoint 注册/吊销/升级/命令下发全部留痕。
