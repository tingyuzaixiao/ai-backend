<p align="center" style="font-size: xx-large">AI算法训练平台-后端</p>
<p align="center">
 <img src="https://img.shields.io/badge/Platform-1.0-success.svg" alt="Build Status">
 <img src="https://img.shields.io/badge/Spring%20Cloud-2023-blue.svg" alt="Coverage Status">
 <img src="https://img.shields.io/badge/Spring%20Boot-3.2-blue.svg" alt="Downloads">
 <img src="https://img.shields.io/badge/Vue-3.4-blue.svg" alt="Downloads">
 <img src="https://img.shields.io/github/license/pig-mesh/pig"/>
</p>

## 系统说明

- 基于 Spring Cloud 、Spring Boot、 OAuth2 的 RBAC **企业快速开发平台**， 同时支持微服务架构和单体架构
- 提供对 Spring Authorization Server 生产级实践，支持多种安全授权模式
- 提供对常见容器化方案支持 Kubernetes、Rancher2 、Kubesphere、EDAS、SAE 支持

#### 分支说明

- jdk17: java17/21 + springboot 3.2 + springcloud 2023
## 快速开始

### 核心依赖

| 依赖                          | 版本         |
|-----------------------------|------------|
| Spring Boot                 | 3.2.4      |
| Spring Cloud                | 2023.0.1   |
| Spring Cloud Alibaba        | 2022.0.0.0 |
| Spring Authorization Server | 1.2.4      |
| Mybatis Plus                | 3.5.6      |
| Vue                         | 3.4        |
| Element Plus                | 2.6        |

### 模块说明

```lua
platform-ui  -- https://github.com/mr-nealon/ai-page.git

platform
├── platform-boot -- 单体模式启动器
├── platform-auth -- 授权服务提供
└── platform-common -- 系统公共模块
     ├── platform-common-bom -- 全局依赖管理控制
     ├── platform-common-core -- 公共工具类核心包
     ├── platform-common-datasource -- 动态数据源包
     ├── platform-common-log -- 日志服务
     ├── platform-common-oss -- 文件上传工具类
     ├── platform-common-mybatis -- mybatis 扩展封装
     ├── platform-common-seata -- 分布式事务
     ├── platform-common-security -- 安全工具类
     ├── platform-common-swagger -- 接口文档
     ├── platform-common-feign -- feign 扩展封装
     └── platform-common-xss -- xss 安全封装
├── platform-register -- Nacos Server
├── platform-gateway -- Spring Cloud Gateway网关
└── platform-upms -- 通用用户权限管理模块
     └── platform-upms-api -- 通用用户权限管理系统公共api模块
     └── platform-upms-biz -- 通用用户权限管理系统业务处理模块
```

### 本地开发 运行

platform 提供了详细的[部署文档 wiki.pig4cloud.com](https://www.yuque.com/pig4cloud/pig/vsdox9)，包括开发环境安装、服务端代码运行、前端代码运行等。

请务必**完全按照**文档部署运行章节 进行操作，减少踩坑弯路！！

### Docker 运行

```
# 下载并运行服务端代码
git clone https://github.com/mr-nealon/ai-backend.git

cd platform && mvn clean install && docker-compose up -d

# 下载并运行前端UI
git clone https://github.com/mr-nealon/ai-page.git

cd platform-ui && npm install -g cnpm --registry=https://registry.npm.taobao.org


cnpm install && cnpm run build:docker && cd docker && docker-compose up -d
```

