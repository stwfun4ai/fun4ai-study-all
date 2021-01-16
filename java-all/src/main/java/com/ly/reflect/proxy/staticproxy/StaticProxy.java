package com.ly.reflect.proxy.staticproxy;

import com.ly.reflect.proxy.SmsService;

/**
 * @Description 静态代理，创建代理类并同样实现发送短信的接口
 * @Created by Administrator
 * @Date 2020/10/8 22:51
 */
public class StaticProxy implements SmsService {
    private SmsService smsService;

    public StaticProxy(SmsService smsService){
        this.smsService = smsService;
    }

    @Override
    public String send(String msg) {
        System.out.println("staticProxy调用send()方法前，添加自己的操作");
        smsService.send(msg);
        System.out.println("staticProxy调用send()方法后，添加自己的操作");
        return null;
    }
}
