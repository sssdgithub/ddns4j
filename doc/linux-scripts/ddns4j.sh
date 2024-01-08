#!/bin/bash

JAR_VERSION="ddns-v1.6.4-RELEASE.jar"

if [ -f /etc/os-release ]; then
    source /etc/os-release
    if [[ "$ID" == "centos" ]]; then
        echo "检测到您的系统是: centos,接下来会安装ddns4j的运行环境请稍后..."
        sudo yum install java-1.8.0-openjdk -y
    elif [[ "$ID" == "ubuntu" ]]; then
        echo "检测到您的系统是: ubuntu,接下来会安装ddns4j的运行环境请稍后..."
        sudo apt-get install openjdk-8-jdk -y
    elif [[ "$ID" == "opencloudos" ]]; then
        echo "检测到您的系统是: opencloudos,接下来会安装ddns4j的运行环境请稍后..."
        sudo yum install java-1.8.0-openjdk -y
    else
        echo "System type: Unknown"
    fi
else
    echo "System type: Unknown"
fi

getAbsolutePath() {
    # 获取当前脚本的绝对路径
    SCRIPT="\$0"
    if [ -h "$SCRIPT" ]; then
        # 如果是符号链接，则获取符号链接的绝对路径
        while [ -h "$SCRIPT" ]; do
            SCRIPT=$(readlink "$SCRIPT")
        done
    fi
    # 返回脚本所在目录的绝对路径
    (cd "$(dirname "$SCRIPT")" && pwd)
}

install() {
  curPath=$(getAbsolutePath)
  echo $curPath
  # 创建一个新的.service文件
  sudo tee /etc/systemd/system/ddns4j.service > /dev/null << EOF
  [Unit]
  Description=ddns4j service
  After=network.target

  [Service]
  User=root
  Type=simple
  WorkingDirectory=$curPath
  ExecStart=/usr/bin/java -jar -Xmx500m -Xms500m $curPath/$JAR_VERSION
  ExecStop=/bin/kill -s QUIT $MAINPID
  Restart=always

  [Install]
  WantedBy=multi-user.target
EOF

  # 重新加载systemd管理的服务
  sudo systemctl daemon-reload

  # 启用服务
  sudo systemctl enable ddns4j.service

  # 启动服务
  sudo systemctl start ddns4j.service
}

uninstall() {
  sudo systemctl stop ddns4j.service
  sudo systemctl disable ddns4j.service

  if [ -f /etc/os-release ]; then
    source /etc/os-release
    if [[ "$ID" == "centos" ]]; then
        echo "检测到您的系统是: centos,接下来会卸载ddns4j的运行环境请稍后..."
        sudo yum remove java-1.8.0-openjdk -y
    elif [[ "$ID" == "ubuntu" ]]; then
        echo "检测到您的系统是: ubuntu,接下来会卸载ddns4j的运行环境请稍后..."
        sudo apt-get remove openjdk-8-jdk -y
    elif [[ "$ID" == "opencloudos" ]]; then
        echo "检测到您的系统是: opencloudos,接下来会卸载ddns4j的运行环境请稍后..."
        sudo yum remove java-1.8.0-openjdk -y
    else
        echo "System type: Unknown"
    fi
  else
    echo "System type: Unknown"
  fi
}


if [ $1 = "install" ];then
        install
        echo "安装成功!请打开浏览器在页面中您的ip:10000查看管理"
elif [ $1 = "uninstall" ];then
        uninstall
        echo "卸载成功!"
else
        echo "Usage: ./ddns4j.sh [install | uninstall]"
        exit 1
fi
