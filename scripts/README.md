# DDNS4J 启动脚本使用说明

## 🚀 快速启动

### Windows
双击运行：`scripts\start-dev.bat`

或从命令行执行（可查看错误信息）：
```cmd
cd d:\codes\projects\ddns4j
scripts\start-dev.bat
```

### Linux/Mac

**首次运行**（添加执行权限）：
```bash
chmod +x scripts/start-dev.sh
```

**启动**：
```bash
./scripts/start-dev.sh
```

或从任意目录：
```bash
/path/to/ddns4j/scripts/start-dev.sh
```

## 📋 脚本列表

| 脚本 | 平台 |
|------|------|
| `start-dev.bat` | Windows |
| `start-dev.sh` | Linux/Mac |

## 💡 功能

- ✅ 自动检查依赖（Node.js、Java、Maven）
- ✅ 自动安装前端依赖（首次运行）
- ✅ 后端在新窗口启动，前端在当前窗口
- ✅ 智能等待后端启动（最多90秒）
- ✅ 自动检测后端是否就绪后再启动前端
- ✅ 自动打开浏览器 http://localhost:3000
- ✅ Windows 使用 PowerShell 检测后端状态
- ✅ Linux/Mac 使用 curl 检测后端状态

## 🔧 手动启动

**生产模式**（已构建）：
```bash
mvn spring-boot:run -pl ddns4j-web
```
访问 http://localhost:10000

**开发模式**（热重载）：
```bash
# 终端1 - 后端
mvn spring-boot:run -pl ddns4j-web

# 终端2 - 前端
cd ddns4j-web/src/main/resources/static
npm run dev
```
访问 http://localhost:3000

## 🐛 常见问题

**Q: 脚本闪退？**  
A: 从命令行执行以查看错误信息：
```cmd
cd d:\codes\projects\ddns4j
scripts\start-dev.bat
```

**Q: 后端启动失败或无法访问？**  
A: 检查以下几点：
1. 确保从项目根目录运行（包含 pom.xml 的目录）
2. 查看后端窗口的输出信息
3. Windows: 检查是否有 Maven 和 Java 环境变量
4. Linux/Mac: 查看日志文件 `cat /tmp/ddns4j-backend-*.log`

手动测试后端：
```cmd
cd d:\codes\projects\ddns4j
mvn spring-boot:run -pl ddns4j-web
```
然后访问 http://localhost:10000/publicAccess/publicAccessDisabled

**Q: 找不到 Node.js/Java/Maven？**  
A: 确保已安装并添加到系统 PATH：
- Node.js: https://nodejs.org/
- Java JDK 8+: https://adoptium.net/
- Maven: https://maven.apache.org/

验证安装：
```cmd
node -v
java -version
mvn -version
```

**Q: 浏览器没打开？**  
A: 手动访问 http://localhost:3000

**Q: 端口被占用？**  
A: 关闭占用端口的程序或修改配置

**Q: Linux 下提示 "Permission denied"？**  
A: 添加执行权限：
```bash
chmod +x scripts/start-dev.sh
```

**Q: Linux 下浏览器没打开？**  
A: 确保安装了 `xdg-open`：
```bash
# Ubuntu/Debian
sudo apt-get install xdg-utils
# CentOS/RHEL
sudo yum install xdg-utils
```

**Q: macOS 下浏览器没打开？**  
A: 脚本会自动使用 `open` 命令，如仍有问题请手动访问 http://localhost:3000

**Q: 后端启动失败？**  
A: 查看日志文件：
```bash
cat /tmp/ddns4j-backend-*.log
```
