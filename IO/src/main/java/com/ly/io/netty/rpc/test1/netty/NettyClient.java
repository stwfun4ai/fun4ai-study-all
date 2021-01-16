package com.ly.io.netty.rpc.test1.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.lang.reflect.Proxy;
import java.util.concurrent.*;

/**
 * @Description
 * @Created by fun4ai
 * @Date 1/6 0006 2:28
 */
public class NettyClient {
    //private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private static ExecutorService executor = new ThreadPoolExecutor(
            3,
            5,
            1L,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(3),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.AbortPolicy());

    private static NettyClientHandler clientHandler;
    private int count;

    public Object getBean(Class<?> providerClass) {
        return Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class<?>[]{providerClass},
                (proxy, method, args) -> {

                    System.out.println("jdk dynamic proxy method come [" + (++count) + "]time");
                    if (clientHandler == null) {
                        initClient();
                    }
                    clientHandler.setArgs((String) args[0]);


                    // 调用call()方法，注意调用get方法会阻塞当前线程，直到得到结果。 所以实际编码中建议使用可以设置超时时间的重载get方法。
                    return executor.submit(clientHandler).get();
                });

    }

    private static void initClient() {
        clientHandler = new NettyClientHandler();

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY,true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new StringDecoder());
                            p.addLast(new StringEncoder());
                            p.addLast(clientHandler);
                        }
                    });
            b.connect("127.0.0.1", 9999).sync();
            // 这里长连接不需要关闭连接SocketChannel，所以也不要关闭线程组
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*finally {
            group.shutdownGracefully();
        }*/

    }


}
