# DDNS4J 项目规格文档

## 1. 项目概述

### 1.1 项目愿景

DDNS4J 是一个开源的动态域名解析解决方案，旨在为个人开发者和小型组织提供简单、可靠、低成本的动态 DNS 服务。项目坚持以下核心价值：

- **简单易用**: 开箱即用，无需复杂配置
- **稳定可靠**: 基于成熟的 Java 技术栈，确保生产环境稳定运行
- **易于扩展**: 模块化架构，支持轻松添加新的 DNS 服务商
- **现代化体验**: 提供美观、响应式的 Web 管理界面

### 1.2 项目范围

**核心功能**:
- 多 DNS 服务商支持（阿里云、腾讯云、Cloudflare、华为云）
- IPv4/IPv6 双栈解析
- 多种 IP 获取方式（网络接口、网卡）
- 可配置的更新频率（1/2/5/10分钟）
- 操作日志记录与查询
- 公网访问控制

**非功能需求**:
- 支持 Docker、Linux systemd、Windows 等多平台部署
- 支持 H2 嵌入式数据库和 MySQL 生产数据库
- 前后端分离架构，便于独立开发和部署

---

## 2. 技术栈规范

### 2.1 后端技术栈

#### 2.1.1 核心技术

| 技术 | 版本要求 | 用途 | 约束 |
|------|----------|------|------|
| Java | 1.8+ | 编程语言 | 必须兼容 JDK 8 |
| Spring Boot | 2.7.x | 应用框架 | 保持 LTS 版本 |
| MyBatis-Plus | 3.5.x | ORM 框架 | 使用其自动填充、分页等功能 |
| Quartz | 内置 | 任务调度 | 通过 Spring Boot Starter 集成 |

#### 2.1.2 数据库

| 类型 | 使用场景 | 配置方式 | 约束 |
|------|----------|----------|------|
| H2 | 开发/轻量部署 | application-h2.yml | 仅用于单机场景 |
| MySQL | 生产环境 | application-mysql.yml | 推荐 MySQL 8.0+ |

**数据库规范**:
- 所有表使用 BIGINT 作为主键类型
- 使用 MyBatis-Plus 的逻辑删除功能
- 时间字段使用 DATETIME 类型
- 敏感字段（如密钥）加密存储

#### 2.1.3 网络通信

| 组件 | 用途 | 约束 |
|------|------|------|
| RestTemplate | 调用云厂商 DNS API | 必须配置超时 |
| 阿里云 SDK | alidns20150109 | 官方最新版本 |
| 华为云 SDK | huaweicloud-sdk-dns | 官方最新版本 |

#### 2.1.4 辅助工具

| 工具 | 版本 | 用途 |
|------|------|------|
| Lombok | 1.18.x | 简化代码，自动生成 getter/setter |
| Jackson | Spring Boot 内置 | JSON 序列化/反序列化 |
| Hibernate Validator | JSR-303 | 参数校验 |

### 2.2 前端技术栈

#### 2.2.1 核心框架

| 技术 | 版本要求 | 用途 | 约束 |
|------|----------|------|------|
| Vue 3 | ^3.4.x | 核心框架 | 必须使用 Composition API |
| TypeScript | ~5.4.x | 类型系统 | 启用严格模式 |
| Vite | ^5.1.x | 构建工具 | 开发体验优化 |
| Tailwind CSS | ^3.4.x | 样式管理 | 必须遵循设计系统 |

#### 2.2.2 状态管理与路由

| 技术 | 版本要求 | 用途 |
|------|----------|------|
| Pinia | ^2.1.x | 状态管理 |
| Vue Router | ^4.3.x | 路由管理 |

#### 2.2.3 UI 组件

| 组件库 | 用途 | 约束 |
|--------|------|------|
| 自定义 UI 组件 | 基础组件 | 在 src/components/ui/ 下开发 |
| Lucide Vue | 图标库 | 统一使用 Lucide 图标 |
| Axios | HTTP 客户端 | 必须有拦截器 |

---

## 3. 架构设计规范

### 3.1 Maven 多模块结构

