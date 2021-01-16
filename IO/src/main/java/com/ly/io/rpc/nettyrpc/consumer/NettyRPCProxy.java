package com.ly.io.rpc.nettyrpc.consumer;

import com.ly.io.rpc.nettyrpc.commons.ClassInfo;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.lang.reflect.Proxy;

/**
 * @Description
 * @Created by fun4ai
 * @Date 1/7 0007 1:25
 */
public class NettyRPCProxy {

    @SuppressWarnings("unchecked")
    public static <T> T create(Object target) {
        return (T) Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                target.getClass().getInterfaces(),
                (proxy, method, args) -> {
                    ClassInfo classInfo = new ClassInfo();
                    classInfo.setClassName(target.getClass().getName());
                    classInfo.setMethodName(method.getName());
                    classInfo.setObjects(args);
                    classInfo.setTypes(method.getParameterTypes());

                    ResultHandler resultHandler = new ResultHandler();

                    EventLoopGroup group = new NioEventLoopGroup();
                    try {
                        Bootstrap b = new Bootstrap();
                        b.group(group)
                                .channel(NioSocketChannel.class)
                                .option(ChannelOption.TCP_NODELAY, true)
                                .handler(new ChannelInitializer<SocketChannel>() {
                                    @Override
                                    protected void initChannel(SocketChannel ch) throws Exception {
                                        ChannelPipeline p = ch.pipeline();
                                        p.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                                        p.addLast("frameEncoder", new LengthFieldPrepender(4));
                                        p.addLast("encoder", new ObjectEncoder());
                                        p.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
                                        p.addLast("handler", resultHandler);
                                    }
                                });
                        ChannelFuture future = b.connect("127.0.0.1", 9999).sync();
                        future.channel().writeAndFlush(classInfo).sync();
                        future.channel().closeFuture().sync();

                    } finally {
                        group.shutdownGracefully();
                    }
                    return resultHandler.getResponse();
                });
    }
}
