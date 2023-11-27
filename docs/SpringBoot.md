# 什么是 Spring Boot？

Spring Boot设计目的是用来简化新 Spring 应用的初始搭建以及开发过程，是为了让开发人员尽可能快的创建并允许Spring 应用程序，尽可能减少项目的配置文件。

从最根本上来讲，Spring Boot 就是一些库的集合，它能够被任意项目的构建系统所使用。它使用 “习惯优于配置” （项目中存在大量的配置，此外还内置一个习惯性的配置）的理念让你的项目快速运行起来。所以 spring boot 其实不是什么新的框架，它默认配置了很多框架的使用方式，就像 maven 整合了所有的 jar 包，spring boot 整合了所有的框架。

总结起来就是：

（1）为所有 Spring 开发提供一个更快更广泛的入门体验。

（2）零配置。无冗余代码生成和XML 强制配置，遵循“**==约定大于配置==**” 。

（3）集成了大量常用的第三方库的配置， Spring Boot 应用为这些第三方库提供了几乎可以零配置的开箱即用的能力。

（4）提供一系列大型项目常用的非功能性特征，如嵌入式服务器、安全性、度量、运行状况检查、外部化配置等。

（5）Spring Boot 不是Spring 的替代者，Spring 框架是通过 IOC 机制来管理 Bean 的。Spring Boot 依赖 Spring 框架来管理对象的依赖。Spring Boot 并不是Spring 的精简版本，而是为使用 Spring 做好各种产品级准备。

## 环境准备

（1）JDK 环境必须是 1.8 及以上。

（2）开发工具建议使用 IDEA，也可以 Eclipse。我这边一直都是

 

## 用 idea 快速搭建 Spring Boot

创建一个新SpringBoot应用程序的方式有多种：

　　1、使用IDEA内置的Spring Initializr创建（File -> New -> Project -> Spring Initializr）

　　2、创建基础Maven项目，修改pom.xml添加spring-boot-parent

　　3、访问 https://start.spring.io 选择依赖后，生成项目并下载，导入到Idea即可。

# 返回统一的数据格式

- Controller：标识一个Spring类是Spring MVC controller处理器。

- RestController： 主要用于Restful接口，返回客户端数据请求。 @Controller + @ResponseBody

其实 RestController 给客户端返回数据时，一般会用jackson序列化返回。而不是直接返回整个pojo类对象。下面就简单介绍下如何统一返回json数据格式：

1、pojo类相关增加序列化格式配置，如上面的User对象的定义

```java
package com.weiz.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;

public class User {
    private  String name;

    @JsonIgnore
    private  String password;

    private Integer age;

    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss",locale = "zh",timezone = "GMT+8")
    private Date birthday;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private  String desc;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    //...
}
```

2、增加Json通用的封装类JsonUtils ，下面这个就是比较常用的json数据封装类。

```java
package com.weiz.utils;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @Title: JSONResult.java
 * @Package com.weiz.utils
 * @Description: 自定义响应数据结构
 *                 这个类是提供给门户，ios，安卓，微信商城用的
 *                 门户接受此类数据后需要使用本类的方法转换成对于的数据类型格式（类，或者list）
 *                 其他自行处理
 *                 200：表示成功
 *                 500：表示错误，错误信息在msg字段中
 *                 501：bean验证错误，不管多少个错误都以map形式返回
 *                 502：拦截器拦截到用户token出错
 *                 555：异常抛出信息
 * Copyright: Copyright (c) 2016
 * 
 * @author weiz
 * @date 2016年4月22日 下午8:33:36
 * @version V1.0
 */
public class JSONResult {

    // 定义jackson对象
    private static final ObjectMapper MAPPER = new ObjectMapper();

    // 响应业务状态
    private Integer status;

    // 响应消息
    private String msg;

    // 响应中的数据
    private Object data;
    
    private String ok;    // 不使用

    public static JSONResult build(Integer status, String msg, Object data) {
        return new JSONResult(status, msg, data);
    }

    public static JSONResult ok(Object data) {
        return new JSONResult(data);
    }

    public static JSONResult ok() {
        return new JSONResult(null);
    }
    
    public static JSONResult errorMsg(String msg) {
        return new JSONResult(500, msg, null);
    }
    
    public static JSONResult errorMap(Object data) {
        return new JSONResult(501, "error", data);
    }
    
    public static JSONResult errorTokenMsg(String msg) {
        return new JSONResult(502, msg, null);
    }
    
    public static JSONResult errorException(String msg) {
        return new JSONResult(555, msg, null);
    }

    public JSONResult() {

    }
    
    public JSONResult(Integer status, String msg, Object data) {
        this.status = status;
        this.msg = msg;
        this.data = data;
    }

    public JSONResult(Object data) {
        this.status = 200;
        this.msg = "OK";
        this.data = data;
    }

    public Boolean isOK() {
        return this.status == 200;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    /**
     * 
     * @Description: 将json结果集转化为LeeJSONResult对象
     *                 需要转换的对象是一个类
     * @param jsonData
     * @param clazz
     * @return
     * 
     * @author weiz
     * @date 2016年4月22日 下午8:34:58
     */
    public static JSONResult formatToPojo(String jsonData, Class<?> clazz) {
        try {
            if (clazz == null) {
                return MAPPER.readValue(jsonData, JSONResult.class);
            }
            JsonNode jsonNode = MAPPER.readTree(jsonData);
            JsonNode data = jsonNode.get("data");
            Object obj = null;
            if (clazz != null) {
                if (data.isObject()) {
                    obj = MAPPER.readValue(data.traverse(), clazz);
                } else if (data.isTextual()) {
                    obj = MAPPER.readValue(data.asText(), clazz);
                }
            }
            return build(jsonNode.get("status").intValue(), jsonNode.get("msg").asText(), obj);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 
     * @Description: 没有object对象的转化
     * @param json
     * @return
     * 
     * @author weiz
     * @date 2016年4月22日 下午8:35:21
     */
    public static JSONResult format(String json) {
        try {
            return MAPPER.readValue(json, JSONResult.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 
     * @Description: Object是集合转化
     *                 需要转换的对象是一个list
     * @param jsonData
     * @param clazz
     * @return
     * 
     * @author weiz
     * @date 2016年4月22日 下午8:35:31
     */
    public static JSONResult formatToList(String jsonData, Class<?> clazz) {
        try {
            JsonNode jsonNode = MAPPER.readTree(jsonData);
            JsonNode data = jsonNode.get("data");
            Object obj = null;
            if (data.isArray() && data.size() > 0) {
                obj = MAPPER.readValue(data.traverse(),
                        MAPPER.getTypeFactory().constructCollectionType(List.class, clazz));
            }
            return build(jsonNode.get("status").intValue(), jsonNode.get("msg").asText(), obj);
        } catch (Exception e) {
            return null;
        }
    }

    public String getOk() {
        return ok;
    }

    public void setOk(String ok) {
        this.ok = ok;
    }

}
```

3、如何调用

```java
@RestController   // RestController = Controller + ResponseBody
@RequestMapping("/user")
public class UserController {
    @RequestMapping("/getUser")
    //@ResponseBody
    public JSONResult getUserJson(){
        User u = new User();
        u.setName("weiz222");
        u.setAge(20);
        u.setBirthday(new Date());
        u.setPassword("weiz222");

        return JSONResult.ok(u);
    }
}
```

# 资源文件属性配置

## application.properties