```
ddns4j/
├── ddns4j-parent/          # 父模块，定义依赖版本
│   └── pom.xml
├── ddns4j-common/          # 公共模块
│   ├── src/main/java/
│   │   └── top/sssd/ddns/common/
│   │       ├── constant/   # 常量定义
│   │       ├── enums/      # 枚举类
│   │       ├── utils/      # 工具类
│   │       ├── valid/      # 校验分组
│   │       └── *.java      # 通用类
│   └── pom.xml
├── ddns4j-core/            # 核心业务模块
│   ├── src/main/java/top/sssd/ddns/
│   │   ├── config/         # 配置类
│   │   ├── handler/        # 处理器
│   │   ├── mapper/        # Mapper 接口
│   │   ├── model/         # 数据模型
│   │   ├── service/       # 服务层
│   │   ├── strategy/      # 策略模式
│   │   ├── task/          # 定时任务
│   │   └── utils/         # 工具类
│   ├── src/main/resources/
│   │   ├── mapper/        # MyBatis XML
│   │   └── sql/           # 数据库脚本
│   └── pom.xml
└── ddns4j-web/            # Web 模块
    ├── src/main/java/top/sssd/ddns/
    │   ├── config/         # Web 配置
    │   ├── controller/    # 控制器
    │   ├── handler/       # 异常处理
    │   └── interceptor/   # 拦截器
    ├── src/main/resources/
    │   ├── static/        # 前端静态资源
    │   └── application*.yml
    └── pom.xml
```

**模块依赖原则**:
- ddns4j-common: 被其他所有模块依赖，不依赖任何业务模块
- ddns4j-core: 依赖 ddns4j-common，包含所有业务逻辑
- ddns4j-web: 依赖 ddns4j-core，仅包含 Web 层代码

### 3.2 前端目录结构

```
src/
├── main.ts                 # 应用入口
├── App.vue                 # 根组件
├── index.css               # 全局样式（Tailwind 入口）
├── api/                    # API 层
│   ├── client.ts          # Axios 实例配置
│   ├── parsingRecord.ts   # 解析记录 API
│   ├── changedLog.ts      # 变更日志 API
│   └── publicAccess.ts    # 公共访问 API
├── components/             # 组件
│   ├── ui/               # 基础 UI 组件（Button, Card, Input等）
│   ├── RecordForm.vue    # 解析记录表单
│   └── ToastContainer.vue # Toast 容器
├── composables/          # 组合式函数
│   └── useToast.ts      # Toast 钩子
├── layouts/              # 布局组件
│   └── MainLayout.vue   # 主布局
├── lib/                 # 工具库
│   └── utils.ts         # 通用工具函数
├── router/              # 路由配置
│   └── index.ts
├── views/               # 页面视图
│   ├── ParsingRecords.vue
│   ├── ChangedLogs.vue
│   └── Settings.vue
└── vite-env.d.ts        # Vite 类型声明
```

**前端代码组织原则**:
- 每个页面对应一个视图组件（views/）
- 可复用逻辑抽取为组合式函数（composables/）
- 基础 UI 组件统一放在 components/ui/
- API 调用统一封装在 api/ 目录

---

## 4. 数据库设计规范

### 4.1 表设计原则

1. **主键设计**: 使用 BIGINT 类型主键，配合 MyBatis-Plus 的 ID 生成器
2. **时间戳**: 所有表包含 create_date 和 update_date 字段
3. **逻辑删除**: 使用 deleted 字段进行逻辑删除
4. **乐观锁**: 需要并发控制的表使用 version 字段

### 4.2 核心表结构

#### 4.2.1 parsing_record（解析记录表）

```sql
CREATE TABLE parsing_record (
    id BIGINT PRIMARY KEY,                    -- 主键
    service_provider INT NOT NULL COMMENT '服务商类型(1:阿里云 2:腾讯云 3:Cloudflare 4:华为云)',
    service_provider_id VARCHAR(255) COMMENT 'AccessKey ID',
    service_provider_secret VARCHAR(255) COMMENT 'AccessKey Secret(加密存储)',
    record_type INT NOT NULL COMMENT '记录类型(1:AAAA 2:A)',
    ip VARCHAR(255) COMMENT '当前解析的IP地址',
    get_ip_mode INT NOT NULL COMMENT '获取IP模式(1:网络接口 2:网卡)',
    get_ip_mode_value VARCHAR(255) COMMENT '获取IP模式的配置值',
    domain VARCHAR(255) NOT NULL COMMENT '完整域名',
    update_frequency INT NOT NULL COMMENT '更新频率(分钟)',
    deleted TINYINT DEFAULT 0,                -- 逻辑删除
    version INT DEFAULT 0,                   -- 乐观锁
    create_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    creator BIGINT,
    updater BIGINT,
    INDEX idx_domain (domain),
    INDEX idx_service_provider (service_provider)
);
```

