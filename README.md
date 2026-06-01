# DDNS4J v1.6.5

**DDNS4J -- 让动态域名解析变的更简单**

<p align="center">
<a href='https://gitee.com/Xsssd/ddns4j/stargazers'><img src='https://gitee.com/Xsssd/ddns4j/badge/star.svg?theme=dark' alt='star'></img></a>
<a href='https://gitee.com/Xsssd/ddns4j/members'><img src='https://gitee.com/Xsssd/ddns4j/badge/fork.svg?theme=dark' alt='fork'></img></a>
<a href='https://img.shields.io/badge/license-apache-blue'><img src='https://img.shields.io/badge/license-apache-blue'></img></a>
</p>

---

## 项目简介

DDNS4J（Dynamic Domain Name System for Java）是一个基于 Spring Boot 和 Vue 3 开发的完全免费开源 DDNS 服务，支持 IPv4 和 IPv6，能够帮助用户动态更新域名解析记录，从而方便地将个人服务器或家庭网络对外提供服务，即使公网 IP 地址发生变化也能保持域名的可访问性。

### 核心功能
- 支持阿里云、腾讯云、Cloudflare 和华为云等主流 DNS 服务商
- 提供 IPv4（A 记录）和 IPv6（AAAA 记录）双栈支持
- 智能 IP 获取：支持通过网卡或网络接口自动获取公网 IP
- 提供基于 Vue 3 + Tailwind CSS 的现代化可视化 Web 管理界面
- 支持定时任务自动更新（1分钟、2分钟、5分钟、10分钟）
- 完整的操作日志记录与查询功能
- 支持 H2（嵌入式）和 MySQL 两种数据库

---

## 使用指南

### 支持平台
- Windows
- Linux
- Docker

### 安装步骤

#### Windows 平台
1. 从 [Releases](https://gitee.com/Xsssd/ddns4j/releases) 下载 `ddns4j_setup.exe` 安装包
2. 双击安装并按照提示完成安装

#### Linux 平台
1. 从 [Releases](https://gitee.com/Xsssd/ddns4j/releases) 下载 `ddns4j-linux.tar.gz`
2. 解压并授权执行：
   ```bash
   tar -zxvf ddns4j-linux.tar.gz && cd ddns4j && chmod +x ddns4j.sh
   ```
3. 安装并启动：
   ```bash
   ./ddns4j.sh install
   ```
4. 卸载：
   ```bash
   ./ddns4j.sh uninstall
   ```

#### Docker 部署
**方式一：使用阿里云镜像**
```bash
docker run -itd --name=ddns4j --restart=always --network=host registry.cn-hangzhou.aliyuncs.com/sssd/ddns4j:v1.6.5
```

**方式二：使用 Docker Hub 镜像**
```bash
docker run -itd --name=ddns4j --restart=always --network=host topsssd/ddns4j:v1.6.5
```

访问 `http://ip:10000` 进入管理界面

---

## 功能说明

- **域名管理**：支持添加、修改、复制、删除域名解析记录
- **IP 自动识别**：支持通过网卡或网络接口两种模式自动获取公网 IP
- **定时更新**：支持自定义更新频率（1分钟、2分钟、5分钟、10分钟）
- **日志查看**：记录所有 DNS 解析更新操作日志
- **多服务商支持**：支持阿里云、腾讯云、Cloudflare、华为云等平台
- **双栈协议**：同时支持 IPv4（A 记录）和 IPv6（AAAA 记录）

---

## 技术栈

### 后端
- **Spring Boot 2.7.x** - 核心应用框架
- **MyBatis-Plus 3.5.x** - ORM 框架，简化 CRUD 操作
- **Quartz** - 定时任务调度，每个解析记录独立任务
- **策略模式 + 工厂模式** - 多 DNS 服务商适配架构
- **RestTemplate** - HTTP 客户端，调用云厂商 API
- **H2 / MySQL** - 双数据库支持（嵌入式 / 生产级）
- **阿里云 SDK / 华为云 SDK** - 官方 SDK 集成

### 前端
- **Vue 3** - 核心框架（Composition API + `<script setup>`）
- **TypeScript** - 类型安全
- **Tailwind CSS** - 原子化 CSS 框架，基于 HSL 的语义化颜色系统
- **Vite** - 构建工具
- **Vue Router** - 路由管理
- **Pinia** - 状态管理

---

## 项目结构

```
├── ddns4j-common/         # 公共模块（常量、枚举、工具类、通用响应）
├── ddns4j-core/           # 核心业务模块
│   ├── config/            # 配置类（数据库、MyBatis-Plus、启动任务）
│   ├── handler/           # 处理器（自动填充、SQL 注入器）
│   ├── mapper/            # MyBatis Mapper 接口
│   ├── model/             # 数据模型（实体类、响应对象）
│   ├── service/           # 服务层接口与实现
│   ├── strategy/          # 策略模式实现（多 DNS 服务商适配）
│   ├── task/              # 定时任务（DynamicDnsJob、ClearLogJob）
│   └── utils/             # DNS API 工具类
├── ddns4j-web/            # Web 模块
│   ├── config/            # Web 配置
│   ├── controller/        # REST API 控制器
│   ├── handler/           # 异常处理器
│   ├── interceptor/       # 拦截器（公网访问控制）
│   └── src/main/resources/static/  # Vue 3 前端项目
├── doc/                   # 文档与图片资源
├── scripts/               # 部署脚本
└── pom.xml                # 父 POM（Maven 多模块管理）
```

---

## 开源许可

本项目使用 Apache-2.0 协议，详情请查看 [LICENSE](LICENSE) 文件。

---

## 联系方式

加入交流群获取更多帮助：
![QQ群二维码](doc/ddns4j交流群(一)群二维码.png)

---

## 更多信息

- 项目演示地址：[https://demo.sssd.top](https://demo.sssd.top)
- 项目官方地址：[https://ddns4j.sssd.top](https://ddns4j.sssd.top)
- 作者博客：[https://blog.sssd.top](https://blog.sssd.top)

---

> DDNS4J 是一个免费开源项目，欢迎参与贡献！