package com.ly.io.netty.tcp.protocol;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @Description
 * @Created by fun4ai
 * @Date 1/4 0004 15:29
 */
public class MyClient {
    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)    // 反射实现客户端的通道
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel sc) throws Exception {
                            ChannelPipeline pipeline = sc.pipeline();

                            // 测试带消息总长度的tcp粘包解决
                            pipeline.addLast(new MyMessageEncoder());
                            pipeline.addLast(new MyMessageDecoder());
                            pipeline.addLast(new MyClientProtocolHandler());

                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect("localhost", 9999).sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}
