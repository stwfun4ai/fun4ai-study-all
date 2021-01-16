# 				Spring Cloud升级

------

<img src="images\spring cloud升级.png" alt="images/" style="zoom:200%;" />

- 服务注册与发现中心
  - ~~Eureka~~	（AP）
  - **ZooKeeper**（CP）
  - **Consul**（CP）
  - **Nacos** （AP/CP）
- 服务调用
  - **Ribbon** 客户端框架
  - **LoadBalancer**
- 服务调用2
  - ~~Feign~~
  - **OpenFeign**
- 服务降级
  - ~~Hystrix~~
  - **resilience4j**
  - **sentinel**
- 服务网关
  - ~~Zuul~~
  - ~~Zuul2~~
  - **gateway**
- 服务配置
  - ~~Config~~
  - **Nacos**
- 服务总线
  - ~~Bus~~
  - **Nacos**
- 链路追踪
  - **slueth zipkin** 