#### 4.2.2 job_task（定时任务表）

```sql
CREATE TABLE job_task (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL COMMENT '任务名称(关联parsing_record.id)',
    status INT NOT NULL COMMENT '状态(0:停止 1:运行)',
    class_name VARCHAR(255) NOT NULL COMMENT '任务类名',
    cron_expression VARCHAR(255) COMMENT 'Cron表达式',
    execute_params TEXT COMMENT '执行参数(JSON)',
    deleted TINYINT DEFAULT 0,
    create_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name),
    INDEX idx_status (status)
);
```

#### 4.2.3 changed_log（变更日志表）

```sql
CREATE TABLE changed_log (
    id BIGINT PRIMARY KEY,
    content TEXT COMMENT '日志内容',
    insert_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_insert_date (insert_date)
);
```

### 4.3 字段命名规范

| 类型 | 命名规范 | 示例 |
|------|----------|------|
| 表名 | 小写下划线 | parsing_record |
| 字段名 | 小写下划线 | service_provider_id |
| 主键 | id | id |
| 外键 | {表名}_id | parsing_record_id |
| 时间戳 | create_date/update_date | create_date |
| 布尔值 | is_{状态}/{状态}_flag | is_deleted |

---

## 5. API 设计规范

### 5.1 RESTful API 设计原则

1. **URL 规范**: 使用名词复数形式，小写字母
2. **HTTP 方法**: GET（查询）、POST（创建）、PUT（更新）、DELETE（删除）
3. **版本控制**: 通过 URL 路径控制，如 /api/v1/parsingRecord
4. **统一响应**: 所有接口返回统一格式

### 5.2 响应格式规范

**成功响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

**分页响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "items": [...],
    "total": 100,
    "page": 1,
    "perPage": 10
  }
}
```

**错误响应**:
```json
{
  "code": 400,
  "message": "参数错误",
  "data": null
}
```

### 5.3 接口清单

#### 5.3.1 解析记录接口

| 接口 | 方法 | 说明 | 请求参数 |
|------|------|------|----------|
| /parsingRecord/page | GET | 分页查询 | page, perPage, serviceProvider, recordType, domain |
| /parsingRecord/add | POST | 新增记录 | ParsingRecord 对象 |
| /parsingRecord/modify | POST | 修改记录 | ParsingRecord 对象 |
| /parsingRecord/delete/{id} | DELETE | 删除记录 | id |
| /parsingRecord/copy | POST | 复制记录 | id |
| /parsingRecord/getIpModeValue | GET | 获取IP模式值 | 无 |

#### 5.3.2 变更日志接口

| 接口 | 方法 | 说明 | 请求参数 |
|------|------|------|----------|
| /changedLog/logs | POST | 查询日志 | page, perPage, startDate, endDate |

#### 5.3.3 公共接口

| 接口 | 方法 | 说明 | 请求参数 |
|------|------|------|----------|
| /publicAccess/publicAccessDisabled | GET | 获取公网访问状态 | 无 |
| /publicAccess/publicAccessDisabled | POST | 设置公网访问状态 | disabled: boolean |

---

## 6. 设计模式规范

### 6.1 策略模式 + 工厂模式

**应用场景**: 多 DNS 服务商支持

**接口定义**:
```java
public interface DynamicDnsStrategy {
    boolean exist(String serviceProviderId, String serviceProviderSecret, 
                 String subDomain, String recordType) throws Exception;
    void add(ParsingRecord parsingRecord, String ip) throws Exception;
    void update(ParsingRecord parsingRecord, String ip, String recordId) throws Exception;
    String getRecordId(ParsingRecord parsingRecord, String ip) throws Exception;
    void remove(ParsingRecord parsingRecord, String ip) throws Exception;
    String getIpBySubDomainWithType(ParsingRecord parsingRecord) throws Exception;
}
```

**工厂类定义**:
```java
@Component
public class DynamicDnsServiceFactory implements ApplicationContextAware {
    private static Map<Integer, DynamicDnsStrategy> dnsStrategyMap = new ConcurrentHashMap<>();
    