(或者application.yml)中包含系统属性、环境变量、命令参数这类信息。[官方文档](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html#appendix.application-properties) 。

```properties
############################################################
#
# Server 服务器相关配置
#
############################################################
# 配置端口号
server.port=8080
# 配置context-path,一般这个配置在正式发布的时候不需要配置。 
#server.context-path=
# 错误页，指定发生错误时，跳转的URL --> BasicErrorController
#server.error.path=/error
# session最大超时时间（分钟），默认30分钟
server.session-timeout=60
# 服务绑定的IP地址，启动服务器时，如本机不是该IP地址则抛出异常启动失败，
# 所以此配置只有在特殊情况下才配置，具体更具各自的业务来设置。
#server.address=192.168.1.9

############################################################
# Server - tomcat 相关配置
############################################################
# tomcat最大线程数，默认200
#server.tomcat.max-threads=250
# tomcat的URI编码格式
server.tomcat.uri-encoding=UTF-8
# 存放Tomcat的日志，Dump等文件的临时文件夹，默认为系统但是tmp文件夹
# （如：C:\\Users\Zhang\AppData\Local\Temp）
#server.tomcat.basedir=D:/springboot-tomcat-tmp
# 打开Tomcat的Access日志，并可以设置日志格式的方法，
#server.tomcat.access-log-enabled=true
#server.tomcat.access-log-pattern=
# accesslog目录，默认在basedir/logs
#server.tomcat.accesslog.directory=
# 日志文件目录
#logging.path=H:/springboot-tomcat-tmp
# 日志文件名称，默认为spring.log
#logging.file=myapp.log
```

## 自定义配置

**1、增加自定义的resource.properties 文件**

```properties
com.weiz.resource.name=weiz
com.weiz.resource.website=www.weiz.com
com.weiz.resource.language=java
```

**2、增加Resource.java 类**

```java
package com.weiz.pojo;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix = "com.weiz.resource")
@PropertySource(value = "classpath:resource.properties")
public class Resource {
    private String name;
    private String website;
    private String language;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
```

**3、调用**

```java
	@Autowired
    private Resource resource;
    
    @RequestMapping("/index")
    public String index(ModelMap map) {
        map.addAttribute("name", resource.getName());
        return "freemarker/index";
    }
```

# Thymeleaf ==//todo==

[Thymeleaf](https://www.cnblogs.com/zhangweizhong/p/12391115.html)

# 整合Mybatis

- 注解形式的，也就是没有Mapper.xml文件
- XML形式的

## xml形式

**1、pom.xml增加mybatis相关依赖**

```xml
<dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>5.1.41</version>
        </dependency>
        <!--mybatis-->
        <dependency>
            <groupId>org.mybatis.spring.boot</groupId>
            <artifactId>mybatis-spring-boot-starter</artifactId>
            <version>1.3.1</version>
        </dependency>
        <!--mapper-->
        <dependency>
            <groupId>tk.mybatis</groupId>
            <artifactId>mapper-spring-boot-starter</artifactId>
            <version>1.2.4</version>
        </dependency>
        <!-- pagehelper -->
        <dependency>
            <groupId>com.github.pagehelper</groupId>
            <artifactId>pagehelper-spring-boot-starter</artifactId>
            <version>1.2.3</version>
        </dependency>
        <!-- druid 数据库连接框架-->
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid-spring-boot-starter</artifactId>
            <version>1.1.9</version>
        </dependency>
        <dependency>
            <groupId>org.mybatis.generator</groupId>
            <artifactId>mybatis-generator-core</artifactId>
            <version>1.3.2</version>
            <scope>compile</scope>
            <optional>true</optional>
        </dependency>
```

**2、application.properties配置数据连接**

```properties
############################################################
# 数据源相关配置，这里用的是阿里的druid 数据源
############################################################
spring.datasource.url=jdbc:mysql://localhost:3306/zwz_test
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
spring.datasource.druid.initial-size=1
spring.datasource.druid.min-idle=1
spring.datasource.druid.max-active=20
spring.datasource.druid.test-on-borrow=true
spring.datasource.druid.stat-view-servlet.allow=true

############################################################
# mybatis 相关配置
############################################################
mybatis.type-aliases-package=com.weiz.pojo
mybatis.mapper-locations=classpath:mapper/*.xml
#这个MyMapper 就是我之前创建的mapper统一接口。后面所有的mapper类都会继承这个接口
mapper.mappers=com.weiz.utils.MyMapper
mapper.not-empty=false
mapper.identity=MYSQL
# 分页框架
pagehelper.helperDialect=mysql
pagehelper.reasonable=true
pagehelper.supportMethodsArguments=true
pagehelper.params=count=countSql
```

**3、在启动主类SpringBootStarterApplication添加扫描器**

```java
@SpringBootApplication
//扫描 mybatis mapper 包路径
@MapperScan(basePackages = "com.weiz.mapper")     // 这一步别忘了。
//扫描 所有需要的包, 包含一些自用的工具类包 所在的路径
@ComponentScan(basePackages = {"com.weiz","org.n3r.idworker"})
public class SpringBootStarterApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootStarterApplication.class, args);
    }

}
```

> 注意：这一步别忘了，需要在SpringBootStarterApplication 启动类中增加包扫描器，自动扫描加载com.weiz.mapper 里面的mapper 类。

## 代码自动生成工具

Mybatis 整合完之后，接下来就是创建表和pojo类，mybatis提供了强大的自动生成功能。只需简单几步就能生成pojo 类和mapper。Spring Boot有Mybatis  generator自动生成代码插件，能自动生成pojo实体类、接口、mapper.xml 文件，提高开发效率。这里就不介绍怎么安装使用mybatis generator插件。介绍一个简单的GeneratorDisplay自动生成类。

**1、增加generatorConfig.xml配置文件**

在resources文件下创建generatorConfig.xml文件。此配置文件独立于项目，只是给自动生成工具类的配置文件，具体配置如下：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE generatorConfiguration
        PUBLIC "-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN"
        "http://mybatis.org/dtd/mybatis-generator-config_1_0.dtd">

<generatorConfiguration>
    <context id="MysqlContext" targetRuntime="MyBatis3Simple" defaultModelType="flat">
        <property name="beginningDelimiter" value="`"/>
        <property name="endingDelimiter" value="`"/>

        <plugin type="tk.mybatis.mapper.generator.MapperPlugin">
            <property name="mappers" value="com.weiz.utils.MyMapper"/>
        </plugin>
　　　　 <!-- 数据库连接配置-->　
        <jdbcConnection driverClass="com.mysql.jdbc.Driver"
                        connectionURL="jdbc:mysql://localhost:3306/zwz_test"
                        userId="root"
                        password="root">
        </jdbcConnection>

        <!-- 对于生成的pojo所在包 -->
        <javaModelGenerator targetPackage="com.weiz.pojo" targetProject="src/main/java"/>

        <!-- 对于生成的mapper所在目录 -->
        <sqlMapGenerator targetPackage="mapper" targetProject="src/main/resources"/>

        <!-- 配置mapper对应的java映射 -->
        <javaClientGenerator targetPackage="com.weiz.mapper" targetProject="src/main/java"
                             type="XMLMAPPER"/>


        <table tableName="sys_user"></table>
         
    </context>
</generatorConfiguration>
```

**2、数据库User表**

需要在数据库中创建相应的表。这个表结构很简单，就是普通的用户表sys_user。

```sql
CREATE TABLE `sys_user` (
  `id` varchar(32) NOT NULL DEFAULT '',
  `username` varchar(32) DEFAULT NULL,
  `password` varchar(64) DEFAULT NULL,
  `nickname` varchar(64) DEFAULT NULL,
  `age` int(11) DEFAULT NULL,
  `sex` int(11) DEFAULT NULL,
  `job` int(11) DEFAULT NULL,
  `face_image` varchar(6000) DEFAULT NULL,
  `province` varchar(64) DEFAULT NULL,
  `city` varchar(64) DEFAULT NULL,
  `district` varchar(64) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `auth_salt` varchar(64) DEFAULT NULL,
  `last_login_ip` varchar(64) DEFAULT NULL,
  `last_login_time` datetime DEFAULT NULL,
  `is_delete` int(11) DEFAULT NULL,
  `regist_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
```

**3、增加统一的mapper接口**

在utils包中创建一个统一的mapper接口，后面所有的mapper类都会继承这个接口。

```java
package com.weiz.utils;

import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

/**
 * 继承自己的MyMapper
 *
 * @author weiz
 * @since 2019-09-06 21:53
 */
public interface MyMapper<T> extends Mapper<T>, MySqlMapper<T> {
    //TODO
    //FIXME 特别注意，该接口不能被扫描到，否则会出错
}
```

MyMapper类是我自定义封装的Maper接口，它集成Mybatis的Mapper<T>, MySqlMapper<T>接口，这里面包含了默认的增删改查的方法。代码生成器自动生成的mapper类，都会统一集成MyMapper接口。

> 特别注意：该接口不能被扫描到，否则会出错。所以我把这个接口放在了utils包中。

**4、创建GeneratorDisplay类**

```java
package com.weiz.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

public class GeneratorDisplay {

    public void generator() throws Exception{

        List<String> warnings = new ArrayList<String>();
        boolean overwrite = true;
        //指定 逆向工程配置文件
        File configFile = new File("generatorConfig.xml"); 
        ConfigurationParser cp = new ConfigurationParser(warnings);
        Configuration config = cp.parseConfiguration(configFile);
        DefaultShellCallback callback = new DefaultShellCallback(overwrite);
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config,
                callback, warnings);
        myBatisGenerator.generate(null);

    } 
    
    public static void main(String[] args) throws Exception {
        try {
            GeneratorDisplay generatorSqlmap = new GeneratorDisplay();
            generatorSqlmap.generator();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
}
```

这个其实也是调用mybatis generator实现的。跟mybatis generator安装插件是一样的。

> 注意：利用Mybatis Generator自动生成代码，对于已经存在的文件会存在覆盖和在原有文件上追加的可能性，不宜多次生成。如需重新生成，需要删除已生成的源文件。

 **5、Mybatis Generator自动生成pojo和mapper**

运行GeneratorDisplay 如下图所示，即可自动生成相关的代码。

## 实现增删改查

在项目中整合了Mybatis并通过自动生成工具生成了相关的mapper和配置文件之后，下面就开始项目中的调用。

**1、在service包下创建UserService及UserServiceImpl接口实现类**

 创建com.weiz.service包，添加UserService接口类

```java
package com.weiz.service;

import com.weiz.pojo.SysUser;

import java.util.List;

public interface UserService {

    public void saveUser(SysUser user) throws Exception;

    public void updateUser(SysUser user);

    public void deleteUser(String userId);

    public SysUser queryUserById(String userId);

    public List<SysUser> queryUserList(SysUser user);

    public List<SysUser> queryUserListPaged(SysUser user, Integer page, Integer pageSize);　　 public void saveUserTransactional(SysUser user);
}
```

创建com.weiz.service.impl包，并增加UserServiceImpl实现类，并实现增删改查的功能，由于这个代码比较简单，这里直接给出完整的代码。

```java
package com.weiz.service.impl;

import com.github.pagehelper.PageHelper;
import com.weiz.mapper.SysUserMapper;
import com.weiz.mapper.SysUserMapperCustom;
import com.weiz.pojo.SysUser;
import com.weiz.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private SysUserMapper userMapper;

    @Override
    public void saveUser(SysUser user) throws Exception {
        userMapper.insert(user);
    }

    @Override
    public void updateUser(SysUser user) {
        userMapper.updateByPrimaryKeySelective(user);
    }

    @Override
    public void deleteUser(String userId) {
        userMapper.deleteByPrimaryKey(userId);
    }

    @Override
    public SysUser queryUserById(String userId) {
        return userMapper.selectByPrimaryKey(userId);
    }

    @Override
    public List<SysUser> queryUserList(SysUser user) {
        return null;
    }

    @Override
    public List<SysUser> queryUserListPaged(SysUser user, Integer page, Integer pageSize) {　　　　 // 分页功能实现
        PageHelper.startPage(page,pageSize);
        Example example = new Example(SysUser.class);
        Example.Criteria criteria = example.createCriteria();

        if (!StringUtils.isEmptyOrWhitespace(user.getUsername())) {
            criteria.andLike("username", "%" + user.getUsername() + "%");
        }

        if (!StringUtils.isEmptyOrWhitespace(user.getNickname())) {
            criteria.andLike("nickname", "%" + user.getNickname() + "%");
        }

        List<SysUser> userList = userMapper.selectByExample(example);

        return userList;
    }

    @Override
    public void saveUserTransactional(SysUser user) {

    }
}
```

**2、编写controller层，增加MybatisController控制器**

```java
package com.weiz.controller;

import com.weiz.utils.JSONResult;
import com.weiz.pojo.SysUser;
import com.weiz.service.UserService;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("mybatis")
public class MyBatisCRUDController {

    @Autowired
    private UserService userService;

    @Autowired
    private Sid sid;

    @RequestMapping("/saveUser")
    public JSONResult saveUser() throws Exception {
　　　　 // 自动生成ID，也可以直接用uuid
        String userId = sid.nextShort();

        SysUser user = new SysUser();
        user.setId(userId);
        user.setUsername("spring boot" + new Date());
        user.setNickname("spring boot" + new Date());
        user.setPassword("abc123");
        user.setIsDelete(0);
        user.setRegistTime(new Date());

        userService.saveUser(user);

        return JSONResult.ok("保存成功");
    }

    @RequestMapping("/updateUser")
    public JSONResult updateUser() {

        SysUser user = new SysUser();
        user.setId("10011001");
        user.setUsername("10011001-updated" + new Date());
        user.setNickname("10011001-updated" + new Date());
        user.setPassword("10011001-updated");
        user.setIsDelete(0);
        user.setRegistTime(new Date());

        userService.updateUser(user);

        return JSONResult.ok("保存成功");
    }


    @RequestMapping("/deleteUser")
    public JSONResult deleteUser(String userId) {

        userService.deleteUser(userId);

        return JSONResult.ok("删除成功");
    }

    @RequestMapping("/queryUserById")
    public JSONResult queryUserById(String userId) {

        return JSONResult.ok(userService.queryUserById(userId));
    }

    @RequestMapping("/queryUserList")
    public JSONResult queryUserList() {

        SysUser user = new SysUser();
        user.setUsername("spring boot");
        user.setNickname("lee");

        List<SysUser> userList = userService.queryUserList(user);

        return JSONResult.ok(userList);
    }

    @RequestMapping("/queryUserListPaged")
    public JSONResult queryUserListPaged(Integer page) {
        if (page == null) {
            page = 1;
        }

        int pageSize = 10;

        SysUser user = new SysUser();

        List<SysUser> userList = userService.queryUserListPaged(user, page, pageSize);

        return JSONResult.ok(userList);
    }

    @RequestMapping("/queryUserByIdCustom")
    public JSONResult queryUserByIdCustom(String userId) {

        return JSONResult.ok(userService.queryUserByIdCustom(userId));
    }

    @RequestMapping("/saveUserTransactional")
    public JSONResult saveUserTransactional() {

        String userId = sid.nextShort();

        SysUser user = new SysUser();
        user.setId(userId);
        user.setUsername("lee" + new Date());
        user.setNickname("lee" + new Date());
        user.setPassword("abc123");
        user.setIsDelete(0);
        user.setRegistTime(new Date());

        userService.saveUserTransactional(user);

        return JSONResult.ok("保存成功");
    }


}
```

**3、测试**

在浏览器输入controller里面定义的路径即可。只要你按照上面的步骤一步一步来，基本上就没问题，是不是特别简单。

# 整合Redis

新项目整合 Redis 非常容易，只需要创建项目时勾上 Redis 即可，这里就不说了。

我们还是来说说怎么在现有的项目中手动整合Redis：

**1、在pom.xml 增加依赖如下**：

```xml
<!-- 引入 redis 依赖 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
    <version>1.5.7.RELEASE</version>
</dependency>
```

**2、资源文件application.properties中对Redis进行配置**

```properties
############################################################
#
# REDIS 配置
#
############################################################
spring.redis.database=0
spring.redis.host=127.0.0.1
spring.redis.port=6379
spring.redis.password=
spring.redis.jedis.pool.max-active=8
spring.redis.jedis.pool.max-wait=-1
spring.redis.jedis.pool.max-idle=10
spring.redis.jedis.pool.min-idle=2
spring.redis.timeout=6000
```

**3、封装Redis工具类**

这个工具类比较简单，封装操作redisTemplate的实现类。这个工具类只是简单的封装了StringRedisTemplate，其他相关的数据类型大家可以根据自己的需要自行扩展。

```java
package com.weiz.utils;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 
 * @Title: RedisOperator.java
 * @Package com.weiz.util
 * @Description: 使用redisTemplate的操作实现类 Copyright: Copyright (c) 2016
 * 
 * @author weiz
 * @date 2017年9月29日 下午2:25:03
 * @version V1.0
 */
@Component
public class RedisOperator {
   
// @Autowired
//    private RedisTemplate<String, Object> redisTemplate;
   
   @Autowired
   private StringRedisTemplate redisTemplate;
   
   // Key（键），简单的key-value操作

   /**
    * 实现命令：TTL key，以秒为单位，返回给定 key的剩余生存时间(TTL, time to live)。
    * 
    * @param key
    * @return
    */
   public long ttl(String key) {
      return redisTemplate.getExpire(key);
   }
   
   /**
    * 实现命令：expire 设置过期时间，单位秒
    * 
    * @param key
    * @return
    */
   public void expire(String key, long timeout) {
      redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
   }
   
   /**
    * 实现命令：INCR key，增加key一次
    * 
    * @param key
    * @return
    */
   public long incr(String key, long delta) {
      return redisTemplate.opsForValue().increment(key, delta);
   }

   /**
    * 实现命令：KEYS pattern，查找所有符合给定模式 pattern的 key
    */
   public Set<String> keys(String pattern) {
      return redisTemplate.keys(pattern);
   }

   /**
    * 实现命令：DEL key，删除一个key
    * 
    * @param key
    */
   public void del(String key) {
      redisTemplate.delete(key);
   }

   // String（字符串）

   /**
    * 实现命令：SET key value，设置一个key-value（将字符串值 value关联到 key）
    * 
    * @param key
    * @param value
    */
   public void set(String key, String value) {
      redisTemplate.opsForValue().set(key, value);
   }

   /**
    * 实现命令：SET key value EX seconds，设置key-value和超时时间（秒）
    * 
    * @param key
    * @param value
    * @param timeout
    *            （以秒为单位）
    */
   public void set(String key, String value, long timeout) {
      redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
   }

   /**
    * 实现命令：GET key，返回 key所关联的字符串值。
    * 
    * @param key
    * @return value
    */
   public String get(String key) {
      return (String)redisTemplate.opsForValue().get(key);
   }

   // Hash（哈希表）

   /**
    * 实现命令：HSET key field value，将哈希表 key中的域 field的值设为 value
    * 
    * @param key
    * @param field
    * @param value
    */
   public void hset(String key, String field, Object value) {
      redisTemplate.opsForHash().put(key, field, value);
   }

   /**
    * 实现命令：HGET key field，返回哈希表 key中给定域 field的值
    * 
    * @param key
    * @param field
    * @return
    */
   public String hget(String key, String field) {
      return (String) redisTemplate.opsForHash().get(key, field);
   }

   /**
    * 实现命令：HDEL key field [field ...]，删除哈希表 key 中的一个或多个指定域，不存在的域将被忽略。
    * 
    * @param key
    * @param fields
    */
   public void hdel(String key, Object... fields) {
      redisTemplate.opsForHash().delete(key, fields);
   }

   /**
    * 实现命令：HGETALL key，返回哈希表 key中，所有的域和值。
    * 
    * @param key
    * @return
    */
   public Map<Object, Object> hgetall(String key) {
      return redisTemplate.opsForHash().entries(key);
   }

   // List（列表）

   /**
    * 实现命令：LPUSH key value，将一个值 value插入到列表 key的表头
    * 
    * @param key
    * @param value
    * @return 执行 LPUSH命令后，列表的长度。
    */
   public long lpush(String key, String value) {
      return redisTemplate.opsForList().leftPush(key, value);
   }

   /**
    * 实现命令：LPOP key，移除并返回列表 key的头元素。
    * 
    * @param key
    * @return 列表key的头元素。
    */
   public String lpop(String key) {
      return (String)redisTemplate.opsForList().leftPop(key);
   }

   /**
    * 实现命令：RPUSH key value，将一个值 value插入到列表 key的表尾(最右边)。
    * 
    * @param key
    * @param value
    * @return 执行 LPUSH命令后，列表的长度。
    */
   public long rpush(String key, String value) {
      return redisTemplate.opsForList().rightPush(key, value);
   }

}
```

**4、创建、获取缓存**

创建RedisController控制器，调用Redis工具类中的get、set等方法，实现创建、获取缓存数据。

```java
package com.weiz.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.weiz.utils.JSONResult;
import com.weiz.pojo.SysUser;
import com.weiz.pojo.User;
import com.weiz.utils.JsonUtils;
import com.weiz.utils.RedisOperator;

@RestController
@RequestMapping("redis")
public class RedisController {
    
    @Autowired
    private StringRedisTemplate strRedis;
    
    @Autowired
    private RedisOperator redis;
    
    @RequestMapping("/test")
    public JSONResult test() {
        SysUser user = new SysUser();
        user.setId("100111");
        user.setUsername("spring boot");
        user.setPassword("abc123");
        user.setIsDelete(0);
        user.setRegistTime(new Date());
        strRedis.opsForValue().set("json:user", JsonUtils.objectToJson(user));

        return JSONResult.ok(user);
    }
    
    @RequestMapping("/getJsonList")
    public JSONResult getJsonList() {
        
        User user = new User();
        user.setAge(18);
        user.setName("慕课网");
        user.setPassword("123456");
        user.setBirthday(new Date());
        
        User u1 = new User();
        u1.setAge(19);
        u1.setName("spring boot");
        u1.setPassword("123456");
        u1.setBirthday(new Date());
        
        User u2 = new User();
        u2.setAge(17);
        u2.setName("hello spring boot");
        u2.setPassword("123456");
        u2.setBirthday(new Date());
        
        List<User> userList = new ArrayList<>();
        userList.add(user);
        userList.add(u1);
        userList.add(u2);
        
        redis.set("json:info:userlist", JsonUtils.objectToJson(userList), 2000);
        
        String userListJson = redis.get("json:info:userlist");
        List<User> userListBorn = JsonUtils.jsonToList(userListJson, User.class);
        
        return JSONResult.ok(userListBorn);
    }
}
```

> 说明：
>
> 1. /test 是没有封装的，原生的Redis 客户端操作Redis的方法。
>
> 2. /getJsonList 为封装的工具类操作调用方法。

**5、测试**

 在浏览器中输入：http://localhost:8080/redis/test 查看是否有数据返回。



# 定时任务Task

`@EnableScheduling` 为开启定时任务。

`@ComponentScan` 定义扫描包的路径。

```java
package com.weiz;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import tk.mybatis.spring.annotation.MapperScan;


@SpringBootApplication
//扫描 mybatis mapper 包路径
@MapperScan(basePackages = "com.weiz.mapper")
//扫描 所有需要的包, 包含一些自用的工具类包 所在的路径
@ComponentScan(basePackages = {"com.weiz","org.n3r.idworker"})
//开启定时任务
@EnableScheduling
//开启异步调用方法
@EnableAsync
public class SpringBootStarterApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringBootStarterApplication.class, args);
    }

}
```

```java
package com.weiz.tasks;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TestTask {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    // 定义每过3秒执行任务
    @Scheduled(fixedRate = 3000)
    // @Scheduled(cron = "4-40 * * * * ?")
    public void reportCurrentTime() {
        System.out.println("现在时间：" + dateFormat.format(new Date()));
    }
}
```

# 异步执行任务

`@EnableAsync` 开启异步调用方法

在application启动类中，加上@EnableAsync注解，Spring Boot 会自动扫描异步任务。

然后

```java
package com.weiz.tasks;

import java.util.concurrent.Future;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

@Component
public class AsyncTask {
    
    @Async
    public Future<Boolean> doTask11() throws Exception {
        long start = System.currentTimeMillis();
        Thread.sleep(1000);
        long end = System.currentTimeMillis();
        System.out.println("任务1耗时:" + (end - start) + "毫秒");
        return new AsyncResult<>(true);
    }
    
    @Async
    public Future<Boolean> doTask22() throws Exception {
        long start = System.currentTimeMillis();
        Thread.sleep(700);
        long end = System.currentTimeMillis();
        System.out.println("任务2耗时:" + (end - start) + "毫秒");
        return new AsyncResult<>(true);
    }
    
    @Async
    public Future<Boolean> doTask33() throws Exception {
        long start = System.currentTimeMillis();
        Thread.sleep(600);
        long end = System.currentTimeMillis();
        System.out.println("任务3耗时:" + (end - start) + "毫秒");
        return new AsyncResult<>(true); 
    }
}
```

# 拦截器

```java
package com.weiz.config;

import com.weiz.controller.interceptor.TwoInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import com.weiz.controller.interceptor.OneInterceptor;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Resource
    private OneInterceptor myInterceptor1;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //添加要拦截的url   1   拦截的路径   放行的路径
        			registry.addInterceptor(myInterceptor1).addPathPatterns("/admin/**").excludePathPatterns("/admin/login");
    }
}
```

```java
package com.weiz.controller.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;


public class OneInterceptor implements HandlerInterceptor  {

    /**
     * 在请求处理之前进行调用（Controller方法调用之前）
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, 
            Object object) throws Exception {
        
        System.out.println("被OneInterceptor拦截，放行...");return true;
    }
    
    /**
     * 请求处理之后进行调用，但是在视图被渲染之前（Controller方法调用之后）
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, 
            Object object, ModelAndView mv)
            throws Exception {
        // TODO Auto-generated method stub
        
    }
    
    /**
     * 在整个请求结束之后被调用，也就是在DispatcherServlet 渲染了对应的视图之后执行
     * （主要是用于进行资源清理工作）
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
            Object object, Exception ex)
            throws Exception {
        // TODO Auto-generated method stub
        
    }
}
```



# 统一异常处理

Spring Boot 实现统一异常处理的方法主要有以下两种：

- 使用@ControllerAdvice和@ExceptionHandler注解   只能处理控制器抛出的异常。

- 使用ErrorController类来实现。   可以处理所有异常。

使用@ControllerAdvice和@ExceptionHandler注解示例如下。

**统一异常处理类**：

```java
package com.weiz.exception;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import com.weiz.utils.JSONResult;

@ControllerAdvice
public class GlobalExceptionHandler  {

    public static final String ERROR_VIEW = "error";


    @ExceptionHandler(value = Exception.class)
    public Object errorHandler(HttpServletRequest reqest, 
            HttpServletResponse response, Exception e) throws Exception {
        
        e.printStackTrace();
        // 是否ajax请求
        if (isAjax(reqest)) {
            return JSONResult.errorException(e.getMessage());
        } else {
            ModelAndView mav = new ModelAndView();
            mav.addObject("exception", e);
            mav.addObject("url", reqest.getRequestURL());
            mav.setViewName(ERROR_VIEW);
            return mav;
        }
    }
    
    public static boolean isAjax(HttpServletRequest httpRequest){
        return  (httpRequest.getHeader("X-Requested-With") != null  
                    && "XMLHttpRequest"
                        .equals( httpRequest.getHeader("X-Requested-With")) );
    }
}
```

> 说明：
>
> - 注解@ControllerAdvice表示这是一个控制器增强类，当控制器发生异常就会被此拦截器被拦截。
>
> - 注解@ExceptionHandler 定义拦截的异常类，可以获取抛出的异常信息。这里可以定义多个拦截方法，拦截不同的异常类，并且可以获取抛出的异常信息，自由度更大。

**错误页面**：

```html
<!DOCTYPE html>
<html>
<head lang="en">
    <meta charset="UTF-8" />
    <title>捕获全局异常</title>
</head>
<body>
    <h1 style="color: red">发生错误：</h1>
    <div th:text="${url}"></div>
    <div th:text="${exception.message}"></div>
</body>
</html>
```

# 统一日志

在resource下创建logback-spring.xml文件：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!-- 日志级别从低到高分为TRACE < DEBUG < INFO < WARN < ERROR < FATAL，如果设置为WARN，则低于WARN的信息都不会输出 -->
<!-- scan:当此属性设置为true时，配置文档如果发生改变，将会被重新加载，默认值为true -->
<!-- scanPeriod:设置监测配置文档是否有修改的时间间隔，如果没有给出时间单位，默认单位是毫秒。
                 当scan为true时，此属性生效。默认的时间间隔为1分钟。 -->
<!-- debug:当此属性设置为true时，将打印出logback内部日志信息，实时查看logback运行状态。默认值为false。 -->
<configuration  scan="true" scanPeriod="10 seconds">
    <contextName>logback</contextName>

    <!-- name的值是变量的名称，value的值时变量定义的值。通过定义的值会被插入到logger上下文中。定义后，可以使“${}”来使用变量。 -->
    <property name="log.path" value="D:/Documents/logs/edu" />

    <!--0. 日志格式和颜色渲染 -->
    <!-- 彩色日志依赖的渲染类 -->
    <conversionRule conversionWord="clr" converterClass="org.springframework.boot.logging.logback.ColorConverter" />
    <conversionRule conversionWord="wex" converterClass="org.springframework.boot.logging.logback.WhitespaceThrowableProxyConverter" />
    <conversionRule conversionWord="wEx" converterClass="org.springframework.boot.logging.logback.ExtendedWhitespaceThrowableProxyConverter" />
    <!-- 彩色日志格式 -->
    <property name="CONSOLE_LOG_PATTERN" value="${CONSOLE_LOG_PATTERN:-%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} %clr(${LOG_LEVEL_PATTERN:-%5p}) %clr(${PID:- }){magenta} %clr(---){faint} %clr([%15.15t]){faint} %clr(%-40.40logger{39}){cyan} %clr(:){faint} %m%n${LOG_EXCEPTION_CONVERSION_WORD:-%wEx}}"/>

    <!--1. 输出到控制台-->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <!--此日志appender是为开发使用，只配置最底级别，控制台输出的日志级别是大于或等于此级别的日志信息-->
        <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
            <level>debug</level>
        </filter>
        <encoder>
            <Pattern>${CONSOLE_LOG_PATTERN}</Pattern>
            <!-- 设置字符集 -->
            <charset>UTF-8</charset>
        </encoder>
    </appender>

    <!--2. 输出到文档-->
    <!-- 2.1 level为 DEBUG 日志，时间滚动输出  -->
    <appender name="DEBUG_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <!-- 正在记录的日志文档的路径及文档名 -->
        <file>${log.path}/edu_debug.log</file>
        <!--日志文档输出格式-->
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n</pattern>
            <charset>UTF-8</charset> <!-- 设置字符集 -->
        </encoder>
        <!-- 日志记录器的滚动策略，按日期，按大小记录 -->
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <!-- 日志归档 -->
            <fileNamePattern>${log.path}/web-debug-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                <maxFileSize>100MB</maxFileSize>
            </timeBasedFileNamingAndTriggeringPolicy>
            <!--日志文档保留天数-->
            <maxHistory>15</maxHistory>
        </rollingPolicy>
        <!-- 此日志文档只记录debug级别的 -->
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>debug</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter>
    </appender>

    <!-- 2.2 level为 INFO 日志，时间滚动输出  -->
    <appender name="INFO_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <!-- 正在记录的日志文档的路径及文档名 -->
        <file>${log.path}/edu_info.log</file>
        <!--日志文档输出格式-->
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n</pattern>
            <charset>UTF-8</charset>
        </encoder>
        <!-- 日志记录器的滚动策略，按日期，按大小记录 -->
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <!-- 每天日志归档路径以及格式 -->
            <fileNamePattern>${log.path}/web-info-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                <maxFileSize>100MB</maxFileSize>
            </timeBasedFileNamingAndTriggeringPolicy>
            <!--日志文档保留天数-->
            <maxHistory>15</maxHistory>
        </rollingPolicy>
        <!-- 此日志文档只记录info级别的 -->
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>info</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter>
    </appender>

    <!-- 2.3 level为 WARN 日志，时间滚动输出  -->
    <appender name="WARN_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <!-- 正在记录的日志文档的路径及文档名 -->
        <file>${log.path}/edu_warn.log</file>
        <!--日志文档输出格式-->
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n</pattern>
            <charset>UTF-8</charset> <!-- 此处设置字符集 -->
        </encoder>
        <!-- 日志记录器的滚动策略，按日期，按大小记录 -->
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${log.path}/web-warn-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                <maxFileSize>100MB</maxFileSize>
            </timeBasedFileNamingAndTriggeringPolicy>
            <!--日志文档保留天数-->
            <maxHistory>15</maxHistory>
        </rollingPolicy>
        <!-- 此日志文档只记录warn级别的 -->
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>warn</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter>
    </appender>

    <!-- 2.4 level为 ERROR 日志，时间滚动输出  -->
    <appender name="ERROR_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <!-- 正在记录的日志文档的路径及文档名 -->
        <file>${log.path}/edu_error.log</file>
        <!--日志文档输出格式-->
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n</pattern>
            <charset>UTF-8</charset> <!-- 此处设置字符集 -->
        </encoder>
        <!-- 日志记录器的滚动策略，按日期，按大小记录 -->
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${log.path}/web-error-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                <maxFileSize>100MB</maxFileSize>
            </timeBasedFileNamingAndTriggeringPolicy>
            <!--日志文档保留天数-->
            <maxHistory>15</maxHistory>
        </rollingPolicy>
        <!-- 此日志文档只记录ERROR级别的 -->
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>ERROR</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter>
    </appender>

    <!--
        <logger>用来设置某一个包或者具体的某一个类的日志打印级别、
        以及指定<appender>。<logger>仅有一个name属性，
        一个可选的level和一个可选的addtivity属性。
        name:用来指定受此logger约束的某一个包或者具体的某一个类。
        level:用来设置打印级别，大小写无关：TRACE, DEBUG, INFO, WARN, ERROR, ALL 和 OFF，
              还有一个特俗值INHERITED或者同义词NULL，代表强制执行上级的级别。
              如果未设置此属性，那么当前logger将会继承上级的级别。
        addtivity:是否向上级logger传递打印信息。默认是true。
        <logger name="org.springframework.web" level="info"/>
        <logger name="org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor" level="INFO"/>
    -->

    <!--
        使用mybatis的时候，sql语句是debug下才会打印，而这里我们只配置了info，所以想要查看sql语句的话，有以下两种操作：
        第一种把<root level="info">改成<root level="DEBUG">这样就会打印sql，不过这样日志那边会出现很多其他消息
        第二种就是单独给dao下目录配置debug模式，代码如下，这样配置sql语句会打印，其他还是正常info级别：
        【logging.level.org.mybatis=debug logging.level.dao=debug】
     -->

    <!--
        root节点是必选节点，用来指定最基础的日志输出级别，只有一个level属性
        level:用来设置打印级别，大小写无关：TRACE, DEBUG, INFO, WARN, ERROR, ALL 和 OFF，
        不能设置为INHERITED或者同义词NULL。默认是DEBUG
        可以包含零个或多个元素，标识这个appender将会添加到这个logger。
    -->

    <!-- 4. 最终的策略 -->
    <!-- 4.1 开发环境:打印控制台-->
    <springProfile name="dev">
        <logger name="com.cms" level="info"/>
        <root level="info">
            <appender-ref ref="CONSOLE" />
            <appender-ref ref="DEBUG_FILE" />
            <appender-ref ref="INFO_FILE" />
            <appender-ref ref="WARN_FILE" />
            <appender-ref ref="ERROR_FILE" />
        </root>
    </springProfile>


    <!-- 4.2 生产环境:输出到文档-->
    <springProfile name="pro">
        <logger name="com.cms" level="warn"/>
        <root level="info">
            <appender-ref ref="ERROR_FILE" />
            <appender-ref ref="WARN_FILE" />
        </root>
    </springProfile>

</configuration>
```

> **注意**
>
> - 日志的环境即spring.profiles.acticve，跟随项目启动。
> - 启动后，即可到自定目录查找到生成的日志文件。
> - 官方推荐使用的xml名字的格式为：logback-spring.xml而不是logback.xml。

# 事务

## 事务传播行为

- Propagation.REQUIRED -- 支持当前事务，如果当前没有事务，就新建一个事务,最常见的选择。
- Propagation.SUPPORTS -- 支持当前事务，如果当前没有事务，就以非事务方式执行
- Propagation.MANDATORY -- 支持当前事务，如果当前没有事务，就抛出异常。
- Propagation.REQUIRES_NEW -- 新建事务，如果当前存在事务，把当前事务挂起, 两个事务之间没有关系，一个异常，一个提交，不会同时回滚。
- Propagation.NOT_SUPPORTED -- 以非事务方式执行操作，如果当前存在事务，就把当前事务挂起。
- Propagation.NEVER -- 以非事务方式执行，如果当前存在事务，则抛出异常



## 实现

在需要事务的方法上添加 `@Transactional`注解（可以加在类上，则该类所有方法都开启事务），并通过`propagation`指定事务机制。

```java
	@Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveUserTransactional(SysUser user) {

        userMapper.insert(user);

        int a = 1 / 0;

        user.setIsDelete(1);
        userMapper.updateByPrimaryKeySelective(user);
    }
```



# 使用JdbcTemplate操作数据库，配置多数据源！

​		JDBC（Java Data Base Connectivity， Java 数据库连接）是一种用于执行各种数据库操作的 API，可以为多种数据库提供统一访问接口。所以，JDBC 就像是一套 Java 访问数据库的 API 规范，利用这套规范屏蔽了各种数据库 API 调用的差异性。当应用程序需要访问数据库时，调用 JDBC API 相关代码进新操作，再由JDBC调用各类数据库的驱动包进行数据操作，最后数据库驱动包和对应的数据库通讯协议完成对应的数据库操作。

​		在Java领域，数据持久化有几个常见的方案，有Spring Boot自带的JdbcTemplate、有MyBatis，还有JPA，在这些方案中，最简单的就是Spring Boot自带的JdbcTemplate，虽然没有MyBatis功能强大，但是，使用比较简单，事实上，JdbcTemplate应该算是最简单的数据持久化方案。

pom添加依赖：

```xml
<dependency>    　　
    <groupId>org.springframework.boot</groupId>    　　
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
<dependency>    　　
    <groupId>mysql</groupId>    　　
    <artifactId>mysql-connector-java</artifactId>
</dependency>
```

> 注意：
>
> 如果是用数据库连接池，记得添加Druid数据库连接池依赖。
>
> 这里可以添加专门为Spring Boot打造的druid-spring-boot-starter，JdbcTemplate默认使用Hikari 连接池，如果需要使用druid，需要另外配置。

在application.properties中提供数据的基本配置：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/zwz_test
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
```

> 注意：在 Spring Boot 2.1.0 中， com.mysql.jdbc.Driver 已经过期，推荐使用com.mysql.cj.jdbc.Driver
>



```java
package com.weiz.service.impl;

import com.weiz.pojo.Product;
import com.weiz.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;


@Service
public class ProductServiceImpl implements ProductService  {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public int save(Product product) {
        return jdbcTemplate.update("INSERT INTO products(name, code, price) values(?, ? , ?)",
                product.getName(), product.getCode(), product.getPrice());
    }

    @Override
    public int update(Product product) {
        return jdbcTemplate.update("UPDATE products SET name = ? , code = ? , price = ? WHERE id=?",
                product.getName(), product.getCode(), product.getPrice(), product.getId());
    }

    @Override
    public int delete(long id) {
        return jdbcTemplate.update("DELETE FROM products where id = ? ",id);
    }

    @Override
    public Product findById(long id) {
        return jdbcTemplate.queryForObject("SELECT * FROM products WHERE id=?", new Object[] { id }, new BeanPropertyRowMapper<Product>(Product.class));
    }

}
```



配置多数据源：

```properties
spring.datasource.primary.jdbc-url=jdbc:mysql://localhost:3306/zwz_test
spring.datasource.primary.username=root
spring.datasource.primary.password=root
spring.datasource.primary.driver-class-name=com.mysql.cj.jdbc.Driver

spring.datasource.secondary.jdbc-url=jdbc:mysql://localhost:3306/zwz_test2
spring.datasource.secondary.username=root
spring.datasource.secondary.password=root
spring.datasource.secondary.driver-class-name=com.mysql.cj.jdbc.Driver
```

上面的配置文件，添加了两个数据源，一个是 zwz_test 库，铃个是 zwz_test2 库。

> 注意：之前单个数据源的数据库连接是：spring.datasource.url，这里多个数据源使用的是 spring.datasource.*.jdbc-url，因为JdbcTemplate默认使用Hikari 连接池，而 HikariCP 读取的是 jdbc-url 。

创建DataSourceConfig，在项目启动的时候读取配置文件中的数据库信息，并对 JDBC 初始化。 

```java
package com.weiz.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {
    @Primary
    @Bean(name = "primaryDataSource")
    @Qualifier("primaryDataSource")
    @ConfigurationProperties(prefix="spring.datasource.primary")
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create().build();
    }
    @Bean(name = "secondaryDataSource")
    @Qualifier("secondaryDataSource")
    @ConfigurationProperties(prefix="spring.datasource.secondary")
    public DataSource secondaryDataSource() {
        return DataSourceBuilder.create().build();
    }
    @Bean(name="primaryJdbcTemplate")
    public JdbcTemplate primaryJdbcTemplate (
            @Qualifier("primaryDataSource") DataSource dataSource ) {
        return new JdbcTemplate(dataSource);
    }
    @Bean(name="secondaryJdbcTemplate")
    public JdbcTemplate secondaryJdbcTemplate(
            @Qualifier("secondaryDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
```

DataSourceConfig类的作用是在项目启动的时候根据特定的前缀加载不同的数据源，再根据构建好的数据源创建不同的 JDBC。 

> 注意事项：使用多个数据源时，需要添加@Primary注解，@Primary：自动装配时当出现多个Bean候选者时，被注解为@Primary的Bean将作为首选者。Primary 意味着"主要的",类似与SQL语句中的"primary key"，有且只能有一个，否则会报错。

需要对 ProductServerice 中的所有方法法进行改造，增加一个传入参数 JdbcTemplate，根据调用方传入的JdbcTemplate 进行操作。

```java
// ProductService 接口
public interface ProductService {
    int save(Product product, JdbcTemplate jdbcTemplate);
    // 省略其他方法  
}

// ProductServiceImpl 
@Service
public class ProductServiceImpl implements ProductService  {
    @Override
    public int save(Product product,JdbcTemplate jdbcTemplate) {
        return jdbcTemplate.update("INSERT INTO products(name, code, price) values(?, ? , ?)",
                product.getName(), product.getCode(), product.getPrice());
    }
    // 省略其他方法 
}
```



# 热部署

## 原理

devtools 使用了两个类加载器（ClassLoader），一个是 Base类加载器（base classloader ）：加载那些不会改变的类，如：第三方Jar包等，而另一个是 Restart类加载器（restart classloader）：负责加载那些正在开发的会改变的类。这样在有代码更改的时候，因为重启的时候只是加载了在开发的Class类，没有重新加载第三方的jar包，所以实现了较快的重启时间。

devtools 监听classpath下的文件变动（发生在保存时机），并且会立即重启应用。从而实现类文件和属性文件的热部署。

## 配置

- pom配置引入devtools的依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <!-- optional=true, 依赖不会传递, 该项目依赖devtools;之后依赖boot项目的项目如果想要使用devtools, 需要重新引入 -->
    <optional>true</optional>
</dependency>
```

- application.properties

```properties
# 关闭缓存即时刷新
#spring.thymeleaf.cache=false

#热部署生效
spring.devtools.restart.enabled=true
#设置重启的目录
spring.devtools.restart.additional-paths=src/main/java
#classpath目录下的WEB-INF文件夹内容修改不重启
spring.devtools.restart.exclude=WEB-INF/**
```

> 说明：
>
> devtools可以实现页面热部署，即页面修改后会立即生效，需要将application.properties文件中配置spring.thymeleaf.cache=false。
>
> devtools会监听classpath下的文件变动，并且会立即重启应用。

- IDEA配置

  如果idea是新安装的或者之前就没有配置过，发现改变代码项目热部署不成功。当我们修改了Java类后，IDEA默认是不自动编译的，而spring-boot-devtools又是监测classpath下的文件发生变化才会重启应用。

  所以需要设置IDEA的自动编译：

  （1）File-Settings-Compiler-Build Project automatically

  （2）ctrl + shift + alt + /,选择Registry,勾上 Compiler autoMake allow when app running 

# Pagehelper分页

```xml
 		<!-- pagehelper -->
        <dependency>
            <groupId>com.github.pagehelper</groupId>
            <artifactId>pagehelper-spring-boot-starter</artifactId>
        </dependency>
```

```properties
# 分页框架
pagehelper.helperDialect=mysql
pagehelper.reasonable=true
pagehelper.supportMethodsArguments=true
pagehelper.params=count=countSql
```

> 说明： 
>
> 　　helperDialect ： 指定数据库，可以不配置，pagehelper插件会自动检测数据库的类型。
>
> 　　resonable ： 分页合理化参数默认false，当该参数设置为true 时，pageNum <= 0 时，默认显示第一页，pageNum 超过 pageSize 时，显示最后一页。
>
> 　　params ： 用于从对象中根据属性名取值，可以配置pageNum，pageSize，count 不用配置映射的默认值。
>
> 　　supportMethodsArguments：分页插件会根据查询方法的参数中，自动根据params 配置的字段中取值，找到合适的值会自动分页。

在原来的UserService类和UserServiceImpl 类中，增加 queryUserListPaged 接口和对应的方法实现。

```java
@Override
    public List<SysUser> queryUserListPaged(SysUser user, Integer page, Integer pageSize) {
        // 开始分页
        PageHelper.startPage(page,pageSize);
        Example example = new Example(SysUser.class);
        Example.Criteria criteria = example.createCriteria();

        if (!StringUtils.isEmptyOrWhitespace(user.getUsername())) {
            criteria.andLike("username", "%" + user.getUsername() + "%");
        }

        if (!StringUtils.isEmptyOrWhitespace(user.getNickname())) {
            criteria.andLike("nickname", "%" + user.getNickname() + "%");
        }

        List<SysUser> userList = userMapper.selectByExample(example);

        return userList;
    }
```

分页的核心就一行代码， PageHelper.startPage(page,pageSize); 这个就标识开始分页。加了这个之后pagehelper 插件就会通过其内部的拦截器，将执行的sql语句，转化为分页的sql语句。

> 注意：使用时PageHelper.startPage(pageNum, pageSize)一定要放在列表查询的方法中，这样在**查询时会查出相应的数据量且会查询出总数。**

```java
@RequestMapping("/queryUserListPaged")
    public JSONResult queryUserListPaged(Integer page) {
        if (page == null) {
            page = 1;
        }

        int pageSize = 10;

        SysUser user = new SysUser();

        List<SysUser> userList = userService.queryUserListPaged(user, page, pageSize);

        return JSONResult.ok(userList);
    }
```

# Mybatis 自定义Mapper实现多表关联查询

1、创建自定义 mapper

在com.weiz.mapper 包中，创建 SysUserMapperCustom 接口

```java
package com.weiz.mapper;

import com.weiz.pojo.SysUser;

import java.util.List;

public interface SysUserMapperCustom {
    List<SysUser> queryUserSimplyInfoById(String userId);
}
```

SysUserMapperCustom 是一个接口，这里只定义了一个方法：queryUserSimplyInfoById 。

 2、创建mapper 配置文件

创建完mapper类之后，需要创建SysuserMapperCustom 类对应的xml 配置文件：SysuserMapperCustom.xml。

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
<mapper namespace="com.weiz.mapper.SysUserMapperCustom" >
  <!-- 查询用户信息 -->
  <select id="queryUserSimplyInfoById" resultType="com.weiz.pojo.SysUser" parameterType="java.lang.String" >
        select
            *
        from
            sys_user
        where
            id = #{id,jdbcType=VARCHAR}
    </select>
    
    <resultMap id="UserMap" type="com.weiz.pojo.SysUser">
        <id column="id" jdbcType="VARCHAR" property="id" />
        <result property="username" column="username" jdbcType="VARCHAR" />
        <!--封装映射company表数据，user表与company表1对1关系，配置1对1的映射
            association:用于配置1对1的映射
                        属性property：company对象在user对象中的属性名
                        属性javaType：company属性的java对象 类型
                        属性column：user表中的外键引用company表
        -->
        <association property="company" javaType="com.weiz.pojo.SysCompany" column="company_id">
            <id property="id" jdbcType="VARCHAR" column="companyid"></id>
            <result property="name" jdbcType="VARCHAR" column="companyname"></result>
        </association>
        <!--配置1对多关系映射
            property：在user里面的List<Account>的属性名
            ofType:当前account表的java类型
            column:外键
        -->
    </resultMap>
    <select id="queryAllUserListCustom" resultMap="UserMap"  >
       SELECT
       u.id,u.username,c.id companyid, c.name companyname
       FROM sys_user u
       LEFT JOIN sys_company c on u.company_id=c.id
    </select>
</mapper>
```

上面配置的sql ，可以看到用户表sys_user 关联 sys_company 表，查询完整的人员公司信息。

> 说明：
>
> 　　1、association：用于配置1对1的映射
>
> 　　　　属性property：company对象在user对象中的属性名
>
> 　　　　属性javaType：company属性的java对象 类型
>
> 　　　　属性column：user表中的外键引用company表。
>
> 　　2、collection：用于配置1对多关系映射
>
> 　　　　property：在user里面的List<Account>的属性名
>
> 　　　　 ofType：当前account表的java类型
>
> 　　　　 column：外键

3、Service调用

首先在UserService接口中增加queryUserByIdCustom方法，然后在对应的 UserServiceImpl 实现类中，注入SysUserMapperCustom 。最后实现queryUserByIdCustom 方法，在方法中调用前面自定义的mapper 类中方法即可。具体代码如下：

```java
// 1. 在UserServiceImpl 中注入SysUserMapperCustom 
@Autowired
private SysUserMapperCustom userMapperCustom;


// 2. 实现接口方法，调用
@Override
public SysUser queryUserByIdCustom(String userId) {
    List<SysUser> userList = userMapperCustom.queryUserSimplyInfoById(userId);

    if (userList != null && !userList.isEmpty()) {
        return (SysUser)userList.get(0);
    }

    return null;
}
```

# 整合mybatis，使用注解的方式实现增删改查

## 整合mybatis

1、pom.xml增加mybatis相关依赖

```xml
		<dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>5.1.41</version>
        </dependency>
        <!--mybatis-->
        <dependency>
            <groupId>org.mybatis.spring.boot</groupId>
            <artifactId>mybatis-spring-boot-starter</artifactId>
            <version>1.3.1</version>
        </dependency>
        <!--mapper-->
        <dependency>
            <groupId>tk.mybatis</groupId>
            <artifactId>mapper-spring-boot-starter</artifactId>
            <version>1.2.4</version>
        </dependency>
        <!-- pagehelper -->
        <dependency>
            <groupId>com.github.pagehelper</groupId>
            <artifactId>pagehelper-spring-boot-starter</artifactId>
            <version>1.2.3</version>
        </dependency>
        <!-- druid 数据库连接框架-->
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid-spring-boot-starter</artifactId>
            <version>1.1.9</version>
        </dependency>
        <dependency>
            <groupId>org.mybatis.generator</groupId>
            <artifactId>mybatis-generator-core</artifactId>
            <version>1.3.2</version>
            <scope>compile</scope>
            <optional>true</optional>
        </dependency>
```

2、application.properties配置数据连接

```properties
############################################################
# 数据源相关配置，这里用的是阿里的druid 数据源
############################################################
spring.datasource.url=jdbc:mysql://localhost:3306/zwz_test
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
spring.datasource.druid.initial-size=1
spring.datasource.druid.min-idle=1
spring.datasource.druid.max-active=20
spring.datasource.druid.test-on-borrow=true
spring.datasource.druid.stat-view-servlet.allow=true

############################################################
# mybatis 相关配置
############################################################
mybatis.type-aliases-package=com.weiz.pojo
mybatis.mapper-locations=classpath:mapper/*.xml
mapper.mappers=com.weiz.utils.MyMapper    #这个MyMapper 就是我之前创建的mapper统一接口。后面所有的mapper类都会继承这个接口
mapper.not-empty=false
mapper.identity=MYSQL
# 分页框架
pagehelper.helperDialect=mysql
pagehelper.reasonable=true
pagehelper.supportMethodsArguments=true
pagehelper.params=count=countSql
```

3、在启动主类添加扫描器

在SpringBootStarterApplication 启动类中增加包扫描器。

```java
@SpringBootApplication
//扫描 mybatis mapper 包路径
@MapperScan(basePackages = "com.weiz.mapper")     // 这一步别忘了。
//扫描 所有需要的包, 包含一些自用的工具类包 所在的路径
@ComponentScan(basePackages = {"com.weiz","org.n3r.idworker"})
public class SpringBootStarterApplication {    
	public static void main(String[] args) {        					SpringApplication.run(SpringBootStarterApplication.class, args);
                                           }
}
```

## 代码自动生成工具

Mybatis 整合完之后，接下来就是创建表和pojo类，mybatis提供了强大的自动生成功能的插件。mybatis generator插件只需简单几步就能生成pojo 类和mapper。操作步骤和xml 配置版也是类似的，唯一要注意的是 **generatorConfig.xml** 的部分配置，要配置按注解的方式生成mapper 。

1、增加generatorConfig.xml配置文件

在resources 文件下创建 generatorConfig.xml 文件。此配置文件独立于项目，只是给自动生成工具类的配置文件，具体配置如下：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE generatorConfiguration
        PUBLIC "-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN"
        "http://mybatis.org/dtd/mybatis-generator-config_1_0.dtd">
<generatorConfiguration>
    <context id="DB2Tables"  targetRuntime="MyBatis3">
        <commentGenerator>
            <property name="suppressDate" value="true"/>
            <!-- 是否去除自动生成的注释 true：是 ： false:否 -->
            <property name="suppressAllComments" value="true"/>
        </commentGenerator>
        <!--数据库链接URL，用户名、密码 -->
        <jdbcConnection driverClass="com.mysql.jdbc.Driver" connectionURL="jdbc:mysql://127.0.0.1:3306/zwz_test" userId="root" password="root">
        </jdbcConnection>
        <javaTypeResolver>
            <property name="forceBigDecimals" value="false"/>
        </javaTypeResolver>
        <!-- 生成模型的包名和位置-->
        <javaModelGenerator targetPackage="com.weiz.pojo" targetProject="src/main/java">
            <property name="enableSubPackages" value="true"/>
            <property name="trimStrings" value="true"/>
        </javaModelGenerator>
        <!-- 生成映射文件的包名和位置-->
        <sqlMapGenerator targetPackage="mapping" targetProject="src/main/resources">
            <property name="enableSubPackages" value="true"/>
        </sqlMapGenerator>
        <!-- 生成DAO的包名和位置-->
        <!-- XMLMAPPER生成xml映射文件, ANNOTATEDMAPPER生成的dao采用注解来写sql -->
        <javaClientGenerator type="ANNOTATEDMAPPER" targetPackage="com.weiz.mapper" targetProject="src/main/java">
            <property name="enableSubPackages" value="true"/>
        </javaClientGenerator>
        <!-- 要生成的表 tableName是数据库中的表名或视图名 domainObjectName是实体类名-->
        <table tableName="sys_user" domainObjectName="User" enableCountByExample="false" enableUpdateByExample="false" enableDeleteByExample="false" enableSelectByExample="false" selectByExampleQueryId="false"></table>
    </context>
</generatorConfiguration>
```

> 注意：
>
> 这里的配置 <javaClientGenerator type="ANNOTATEDMAPPER" targetPackage="com.weiz.mapper" targetProject="src/main/java">
>
> type 的值很重要：
> 　　XMLMAPPER ： 表示生成xml映射文件。
>
> 　　ANNOTATEDMAPPER： 表示生成的mapper 采用注解来写sql。

2、数据库User表

需要在数据库中创建相应的表。

```sql
CREATE TABLE `sys_user` (
  `id` varchar(32) NOT NULL DEFAULT '',
  `username` varchar(32) DEFAULT NULL,
  `password` varchar(64) DEFAULT NULL,
  `nickname` varchar(64) DEFAULT NULL,
  `age` int(11) DEFAULT NULL,
  `sex` int(11) DEFAULT NULL,
  `job` int(11) DEFAULT NULL,
  `face_image` varchar(6000) DEFAULT NULL,
  `province` varchar(64) DEFAULT NULL,
  `city` varchar(64) DEFAULT NULL,
  `district` varchar(64) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `auth_salt` varchar(64) DEFAULT NULL,
  `last_login_ip` varchar(64) DEFAULT NULL,
  `last_login_time` datetime DEFAULT NULL,
  `is_delete` int(11) DEFAULT NULL,
  `regist_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
```

3、创建GeneratorDisplay类

```java
package com.weiz.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

public class GeneratorDisplay {

    public void generator() throws Exception{

        List<String> warnings = new ArrayList<String>();
        boolean overwrite = true;
        //指定 逆向工程配置文件
        File configFile = new File("generatorConfig.xml"); 
        ConfigurationParser cp = new ConfigurationParser(warnings);
        Configuration config = cp.parseConfiguration(configFile);
        DefaultShellCallback callback = new DefaultShellCallback(overwrite);
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config,
                callback, warnings);
        myBatisGenerator.generate(null);

    } 
    
    public static void main(String[] args) throws Exception {
        try {
            GeneratorDisplay generatorSqlmap = new GeneratorDisplay();
            generatorSqlmap.generator();
        } catch (Exception e) {
            e.printStackTrace();
        }        
    }
}
```

这个其实也是调用mybatis generator实现的。跟mybatis generator安装插件是一样的。

> 注意：利用Generator自动生成代码，对于已经存在的文件会存在覆盖和在原有文件上追加的可能性，不宜多次生成。如需重新生成，需要删除已生成的源文件。

4、Mybatis Generator自动生成pojo和mapper

运行GeneratorDisplay 如下图所示，即可自动生成相关的代码。

pojo 包里面自动生成了User 实体对象 ，mapper包里面生成了 UserMapper 和UserSqlProvider 类。UserMapper 是所有方法的实现。UserSqlProvider则是为UserMapper 实现动态SQL。

> 注意：
>
> 　　UserMapper 中的所有的动态SQL脚本，都定义在类UserSqlProvider中。若要增加新的动态SQL，只需在UserSqlProvider中增加相应的方法，然后在UserMapper中增加相应的引用即可，
>
> 　　如：@UpdateProvider(type=UserSqlProvider.class, method="updateByPrimaryKeySelective")。

# 整合mybatis，使用注解实现动态Sql、参数传递等常用操作！

1、@Select 注解

@Select，主要在查询的时候使用，查询类的注解，一般简单的查询可以使用这个注解。

```java
@Select({
    "select",
    "id, company_id, username, password, nickname, age, sex, job, face_image, province, ",
    "city, district, address, auth_salt, last_login_ip, last_login_time, is_delete, ",
    "regist_time",
    "from sys_user",
    "where id = #{id,jdbcType=VARCHAR}"
})
@Results({
    @Result(column="id", property="id", jdbcType=JdbcType.VARCHAR, id=true),
    @Result(column="company_id", property="companyId", jdbcType=JdbcType.VARCHAR),
    @Result(column="face_image", property="faceImage", jdbcType=JdbcType.VARCHAR),
    @Result(column="auth_salt", property="authSalt", jdbcType=JdbcType.VARCHAR),
    @Result(column="last_login_ip", property="lastLoginIp", jdbcType=JdbcType.VARCHAR),
    @Result(column="last_login_time", property="lastLoginTime", jdbcType=JdbcType.TIMESTAMP),
    @Result(column="is_delete", property="isDelete", jdbcType=JdbcType.INTEGER),
    @Result(column="regist_time", property="registTime", jdbcType=JdbcType.TIMESTAMP)
})
User selectByPrimaryKey(String id);
```

2、@Insert 注解

@Insert，插入数据时使用，直接传入数据实体类，mybatis 会属性自动解析到对应的参数。所以需要将 #后面的参数和实体类属性保持一致。

```java
@Insert({
        "insert into sys_user (id, company_id, ",
        "username, password, ",
        "nickname, age, sex, ",
        "job, face_image, ",
        "province, city, ",
        "district, address, ",
        "auth_salt, last_login_ip, ",
        "last_login_time, is_delete, ",
        "regist_time)",
        "values (#{id,jdbcType=VARCHAR}, #{companyId,jdbcType=VARCHAR}, ",
        "#{username,jdbcType=VARCHAR}, #{password,jdbcType=VARCHAR}, ",
        "#{nickname,jdbcType=VARCHAR}, #{age,jdbcType=INTEGER}, #{sex,jdbcType=INTEGER}, ",
        "#{job,jdbcType=INTEGER}, #{faceImage,jdbcType=VARCHAR}, ",
        "#{province,jdbcType=VARCHAR}, #{city,jdbcType=VARCHAR}, ",
        "#{district,jdbcType=VARCHAR}, #{address,jdbcType=VARCHAR}, ",
        "#{authSalt,jdbcType=VARCHAR}, #{lastLoginIp,jdbcType=VARCHAR}, ",
        "#{lastLoginTime,jdbcType=TIMESTAMP}, #{isDelete,jdbcType=INTEGER}, ",
        "#{registTime,jdbcType=TIMESTAMP})"
    })
    int insert(User record);
```

3、@Update 注解

@Update，一般数据更新操作可以使用 @Update注解实现。

```java
@Update({
        "update sys_user",
        "set company_id = #{companyId,jdbcType=VARCHAR},",
          "username = #{username,jdbcType=VARCHAR},",
          "password = #{password,jdbcType=VARCHAR},",
          "nickname = #{nickname,jdbcType=VARCHAR},",
          "age = #{age,jdbcType=INTEGER},",
          "sex = #{sex,jdbcType=INTEGER},",
          "job = #{job,jdbcType=INTEGER},",
          "face_image = #{faceImage,jdbcType=VARCHAR},",
          "province = #{province,jdbcType=VARCHAR},",
          "city = #{city,jdbcType=VARCHAR},",
          "district = #{district,jdbcType=VARCHAR},",
          "address = #{address,jdbcType=VARCHAR},",
          "auth_salt = #{authSalt,jdbcType=VARCHAR},",
          "last_login_ip = #{lastLoginIp,jdbcType=VARCHAR},",
          "last_login_time = #{lastLoginTime,jdbcType=TIMESTAMP},",
          "is_delete = #{isDelete,jdbcType=INTEGER},",
          "regist_time = #{registTime,jdbcType=TIMESTAMP}",
        "where id = #{id,jdbcType=VARCHAR}"
    })
    int updateByPrimaryKey(User record);
```

4、@Delete 注解
@Delete 数据删除的注解

```java
	@Delete({
        "delete from sys_user",
        "where id = #{id,jdbcType=VARCHAR}"
    })
    int deleteByPrimaryKey(String id);
```

5、@Results 和 @Result 注解 

@Results 和 @Result 主要作用是，当有一些特殊的场景需要处理，查询的返回结果与期望的数据格式不一致时，可以将将数据库中查询到的数值自动转化为具体的属性或类型，，修饰返回的结果集。比如查询的对象返回值属性名和字段名不一致，或者对象的属性中使用了枚举等。如果实体类属性和数据库属性名保持一致，就不需要这个属性来修饰。

```java
@Select({
    "select",
    "id, company_id, username, password, nickname, age, sex, job, face_image, province, ",
    "city, district, address, auth_salt, last_login_ip, last_login_time, is_delete, ",
    "regist_time",
    "from sys_user",
    "where id = #{id,jdbcType=VARCHAR}"
})
@Results({
    @Result(column="id", property="id", jdbcType=JdbcType.VARCHAR, id=true),
    @Result(column="company_id", property="companyId", jdbcType=JdbcType.VARCHAR),
    @Result(column="face_image", property="faceImage", jdbcType=JdbcType.VARCHAR),
    @Result(column="auth_salt", property="authSalt", jdbcType=JdbcType.VARCHAR),
    @Result(column="last_login_ip", property="lastLoginIp", jdbcType=JdbcType.VARCHAR),
    @Result(column="last_login_time", property="lastLoginTime", jdbcType=JdbcType.TIMESTAMP),
    @Result(column="is_delete", property="isDelete", jdbcType=JdbcType.INTEGER),
    @Result(column="regist_time", property="registTime", jdbcType=JdbcType.TIMESTAMP)
})
User selectByPrimaryKey(String id);
```

上面的例子可以看到，数据库中的company_id 字段和实体类中定义的 companyId 属性的名称不一致，需要Result 转换。

## 传参方式

1、直接传参

对于单个参数的方法，可直接使用 #{id} 的方式接收同名的变量参数。

```java
	@Delete("delete from sys_user where id = #{id,jdbcType=VARCHAR}")
    int deleteByPrimaryKey(String id);
```

 2、使用 @Param 注解

@Param注解的作用是给参数命名，参数命名后就能根据名字得到参数值，正确的将参数传入sql语句中 。如果你的方法有多个参数，@Param 注解 会在方法的参数上就能为它们取自定义名字，参数则先以 "param" 作前缀，再加上它们的参数位置作为参数别名。例如， #{param1}、 #{param2}，这个是默认值。如果注解是 @Param("person")，那么参数就会被命名为 #{person}。

```java
@Select("SELECT * FROM sys_user WHERE username = #{username} and password = #{password}")
List<User> getListByUserSex(@Param("username") String userName, @Param("password") String password);

// 不自定义param 时，默认使用 param + 参数序号 或者 0，1，值就是参数的值。
@Select("SELECT * FROM sys_user WHERE username = #{param1} and password = #{param2}")
List<User> getListByUserSex(String userName, String password);
```

 3、Map 传值 
需要传送多个参数时，也可以考虑使用 Map的形式。

```java
@Select("SELECT * FROM sys_user WHERE username=#{username} AND password = #{password}")
List<User> getListByNameAndSex(Map<String, Object> map);
```

调用时将参数依次加入到 Map 中即可。

```java
Map param= new HashMap();
param.put("username","admin");
param.put("password","123456");
List<User> users = userMapper.getListByNameAndSex(param)
```

 4、使用pojo对象
使用pojo对象传参是比较常用的传参方式。像上面的insert、update 等方法。都是直接传入user对象。

```java
@Update({
    "update sys_user",
    "set company_id = #{companyId,jdbcType=VARCHAR},",
      "username = #{username,jdbcType=VARCHAR},",
      "password = #{password,jdbcType=VARCHAR},",
      "nickname = #{nickname,jdbcType=VARCHAR},",
      "age = #{age,jdbcType=INTEGER},",
      "sex = #{sex,jdbcType=INTEGER},",
      "job = #{job,jdbcType=INTEGER},",
      "face_image = #{faceImage,jdbcType=VARCHAR},",
      "province = #{province,jdbcType=VARCHAR},",
      "city = #{city,jdbcType=VARCHAR},",
      "district = #{district,jdbcType=VARCHAR},",
      "address = #{address,jdbcType=VARCHAR},",
      "auth_salt = #{authSalt,jdbcType=VARCHAR},",
      "last_login_ip = #{lastLoginIp,jdbcType=VARCHAR},",
      "last_login_time = #{lastLoginTime,jdbcType=TIMESTAMP},",
      "is_delete = #{isDelete,jdbcType=INTEGER},",
      "regist_time = #{registTime,jdbcType=TIMESTAMP}",
    "where id = #{id,jdbcType=VARCHAR}"
})
int updateByPrimaryKey(User record);
```


以上，就是Mybatis 传参的四种方式。根据方法的参数选择合适的传值方式。

## 动态 SQL

实际项目中，除了使用一些常用的增删改查的方法之外，有些复杂的需求，可能还需要执行一些自定义的动态sql。mybatis 除了提供了@Insert、@Delete 这些常用的注解，还提供了多个注解如：@InsertProvider,@UpdateProvider,@DeleteProvider和@SelectProvider，用来建立动态sql 和让 mybatis 执行这些sql 的注解。下面就来实现按字段更新的功能。

1、首先在 UserSqlProvider 中创建 拼接sql的方法。

```java
public String updateByPrimaryKeySelective(User record) {
        BEGIN();
        UPDATE("sys_user");
        
        if (record.getCompanyId() != null) {
            SET("company_id = #{companyId,jdbcType=VARCHAR}");
        }
        
        if (record.getUsername() != null) {
            SET("username = #{username,jdbcType=VARCHAR}");
        }
        
        if (record.getPassword() != null) {
            SET("password = #{password,jdbcType=VARCHAR}");
        }
        
        if (record.getNickname() != null) {
            SET("nickname = #{nickname,jdbcType=VARCHAR}");
        }
        
        if (record.getAge() != null) {
            SET("age = #{age,jdbcType=INTEGER}");
        }
        
        if (record.getSex() != null) {
            SET("sex = #{sex,jdbcType=INTEGER}");
        }
        
        if (record.getJob() != null) {
            SET("job = #{job,jdbcType=INTEGER}");
        }
        
        if (record.getFaceImage() != null) {
            SET("face_image = #{faceImage,jdbcType=VARCHAR}");
        }
        
        if (record.getProvince() != null) {
            SET("province = #{province,jdbcType=VARCHAR}");
        }
        
        if (record.getCity() != null) {
            SET("city = #{city,jdbcType=VARCHAR}");
        }
        
        if (record.getDistrict() != null) {
            SET("district = #{district,jdbcType=VARCHAR}");
        }
        
        if (record.getAddress() != null) {
            SET("address = #{address,jdbcType=VARCHAR}");
        }
        
        if (record.getAuthSalt() != null) {
            SET("auth_salt = #{authSalt,jdbcType=VARCHAR}");
        }
        
        if (record.getLastLoginIp() != null) {
            SET("last_login_ip = #{lastLoginIp,jdbcType=VARCHAR}");
        }
        
        if (record.getLastLoginTime() != null) {
            SET("last_login_time = #{lastLoginTime,jdbcType=TIMESTAMP}");
        }
        
        if (record.getIsDelete() != null) {
            SET("is_delete = #{isDelete,jdbcType=INTEGER}");
        }
        
        if (record.getRegistTime() != null) {
            SET("regist_time = #{registTime,jdbcType=TIMESTAMP}");
        }
        
        WHERE("id = #{id,jdbcType=VARCHAR}");
        
        return SQL();
    }
```

2、Mapper 中引入 updateByPrimaryKeySelective 方法

```java
    @UpdateProvider(type=UserSqlProvider.class, method="updateByPrimaryKeySelective")
    int updateByPrimaryKeySelective(User record);
```

> 说明：
>
> 　　type：动态生成 SQL 的类。
>
> 　　method：类中具体的方法名。

# 整合Mybatis 实现多数据源配置

1、首先在系统配置文件中，需要配置多个数据源，即在application.properties 文件中增加如下配置：

```properties
# mybatis 多数据源配置
# 数据库1的配置
spring.datasource.test1.driver-class-name = com.mysql.jdbc.Driver
spring.datasource.test1.jdbc-url = jdbc:mysql://localhost:3306/zwz_test?useUnicode=true&characterEncoding=utf-8&useSSL=false
spring.datasource.test1.username = root
spring.datasource.test1.password = root
# 数据库2的配置
spring.datasource.test2.driver-class-name = com.mysql.jdbc.Driver
spring.datasource.test2.jdbc-url = jdbc:mysql://localhost:3306/zwz_test2?useUnicode=true&characterEncoding=utf-8&useSSL=false
spring.datasource.test2.username = root
spring.datasource.test2.password = root
```

> 注意：
>
> 1、这里配置的是两个一样的数据库zwz_test 和zwz_test2。
>
>  2、数据库连接的配置使用jdbc-url ， 不是之前的url ，这点需要注意。

2、主数据源配置类

在config 包中，创建 DataSource1Config 类。此类配置主数据源。

```java
package com.weiz.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = "com.weiz.mapper.test1", sqlSessionFactoryRef = "test1SqlSessionFactory")
public class DataSource1Config {
        @Bean(name = "test1DataSource")
        @ConfigurationProperties(prefix = "spring.datasource.test1")
        @Primary
        public DataSource testDataSource() {
                return DataSourceBuilder.create().build();
        }

        @Bean(name = "test1SqlSessionFactory")
        @Primary
        public SqlSessionFactory testSqlSessionFactory(@Qualifier("test1DataSource") DataSource dataSource) throws Exception {
                SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
                bean.setDataSource(dataSource);
                return bean.getObject();
        }

        @Bean(name = "test1TransactionManager")
        @Primary
        public DataSourceTransactionManager testTransactionManager(@Qualifier("test1DataSource") DataSource dataSource) {
                return new DataSourceTransactionManager(dataSource);
        }

        @Bean(name = "test1SqlSessionTemplate")
        @Primary
        public SqlSessionTemplate testSqlSessionTemplate(@Qualifier("test1SqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
                return new SqlSessionTemplate(sqlSessionFactory);
        }
}
```

在config 包中，创建DataSource2Config 类。此类配置其他普通数据源。

```java
package com.weiz.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = "com.weiz.mapper.test2", sqlSessionFactoryRef = "test2SqlSessionFactory")
public class DataSource2Config {
        @Bean(name = "test2DataSource")
        @ConfigurationProperties(prefix = "spring.datasource.test2")
        public DataSource testDataSource() {
                return DataSourceBuilder.create().build();
        }

        @Bean(name = "test2SqlSessionFactory")
        public SqlSessionFactory testSqlSessionFactory(@Qualifier("test2DataSource") DataSource dataSource) throws Exception {
                SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
                bean.setDataSource(dataSource);
                return bean.getObject();
        }

        @Bean(name = "test2TransactionManager")
        public DataSourceTransactionManager testTransactionManager(@Qualifier("test2DataSource") DataSource dataSource) {
                return new DataSourceTransactionManager(dataSource);
        }

        @Bean(name = "test2SqlSessionTemplate")
        public SqlSessionTemplate testSqlSessionTemplate(@Qualifier("test2SqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
                return new SqlSessionTemplate(sqlSessionFactory);
        }
}
```

说明， DataSource1Config 和 DataSource2Config 即是相关的主数据源配置类和普通数据源配置类。

com.weiz.mapper.test1 为 扫描的mapper的路径。

可以看到两个数据源都配置的各自的DataSource、SqlSessionFactory、TransactionManager和SqlSessionTemplate 。

> 虽然两个类看着差不多，但是需要特别注意以下几点：
>
> 　　1、主数据源配置需要加@Primary 注解，其他普通数据源不能加这个注解，否则会报错，复制的时候小心。
>
> 　　2、各个数据源配置的 basePackages 扫描路径需要配置正确。配置错了不会出异常，但是运行的时候，会找错数据库。

 3、调用测试

首先，创建com.weiz.mapper.test1 和 com.weiz.mapper.test2 包，将之前的UserMapper ，重名命为User1Mapper 和User2Mapper 复制到相应的包中。

然后，UserServiceImpl 分别注入两个不同的 Mapper，想操作哪个数据源就使用哪个数据源的 Mapper 进行操作处理。

```java
package com.weiz.service.impl;

import com.weiz.mapper.test1.User1Mapper;
import com.weiz.mapper.test2.User2Mapper;
import com.weiz.pojo.User;
import com.weiz.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private User1Mapper user1Mapper;

    @Autowired
    private User2Mapper user2Mapper;

    @Override
    public int saveUser(User user) {
        user1Mapper.insert(user);
        return user2Mapper.insert(user);
    }

    @Override
    public int updateUser(User user) {
        user1Mapper.updateByPrimaryKey(user);
        return user2Mapper.updateByPrimaryKey(user);
    }

    @Override
    public int deleteUser(String userId) {
        user1Mapper.deleteByPrimaryKey(userId);
        return user2Mapper.deleteByPrimaryKey(userId);
    }

    @Override
    public User queryUserById(String userId) {
        user1Mapper.selectByPrimaryKey(userId);
        return user2Mapper.selectByPrimaryKey(userId);
    }
}
```

> 可能会遇到的坑
>
> 1、数据库连接的配置使用jdbc-url ， 不是之前的url 。这点需要注意。
>
> 2、主数据源配置需要加@Primary 注解，其他普通数据源不能加这个注解，否则会报错，复制的时候小心。
>
> 3、各个数据源配置的 basePackages 扫描路径需要配置正确。配置错了不会出异常，但是运行的时候，会找错数据库。
>
> 4、如果Mybatis使用的是xml 配置版，xml位置需要在每个config显示置顶位置。

# RESTful

RESTful 是目前最流行的互联网软件架构。 REST（Representational State Transfer，表述性状态转移）一词是由 Roy Thomas Fielding 在他 2000 年博士论文中提出的，定义了他对互联网软件的架构原则，如果一个架构符合 REST 原则，则称它为 RESTful 架构。

RESTful 把服务器端，所有的应用程序状态和功能定义为“资源”（Resource）。从 RESTful 的⻆度看，网络上的任何东西都是资源，它可以是一段文本、一张图片、一个服务等，每个资源都对应一个特定的 URI（统一资源定位符），并把它进行标示，访问这个 URI 就可以获得这个资源 。

资源可以有多种表现形式，也就是资源的“表述”（Representation），比如一张图片可以使用 JPEG 格式也可以使用 PNG 格式。 URI 只是代表了资源的实体，并不能代表它的表现形式。互联网中，客户端和服务端之间的互动传递的就是资源的表述，我们上网的过程，就是调用资源的 URI，获取它不同表现形式的过程。这种互动只能使用无状态协议 HTTP，也就是说，服务端必须保存所有的状态，客户端可以使用 HTTP 的几个基本操作，包括 GET（获取）、 POST（创建）、 PUT（更新）与DELETE（删除），使得服务端上的资源发“状态转化”（State Transfer），也就是所谓的“表述性状态转移” 。

Restful 相比于 SOAP 更加简单明了，它并没有一个明确的标准，而更像是一种设计的风格。

Restful 特点包括：

　　1、每一个URI代表1种资源；

　　2、客户端使用GET、POST、PUT、DELETE4个表示操作方式的动词对服务端资源进行操作：GET用来获取资源，POST用来新建资源（也可以用于更新资源），PUT用来更新资源，DELETE用来删除资源；

　　3、通过操作资源的表现形式来操作资源；

　　4、资源的表现形式是XML或者HTML；

　　5、客户端与服务端之间的交互在请求之间是无状态的，从客户端到服务端的每个请求都必须包含理解请求所必需的信息。

## Spring Boot 实现Restful 方案

Spring Boot 开发Restful j接口非常简单，通过不同的注解来支持前端的请求，除了经常使用的@RestController 注解外，Spring Boot 还提了一些组合注解。这些注解来帮助简化常用的 HTTP 方法的映射，并更好地表达被注解方法的语义 。

Srping Boot 提供了与Rest 操作方式（GET、POST、PUT、DELETE）对应的注解：

　　1、@GetMapping，处理 Get 请求
　　2、@PostMapping，处理 Post 请求
　　3、@PutMapping，用于更新资源
　　4、@DeleteMapping，处理删除请求
　　5、@PatchMapping，用于更新部分资源

> @PutMapping 主要是用来更新整个资源的，@PatchMapping 主要表示更新部分字段

之前我们也介绍过，Spring Boot 提供了专门做数据处理的控制器：@RestController ，其实这些注解就是我们使用的 @RequestMapping 的简写版本：@GetMapping 其实就等于@RequestMapping(value = "/xxx",method = RequestMethod.GET) 。

## Spring Boot 快速实现Restful

1、设计接口

根据之前介绍的Restful 设计风格设计一组对用户操作的 RESTful API

- /user     POST     创建用户
- /user/id   GET      根据 id 获取用户信息
- /user     PUT      更新用户
- /user/id   DELETE   根据 id删除对应的用户

以上，就是对user操作的接口定义，在实际项目的Restful API 接口文档还会定义全部请求的数据结构体。

 2、实现接口

首先创建UserController 控制器，定义之前设计的相关接口。

```java
package com.weiz.controller;

import com.weiz.pojo.SysUser;
import com.weiz.service.UserService;
import com.weiz.utils.JSONResult;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private Sid sid;

    @PostMapping(value = "user")
    public JSONResult create() throws Exception {

        String userId = sid.nextShort();
        SysUser user = new SysUser();
        user.setId(userId);
        user.setUsername("spring boot" + new Date());
        user.setNickname("spring boot" + new Date());
        user.setPassword("abc123");
        user.setIsDelete(0);
        user.setRegistTime(new Date());

        userService.saveUser(user);
        return JSONResult.ok("保存成功");
    }

    @PutMapping(value = "user")
    public JSONResult update() {
        SysUser user = new SysUser();
        user.setId("10011001");
        user.setUsername("10011001-updated" + new Date());
        user.setNickname("10011001-updated" + new Date());
        user.setPassword("10011001-updated");
        user.setIsDelete(0);
        user.setRegistTime(new Date());
        userService.updateUser(user);

        return JSONResult.ok("保存成功");
    }


    @DeleteMapping("user/{userId}")
    public JSONResult delete(@PathVariable String userId) {
        userService.deleteUser(userId);
        return JSONResult.ok("删除成功");
    }

    @GetMapping("user/{userId}")
    public JSONResult queryUserById(@PathVariable String userId) {
        return JSONResult.ok(userService.queryUserById(userId));
    }

}
```

> 说明：
>
> 　　1、@PathVariable 注解，用于参数映射。
>
> 　　2、Rest需要注意请求的方式，可以看到PUT和POST的URL是相同的，但是后端处理逻辑不同，所以使用的时候千万别搞混了。

 3、测试
实际开发测试的过程中，一般使用postman测试相关的接口。当然，也可以用单元测试来实现。这里简单起见，直接用postman来测试刚刚定义的人员操作接口。

## 版本号

Spring Boot如何实现

实现方案：

- 首先创建自定义的@APIVersion 注解和自定义URL匹配规则ApiVersionCondition。

- 然后创建自定义的 RequestMappingHandlerMapping 匹配对应的request，选择符合条件的method handler。

 

1、创建自定义注解

首先，在com.weiz.config 包下，创建一个自定义版本号标记注解 @ApiVersion。

```java
package com.weiz.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API版本控制注解
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiVersion {
    /**
     * @return 版本号
     */
    int value() default 1;
}
```

> 说明： ApiVersion 为自定义的注解，API版本控制，返回对应的版本号。

2、自定义url匹配逻辑

创建 ApiVersionCondition 类，并继承RequestCondition 接口，作用是：版本号筛选，将提取请求URL中版本号，与注解上定义的版本号进行比对，以此来判断某个请求应落在哪个controller上。

在com.weiz.config 包下创建ApiVersionCondition 类，重写 RequestCondition，创建自定义的url匹配逻辑。

```java
package com.weiz.config;

import org.springframework.web.servlet.mvc.condition.RequestCondition;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApiVersionCondition implements RequestCondition<ApiVersionCondition> {
    private final static Pattern VERSION_PREFIX_PATTERN = Pattern.compile(".*v(\\d+).*");

    private int apiVersion;

    ApiVersionCondition(int apiVersion) {
        this.apiVersion = apiVersion;
    }

    private int getApiVersion() {
        return apiVersion;
    }


    @Override
    public ApiVersionCondition combine(ApiVersionCondition apiVersionCondition) {
        return new ApiVersionCondition(apiVersionCondition.getApiVersion());
    }

    @Override
    public ApiVersionCondition getMatchingCondition(HttpServletRequest httpServletRequest) {
        Matcher m = VERSION_PREFIX_PATTERN.matcher(httpServletRequest.getRequestURI());
        if (m.find()) {
            Integer version = Integer.valueOf(m.group(1));
            if (version >= this.apiVersion) {
                return this;
            }
        }
        return null;
    }

    @Override
    public int compareTo(ApiVersionCondition apiVersionCondition, HttpServletRequest httpServletRequest) {
        return apiVersionCondition.getApiVersion() - this.apiVersion;
    }
}
```

当方法级别和类级别都有ApiVersion注解时，二者将进行合并（ApiVersionRequestCondition.combine）。最终将提取请求URL中版本号，与注解上定义的版本号进行比对，判断url是否符合版本要求。

3、自定义匹配的处理器

在com.weiz.config 包下创建 ApiRequestMappingHandlerMapping 类，重写部分 RequestMappingHandlerMapping 的方法。

```java
package com.weiz.config;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;

public class ApiRequestMappingHandlerMapping extends RequestMappingHandlerMapping {
    private static final String VERSION_FLAG = "{version}";

    private static RequestCondition<ApiVersionCondition> createCondition(Class<?> clazz) {
        RequestMapping classRequestMapping = clazz.getAnnotation(RequestMapping.class);
        if (classRequestMapping == null) {
            return null;
        }
        StringBuilder mappingUrlBuilder = new StringBuilder();
        if (classRequestMapping.value().length > 0) {
            mappingUrlBuilder.append(classRequestMapping.value()[0]);
        }
        String mappingUrl = mappingUrlBuilder.toString();
        if (!mappingUrl.contains(VERSION_FLAG)) {
            return null;
        }
        ApiVersion apiVersion = clazz.getAnnotation(ApiVersion.class);
        return apiVersion == null ? new ApiVersionCondition(1) : new ApiVersionCondition(apiVersion.value());
    }

    @Override
    protected RequestCondition<?> getCustomMethodCondition(Method method) {
        return createCondition(method.getClass());
    }

    @Override
    protected RequestCondition<?> getCustomTypeCondition(Class<?> handlerType) {
        return createCondition(handlerType);
    }
}
```

4、配置注册自定义的RequestMappingHandlerMapping

重写请求过处理的方法，将之前创建的 ApiRequestMappingHandlerMapping 注册到系统中。

```java
package com.weiz.config;

import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
public class WebMvcRegistrationsConfig implements WebMvcRegistrations {
    @Override
    public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
        return new ApiRequestMappingHandlerMapping();
    }
}
```

**测试**：

配置完成之后，接下来编写测试的控制器进行测试。

1、在Controller/api 目录下，分别创建UserV1Controller 和 UserV2Controller

UserV1Controller

```java
@RequestMapping("api/{version}/user")
@RestController
public class UserV1Controller {

    @GetMapping("/test")
    public String test() {
        return "version1";
    }
    @GetMapping("/extend")
    public String extendTest() {
        return "user v1 extend";
    }
}
```

UserV2Controller

```java
@RequestMapping("api/{version}/user")
@RestController
@ApiVersion(2)
public class UserV2Controller {
    @GetMapping("/test")
    public String test() {
        return "user v2 test";
    }
}
```

2、启动项目后，输入相关地址，查看版本控制是否生效

> 说明：
>
> 　　上图的前两个截图说明，请求正确的版本地址，会自动匹配版本的对应接口。当请求的版本大于当前版本时，默认匹配当前版本。
>
> 　　第三个截图说明，当请求对应的版本不存在接口时，会匹配之前版本的接口，即请求/v2/user/extend 接口时，由于v2 控制器未实现该接口，所以自动匹配v1 版本中的接口。这就是所谓的版本继承。



## Swagger2构建API文档

1、配置Swagger2的依赖

在pom.xml 配置文件中，增加Swagger 2 的相关依赖，具体如下：

```xml
<!-- swagger2 依赖配置-->
<dependency>
    <groupId>io.springfox</groupId>
    <artifactId>springfox-swagger2</artifactId>
    <version>2.8.0</version>
</dependency>
<dependency>
    <groupId>io.springfox</groupId>
    <artifactId>springfox-swagger-ui</artifactId>
    <version>2.8.0</version>
</dependency>
```

注意，swagger 2 的版本号和 spring boot的版本号有些不匹配，最开始用2.2的版本和spring boot 的版本还不匹配，后来把 swagger 2 换成了2.8。

2、创建Swagger 2配置类

在com.weiz.config 包中，增加Swagger 2 的配置类，SwaggerConfig 类，具体代码如下：

```java
package com.weiz.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class Swagger2Config implements WebMvcConfigurer {
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.weiz.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Spring Boot中使用Swagger2构建RESTful APIs")
                .description("Spring Boot相关文章请关注：https://www.cnblogs.com/zhangweizhong")
                .termsOfServiceUrl("https://www.cnblogs.com/zhangweizhong")
                .contact("架构师精进")
                .version("1.0")
                .build();
    }

    /**
     *  swagger增加url映射
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}
```

说明：@Configuration 注解让Spring boot来加载该类配置，@EnableSwagger2注解启用Swagger 2，通过配置一个Docket Bean，配置映射路径和要扫描的接口的位置。apiInfo，主要配置一下Swagger2文档网站的信息，例如网站的title、网站的描述、使用的协议等等。

> 注意：
>
> 1、basePackage 可以在SwaggerConfig 里面配置 com.weiz.controller，也可以在启动器里面 ComponentScan 配置。
>
> 2、需要在swaggerconfig 中配置swagger 的url 映射。

 3、添加文档说明内容

上面配置完成之后，接下来需要在api 接口上增加内容说明。这里方便起见，就直接在之前的UserController 中，增加相应的接口内容说明，代码如下所示：

```java
package com.weiz.controller;

import com.weiz.pojo.SysUser;
import com.weiz.service.UserService;
import com.weiz.utils.JSONResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = {"用户接口"})
@RestController
@RequestMapping("/")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private Sid sid;

    @ApiOperation(value="创建用户", notes="根据User对象创建用户")
    @PostMapping(value = "user")
    public JSONResult create(@RequestBody SysUser user) throws Exception {
        String userId = sid.nextShort();
        user.setId(userId);
        userService.saveUser(user);
        return JSONResult.ok("保存成功");
    }

    @ApiOperation(value="更新用户详细信息", notes="根据id来指定更新对象，并根据传过来的user信息来更新用户详细信息")
    @PutMapping(value = "user")
    public JSONResult update(@RequestBody SysUser user) {
        userService.updateUser(user);
        return JSONResult.ok("保存成功");
    }

    @ApiOperation(value="删除用户", notes="根据url的id来指定删除对象")
    @DeleteMapping("user/{userId}")
    public JSONResult delete(@PathVariable String userId) {
        userService.deleteUser(userId);
        return JSONResult.ok("删除成功");
    }

    @ApiOperation(value="查询用户",notes = "通过用户ID获取用户信息")
    @GetMapping("user/{userId}")
    public JSONResult queryUserById(@PathVariable String userId) {
        return JSONResult.ok(userService.queryUserById(userId));
    }
}
```

> 说明：
>
> 　　1、@Api注解，用来给整个controller 增加说明。
>
> 　　2、@ApiOperation注解，用来给各个API 方法增加说明。
>
> 　　3、@ApiImplicitParams、@ApiImplicitParam注解，用来给参数增加说明。
>
> 　　4、Swagger 还有用于对象参数的注解，对象参数的描述也可以放在实体类中。这里不细说，大家可以自行研究。

 

测试验证

完成上面的配置和代码修改之后，Swagger 2 就集成到Spring boot 项目中了，接下来启动Spring Boot程序，访问：[http://localhost:8088/swagger-ui.html](http://localhost:8080/swagger-ui.html) 



# 多环境配置

1、首先，在resource 目录下，分别创建 application-dev.properties、application-test.properties 和 application-production.properties 三个配置文件对应：开发环境、测试环境 和 生产环境。

> 说明：
>
> 　　application.properties 为项目主配置文件，包含项目所需的所有公共配置。
>
> 　　application-dev.properties 为开发环境配置文件，配置开发环境所需的单独配置。
>
> 　　application-test.properties 为测试环境配置文件。
>
> 　　application-production.properties 为生产环境配置文件。

2、修改各个环境的配置文件，修改 application.properties 主配置文件激活不同环境的配置文件 spring.profiles.active=dev

```properties
# 服务器端口配置
server.port=8088

# 数据源相关配置，这里用的是阿里的druid 数据源
spring.datasource.druid.initial-size=1
spring.datasource.druid.min-idle=1
spring.datasource.druid.max-active=20
spring.datasource.druid.test-on-borrow=true
spring.datasource.druid.stat-view-servlet.allow=true

# mybatis 相关配置
mybatis.type-aliases-package=com.weiz.pojo
mybatis.mapper-locations=classpath:mapper/*.xml
mapper.mappers=com.weiz.utils.MyMapper
mapper.not-empty=false
mapper.identity=MYSQL
# 分页框架
pagehelper.helperDialect=mysql
pagehelper.reasonable=true
pagehelper.supportMethodsArguments=true
pagehelper.params=count=countSql
# 开发环境
spring.profiles.active=dev
# 测试环境
# spring.profiles.active=test
# 生产环境
# spring.profiles.active=production
```

## 启动项目指定环境

- `java -jar myapp.jar --spring.profiles.active=dev`
- idea中配置VM参数，`-Dspring.profiles.active=dev`

# 读取配置文件

## 1、使用@Value注解

默认读取的是application.properties。如果是自定义的配置文件，则需要用 @PropertySource 来指定具体要读取的配置文件。

> @PropertySource注解用于指定资源文件读取的位置，它不仅能读properties文件，也能读xml文件，并且通过yaml解析器，配合自定义PropertySourceFactory实现解析yanl文件。

```properties
# 自定义配置
com.weiz.costum.name=weiz-value
com.weiz.costum.website=www.weiz.com
com.weiz.costum.language=java
```

```java
 @Value("${com.weiz.costum.name}")
 private String name;
 @Value("${com.weiz.costum.website}")
 private String website;
 @Value("${com.weiz.costum.language}")
 private String language;

 @RequestMapping("/getvalue")
 public String getValue() {
     System.out.println(name);
     System.out.println(website);
     System.out.println(language);
     return "getvalue";
 }
```

代码说明：

- @Value 为读取配置的注解。需要配置完整的key路径。
- @Value 默认读取application.properties 文件，如果需要自定义配置文件，需要通过@PropertySource 指定。

上面的代码，可以把@Value 的相关代码封装到单独的类中，在该类增加@Component注解，然后读取配置文件。然后在调用的类中注入该类即可。

## 2、使用Environment读取文件

Environment的使用非常方便，只要在使用的类中注入Environment，就能很方便就读取到相应的配置。

```java
    @Autowired
    private Environment env;

    @RequestMapping("/getenv")
    public String getEnvironment() {
        System.out.println(env.getProperty("com.weiz.resource.name"));
        System.out.println(env.getProperty("com.weiz.resource.website"));
        System.out.println(env.getProperty("com.weiz.resource.language"));
        return "hello";
    }
```

代码说明：

- 使用Environment无需指定配置文件，获取的是系统加载的全部配置文件中的配置。
- 注意配置文件的编码格式。

## 3、使用@ConfigurationProperties注解

在实际项目中，当项目需要注入的变量值很多时，上述所述的@value 和 Environment 两种方法会比较繁琐，这时候我们通常使用基于类型安全的配置方式，将properties属性和一个Bean关联在一起，即使用注解@ConfigurationProperties读取配置文件数据。 

1、增加自定义配置文件

在src\main\resources下新建website.properties配置文件：

```properties
com.weiz.resource.name=weiz
com.weiz.resource.website=www.weiz.com
com.weiz.resource.language=java
```

 2、增加自定义配置对象类

首先创建WebSiteProperties 自定义配置对象类。然后，使用@ConfigurationProperties 注解将配置文件属性注入到自定义配置对象类中

```java
package com.weiz.pojo;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix = "com.weiz.resource")
@PropertySource(value = "classpath:website.properties")
public class WebSiteProperties {
    private String name;
    private String website;
    private String language;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
```

代码说明：

- @ConfigurationProperties(prefix = "com.weiz.resource") 绑定属性，其中prefix表示所绑定的属性的前缀。
- @PropertySource(value = "classpath:website.properties") 指定读取的配置文件及其路径。

通过上面的WebSiteProperties类，即可读取全部对应的配置项。

3、使用配置

```java
	@Autowired
    private WebSiteProperties properties;

    @RequestMapping("/getpro")
    public String getProperties() {
        System.out.println(properties.getName());
        System.out.println(properties.getWebsite());
        System.out.println(properties.getLanguage());
        return "hello";
    }
```

## 注意事项

在实际项目中，会碰到很多读取配置文件的业务场景，需要注意各种坑，否则会让你很惆怅。

　　1、yml 文件注意空格和格式缩进。

　　2、properties文件默认使用的是iso8859-1。容易出现乱码问题，如果有中文，需要指定编码格式。

　　3、系统中 yml文件的加载顺序高于properties，但是读取配置信息的时候会读取后加载。

　　4、@PropertySource注解默认只会加载 properties文件，yml 文件不需要此注解。

　　5、@PropertySource注解可以与任何一种方式联合使用。

　　6、简单值推荐使用@Value，复杂对象推荐使用@ConfigurationProperties。

# Spring Data JPA

JPA （Java Persistence API）， Java持久层 API的简称，是JDK 5.0注解或XML描述对象－关系表的映射关系，并将运行期的实体对象持久化到数据库中，JPA是一个基于O/R映射的标准规范。

JPA的总体思想和现有Hibernate、TopLink、JDO等ORM框架大体一致。主要包括括以下3方面的技术：

1. ORM映射元数据 将实体对象持久化到数据库表中
2. API，用来操作实体对象，执行CRUD操作
3. 查询语言，通过面向对象，而非面向数据库的查询语言查询数据

Spring Data JPA 是 Spring 基于 ORM 框架、JPA 规范的基础上封装的一套JPA应用框架，可使开发者用极简的代码即可实现对数据的访问和操作。它提供了包括增删改查等在内的常用功能，且易于扩展！学习并使用 Spring Data JPA 可以极大提高开发效率。

SpringData：其实Spring Data 就是Spring提供了一个操作数据的框架。而Spring Data JPA只是Spring Data框架下的一个基于JPA标准操作数据的模块。

##  SpringBoot整合SpringData JPA

1、在pom.xml 中，增加如下配置：注意，需要添加MySql驱动。

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
</dependency>
```

2、在application.properties 中配置数据源和jpa的基本的相关属性

```properties
#数据库连接
spring.datasource.url=jdbc:mysql://localhost:3306/zwz_test?serverTimezone=UTC&useUnicode=true&characterEncoding=utf-8&useSSL=true
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
#jpa配置
spring.jpa.properties.hibernate.hbm2ddl.auto=create
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL5InnoDBDialect
#SQL 输出
spring.jpa.show-sql=true
#format 下 SQL 进输出
spring.jpa.properties.hibernate.format_sql=true
```

在上面的参数设置说明下：

- spring.jpa.properties.hibernate.hbm2ddl.auto： 配置实体类维护数据库表结构的具体行为，
  - update：最常用的属性，表示当实体类的属性发生变化时，表结构跟着更新；
  - create：表示启动的时候删除上一次生成的表，并根据实体类重新生成表，这个时候之前表中的数据就会被清空；
  - create-drop：表示启动时根据实体类生成表，但是当sessionFactory关闭的时候表会被删除；
  - validate：表示启动时验证实体类和数据表是否一致；
  - none：则什么都不做。

- spring.jpa.show-sql ：表示hibernate在操作的时候在控制台打印真实的sql语句，方便调试。

- spring.jpa.properties.hibernate.format_sql：表示格式化输出的json字符串，方便查看。

- spring.jpa.properties.hibernate.dialect：指定数据库的存储引擎为 InnoDB 

3、数据库实体类是一个 POJO Bean 对象。定义实体类后，在项目启动时，系统会根据实体类创建对应的数据表，实体类如下：

```java
package com.weiz.pojo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 用户实体类
 *
 * @author
 * @since
 */
@Entity
@Table(name = "Users")
public class Users {
    @Id
    private Long id;

    @Column(length = 32)
    private String name;

    @Column(length = 32)
    private String account;

    @Column(length = 64)
    private String pwd;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }
}
```

代码说明：

　　@Entity：必选的注解，声明这个类对应了一个数据库表。
　　@Table：可选的注解。声明了数据库实体对应的表信息。包括表名称、索引信息等。这里声明这个实体类对应的表名是 Users。如果没有指定，则表名和实体的名称保持一致，跟@Entity 注解配合使用。
　　@Id注解：声明了实体唯一标识对应的属性，。
　　@Column注解：用来声明实体属性的表字段的定义。默认的实体每个属性都对应了表的一个字段。字段的名称默认和属性名称保持一致（并不一定相等）。字段的类型根据实体属性类型自动推断。这里主要是声明了字符字段的长度。如果不这么声明，则系统会采用 255 作为该字段的长度。

> 注意：这些注解是建立基于 POJO 的实体对象，需要注意的是 JPA 与 Mybatis 是有区别的。

4、运行验证

以上就是整合jpa的全部配置，配置完之后，启动项目，我们就可以看到日志中如下的内容：同时，连上数据库之后，可以看到Users 表也创建成功了。

5、定义Repository 

项目整合 jpa 成功之后，接下来可以定义Repository 数据访问接口了，只需要继承 JpaRepository 类，就会帮我们自动生成很多内置方法，如下：

```java
package com.weiz.dao;

