#!/bin/bash
set -e

# 基础路径
BASE_PATH=/opt/deploy/jenkins/ai-backend/
# 编译后 jar 的地址。部署时，Jenkins 会上传 jar 包到该目录下
SOURCE_PATH=$BASE_PATH/build
# 服务名称。同时约定部署服务的 jar 包名字也为它。
SERVER_NAME=platform-boot
# 环境
PROFILES_ACTIVE=docker-65

# 备份
function backup() {
    # 如果不存在，则无需备份
    if [ ! -f "$BASE_PATH/$SERVER_NAME.jar" ]; then
        echo "[backup] $BASE_PATH/$SERVER_NAME.jar 不存在，跳过备份"
    # 如果存在，则备份到 backup 目录下，使用时间作为后缀
    else
        echo "[backup] 开始备份 $SERVER_NAME ..."
        cp $BASE_PATH/$SERVER_NAME.jar $BASE_PATH/backup/$SERVER_NAME-$DATE.jar
        echo "[backup] 备份 $SERVER_NAME 完成"
    fi
}

# 最新构建代码 移动到项目环境
function transfer() {
    echo "[transfer] 开始转移 $SERVER_NAME.jar"

    # 删除原 jar 包
    if [ ! -f "$BASE_PATH/$SERVER_NAME.jar" ]; then
        echo "[transfer] $BASE_PATH/$SERVER_NAME.jar 不存在，跳过删除"
    else
        echo "[transfer] 移除 $BASE_PATH/$SERVER_NAME.jar 完成"
        rm $BASE_PATH/$SERVER_NAME.jar
    fi

    # 复制新 jar 包
    echo "[transfer] 从 $SOURCE_PATH 中获取 $SERVER_NAME.jar 并迁移至 $BASE_PATH ...."
    cp $SOURCE_PATH/$SERVER_NAME.jar $BASE_PATH

    echo "[transfer] 转移 $SERVER_NAME.jar 完成"
}

function build() {

    echo "开始构建镜像"
    cd /work/java/ai-backend
    docker build -t platform-boot .
    echo "镜像构建完成"
}


# 部署
function deploy() {
    DATE=$(date +%Y%m%d%H%M)
    cd $BASE_PATH
    # 备份原 jar
    backup
    # 部署新 jar
    transfer
}

deploy