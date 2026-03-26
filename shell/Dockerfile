#获取base image
FROM registry.cn-hangzhou.aliyuncs.com/dockerhub_mirror/java:21-anolis
#类似于执行 linux指令
RUN mkdir -p /platform-boot

WORKDIR /platform-boot
#类似于linux copy指令
COPY platform-boot.jar app.jar
#对外端口
EXPOSE 9999
ENV TZ=Asia/Shanghai
ENV LANG C.UTF-8
ENV LC_ALL C.UTF-8
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
#执行命令 java -jar /opt/app/platform-boot.jar
CMD java -jar app.jar $ARGS