import com.weiz.pojo.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRespository extends JpaRepository<Users, Long> {

}
```

上面的代码可以看到，我们基本上一行代码也不用写，就能实现Users 用户的增删改查等全部的方法。

6、调用

```java
package com.weiz.controller;

import com.weiz.dao.UserRespository;
import com.weiz.pojo.Users;
import com.weiz.utils.JSONResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRespository userRespository;

    @RequestMapping("/save")
    public JSONResult save(){
        Users user = new Users();
        user.setId((long) 1);
        user.setName("spring boot jpa");
        user.setPwd("123456");
        userRespository.save(user);
        return JSONResult.ok("保存成功");
    }

    @RequestMapping("/update")
    public JSONResult update() {
        Users user = new Users();
        user.setId((long) 1);
        user.setName("spring boot jpa update");
        user.setPwd("123456");
        user.setAccount("sbj");
        userRespository.save(user);
        return JSONResult.ok("修改成功");
    }

    @RequestMapping("/delete")
    public JSONResult delete() {
        Users user = new Users();
        user.setId((long) 1);
        userRespository.delete(user);
        return JSONResult.ok("删除成功");
    }

    @RequestMapping("/select")
    public JSONResult select() {
        Optional<Users> users = userRespository.findById((long) 1);
        return JSONResult.ok(users);
    }
}
```

## 自定义查询

1、预定义查询

因为UserRepository继承了 JpaRepository 拥有了父类的这些JPA自带的方法。调用预定义方法：

```java
@RequestMapping("/test")
public void test() {
    Users user = new Users();
    user.setId((long) 1);

    userRespository.findById((long) 1);
    userRespository.findAll();
    userRespository.delete(user);
    userRespository.deleteById((long) 1);
    userRespository.existsById((long) 1);
}
```

上面所有JpaRepository父类拥有的方法都可以直接调用 。

2、自定义查询

Spring Data JPA 支持根据实体的某个属性实现数据库操作，主要的语法是 findByXX、 readAByXX、queryByXX、 countByXX、 getByXX 后跟属性名称，利用这个功能仅需要在定义的 Repository 中添加对应的方法名即可，无需具体实现完整的方法，使用时 Spring Boot 会自动动帮我们实现对应的sql语句。

- 属性查询

根据姓名查询，示例如下：

```java
@Repository
public interface UserRespository extends JpaRepository<Users, Long> {
    Users findByName(String name,String account);
}
```

上面的实例可以看到，我们可以在UserRepository 接口中进行接口声明。例如，如果想根据实体的 name和account 这两个属性来进行查询User的信息。那么直接在 UserRepository 中增加一个接口声明即可。

- 组合条件查询

JPA不仅支持单个属性查询，还能支持多个属性，根据And、or 等关键字组合查询：

```java
　　Users findByNameAndAccount(String name,String account);
```

上面的例子，就是根据姓名和账号两个条件组合查询。这个是组合查询的例子，删除和统计也是类似的：deleteByXXXAndXXX、countByXXXAndXXX。可以根据查询的条件不断地添加和拼接， Spring Boot 都可以正确解析和执行。

- JPA关键字

JPA的自定义查询除了And、or 关键字外，基本上SQL语法中的关键字，JPA都支持，比如：like，between 等。

3、自定义SQL语句

上面介绍了JPA的很多条件查询的方法。但是，实际项目中，还是有些场景上面的查询条件无法满足。那么我们就可以通过 @Query 注解写hql 来实现。

```
@Query("select u from Users u where u.name = :name1")
List<UserDO> findByHql(@Param("name1") String name1);
```

说明：

　　1、@Query 注解，表示用执行hql语句。

　　2、name1等参数对应定义的参数。

上面是通过hql，如果hql 写着不习惯，也可以用本地 SQL 语句来完成查询：

```java
@Query(value = "select * from users where name = ?1",nativeQuery = true)
List<User> findUserBySql(String name);
```

上面示例中的 ?1 表示方法参数中的顺序，nativeQuery = true 表示执行原生sql语句。

## 实体映射关系 一对一 一对多 多对多

### 常用注解详解

@JoinColumn指定该实体类对应的表中引用的表的外键，name属性指定外键名称，referencedColumnName指定应用表中的字段名称

@JoinColumn（name=”role_id”）: 标注在连接的属性上(一般多对1的1)，指定了本类用1的外键名叫什么。

@JoinTable(name="permission_role") ：标注在连接的属性上(一般多对多)，指定了多对多的中间表叫什么。

> 备注：Join的标注，和下面几个标注的mappedBy属性互斥！

@OneToOne 配置一对一关联，属性targetEntity指定关联的对象的类型 。

@OneToMany注解“一对多”关系中‘一’方的实体类属性（该属性是一个集合对象），targetEntity注解关联的实体类类型，mappedBy注解另一方实体类中本实体类的属性名称

@ManyToOne注解“一对多”关系中‘多’方的实体类属性（该属性是单个对象），targetEntity注解关联的实体类类型

 　属性1： mappedBy="permissions" 表示，当前类不维护状态，属性值其实是本类在被标注的链接属性上的链接属性，此案例的本类时Permission，连接属性是roles,连接属性的类的连接属性是permissions 

​    属性2： fetch = FetchType.LAZY 表示是不是懒加载，默认是，可以设置成FetchType.EAGER

​    属性3：cascade=CascadeType.ALL 表示当前类操作时，被标注的连接属性如何级联，比如班级和学生是1对多关系，cascade标注在班级类中，那么执行班级的save操作的时候(班级.学生s.add(学生))，能级联保存学生,否则报错，需要先save学生，变成持久化对象，在班级.学生s.add(学生)

> 注意:只有OneToOne，OneToMany，ManyToMany上才有mappedBy属性，ManyToOne不存在该属性；

###  一对一

首先，一对一的实体关系最常用的场景就是主表与从表，即主表存关键经常使用的字段，从表保存非关键字段，类似 User与UserDetail 的关系。主表和详细表通过外键一一映射。

一对一的映射关系通过@OneToOne 注解实现。通过 @JoinColumn 配置一对一关系。

其实，一对一有好几种，这里举例的是常用的一对一双向外键关联（改造成单向很简单，在对应的实体类去掉要关联其它实体的属性即可），并且配置了级联删除和添加，相关类如下：

1、User 实体类定义：

```java
package com.weiz.pojo;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "Users")
public class Users {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String account;
    private String pwd;

