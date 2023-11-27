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


