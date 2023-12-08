# ORM定义

**对象关系映射（Object Relational Mapping，简称ORM）模式是一种为了解决面向对象与关系数据库存在的互不匹配的现象的技术**。它通过描述 Java 对象与数据库表之间的映射关系，自动将 Java 应用程序中的对象持久化到关系型数据库的表中。

- Mybatis
- Mybatis Plus

- Hibernate

- SpringData JPA (Java Persistence API)
- Jfinal
- eclipseLink

# MyBatis配置文件样式

## 核心 MyBatis-config.xml

```xml
<?xml version="1.0" encoding="UTF-8" ?>  
<!DOCTYPE configuration  
PUBLIC "-//mybatis.org//DTD Config 3.0//EN"  
"http://mybatis.org/dtd/mybatis-3-config.dtd">  <!--核心配置文件，这里是mybatis-3-config.dtd-->
<configuration> 

<!--MyBatis核心配置文件中，标签的顺序：
	properties,settings,typeAliases,typeHandlers,objectFactory,objectWrapperFactory,
	reflectorFactory,plugins,environment,databaseIdPorvider,mappers-->

	<!--引入jdbc.properties，让核心配置文件与jdbc.properties产生关联，让其能访问jdbc.properties-->
	<properties resource="jdbc.properties"/>

    <settings>
        <!--将表中字段的下划线自动转换为驼峰-->
        <setting name="mapUnderscoreToCamelCase" value="true"/>
        <!--开启延迟加载-->
        <setting name="lazyLoadingEnabled" value="true"/>
    </settings>
    
	<!--设置类型别名（resultType），alias标签可以省略，省略时自动默认为类名且不区分大小写。-->
	<typeAliases>
        
		<!-- <typeAlias type="com.atguigu.mybatis.pojo.User" alias="User"></typeAlias> -->
        
        <!--以包为单位，将包下所有的类型设置默认的类型别名，即类名且不区分大小写-->
        <package name="com.atguigu.mybatis.pojo"/>
	</typeAliases>

	<!--设置连接数据库的环境
		environments:配置多个连接数据库的环境
		属性：
			default:设置默认使用的环境id 
	-->
	<environments default="development">
		<!--environment:配置某个具体的环境
			属性：
			id：表示连接数据库环境的唯一标识，不能重复
		-->
		<environment id="development">
			<!--
				transactionManager:设置事务管理方式
				属性：
					type="JDBC/MANAGED"
					JDBC:标识当前环境中，执行SQL时，使用的是JDBC中原生的事务管理方式，即事务的提交和回滚需要手动设置。
					MANAGED:被管理，例如Srping
				-->
			<transactionManager type="JDBC"/>
			<!--
				dataSource:配置数据源
				属性：
					type:设置数据源的类型
					type="POOLED/UNPOOLED/JNDI"
					POOLED:表示使用数据库连接池缓存数据库连接
					UNPOOLED：表示不使用数据库连接池
					JNDI:表示使用上下文中的数据源
				注：Spirng整合MyBatis时，Spirng本身就有数据源，Spirng可以管理数据源（Spirng中就是一个对象）,MyBatis使用Spring的数据源即可-->
			<dataSource type="POOLED"> 
				<!--设置连接数据源的驱动-->
				<property name="driver" value=${jdbc.driver}/>
 				<!--设置连接数据库的连接地址-->
				<property name="url" value=${jdbc.url}/>  
				<property name="username" value=${jdbc.username}/>  
				<property name="password" value=${jdbc.password}/>  
			</dataSource>  
		</environment>  
	</environments>  
	<!--引入映射文件-->  
	<mappers>  
		<!--resource目录下，不能用 . 因为不是包，是文件路径-->  
		<mapper resource="mappers/UserMapper.xml"/> 
        <!--
			以包为单位引入映射文件
			要求：
				1、mapper接口所在的包要和映射文件所在的包一致
				2、mapper接口要和映射文件的名字一致
			注：resource下一次创建多个包，不能用 . 要用 / 创建-->
        <package name="com.atguigu.mybatis.mapper"/>
	</mappers>  
</configuration>
```

## 映射文件 [Table]Mapper.xml

