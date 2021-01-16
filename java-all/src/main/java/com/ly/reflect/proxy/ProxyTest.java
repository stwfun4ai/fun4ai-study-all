package com.ly.reflect.proxy;

import com.ly.reflect.proxy.dynamicproxy.CglibDynamicProxy;
import com.ly.reflect.proxy.dynamicproxy.CglibFilter;
import com.ly.reflect.proxy.dynamicproxy.JdkDynamicProxy;
import com.ly.reflect.proxy.staticproxy.StaticProxy;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

import java.lang.reflect.Proxy;


/**
 * @Description
 * @Created by Administrator
 * @Date 2020/10/8 23:00
 */
public class ProxyTest {
    public static void main(String[] args) {
        SmsService smsService = new SmsServiceImpl();

        /**
         *  静态代理测试
         */
        StaticProxy staticProxy = new StaticProxy(smsService);
        staticProxy.send("静态代理。。。");
        System.out.println("============================================");

        /**
         *  JDK动态代理 被代理对象必须实现接口
         */
        SmsService jdkProxy = (SmsService) Proxy.newProxyInstance(
                smsService.getClass().getClassLoader(), //被代理类的类加载器
                smsService.getClass().getInterfaces(),  //被代理类需要实现的接口(可多个)
                new JdkDynamicProxy(smsService));//代理类具体实现

        jdkProxy.send("JDK动态代理。。。");
        System.out.println("============================================");

        /**
         *  Cglib动态代理 不需要实现接口  需要导入jar包cglib和asm,类和方法不能声明为final
         */

        SmsServiceImpl cglibProxy = (SmsServiceImpl) Enhancer.create(
                SmsServiceImpl.class,
                null,
                new CglibFilter(),
                new Callback[]{new CglibDynamicProxy(), NoOp.INSTANCE});    //NoOp.INSTANCE是CGlib所提供的实际是一个没有任何操作的拦截器
        cglibProxy.send("Cglib动态代理。。。");





    }
}
