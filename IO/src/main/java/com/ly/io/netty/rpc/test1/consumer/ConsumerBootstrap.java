package com.ly.io.netty.rpc.test1.consumer;


import com.ly.io.netty.rpc.test1.api.HelloService;
import com.ly.io.netty.rpc.test1.netty.NettyClient;

import java.util.concurrent.TimeUnit;

/**
 * @Description
 * @Created by fun4ai
 * @Date 1/6 0006 3:24
 */
public class ConsumerBootstrap {
    public static void main(String[] args) {

        // 消费者
        NettyClient customer = new NettyClient();
        // 代理对象
        HelloService provider = (HelloService) customer.getBean(HelloService.class);

        for (; ; ) {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //通过代理对象调用服务提供者的方法(服务)
            String result = provider.hello("hello rpc~");
            System.out.println("result:" + result);
            System.out.println("=================================");
        }


    }
}