```xml
<?xml version="1.0" encoding="UTF-8" ?>  
<!DOCTYPE mapper  
PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"  
"http://mybatis.org/dtd/mybatis-3-mapper.dtd">  <!--mapper文件，这里是mybatis-3-mapper.dtd-->

<!--映射文件的namespace和mapper接口的全类名保持一致。-->
<mapper namespace="com.atguigu.mybatis.mapper.UserMapper">  

	<!--映射文件中的SQL语句的id和mapper接口中的方法名保持一致。-->  
	<insert id="insertUser">  
		insert into t_user values(null,'张三','123',23,'女')  
	</insert>

	<!--查询语句必须设置resultType或resultMap
			resultType:设置默认的映射关系，MyBatis根据字段名与属性名自动映射,适用于字段名与属性名一致的情况。
			resultMap:设置自定义的映射关系，适用于字段名与属性名不一致的情况，或一对多，多对一-->  
	<select id="selectUserById" resultType="com.lmj.mybatis.pojo.User">
		select * from t_user where id = 1
	</select>
</mapper>
```



# MyBatis中使用#和$书写占位符有什么区别？

- #占位符将传入的数据都当成一个字符串，会对传入的数据自动加上引号；
- $将传入的数据直接显示生成在SQL中。

> 注意：使用$占位符可能会导致SQL注射攻击，能用#的地方就不要使用$，
>
> order by句子 或 from 表名/列名  的时候应该用$而不是#。

# 多个字面量类型的参数

若mapper接口中的方法参数为多个时，此时MyBatis会自动将这些参数放在一个map集合中

1. 以arg0,arg1…为键，以参数为值；
2. 以param1,param2…为键，以参数为值。

> 使用arg或param或混着用都行，要注意的是，arg是从arg0开始的，param是从param1开始的

# 模糊查询like

```sql
select * from t_user where username like “%”#{mohu}“%”
```



# MyBatis中的动态SQL

- if
- choose / when / otherwise
- trim
- where
- set
- foreach

```xml
<select id="bar" resultType="Blog">
    select * from t_blog where id in
    <foreach collection="array" index="index" 
        item="item" open="(" separator="," close=")">
        #{item}
    </foreach>
</select>
```

# MyBatis缓存中的常用概念

MyBatis 缓存：它用来优化 SQL 数据库查询的，但是可能会产生脏数据。

SqlSession：代表和数据库的一次会话，向用户提供了操作数据库的方法。

MappedStatement：代表要发往数据库执行的指令，可以理解为是 SQL 的抽象表示。

Executor：代表用来和数据库交互的执行器，接受 MappedStatment 作为参数。

namespace：每个 Mapper 文件只能配置一个 namespace，用来做 Mapper 文件级别的缓存共享。

映射接口：定义了一个接口，然后里面的接口方法对应要执行 SQL 的操作，具体要执行的 SQL 语句是写在映射文件中。

映射文件：MyBatis 编写的 XML 文件，里面有一个或多个 SQL 语句，不同的语句用来映射不同的接口方法。通常来说，每一张单表都对应着一个映射文件。

# MyBatis 中一级缓存与二级缓存的区别？

合理利用缓存可以避免频繁操作数据库，减轻数据库压力，同时提高系统的性能。

- 一级缓存是`sqlSession`级别的，`Mybatis`对缓存提供支持，但是在没有配置的默认情况下，它只开启一级缓存。一级缓存在操作数据库时需要构造`sqlSession`对象，它持有了`Executor`，每个`Executor`中有一个`LocalCache`，它是一个`（HashMap）`结构用于存储缓存数据（key是SQL语句，value是查询结果）。不同的`sqlSession`之间的缓存数据区域是互相不影响的。也就是他只能作用在同一个`sqlSession`中，不同的`sqlSession`中的缓存是互相不能读取的。当在同一个`sqlSession`中执行两次相同的`sql`语句时，第一次执行完毕会将数据库中查询的数据写到缓存（内存）。

  `<setting name="localCacheScope" value="SESSION"/>`

- 二级缓存是mapper级别的缓存，多个`sqlSession` 去操作同一个mapper的`sql`语句，它们可以共用二级缓存，二级缓存是跨`sqlSession`的。**即同一个namespace下的所有操作语句都影响同一个Cache。**

  `<settingname="cacheEnabled"value="true"/>`

