package com.ly.io.netty.rpc.test1.provider;


import com.ly.io.netty.rpc.test1.api.HelloService;

/**
 * @Description
 * @Created by fun4ai
 * @Date 1/6 0006 2:04
 */
public class HelloServiceImpl implements HelloService {

    @Override
    public String hello(String msg) {
        System.out.println("receive from customer:" +msg);
        return "provider receive the message: " + msg;
    }
}