    public DynamicDnsStrategy getServiceInstance(Integer serviceProvider) {
        return dnsStrategyMap.get(serviceProvider);
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        // 从 Spring 容器获取所有策略 Bean 并注册
    }
}
```

**扩展规范**:
- 新增服务商时，只需创建新的策略实现类
- 在 application.yml 中配置服务商 ID 与 Bean 名称的映射
- 无需修改工厂类或业务代码

### 6.2 模板方法模式

**应用场景**: IP 获取逻辑

**抽象类定义**:
```java
public abstract class AbstractIpGetter {
    public final String getIp(ParsingRecord record) {
        validate(record);
        return doGetIp(record);
    }
    
    protected abstract void validate(ParsingRecord record);
    protected abstract String doGetIp(ParsingRecord record);
}
```

**具体实现**:
- InterfaceIpGetter: 通过网络接口获取 IP
- NetworkIpGetter: 通过网卡获取 IP

### 6.3 单例模式

**应用场景**: 
- DNS API 工具类（AliDnsUtils, TencentDnsUtils 等）
- RestTemplate 实例配置

---

## 7. 前端开发规范

### 7.1 TypeScript 类型定义

#### 7.1.1 数据模型类型

```typescript
// src/api/parsingRecord.ts
export interface ParsingRecord {
  id?: number
  serviceProvider: number
  serviceProviderId: string
  serviceProviderSecret: string
  recordType: number
  ip?: string
  getIpMode: number
  getIpModeValue: string
  domain: string
  updateFrequency: number
  createDate?: string
  updateDate?: string
}

export interface ParsingRecordQuery {
  page: number
  perPage: number
  serviceProvider?: number
  recordType?: number
  domain?: string
}

export interface ParsingRecordPageResponse {
  items: ParsingRecord[]
  total: number
  page: number
  perPage: number
}
```

#### 7.1.2 API 返回类型

```typescript
// src/api/client.ts
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}
```

### 7.2 组件开发规范

#### 7.2.1 UI 组件模板

```vue
<!-- src/components/ui/Button.vue -->
<script setup lang="ts">
import { computed } from 'vue'
import type { Variant, Size } from '@/types'

interface Props {
  variant?: Variant
  size?: Size
  disabled?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'primary',
  size: 'md',
  disabled: false
})

const emit = defineEmits<{
  click: [event: MouseEvent]
}>()

const classes = computed(() => {
  // 组合类名逻辑
})

const handleClick = (event: MouseEvent) => {
  if (!props.disabled) {
    emit('click', event)
  }
}
</script>

<template>
  <button :class="classes" :disabled="disabled" @click="handleClick">
    <slot />
  </button>
</template>
```

#### 7.2.2 视图组件模板

```vue
<!-- src/views/Example.vue -->
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { exampleApi } from '@/api/example'
import { useToast } from '@/composables/useToast'

const { success, error } = useToast()
const loading = ref(false)
const data = ref([])

const fetchData = async () => {
  loading.value = true
  try {
    const result = await exampleApi.getList()
    data.value = result.items
  } catch (err: any) {
    error(err.message || '获取数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="container">
    <!-- 页面内容 -->
  </div>
</template>
```

### 7.3 Tailwind CSS 使用规范

#### 7.3.1 颜色系统

```typescript
// tailwind.config.ts
module.exports = {
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#f0f9ff',
          100: '#e0f2fe',
          // ... 其他色阶
          600: '#0284c7',
          700: '#0369a1',
        },
        secondary: {
          // ... 次要色定义
        }
      }
    }
  }
}
```

#### 7.3.2 响应式断点

| 断点 | 最小宽度 | 用途 |
|------|----------|------|
| sm | 640px | 平板竖屏 |
| md | 768px | 平板横屏 |
| lg | 1024px | 小屏幕笔记本 |
| xl | 1280px | 桌面显示器 |

### 7.4 状态管理规范

#### 7.4.1 Store 定义

```typescript
// src/stores/parsingRecord.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { parsingRecordApi, type ParsingRecord } from '@/api/parsingRecord'

