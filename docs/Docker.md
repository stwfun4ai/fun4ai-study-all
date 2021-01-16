# 自己玩玩

**先查看https://hub.docker.com/ 具体环境的description有启动带参命令**

```bash
#nacos单机版
docker pull nacos/nacos-server
docker run --name nacos-standalone -e MODE=standalone -p 8848:8848 -d nacos/nacos-server:latest

#sentinel
docker pull bladex/sentinel-dashboard
docker run --name sentinel  -d -p 8858:8858 -d  bladex/sentinel-dashboard

#zipkin
docker pull openzipkin/zipkin:latest
docker run -d -p 9411:9411 openzipkin/zipkin

#rabbitmq(带management管理界面)
docker pull rabbitmq:3.7.7-management	
docker run -d --name rabbitmq3.7.7 -p 5672:5672 -p 15672:15672  --hostname myRabbit  -e RABBITMQ_DEFAULT_USER=admin -e RABBITMQ_DEFAULT_PASS=admin rabbitmq:3.7.7-management

#elasticSearch 限制内存
docker run -d --name elasticsearch -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" -e ES_JAVA_OPTS="-Xms64m -Xmx512m" elasticsearch:7.6.2

#mysql
docker run -d -p 3306:3306 -v /home/mysql/conf:/etc/mysql/conf.d -v /home/mysql/data:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=root --name mysql01 mysql:5.7

#redis
docker run -d --name f-redis -p 6379:6379 redis redis-server
#客户端连接
docker exec -it f-redis redis-cli



##### 待玩集群mysql nginx #####

#提交镜像
docker commit -a="fun4ai" -m="add webapps" 容器id 提交镜像名称:版本号
```



![image-20201114223654259](images\docker commandLine.png)



# 安装



## 安装步骤



1. 官网安装参考手册：https://docs.docker.com/engine/install/centos/

2. 确定CentOS7以上版本

3. yum安装gcc相关环境（需确保虚拟机可以上外网）

   ```bash
   yum -y install gcc
   yum -y install gcc-c++
   ```

4. 卸载旧版本

   ```bash
   yum remove docker \
                     docker-client \
                     docker-client-latest \
                     docker-common \
                     docker-latest \
                     docker-latest-logrotate \
                     docker-logrotate \
                     docker-engine
   ```

5. 安装需要的软件包

   ```bash
   yum install -y yum-utils
   ```

6. 设置镜像仓库

   ```bash
   # 使用阿里云镜像
   yum-config-manager \
       --add-repo \
       http://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
   ```

7. 更新yum软件包索引

   ```bash
   yum makecache fast
   ```

8. 安装Docker CE

   ```bash
   yum install -y docker-ce docker-ce-cli containerd.io
   ```

9. 启动Docker

   ```bash
   systemctl start docker
   ```

10. 测试命令

    ```bash
    docker version
    
    docker run hello-world
    
    docker images
    ```

11. 卸载

    ```bash
    systemctl stop docker
    
    yum -y remove docker-ce docker-client containerd.io
    
    rm -rf /var/lib/docker
    ```

## ==阿里云镜像加速==

```bash
sudo mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": ["https://y04yfsuz.mirror.aliyuncs.com"]
}
EOF
sudo systemctl daemon-reload
sudo systemctl restart docker
```













# 容器数据卷

​	目录挂载，双向绑定

```bash
docker run -d -v 主机目录:容器目录 容器id 
```

 

**具名和匿名挂载**

```bash
#匿名挂载
docker run -d -P --name nginx01 -v /etc/nginx nginx
docker volume ls

#具名挂载
docker run -d -P --name nginx-2 -v juming-nginx:/etc/nginx nginx
docker volume ls
docker volume inspect juming-nginx
[
	{
		...
		"Mountpoint": "/var/lib/docker/volumes/juming-ngin/_data",
		"Name": "juming-nginx",
		...
	}
]
```

默认目录 **/var/lib/docker/volumes/xxx/_data**



```bash
#ro只读，只能通过宿主机操作，容器无法操作！
#rw读写
docker run -d -P --name nginx-2 -v juming-nginx:/etc/nginx:ro nginx
```

**dockerfile挂载**

```bash
#新建dockerfile1文件内容如下(用的匿名挂载)
#指令（大写） 参数
FROM centos
VOLUME ["volume01","volume02"]
CMD echo "-----end-----"
CMD /bin/bash
```

```bash
docker build -f /home/dockertest/dockerfile1 -t fun4ai/centos .
```

运行容器查看`docker inspect 容器id` 可查看挂载路径



**数据卷容器**

多个mysql同步数据