    @OneToOne(cascade = {CascadeType.PERSIST,CascadeType.REMOVE})
    @JoinColumn(name="detailId",referencedColumnName = "id")
    private UsersDetail userDetail;

    @Override
    public String toString() {
        return String.format("Book [id=%s, name=%s, user detail=%s]", id, userDetail.getId());
    }
}
```

@OneToMany(targetEntity=UsersDetail.class,fetch=FetchType.LAZY,mappedBy="source")
关联的实体的主键一般是用来做外键的。但如果此时不想主键作为外键，则需要设置referencedColumnName属性。当然这里关联实体(Address)的主键 id 是用来做主键，所以这里第20行的 referencedColumnName = "id" 实际可以省略。

2、从表 UserDetail 实体类定义

```java
package com.weiz.pojo;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "UsersDetail")
public class UsersDetail {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "address")
    private String address;

    @Column(name = "age")
    private Integer age;

    @Override
    public String toString() {
        return String.format("UsersDetail [id=%s, address=%s, age=%s]", id,address,age);
    }
}
```

3、测试

```java
@RequestMapping("/save")
    public JSONResult save(){
        //用户
        Users user = new Users();
        user.setName("springbootjpa");
        user.setAccount("admin");
        user.setPwd("123456");
        //详情
        UsersDetail usersDetail = new UsersDetail();
        usersDetail.setAge(19);
        usersDetail.setAddress("beijing,haidian,");
        //保存用户和详情
        user.setUserDetail(usersDetail);
        userRespository.save(user);
        return JSONResult.ok("保存成功");
    }
