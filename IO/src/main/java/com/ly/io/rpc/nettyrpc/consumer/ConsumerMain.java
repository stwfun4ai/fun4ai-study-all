package com.ly.io.rpc.nettyrpc.consumer;

import com.ly.io.rpc.nettyrpc.commons.HelloNettyRPC;
import com.ly.io.rpc.nettyrpc.commons.HelloNettyRPCImpl;

/**
 * @Description
 * @Created by fun4ai
 * @Date 1/7 0007 1:22
 */
public class ConsumerMain {
    public static void main(String[] args) {
        HelloNettyRPC helloNettyRPC = new HelloNettyRPCImpl();
        helloNettyRPC = NettyRPCProxy.create(helloNettyRPC);
        System.out.println(helloNettyRPC.hello("rpc"));
    }
}
