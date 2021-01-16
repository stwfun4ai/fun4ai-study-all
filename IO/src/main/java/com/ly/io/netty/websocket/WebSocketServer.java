package com.ly.io.netty.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.net.InetSocketAddress;

/**
 * @Description netty对webSocket的支持
 * @Created by fun4ai
 * @Date 2020/12/28 0:48
 */
public class WebSocketServer {
    public static void main(String args[]) throws Exception {
        EventLoopGroup boosGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        try {
            serverBootstrap.group(boosGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel sc) throws Exception {
                                ChannelPipeline pipeline = sc.pipeline();
                                //HttpServerCodec: 针对http协议进行编解码
                                pipeline.addLast(new HttpServerCodec());
                                //ChunkedWriteHandler分块写处理，文件过大会将内存撑爆
                                pipeline.addLast(new ChunkedWriteHandler());
                                /**
                                 * 聚合多段
                                 * 作用是将一个Http的消息组装成一个完成的HttpRequest或者HttpResponse，那么具体的是什么
                                 * 取决于是请求还是响应, 该Handler必须放在HttpServerCodec后的后面
                                 */
                                pipeline.addLast(new HttpObjectAggregator(8192));
                                /**
                                 * websocket的数据以帧[frame]的形式传输
                                 * WebSocketFrame 下6个子类
                                 * 浏览器请求如 ws://localhost:8899/hello   /hello为访问websocket时的uri
                                 * 核心功能为将Http协议升级为ws协议，保持长连接(通过状态码101)
                                 */
                                pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));

                                //自定义的处理器
                                pipeline.addLast(new TextWebSocketFrameHandler());
                        }
                    });

            //使用了不同的端口绑定方式
            ChannelFuture channelFuture = serverBootstrap.bind(new InetSocketAddress(8899)).sync();
            //关闭连接
            channelFuture.channel().closeFuture().sync();
        } finally {
            //优雅关闭
            boosGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }


}
