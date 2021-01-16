package com.ly.io.rpc.socketrpc;

/**
 * @Description
 * @Created by fun4ai
 * @Date 1/7 0007 18:35
 */
public class HelloSocketRPCImpl implements HelloSocketRPC {

    @Override
    public String hello(String name) {
        return "HelloSocketRPC " + name;
    }
}
