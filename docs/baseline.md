# 基线记录（待实测）

> 状态：v1 实现完成后填写；本文件是 `docs/06-minimal-loop.md` 的唯一实测记录位置。

## 环境

| 项 | 值 |
|---|---|
| 操作系统 | Windows 11 |
| JDK | OpenJDK 21.0.2（`tools/jdk21`） |
| Maven | 3.8.1（`tools/mvn21.ps1`） |
| 机器规格 | 4C / 8G（待确认） |

## 指标

| 指标 | 目标 | 实测 1 | 实测 2 | 实测 3 | 中位数 |
|---|---|---|---|---|---|
| host 启动到 `/readyz` 200 | ≤ 3s | — | — | — | — |
| 100 次 reload 总耗时 | 全部成功 | — | — | — | — |
| twin 往返（PATCH → reported） | 无目标 | — | — | — | — |
| host fat jar 体积 | ≤ 30MB | — | — | — | — |
| 插件 jar 体积 | 无目标 | — | — | — | — |
| endpoint-cli 包体积 | 无目标 | — | — | — | — |

## 测量方法

- 启动：进程创建到 `/readyz` 首次 200，连续 3 次。
- reload：连续 100 次 `:reload`，每次后检查 `context.getEffects()` 为空。
- twin：同机 HTTP 往返，记录毫秒。
- 体积：构建产物字节数。
