# Foundation 是独立平台，ddns4j 只是首个验证应用

Foundation 需要独立演进、独立发版、可承载 ddns4j 之外的业务，因此不把它做成 ddns4j 的内部模块；ddns4j 仅作为第一个应用包验证底座能力。代价是底座必须自己承担版本兼容、文档与 API 稳定性，且内核不得出现任何 ddns4j 业务词汇。

**Status**: accepted

**Considered Options**:
- 先在 ddns4j 内做可抽取 kernel，第二个应用出现再拆出：风险低，但容易被业务绑死。
- 不做平台，只把 ddns4j 插件化：成本最低，但无法承载后续业务。

**Consequences**:
- 内核与规范里禁止出现 `ddns4j` / DNS 等业务概念。
- Foundation 拥有独立版本线与兼容承诺，ddns4j 跟随它升级。
- 第二个应用出现前，不为第三方开发者承诺 API 稳定性与插件市场。
