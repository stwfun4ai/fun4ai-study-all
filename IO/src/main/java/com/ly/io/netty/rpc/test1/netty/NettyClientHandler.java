package com.ly.io.netty.rpc.test1.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.Callable;

/**
 * @Description
 * @Created by fun4ai
 * @Date 1/6 0006 3:01
 */
public class NettyClientHandler extends ChannelInboundHandlerAdapter implements Callable {

    // 上下文
    private ChannelHandlerContext context;

    // 返回结果
    private String result;

    // 客户端调用传入消息
    private String args;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelActive");
        context = ctx;
    }

    // 必须加同步锁，完成之后通过notify告知call()已收到消息
    @Override
    public synchronized void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("channelRead");
        result = msg.toString();
        notify();
    }

    // 必须加同步锁，通过wait等到channelRead
    @Override
    public synchronized Object call() throws Exception {
        System.out.println("call before wait");
        context.writeAndFlush(args);
        wait();
        System.out.println("call after wait");
        return result;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    public String getArgs() {

        return args;
    }

    public void setArgs(String args) {
        System.out.println("set args: " + args);
        this.args = args;
    }
}
