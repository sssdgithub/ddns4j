# 基线记录

> 本文件是 `docs/06-minimal-loop.md` 的唯一实测记录位置。实测时间：2026-11-20。

## 环境

| 项 | 值 |
|---|---|
| 操作系统 | Windows 11 Pro |
| CPU | AMD Ryzen 5 5600G（6 核 / 12 线程） |
| 内存 | 31.3 GB |
| JDK | OpenJDK 21.0.2（`tools/jdk21`） |
| Maven | 3.8.1（`tools/mvn21.ps1`） |
| Spring Boot | 4.2.0（Spring Framework 7.1.0） |
| PF4J | 3.16.1 |
| 构建 | `.\tools\build.ps1` |
| 验收 | `.\tools\verify.ps1`（16/16 通过） |

## 指标

| 指标 | 目标 | 实测 1 | 实测 2 | 实测 3 | 中位数 | 结论 |
|---|---|---|---|---|---|---|
| host 启动到 `/readyz` 200（进程创建起） | ≤ 3s | 2867ms | 2820ms | 2832ms | 2832ms | 达标 |
| Spring 应用启动耗时（日志 `Started HostApplication in`） | 参考 | 2.055s | 2.099s | 2.069s | 2.069s | — |
| 100 次 `:reload` 总耗时 | 全部成功 | 301ms | — | — | 301ms | 达标（0 失败，无残留） |
| twin 往返（PATCH → `/twin/report`，同机 HTTP，20 次） | 无目标 | min 2.9ms | 中位 3.7ms | max 11.0ms | 3.7ms | — |
| host fat jar 体积 | ≤ 30MB | 23885 KB（23.3MB） | — | — | — | 达标 |
| 插件 jar 体积 | 无目标 | 5.3 KB | — | — | — | — |
| endpoint-cli 包体积 | 无目标 | 2269 KB（2.2MB） | — | — | — | — |

## 测量方法

- 启动：`Start-Process` 到 `/readyz` 首次 200，100ms 轮询，连续 3 次（`tools/verify.ps1`）。
- reload：连续 100 次 `POST /api/v1/modules/{id}:reload`；随后校验路由与 `demo.greeting` 服务各 1 条（无泄漏），stop 后 `getEffects()` 为空，日志无 ERROR。
- twin：同机 HTTP，PATCH（`If-Match`）+ `/endpoint/v1/twin/report` 为一轮，共 20 轮取中位数；含 PowerShell 客户端开销。
- 体积：构建产物字节数（`tools/build.ps1` 输出）。

## 结论与备注

- 16 项验收（A1–A16）全部通过，明细见 `tools/verify.ps1` 输出。
- 启动目标以「进程创建到就绪」为准；Spring 应用自身启动约 2.0–2.1s。
- 100 次 reload 后 `mod_*` 对应路由 1 条、`demo.greeting` 服务 1 条，说明 effect 回滚与重新装配没有累积泄漏。
- 凭据交付仅存在内存中，验收脚本对 host 日志做了明文扫描，未发现交付值。
- 测试：kernel 12 个单元测试 + host 1 个端到端集成测试全部通过。
