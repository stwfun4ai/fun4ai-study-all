package com.ly.io.rpc.nettyrpc.provider;

import com.ly.io.rpc.nettyrpc.commons.ClassInfo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description
 * @Created by fun4ai
 * @Date 1/7 0007 1:49
 */
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    private static Map<String, Object> map = new ConcurrentHashMap<>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ClassInfo classInfo = (ClassInfo) msg;
        Object clazz = null;
        //用于记录反射获得类，这样可以提高性能
        if (!map.containsKey(classInfo.getClassName())) {
            try {
                clazz = Class.forName(classInfo.getClassName()).newInstance();
                map.put(classInfo.getClassName(), clazz);
            //} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            clazz = map.get(classInfo.getClassName());
        }
        Method method = clazz.getClass().getMethod(classInfo.getMethodName(), classInfo.getTypes());
        Object result = method.invoke(clazz, classInfo.getObjects());
        ctx.writeAndFlush(result);
        //ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
