package com.ly.io.netty.tcp.protocol;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @Description tcp粘包拆包解决--使用协议添加消息总长度编解码
 * @Created by fun4ai
 * @Date 1/4 0004 15:29
 */
public class MyServer {
    public static void main(String[] args) throws Exception{
        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap();

        try {
            serverBootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)  // 反射实现服务端的通道
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel sc) throws Exception {
                            ChannelPipeline pipeline = sc.pipeline();

                            pipeline.addLast(new MyMessageDecoder());
                            pipeline.addLast(new MyMessageEncoder());
                            pipeline.addLast(new MyServerProtocolHandler());
                        }
                    });
            System.out.println("netty 服务器启动===");

            // 绑定端口并同步
            ChannelFuture channelFuture = serverBootstrap.bind(9999).sync();
            // 对关闭通道进行监听
            channelFuture.channel().closeFuture().sync();

        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }

    }
}

