package com.ly.reflect.proxy.dynamicproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @Description JDK动态代理
 * @Created by Administrator
 * @Date 2020/10/8 23:07
 */
public class JdkDynamicProxy implements InvocationHandler {
    //被代理对象
    private Object target;

    public JdkDynamicProxy(Object target) {
        this.target = target;
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("jdkProxy调用【" + method.getName() + "】方法前，添加自己的操作");
        Object result = method.invoke(target, args);
        System.out.println("jdkProxy调用【" + method.getName() + "】方法后，添加自己的操作");
        return result;
    }
}
