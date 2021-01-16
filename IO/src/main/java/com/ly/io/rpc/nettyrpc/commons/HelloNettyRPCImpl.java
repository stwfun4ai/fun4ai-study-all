package com.ly.io.rpc.nettyrpc.commons;



/**
 * @Description
 * @Created by fun4ai
 * @Date 1/7 0007 1:16
 */
public class HelloNettyRPCImpl implements HelloNettyRPC {

    @Override
    public String hello(String name) {
        return "HelloNettyRPC " + name;
    }
}