```

### 一对多和对多对一

一对多和多对一的关系映射，最常见的场景就是：人员角色关系。实体Users：人员。 实体 Roles：角色。 人员 和角色是一对多关系(双向)。那么在JPA中，如何表示一对多的双向关联呢？

JPA使用@OneToMany和@ManyToOne来标识一对多的双向关联。一端(Roles)使用@OneToMany,多端(Users)使用@ManyToOne。在JPA规范中，一对多的双向关系由多端(Users)来维护。也就是说多端(Users)为关系维护端，负责关系的增删改查。

一端(Roles)则为关系被维护端，不能维护关系。 一端(Roles)使用@OneToMany注释的mappedBy="role"属性表明Author是关系被维护端。 

多端(Users)使用@ManyToOne和@JoinColumn来注释属性 role，@ManyToOne表明Article是多端，@JoinColumn设置在Users表中的关联字段(外键)。 

1、原先的User 实体类修改如下：

```java
package com.weiz.pojo;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "Users")
public class Users {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String account;
    private String pwd;

    @OneToOne(cascade = {CascadeType.PERSIST,CascadeType.REMOVE})
    @JoinColumn(name="detailId",referencedColumnName = "id")
    private UsersDetail userDetail;

    /**一对多，多的一方必须维护关系，即不能指定mapped=""**/
    @ManyToOne(fetch = FetchType.LAZY,cascade=CascadeType.MERGE)
    @JoinColumn(name="role_id")
    private Roles role;

