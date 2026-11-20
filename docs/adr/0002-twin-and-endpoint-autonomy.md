# Twin 与端点自治

控制面需要区分"我要求了什么"与"端点实际应用了什么"，并在断网、升级失败、人工改动时发现漂移。因此每个 Endpoint 与 Module 都持有 desired/reported 两份状态，控制面写 desired（带 revision 与 `If-Match`，采用 JSON Merge Patch），端点应用后写 reported。周期任务由端点按 desired 本地调度，宿主只下发配置与一次性命令；跨端只允许 Twin 与命令，不允许跨进程服务调用。

**Status**: accepted

**Considered Options**:
- 单一 `configRevision`：只能证明"发过"，无法证明"应用了"。
- 宿主统一调度：控制面变单点，断网即停摆，与 BYOC 定位冲突。
- 跨端服务调用：等价于自建 RPC，引入超时、重试、版本协商与离线语义。

**Consequences**:
- 控制台必须展示 desired/reported 差异与最后错误。
- 端点具备离线自转能力，控制面故障不阻断已下发任务。
- Service 只在进程内可见；Twin 与命令是唯一跨端通道。
