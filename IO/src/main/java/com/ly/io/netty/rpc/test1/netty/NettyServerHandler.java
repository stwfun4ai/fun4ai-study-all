package com.ly.io.netty.rpc.test1.netty;

import com.ly.io.netty.rpc.test1.api.HelloService;
import com.ly.io.netty.rpc.test1.provider.HelloServiceImpl;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @Description
 * @Created by fun4ai
 * @Date 1/6 0006 2:20
 */
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 获取客户端发送的消息，并调用服务
        System.out.println("msg: " + msg);
        HelloService helloService = new HelloServiceImpl();
        String result = helloService.hello(msg.toString());
        ctx.writeAndFlush(result);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
