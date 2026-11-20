# Foundation

一个语言无关的插件化数字底座：内核负责装配与治理，业务能力以插件与应用包接入。ddns4j 是它的第一个验证应用。

## Language

**Foundation**:
运行插件、管理端点与分发能力的底座本身。
_Avoid_: 平台产品、框架、ddns4j 底座

**Host**:
承载内核、控制面与宿主模块的进程。
_Avoid_: 服务端、主节点

**Plugin**:
可分发的包，由 manifest 与入口组成。
_Avoid_: 扩展、组件、驱动

**Module**:
Plugin 在宿主或某个 Endpoint 上的一次部署单元，拥有独立配置与状态。
_Avoid_: 服务、Bean

**Host Module**:
部署在宿主上的 Module。
_Avoid_: 服务端插件实例

**Endpoint Module**:
部署在某个 Endpoint 上的 Module。
_Avoid_: 客户端插件

**App（应用包）**:
聚合型 Plugin，声明一组模块、控制台页面与默认配置。
_Avoid_: 解决方案、产品包

**Endpoint**:
被底座纳管的端，例如路由器、NAS、桌面客户端或移动端。
_Avoid_: Node、Agent

**Profile**:
Endpoint 的角色档案（edge/desktop/mobile/relay），决定可用字段与生命周期约束。
_Avoid_: 类型、端类型

**Capability**:
对外声明的一项能力，与具体实现解耦。
_Avoid_: 特性、功能点

**Service**:
运行时由某个 Module 提供的具名能力，可被替换。
_Avoid_: Bean、组件

**Effect**:
一次可逆的副作用登记，卸载时按后进先出撤销。
_Avoid_: 回调、监听器

**Inject**:
Module 对 Service 的声明式依赖；依赖齐备才激活。
_Avoid_: 依赖注入、引用

**Scope**:
Service 解析的作用域，支持继承、隔离与配置拦截。
_Avoid_: 上下文、租户

**Twin**:
同一对象的期望（desired）与实际（reported）两份文档。
_Avoid_: 配置、影子

**Package**:
可分发的字节包，携带 manifest、校验和与签名。
_Avoid_: 制品、安装包

**Channel**:
Package 的发布通道（stable/beta）。
_Avoid_: 环境、分支

**Tenant**:
治理隔离单元；仅 Enterprise 多租户。
_Avoid_: 组织、客户
