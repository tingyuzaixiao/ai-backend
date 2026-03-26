FROM registry.cn-hangzhou.aliyuncs.com/dockerhub_mirror/mysql-server:8.0.32

MAINTAINER lee(witer_lee@163.com)

ENV TZ=Asia/Shanghai

RUN ln -sf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

COPY ./pig.sql /docker-entrypoint-initdb.d

COPY ./pig_config.sql /docker-entrypoint-initdb.d

EXPOSE 3306
