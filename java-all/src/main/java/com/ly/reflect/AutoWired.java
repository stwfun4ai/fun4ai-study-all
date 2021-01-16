package com.ly.reflect;

import java.lang.annotation.*;

/**
 * @Description 使用反射自己实现注解自动注入
 * @Created by fun4ai
 * @Date 1/10 0010 23:53
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
@Documented
public @interface AutoWired {
}
