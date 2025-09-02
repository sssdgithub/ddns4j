# DDNS4J v1.6.5

**DDNS4J -- 让动态域名解析变的更简单**

<p align="center">
<a href='https://gitee.com/Xsssd/ddns4j/stargazers'><img src='https://gitee.com/Xsssd/ddns4j/badge/star.svg?theme=dark' alt='star'></img></a>
<a href='https://gitee.com/Xsssd/ddns4j/members'><img src='https://gitee.com/Xsssd/ddns4j/badge/fork.svg?theme=dark' alt='fork'></img></a>
<a href='https://img.shields.io/badge/license-apache-blue'><img src='https://img.shields.io/badge/license-apache-blue'></img></a>
</p>

---

## 项目简介

DDNS4J 是一个基于 SpringBoot 和 Amis 开发的完全免费开源 DDNS 服务，支持 IPv4 和 IPv6，能够帮助用户动态更新域名解析记录，从而方便地将个人服务器或家庭网络对外提供服务。

### 核心功能
- 支持阿里云、腾讯云、Cloudflare 和华为云等主流 DNS 服务商
- 提供 IPv4 和 IPv6 双栈支持
- 自动识别公网 IP 地址并更新解析记录
- 提供可视化 Web 管理界面
- 支持定时任务自动更新
- 提供日志记录与管理功能

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
- **IP 自动识别**：支持通过网卡或网络接口自动获取公网 IP
- **定时更新**：可设置更新频率（每分钟、每小时、每天）
- **日志查看**：记录所有解析更新操作日志
- **多服务支持**：支持阿里云、腾讯云、Cloudflare、华为云等平台
- **代理支持**：Cloudflare 默认开启代理模式

---

## 技术栈

### 后端
- SpringBoot
- MyBatisPlus
- Quartz 定时任务
- RestTemplate 网络请求
- MySQL / H2 数据库

### 前端
- Amis 可视化框架
- HTML / CSS / JavaScript

---

## 项目结构

```
├── doc/                # 文档与图片资源
├── src/                # 源码目录
│   ├── main/
│   │   ├── java/       # Java 源代码
│   │   └── resources/  # 配置文件与静态资源
│   └── test/           # 测试代码
└── pom.xml             # Maven 项目配置
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