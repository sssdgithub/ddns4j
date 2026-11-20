# 06 — 包、签名与分发

## 1. 包类型

| type | 内容 | 分发目标 |
|---|---|---|
| `server-plugin` | 服务端插件 jar/目录 + `plugin.yaml` | Foundation Host |
| `endpoint-package` | 端点模块包（二进制/脚本 + `plugin.yaml`） | Endpoint |
| `app-bundle` | 应用包：服务端插件 + 端点模块 + 控制台页面 + 默认配置 | Host + Endpoint |
| `config-bundle` | 默认配置/策略集 | Host |

## 2. Package Manifest

`package.yaml`（与 `package-manifest.schema.json` 一致）：

```yaml
apiVersion: foundation/v1
id: foundation.dns.aliyun
type: server-plugin
version: 1.0.0
channel: stable              # stable | beta
platform: any                # any | linux-amd64 | linux-arm64 | linux-armv7 | windows-amd64 | darwin-arm64
sha256: 9f2c...
size: 1843200
signature: ed25519:...       # 可选但推荐
minHost: 0.1.0
maxHost: 0.x
createdAt: 2026-11-19T10:00:00Z
changelog: |
  - 初始版本
artifacts:
  - name: plugin.jar
    sha256: ...
    url: https://.../plugin.jar
manifest:
  inline: ./plugin.yaml      # 或 install 后从包内读取
```

规则：

1. `sha256` 与 `size` 必填；下载后必须校验。
2. `signature` 使用 Ed25519 对 `sha256` 或包体签名；校验失败拒绝安装。
3. `channel` 决定端点拉取策略；`stable` 默认。
4. `minHost/maxHost` 不满足时拒绝加载。
5. 同一 `id + version + platform` 不可重复发布；重发需新版本号。

## 3. 存储后端

| 部署 | 存储 | 说明 |
|---|---|---|
| Core 本地 | 本地目录 / GitHub Release | 零运维 |
| Core 单机 | 本地对象目录（文件系统） | 默认 |
| Enterprise | Nexus OSS / Gitea Package Registry / S3 兼容 | 权限、审计、内网分发 |
| 大规模端点 | Eclipse hawkBit + RAUC/SWUpdate | 仅当端点上百，不自研 OTA |

制品库只解决"存与权限"；发现、匹配、灰度由 Foundation 的 catalog 负责。

## 4. Catalog

```json
{
  "schemaVersion": 1,
  "items": [
    { "id": "foundation.dns.aliyun", "type": "server-plugin", "version": "1.0.0",
      "platform": "any", "sha256": "...", "minHost": "0.1.0",
      "channel": "stable", "download": "https://..." }
  ]
}
```

- `GET /api/v1/packages` 的底层数据源可以是 catalog 或制品库。
- 端点升级时，服务端按 `profile + platform + channel + labels` 匹配候选包。
- catalog 可缓存；必须支持 ETag。

## 5. 灰度与回滚

| 能力 | 规定 |
|---|---|
| 目标筛选 | `targetLabels`（AND）或显式端点列表 |
| 批次 | `batchPercent` 1–100；每批完成后可暂停 |
| 观察窗 | 批次之间等待端点回报 `reported.revision` 与错误 |
| 失败阈值 | 单批失败率超阈值自动暂停（Enterprise 可配） |
| 回滚 | 指定上一版本重新下发；不复用旧包，重新走校验 |
| 审计 | 每次 rollout / pause / rollback 记审计 |

## 6. 端点包与升级

- 端点包必须声明 `platform`；一个包只对应一个平台/架构。
- 端点 runtime 负责：下载 → sha256 校验 → 签名校验 → 原子替换 → 重启 → 回报。
- 失败不替换，上报 `lastError`，保持旧版本可用。
- `edge` 建议静态单文件；`desktop/mobile` 使用各自平台的安装包格式。

## 7. 安全

1. 包上传需要 `packages:write`；灰度需要 `packages:rollout`。
2. 签名策略：Core 强制校验 `sha256`，签名可选（缺失时记录警告）；Enterprise 强制签名，缺失拒绝安装。
3. 包元数据与制品分离：数据库存元数据，制品存对象存储。
4. 禁止在包 manifest 中出现明文凭据；引用通过 `credentialRef`。
5. 供应链：记录构建来源（commit、CI 运行号），可选 SBOM。

## 8. 与旧 P0 的关系

旧 P0（JDK17 + Boot3 + PF4J 热重载）只验证"宿主能加载插件"，不覆盖包分发。Foundation v1 将包分发纳入规范，但参考实现只要求本地目录 + 一个 `endpoint-package`，不接制品库。
