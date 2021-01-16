package com.ly.io.rpc.nettyrpc.provider;


/**
 * @Description
 * @Created by fun4ai
 * @Date 1/7 0007 1:15
 */
public class ProviderMain {
    public static void main(String[] args) {
        NettyServer.startServer("127.0.0.1", 9999);
    }
}