先查二级缓存的查询，没命中再查一级缓存，也没命中查数据库。SQLSession关闭之后，一级缓存中的数据会写入二级缓存。

可自定义缓存或使用第三方缓存EHCache。

# 整合第三方缓存 EHCache

添加 mybatis-ehcache 依赖包。

```xml
<!-- Mybatis EHCache整合包 -->
<dependency>
	<groupId>org.mybatis.caches</groupId>
	<artifactId>mybatis-ehcache</artifactId>
	<version>1.2.1</version>
</dependency>
<!-- slf4j日志门面的一个具体实现 -->
<dependency>
	<groupId>ch.qos.logback</groupId>
	<artifactId>logback-classic</artifactId>
	<version>1.2.3</version>
</dependency>
```

创建EHCache的配置文件ehcache.xml。

```xml
<?xml version="1.0" encoding="utf-8" ?>
<ehcache xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:noNamespaceSchemaLocation="../config/ehcache.xsd">
    <!-- 磁盘保存路径 -->
    <diskStore path="D:\atguigu\ehcache"/>
    <defaultCache
            maxElementsInMemory="1000"
            maxElementsOnDisk="10000000"
            eternal="false"
            overflowToDisk="true"
            timeToIdleSeconds="120"
            timeToLiveSeconds="120"
            diskExpiryThreadIntervalSeconds="120"
            memoryStoreEvictionPolicy="LRU">
    </defaultCache>
</ehcache>
```

在xxxMapper.xml文件中设置二级缓存类型

`<cache type="org.mybatis.caches.ehcache.EhcacheCache"/>`



# MyBatis 是如何进行分页的？分页插件的原理是什么？

1. MyBatis 使用 RowBounds 对象进行分页，它是针对 ResultSet 结果集执行的内存分页，而非物理分页；
2. 可以在 sql 内直接书写带有物理分页的参数来完成物理分页功能，
3. 也可以使用分页插件来完成物理分页。如pagehelper

分页插件的基本原理是使用 MyBatis 提供的插件接口，实现自定义插件，在插件的拦截方法内拦截待执行的 sql，然后重写 sql，根据 dialect 方言，添加对应的物理分页语句和物理分页参数。

举例：`select _ from student` ，拦截 sql 后重写为：`select t._ from （select \* from student）t limit 0，10`

> limit a, b	//a:OFFSET	b:pageSize

# MyBatis 详细工作流程？

[![img](images\MyBatis工作流程.png)](https://camo.githubusercontent.com/fe8aec218f524f9e4a2eea96485cf53399e6d338b578a3e98c7554585621c525/687474703a2f2f7777772e6d7962617469732e636e2f7573722f75706c6f6164732f323031392f31302f3332363531373634332e706e67)

1. 读取`MyBatis`的配置文件。`mybatis-config.xml`为`MyBatis`的全局配置文件，用于配置数据库连接信息。
2. 加载映射文件。映射文件即SQL映射文件，该文件中配置了操作数据库的SQL语句，需要在`MyBatis`配置文件`mybatis-config.xml`中加载。`mybatis-config.xml` 文件可以加载多个映射文件，每个文件对应数据库中的一张表。
3. 构造会话工厂。通过`MyBatis`的环境配置信息构建会话工厂`SqlSessionFactory`。
4. 创建会话对象。由会话工厂创建`SqlSession`对象，该对象中包含了执行SQL语句的所有方法。
5. Executor执行器。`MyBatis`底层定义了一个Executor接口来操作数据库，它将根据`SqlSession`传递的参数动态地生成需要执行的SQL语句，同时负责查询缓存的维护。
6. `MappedStatement`对象。在Executor接口的执行方法中有一个`MappedStatement`类型的参数，该参数是对映射信息的封装，用于存储要映射的SQL语句的id、参数等信息。
7. 输入参数映射。输入参数类型可以是Map、List等集合类型，也可以是基本数据类型和POJO类型。输入参数映射过程类似于JDBC对`preparedStatement`对象设置参数的过程。
8. 输出结果映射。输出结果类型可以是Map、List等集合类型，也可以是基本数据类型和POJO类型。输出结果映射过程类似于JDBC对结果集的解析过程。