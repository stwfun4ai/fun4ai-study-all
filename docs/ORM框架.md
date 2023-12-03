# 定义

**对象关系映射（Object Relational Mapping，简称ORM）模式是一种为了解决面向对象与关系数据库存在的互不匹配的现象的技术**。它通过描述 Java 对象与数据库表之间的映射关系，自动将 Java 应用程序中的对象持久化到关系型数据库的表中。



- Mybatis
- Mybatis Plus

- Hibernate

- SpringData JPA (Java Persistence API)
- Jfinal
- eclipseLink



# MyBatis中使用#和$书写占位符有什么区别？

答：#将传入的数据都当成一个字符串，会对传入的数据自动加上引号；$将传入的数据直接显示生成在SQL中。注意：使用$占位符可能会导致SQL注射攻击，能用#的地方就不要使用$，写order by子句的时候应该用$而不是#。

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

# Mybatis 中一级缓存与二级缓存的区别？

合理利用缓存可以避免频繁操作数据库，减轻数据库压力，同时提高系统的性能。

一级缓存是`sqlSession`级别的，`Mybatis`对缓存提供支持，但是在没有配置的默认情况下，它只开启一级缓存。一级缓存在操作数据库时需要构造`sqlSession`对象，在对象中有一个数据结构`（HashMap）`用于存储缓存数据。不同的`sqlSession`之间的缓存数据区域是互相不影响的。也就是他只能作用在同一个`sqlSession`中，不同的`sqlSession`中的缓存是互相不能读取的。当在同一个`sqlSession`中执行两次相同的`sql`语句时，第一次执行完毕会将数据库中查询的数据写到缓存（内存）。

二级缓存是mapper级别的缓存，多个`sqlSession` 去操作同一个mapper的`sql`语句，它们可以公用二级缓存，二级缓存是跨`sqlSession`的。

# MyBatis 是如何进行分页的？分页插件的原理是什么？

1. MyBatis 使用 RowBounds 对象进行分页，它是针对 ResultSet 结果集执行的内存分页，而非物理分页；
2. 可以在 sql 内直接书写带有物理分页的参数来完成物理分页功能，
3. 也可以使用分页插件来完成物理分页。

分页插件的基本原理是使用 MyBatis 提供的插件接口，实现自定义插件，在插件的拦截方法内拦截待执行的 sql，然后重写 sql，根据 dialect 方言，添加对应的物理分页语句和物理分页参数。

举例：`select _ from student` ，拦截 sql 后重写为：`select t._ from （select \* from student）t limit 0，10`

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