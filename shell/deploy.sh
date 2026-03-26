#!/bin/bash
set -e

## 第一步：删除可能启动的老 platform-boot 容器
echo "开始删除 platform-boot 容器"
docker stop platform-boot || true
docker rm platform-boot || true
echo "完成删除 platform-boot 容器"

## 第二步：启动新的 platform-boot 容器 \
echo "开始启动 platform-boot 容器"
docker run -d \
--name platform-boot \
-p 9999:9999 \
-e "SPRING_PROFILES_ACTIVE=docker-65" \
-v /opt/deploy/jenkins/ai-backend/logs:/opt/deploy/backend/platform/logs/ \
platform-boot
echo "正在启动 platform-boot 容器中，需要等待 60 秒左右"