    @Override
    public String toString() {
        return String.format("Book [id=%s, name=%s, user detail=%s]", id, userDetail.getId());
    }
}
```

2、角色实体类

```java
package com.weiz.pojo;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "Roles")
public class Roles {
    @Id
    @GeneratedValue()
    private Long id;

    private String name;
  
 　 @OneToMany(mappedBy="role",fetch=FetchType.LAZY,cascade=CascadeType.ALL)
    private Set<Users> users = new HashSet<Users>();
}
```

最终生成的表结构 Users 表中会增加role_id 字段。

3、测试

```java
@RequestMapping("/updateRole/{id}")
    public JSONResult updateRole(@PathVariable Long id) {
        Users user = userRespository.findById(id).orElse(null);
        Long roleId = Long.valueOf(25);
        Roles role = roleRespository.findById(roleId).orElse(null);
        if (user!=null){
            user.setRole(role);
        }
        userRespository.save(user);
        return JSONResult.ok("修改成功");
    }
```

主要特别注意的是更新和删除的级联操作：

其中 @OneToMany 和 @ManyToOne 用得最多，这里再补充一下 关于级联，一定要注意，要在关系的维护端，即 One 端。
比如 人员和角色，角色是One，人员是Many；cascade = CascadeType.ALL 只能写在 One 端，只有One端改变Many端，不准Many端改变One端。 特别是删除，因为 ALL 里包括更新，删除。
如果删除一条评论，就把文章删了，那算谁的。所以，在使用的时候要小心。一定要在 One 端使用。

###  三、多对多 

多对多的映射关系最常见的场景就是：权限和角色关系。角色和权限是多对多的关系。一个角色可以有多个权限，一个权限也可以被很多角色拥有。 JPA中使用@ManyToMany来注解多对多的关系，由一个关联表来维护。这个关联表的表名默认是：主表名+下划线+从表名。(主表是指关系维护端对应的表,从表指关系被维护端对应的表)。这个关联表只有两个外键字段，分别指向主表ID和从表ID。字段的名称默认为：主表名+下划线+主表中的主键列名，从表名+下划线+从表中的主键列名。 

需要注意的：

1、多对多关系中一般不设置级联保存、级联删除、级联更新等操作。
2、可以随意指定一方为关系维护端，在这个例子中，我指定 User 为关系维护端，所以生成的关联表名称为： role_permission，关联表的字段为：role_id 和 permission_id。
3、多对多关系的绑定由关系维护端来完成，即由 role1.setPermissions(ps);来绑定多对多的关系。关系被维护端不能绑定关系，即permission不能绑定关系。
4、多对多关系的解除由关系维护端来完成，即由 role1.getPermissions().remove(permission);来解除多对多的关系。关系被维护端不能解除关系，即permission不能解除关系。
5、如果Role和Permission已经绑定了多对多的关系，那么不能直接删除Permission，需要由Role解除关系后，才能删除Permission。但是可以直接删除Role，因为Role是关系维护端，删除Role时，会先解除Role和Permission的关系，再删除Role。

下面，看看角色Roles 和 权限 Permissions 的多对多的映射关系实现，具体代码如下：

1、角色Roles 实体类定义：

```java
package com.weiz.pojo;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "Roles")
public class Roles {
    @Id
    @GeneratedValue()
    private Long id;

