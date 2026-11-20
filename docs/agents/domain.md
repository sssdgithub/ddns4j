# Domain Docs

工程技能在探索代码前后如何消费本仓库的领域文档。

## 探索前先读

- **`GLOSSARY.md`**（仓库根目录）：领域术语与禁用同义词。
- **`docs/adr/`**：与当前工作区域相关的 ADR。

当前是**单上下文仓库**：没有 `GLOSSARY-MAP.md`，也没有 `src/<context>/docs/adr/`。

现有 ADR：

- `0001-foundation-is-independent.md`
- `0002-twin-and-endpoint-autonomy.md`
- `0003-credential-handoff.md`

## 使用术语表词汇

输出中的领域概念（issue 标题、重构提案、假设、测试名）必须使用 `GLOSSARY.md` 定义的主词，不要使用它列在 `_Avoid_` 下的同义词。若需要的概念不在表中：先判断是否在发明项目不使用的语言；确属缺口时留给 `/domain-modeling` 补充。

## 与 ADR 冲突

若输出与现有 ADR 矛盾，显式指出，不要静默覆盖：

> _与 ADR-0002（端点自治）冲突，但值得重新讨论，因为……_
