package com.ly.reflect.proxy.dynamicproxy;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * @Description Cglib动态代理 需要导入jar包cglib asm   类和方法不能声明为final
 * @Created by Administrator
 * @Date 2020/10/8 23:07
 */
public class CglibDynamicProxy implements MethodInterceptor {


    /**
     * @param object 被代理的对象（需要增强的对象）
     * @param method    被拦截的方法（需要增强的方法）
     * @param args   方法参数
     * @param methodProxy   用于调用原始方法
     * @return
     * @throws Throwable
     */
    @Override
    public Object intercept(Object object, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {

        System.out.println("cglibProxy调用【"+method.getName()+"】方法前，添加自己的操作");
        Object result = methodProxy.invokeSuper(object, args);
        System.out.println("cglibProxy调用【"+method.getName()+"】方法后，添加自己的操作");
        return result;
    }
}