    private String name;
   
    @ManyToMany(cascade = CascadeType.MERGE,fetch = FetchType.LAZY)
    @JoinTable(name="permission_role")
    private Set<Permissions> permissions = new HashSet<Permissions>();

    @OneToMany(mappedBy="role",fetch=FetchType.LAZY,cascade=CascadeType.ALL)
    private Set<Users> users = new HashSet<Users>();
}
```

代码说明：

cascade表示级联操作，all是全部，一般用MERGE 更新,persist表示持久化即新增
此类是维护关系的类，删除它，可以删除对应的外键,但是如果需要删除对应的权限就需要CascadeType.all
cascade:作用在本放，对于删除或其他操作本方时，对标注连接方的影响！和数据库一样！！

 2、权限Permissions 实体类定义：

```java
package com.weiz.pojo;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

/**
 * 权限表
 */
@Getter
@Setter
@Entity
@Table(name="Permissions")
public class Permissions {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String type;
    private String url;
    @Column(name="perm_code")
    private String permCode;

    @ManyToMany(mappedBy="permissions",fetch = FetchType.LAZY)
    private Set<Roles> roles;
}
```

注意：不能两边用mappedBy:这个属性就是维护关系的意思！谁主类有此属性谁不维护关系。
\* 比如两个多对多的关系是由role中的permissions维护的，那么，只有操作role实体对象时，指定permissions，才可建立外键的关系。
\* 只有OneToOne，OneToMany，ManyToMany上才有mappedBy属性，ManyToOne不存在该属性； 并且mappedBy一直和joinXX互斥。

 

注解中属性的汉语解释：权限不维护关系，关系表是permission_role，全部懒加载，角色的级联是更新 (多对多关系不适合用all，不然删除一个角色，那么所有此角色对应的权限都被删了，级联删除一般用于部分一对多时业务需求上是可删的，比如品牌类型就不适合删除一个类型就级联删除所有的品牌，一般是把此品牌的类型设置为null(解除关系)，然后执行删除，就不会报错了！)

3、测试

```java
@RequestMapping("/save")
    public JSONResult save(){
        // 角色
        Roles role1 = new Roles();
        role1.setName("admin role");
        // 角色赋权限
        Set<Permissions> ps = new HashSet<Permissions>();
        for (int i = 0; i < 3; i++) {
            Permissions pm = new Permissions();
            pm.setName("permission"+i);
            permissionRespository.save(pm);  /**由于我的Role类没有设置级联持久化，所以这里需要先持久化pm,否则报错！*/
            ps.add(pm);
        }
        role1.setPermissions(ps);
        // 保存
        roleRespository.save(role1);
        return JSONResult.ok("保存成功");
    }
