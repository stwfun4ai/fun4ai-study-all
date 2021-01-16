package com.ly.reflect.proxy;

/**
 * @Description
 * @Created by Administrator
 * @Date 2020/10/8 22:48
 */
public class SmsServiceImpl implements SmsService {

    @Override
    public String send(String msg) {
        System.out.println("send message:" + msg);
        return msg;
    }
}