export const useParsingRecordStore = defineStore('parsingRecord', () => {
  const records = ref<ParsingRecord[]>([])
  const loading = ref(false)
  
  const fetchRecords = async () => {
    loading.value = true
    try {
      const result = await parsingRecordApi.getPage({ page: 1, perPage: 10 })
      records.value = result.items
    } finally {
      loading.value = false
    }
  }
  
  return { records, loading, fetchRecords }
})
```

---

## 8. 安全规范

### 8.1 后端安全

1. **密钥加密**: AccessKey Secret 必须加密存储，使用 AES 或 DES 加密
2. **参数校验**: 所有接口参数必须经过 Hibernate Validator 校验
3. **异常处理**: 统一异常处理器，避免敏感信息泄露
4. **SQL 注入**: 使用 MyBatis-Plus 的 Lambda 查询，避免 SQL 注入
5. **XSS 防护**: 对用户输入进行 HTML 转义

### 8.2 前端安全

1. **敏感信息**: AccessKey 等敏感信息不存储在 localStorage
2. **HTTPS**: 生产环境必须使用 HTTPS
3. **CSRF**: 请求携带 CSRF Token

### 8.3 部署安全

1. **环境隔离**: 生产环境与开发环境配置分离
2. **最小权限**: 数据库账户使用最小权限原则
3. **日志脱敏**: 日志中不记录敏感信息

---

## 9. 性能优化规范

### 9.1 后端优化

1. **数据库索引**: 为常用查询字段创建索引
2. **分页查询**: 列表查询必须分页，限制单页数量
3. **连接池**: 合理配置数据库连接池大小
4. **缓存**: 对不常变化的数据（如服务商列表）添加缓存

### 9.2 前端优化

1. **路由懒加载**: 使用 defineAsyncComponent 懒加载路由组件
2. **图片优化**: 合理压缩图片，使用 WebP 格式
3. **代码分割**: 使用 Vite 的代码分割功能
4. **Tree Shaking**: 确保只引入使用的组件和工具函数

---

## 10. 测试规范

### 10.1 单元测试

**后端测试**:
- 使用 JUnit 5 + Mockito
- 测试覆盖率目标: 核心业务逻辑 > 80%
- 测试文件放在 src/test/java/ 目录

**前端测试**:
- 使用 Vitest 进行单元测试
- 测试覆盖率目标: 工具函数 > 90%
- 测试文件放在 __tests__/ 目录或 *.test.ts

### 10.2 集成测试

1. **API 测试**: 使用 Postman 或自动化测试工具
2. **E2E 测试**: 使用 Playwright 进行端到端测试

### 10.3 测试示例

```java
// 后端单元测试示例
@Test
void testIpGetter() {
    ParsingRecord record = new ParsingRecord();
    record.setGetIpMode(1);
    record.setGetIpModeValue("https://ipv4.icanhazip.com");
    
    String ip = ipGetter.getIp(record);
    assertNotNull(ip);
    assertTrue(IpValidator.isValid(ip));
}
```

```typescript
// 前端单元测试示例
import { describe, it, expect } from 'vitest'
import { formatDate } from '@/lib/utils'

describe('formatDate', () => {
  it('should format date correctly', () => {
    const result = formatDate('2024-01-01')
    expect(result).toBe('2024-01-01')
  })
})
```

---

## 11. 文档规范

### 11.1 代码文档

1. **类文档**: 所有 public 类必须添加 Javadoc 注释
2. **方法文档**: 复杂业务逻辑方法必须添加注释
3. **参数注释**: 所有接口参数必须添加 @Param 或 @RequestParam 注解说明

### 11.2 API 文档

使用 Swagger/OpenAPI 自动生成 API 文档：
- 添加 springdoc-openapi 依赖
- 使用 @Api, @ApiOperation 等注解标注接口
- 访问 /swagger-ui.html 查看文档

### 11.3 项目文档

1. **README.md**: 项目介绍、快速开始
2. **技术文档.md**: 详细技术说明
3. **规格文档.md**: 开发规范和约束
4. **CHANGELOG.md**: 版本更新记录

---

## 12. 版本管理规范

### 12.1 Git 分支策略

```
main (主分支)
  └── develop (开发分支)
        ├── feature-* (功能分支)
        ├── fix-* (修复分支)
        └── release-* (发布分支)