```

配置说明：由于多对1不能用mapped那么，它必然必须维护关系，即mapped属性是在1的一方，维护关系是多的一方由User维护的，User的级联是更新，Role的级联是All，User的外键是role_id指向Role。

 

说明：test1我们可以看到，由于role方是维护关系的，所以建立Roles.set(Permissions)就能把关系表建立，但是注意一点，由于我没有设置级联=all，而Permissions是个临时对象，而临时对象保存时会持久化，如果不是我级联保存的话，那么会报错，解决办法如测试范例，先通过save(pm),再操作。

​     test2我们可以观察到，当执行完后，中间表的删除是由维护关系的role删除了（自己都删除了，关系肯定也需要维护的），但是，permission表还存在数据。

​     test3我们可以观察到，我把role.setPermission(null)，就可以解除关系，中间表的对应的记录也没有了。

### 总结

维护关系是由mapped属性决定，标注在那，那个就不维护关系。级联操作是作用于当前类的操作发生时，对关系类进行级联操作。

和hibernate使用没多大区别啊！



# SpringBoot核心注解及其组成

启动类上的注解是@SpringBootApplication ，组成除了4个原注解外还有下面3种：

- @SpringBootConfiguration：组合了@Configuration注解，实现配置文件的功能。
- @EnableAutoConfiguration：打开自动配置的功能，也可以关闭某个自动配置的选项，如关闭数据源自动配置功能 @SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
- @ComponentScan：Spring组件扫描。



# 自动装配流程

执行Main()方法中的run()方法，它会去做IOC的初始化。注解初始化会将java配置类的类对象传递进去，然后走到@SpringBootApplication注解，其中起作用的是@enableAutoConfiguration注解，它会去加载spring.factories和spring-autoconfigure-metadata.properties配置文件进行候选以及筛选的工作，加载进内存后，实际上我们会在AutoConfigurationSelect中把与之对应的返回类型的全类路径的类型对象加载到容器中。



# 自动装配原理

@SpringApplication -> @Configuration -> ConfigurationClassPostProcessor -> @Import ->延迟加载 -> 自动装配 -> SPI 去重 排除 过滤

META-INFO/spring.factories文件



# @Import注解

是在Spring3.0的时候提供，目的是为了替换在XML配置文件中的Import标签。它除了可以导入第三方的Java配置类还扩展了其他功能：

- 可以把某个类型的对象注入到容器中
- 导入的类型如果实现了ImportSelector接口，那么会调用接口中声明的方法，然后把返回类型的全类路径的类型对象注入到容器中。
- 导入的类型实现了ImportBeanDefinitionRegistrar接口，那么会调用声明的方法中显式提供的注册器来完成注入。可以自行封装BeanDefinition。

# SpringBoot自动装配中为什么用DeferredImportSelector

延迟注入bean实例的作用。

在SpringBoot自动装配中核心是会加载所有依赖中的META/spring.factories文件中的配置信息。有多个文件需要加载需要多次操作。可考虑把所有的信息都加载后再统一把需要注入到容器中的内容注入进去。



# 对属性文件中的账号密码加密

清楚SpringBoot的执行流程：																			获取加载的属性信息，解密覆盖

SpringBoot项目启动 -> 属性文件加载(ConfigFileApplicationListener加载属性文件) -> Spring容器刷新

- 自定义监听器在加载解析了配置文件之后解密覆盖

- 通过对应的后置处理器解密覆盖

  ```java
  public class SafeEncryptProcessor implements EnvironmenPostProcessor{
      @Override
      public void postProcessorEnvironment(ConfigurableEnvironment environment, SpringApplication application){
          //...
      }
  }
  ```

  

# bootstrap.yml的意义

SpringBoot中默认支持的属性文件：

- application.properties
- application.xml
- application.yml
- application.yaml

bootstrap.yml在SpringBoot中默认是不支持的，需要在SpringCloud环境下才支持，作用是在SpringBoot项目启动之前启动一个父容器，可以完成一些加载初始化操作如加载配置中心的信息。



# SpringBoot配置加载顺序

1. 开发者工具Devtools全局配置参数
2. 单元测试上的@TestPropertySource注解指定的参数
3. 单元测试上的@SpringBootTest注解指定的参数
4. 命令行指定的参数，如 java -jar springboot.jar --name="Java技术栈"
5. 命令行中的SPRING_APPLICATION_JSON指定参数，如 java -Dspring.application.json='{"name":"Java技术栈"}' -jar springboot.jar
6. ServletConfig初始化参数
7. ServletContext初始化参数
8. JNDI参数，如 java:comp/env/spring.application.json
9. Java系统参数，来源 System.getProperties()
10. 操作系统环境变量参数
11. RandomValuePropertySource随机数，仅匹配：random.*
12. JAR包外面的配置文件参数 application-{profile}.properties (YAML)
13. JAR包里面的配置文件参数 application-{profile}.properties (YAML)
14. JAR包外面的配置文件参数 application.properties (YAML)
15. JAR包里面的配置文件参数 application.properties (YAML)
16. @configuration配置文件上 @PropertySource注解加载的参数
17. 默认参数，通过SpringApplication.setDefaultProperties指定

**==数字越小优先级越高，即数字小的会覆盖数字大的参数值。==**



# 如何在SpringBoot启动的时候运行一些特定的代码

可以实现ApplicationRunner或者CommandLineRunner，这2个接口实现方式都一样，都只提供一个run方法，实现上述接口的类加入IOC容器即可生效。