```bash
docker run -d -p 3306:3306 -v /etc/mysql/conf.d -v /var/lib/mysql -e MYSQL_ROOT_PASSWORD=root --name mysql01 mysql:5.7

docker run -d -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root --name mysql02 --volume-from mysql01 mysql:5.7
#这个时候可以实现俩个容器数据同步
```



> 结论

​	容器之间配置信息的传递，数据卷容器的生命周期一直持续到没有容器使用为止。

但是一旦你持久化到本地，这个时候本地的数据是不会删除的。







# DockerFile



![image-20201115130622102](images\dockerfile1.png)

## DockerFile指令



![image-20201115130854739](images\dockerfile指令.png)

```bash
CMD			#指定容器启动时运行命令，之后最后一个会生效，可被替代
ENTRYPOINT  #指定容器启动时运行命令，可追加命令
ONBUILD		#当构建一个被继承DockerFile会运行指令，触发指令。
COPY		#类似ADD，将文件拷贝到镜像中
ENV			#构建时候设置环境变量
```

## 实战测试

​	1、构建centos添加vim net

![image-20201115132506197](images\dockerfile-build-centos.png)

```bash
docker history 镜像id	#查看镜像构建步骤历史
```

2、构建Tomcat镜像

1. 准备镜像文件：tomcat压缩包、jdk压缩包
2. 编写dockerfile文件，官方命名`Dockerfile`，build会自动寻找这个文件，就不需要-f指定了

```bash
FROM centos
MAINTAINER fun4ai<pepsily113@163.com>

COPY readme.txt /usr/local/readme.txt

ADD jdk-8u-11-linux-x64.tar.gz /usr/local/
ADD apache-tomcat-9.0.22.tar.gz /usr/local/

RUN yum -y install vim

ENV MYPATH /usr/local
WORKDIR $MYPATH

ENV JAVA_HOME /usr/local/jdk1.8.0_11
ENV CLASSPATH $JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar
ENV CATALINA_HOME /usr/local/apache-tomcat-9.0.22
ENV CATALINA_BASH /usr/local/apache-tomcat-9.0.22
ENV PATH $PATH:$JAVA_HOME/bin:$CATALINA_HOME/lib:$CATALINA_HOME/bin

EXPOSE 8080

CMD /usr/local/apache-tomcat-9.0.22/bin/startup.sh && tail -F /usr/local/apache-tomcat-9.0.22/bin/logs/catalina.out
```

3. 构建镜像

   ```
   docker build -t diytomcat
   ```

   

4. 启动镜像

   ```
   docker run -d -p 8080:8080 --name mytomcat -v /home/fun4ai/build/tomcat/test:/usr/local/apache-tomcat-9.0.22/webapps/test -v /home/fun4ai/build/tomcat/tomcatlogs:/usr/local/apache-tomcat-9.0.22/logs diytomcat
   ```

5. 访问测试

6. 发布项目（由于做了卷挂载，可以直接在本地编写项目发布。）

## 发布镜像

> docker hub

```bash
docker login -u fun4ai
#输入密码
docker tag 镜像id fun4ai/diytomcat:1.0
docker push fun4ai/diytomcat:1.0
```

> 发布到阿里云

登录找到容器镜像服务，创建命名空间，创建容器镜像push



# Docker 网络

理解docker0

docker 是如何处理容器网络访问的？

```bash
docker run -d -P --name tomcat01 tomcat
#查看容器内部网络地址ip addr，容器启动时会得到一个eth0@if262 ip地址，docker分配的。
docker exec -it tomcat01 ip addr
#linux 可以ping通容器内部
ping xxx.xxx.xxx.xxx
```

> 原理

1、我们每启动一个docker容器，docker就会给容器分配一个ip,我们只要安装了docker，就会有一个网卡docker0

**桥接模式，使用的是evth-pair技术！**

```bash
# 容器带来网卡都是成对出现
# evth-pair 就是一堆虚拟设备接口，成对出现一端连着协议，一端彼此相连
# 正因为这个特性，evth-pair充当一个桥梁，连接各种虚拟网络设备
# OpenStac、Docker容器之间的连接、OVS的连接，都是使用evth-pair技术
```

3、docker容器之间可以互相ping通

![image-20201115230634080](images\idocker net.png)

> 结论：所有容器不指定网络的情况下，都是docker0路由的， 分配一个默认可用IP



> 思考场景

​		编写一个微服务 database url = ip;项目重启，数据库ip换掉了，希望可以通过名字而不是ip地址来访问容器？

​		

```bash
# 通过添加--link解决网络连通问题
docker run -d -P --name tomcat02 --link tomcat01 tomcat
docker exec -it tomcat02 ping tomcat01 #可以ping通
```

`--link`就是在镜像hosts文件中增加一个 172.18.03 tomcat01 xxxx 映射

**不建议使用!**`--link`



