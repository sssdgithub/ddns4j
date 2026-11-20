# Issue Tracker

## 位置

- 仓库：https://gitee.com/Xsssd/ddns4j
- Issues：https://gitee.com/Xsssd/ddns4j/issues
- 平台相关的工作虽然位于 `feat/foundation` 分支，但 issue 与仓库共用；标题统一加 `[Foundation]` 前缀。

## 读写方式

本环境没有 `gh` / `glab` CLI，Gitee 也没有官方等价物。约定如下：

- **新建**：在 Gitee 网页「Issues → 新建 Issue」创建。
- **读取**：网页查看；若配置了 Gitee Access Token，可用 OpenAPI v5 读取：
  `GET https://gitee.com/api/v5/repos/Xsssd/ddns4j/issues?state=open&access_token=...`
- **更新状态/标签**：网页操作。没有本地命令可以代劳。
- **PR 作为请求入口**：关闭。外部 PR 不进入 triage 队列。

## 标题约定

| 范围 | 前缀 |
|---|---|
| Foundation 内核/规范/控制面 | `[Foundation]` |
| ddns4j 应用包 | `[ddns4j]` |
| 跨两者 | `[Foundation][ddns4j]` |

## 标签

triage 标签需在 Gitee「仓库 → 管理 → 标签」手工创建，映射见 `triage-labels.md`。
