package com.ly.reflect.proxy.dynamicproxy;

import net.sf.cglib.proxy.CallbackFilter;

import java.lang.reflect.Method;

/**
 * @Description
 * @Created by Administrator
 * @Date 2020/10/9 16:19
 */
public class CglibFilter implements CallbackFilter {

    @Override
    public int accept(Method method) {
        //对应callback[]数组顺序
        //即send方法使用CglibDynamicProxy拦截，非send方法使用NoOp.INSTANCE拦截
        // 可以过滤动态代理的方法
        if ("send".equalsIgnoreCase(method.getName())) {
            return 0;
        }
        return 1;
    }
}