**现在自定义网络，不使用docker0!它存在的问题是不支持容器文件名访问！**

## 自定义网络

> 查看所有docker网络

```bash
docker network ls	
```

**网络模式：**

- Bridge: 桥接 docker(默认，自己创建也使用桥接模式)
- none: 不配置网络
- host: 和宿主机共享网络
- container: 容器内网络连通（用的少，局限大）

```bash
#创建自定义网络
docker network create --driver bridge --subnet 192.168.0.0/16 --gateway 192.168.0.1 mynet
#查看docker网络
docker network ls
docker network inspect mynet

#启动容器使用自定义网络
docker run -d -P --name tomcat-net-01 --net mynet tomcat
docker run -d -P --name tomcat-net-02 --net mynet tomcat

#ping测试都能ping通，不需要--link
docker exec -it tomcat-net-01 ping 192.168.0.3
docker exec -it tomcat-net-01 ping tomcat-net-02
```

好处：redis/mysql 不同的集群使用不同的网络保证安全健康！

## 网络连通

```bash
# 测试使用默认网卡创建的tomcat-01容器 打通到mynet
docker network connect mynet tomcat01
# 连通之后就是将tomcat-01放到mynet网络下192.168.0.4
docker network inspect mynet
#一个容器俩个IP地址	如阿里云服务公网IP、私网IP

#测试tomcat-01 ping tomcat-net-01  OK!
docker exec  -it tomcat01 ping tomcat-net-01

#tomcat02依旧打不通的
docker exec  -it tomcat02 ping tomcat-net-01
```

> 结论

**假设要跨网络操作别人，就需要使用docker network connect连通！**



# 实战部署redis集群

???



# SpringBoot微服务打包Docker镜像

1. 构建SpringBoot项目

2. 打包应用

3. 编写Dockerfile

   ```bash
   FROM java:8
   COPY *.jar /app.jar
   CMD ["---server.port=8080"]
   EXPOSE 8080
   ENTRYPOINT ["java","-jar","/app.jar"]
   ```

4. 构建镜像

5. 发布运行

```bash
# -t name:tag （镜像名称：标签）
# -f Dockerfile文件，当前目录可省略
# 最后.代表当前路径不能少
docker build -t fun4ai/myspringboot:1.0 .
```















# 企业实战

# Docker Compose

步骤

- 使用 Dockerfile 定义应用程序的环境。
- 使用 docker-compose.yml 定义构成应用程序的服务，这样它们可以在隔离环境中一起运行。
- 最后，执行 docker-compose up 命令来启动并运行整个应用程序。

```yaml
# yaml 配置实例
version: '3.8'
services:
  web:
    build: .
    ports:
   		- "5000:5000"
    volumes:
   		- .:/code
    	- logvolume01:/var/log
    links:
   		- redis
  redis:
    image: redis
volumes:
  logvolume01: {}
```



## 安装

Linux 上我们可以从 Github 上下载它的二进制包来使用，最新发行的版本地址：https://github.com/docker/compose/releases。

```bash
# 安装其他版本更换1.27.4
sudo curl -L "https://github.com/docker/compose/releases/download/1.27.4/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose

# 将可执行权限应用于二进制文件
sudo chmod +x /usr/local/bin/docker-compose
# 创建软链
sudo ln -s /usr/local/bin/docker-compose /usr/bin/docker-compose
# 测试安装是否成功
docker-compose --version
```



## 体验

1、应用

2、Dockerfile 将应用打包为镜像

3、 docker-compose.yml文件(定义整个服务，需要的环境web、redis....)完整的上线服务。

4、启动compose项目（docker-compose up）



**Docker小结**

1、Docker镜像。run=>容器

2、Dockerfile 构建镜像（服务打包）

3、docker-compose 启动项目（编排、多个微服务/环境）

4、docker网络



**yaml规则**

https://docs.docker.com/compose/compose-file/

```bash
# 3层

version: '' # 版本
services:	# 服务
	服务1：web
	#服务配置
		images
		build
		network
	
	服务2：redis
# 其他配置 网络/卷、全局规则
volumes:
network:
config:
```

## wordpress

https://docs.docker.com/compose/wordpress/

## 实战

1、编写项目微服务

2、 Dockerfile构建镜像

```bash
FROM java:8
COPY *.jar /app.jar
CMD ["------server.port=8080"]
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
```



3、docker-compose.yml编排项目

```bash
version: '3.8'
services:
  fun4aiapp:
    build: .
    image: fun4aiapp:1.0
    depends_on:
      - redis
    ports:
      - "8080:8080"
  redis:
    image: "redis:alpine"
```



4、丢到服务器docker-compose up



```bash
docker-compose up --build 		#重新构建
```































# Docker Swarm









# CI/CD Jenkins 流水线