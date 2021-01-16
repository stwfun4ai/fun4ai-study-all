package com.ly.io.rpc.socketrpc;

/**
 * @Description
 * @Created by fun4ai
 * @Date 1/7 0007 18:33
 */
public class SocketRPCClient {
    public static void main(String[] args) {
        HelloSocketRPC helloSocketRPC = new HelloSocketRPCImpl();
        helloSocketRPC = SocketRPCProxy.create(helloSocketRPC);
        System.err.println(helloSocketRPC.hello("rpc"));
    }

}