```

### 12.2 Commit 规范

使用 Conventional Commits 规范：

```
<type>(<scope>): <subject>

# 类型
feat: 新功能
fix: 修复问题
docs: 文档变更
style: 代码格式（不影响功能）
refactor: 重构
perf: 性能优化
test: 测试
chore: 构建或辅助工具

# 示例
feat(dns): 添加 DNSPod DNS 服务商支持
fix(frontend): 修复解析记录表单提交问题
docs(api): 更新 API 文档
```

### 12.3 版本号规范

使用语义化版本 (Semantic Versioning)：

```
主版本号.次版本号.修订号

- 主版本号: 不兼容的重大变更
- 次版本号: 向下兼容的功能新增
- 修订号: 向下兼容的问题修复

示例: v1.6.5
```

---

## 13. 部署规范

### 13.1 环境配置

| 环境 | 用途 | 数据库 | 配置 |
|------|------|--------|------|
| 开发环境 | 本地开发 | H2 | application-dev.yml |
| 测试环境 | 功能测试 | MySQL | application-test.yml |
| 生产环境 | 正式运行 | MySQL | application-prod.yml |

### 13.2 Docker 部署

```dockerfile
FROM openjdk:11-jdk-slim

WORKDIR /usr/local/

COPY target/ddns-*.jar /usr/local/app.jar

# 设置时区
RUN ln -snf /usr/share/zoneinfo/Asia/Shanghai /etc/localzone

EXPOSE 10000

ENTRYPOINT ["java", "-Xmx256m", "-Xms256m", "-jar", "app.jar"]
```

### 13.3 监控与日志

1. **日志级别**: ERROR, WARN, INFO, DEBUG
2. **日志格式**: 时间 + 级别 + 类名 + 内容
3. **日志文件**: 按天切割，保留 30 天
4. **监控指标**: 定时任务执行次数、API 调用成功率

---

## 14. 扩展性指导

### 14.1 新增 DNS 服务商

按以下步骤添加新的 DNS 服务商：

1. **创建策略实现类**: 实现 DynamicDnsStrategy 接口
2. **添加工具类**: 封装云厂商 API 调用
3. **注册枚举值**: 在 ServiceProviderEnum 中添加
4. **配置映射**: 在 application.yml 中配置 Bean 映射
5. **更新前端**: 添加服务商选择选项
6. **编写测试**: 添加单元测试和集成测试
7. **更新文档**: 更新技术文档和本规格文档

### 14.2 新增 IP 获取方式

按以下步骤添加新的 IP 获取方式：

1. **创建获取器类**: 继承 AbstractIpGetter 或实现 IpGetter 接口
2. **注册获取器**: 在 IP 获取工厂中注册
3. **更新枚举**: 在 GetIpModeEnum 中添加新模式
4. **更新前端**: 添加相应的 UI 选项

### 14.3 新增页面功能

按以下步骤添加新的前端页面：

1. **创建视图组件**: 在 src/views/ 下创建
2. **配置路由**: 在 src/router/index.ts 中添加
3. **添加 API**: 在 src/api/ 下封装 API 调用
4. **更新导航**: 在布局组件中添加菜单入口
5. **添加权限控制**: 如需要，配置路由守卫

---

## 15. 代码质量检查清单

### 15.1 代码审查要点

- [ ] 代码符合本规格文档的规范
- [ ] 所有 public 方法有 Javadoc 注释
- [ ] 参数校验完整，异常信息清晰
- [ ] 无硬编码的配置或魔法数字
- [ ] 单元测试覆盖核心逻辑
- [ ] API 文档已更新

### 15.2 前端审查要点

- [ ] TypeScript 类型定义完整
- [ ] 组件 Props 和 Emits 有类型定义
- [ ] 遵循 Tailwind CSS 设计系统
- [ ] 响应式布局正确
- [ ] 错误处理完善
- [ ] 加载状态和空状态有处理

---

**文档版本**: v1.0
**最后更新**: 2026-05-23
**维护者**: DDNS4J 开发团队
