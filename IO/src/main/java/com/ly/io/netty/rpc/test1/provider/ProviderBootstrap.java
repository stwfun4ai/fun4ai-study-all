package com.ly.io.netty.rpc.test1.provider;


import com.ly.io.netty.rpc.test1.netty.NettyServer;

/**
 * @Description
 * @Created by fun4ai
 * @Date 1/6 0006 2:25
 */
public class ProviderBootstrap {

    public static void main(String[] args) {
        NettyServer.startServer("127.0.0.1", 9999);
    }
}
