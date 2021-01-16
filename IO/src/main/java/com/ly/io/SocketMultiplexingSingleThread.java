package com.ly.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * @Description Java NIO selector多路复用
 * @Created by fun4ai
 * @Date 2020/12/24 17:48
 */
public class SocketMultiplexingSingleThread {

    private ServerSocketChannel server = null;

    //linux 多路复用器（select poll epoll kqueue） nginx envent{}
    private Selector selector = null;
    int port = 9090;

    public void initServer() {
        try {
            // 获取通道
            server = ServerSocketChannel.open();
            // 设置为非阻塞
            server.configureBlocking(false);
            // 绑定连接
            server.bind(new InetSocketAddress(port));

            // select poll *epoll 优先选择epoll 可以通过-D修正
            // epoll下 open => epoll_create => fd7
            // 获取Selector选择器
            selector = Selector.open();

            /**
             * server = fd3
             *select poll: jvm里开辟一个数组
             * epoll: epoll_ctl(7,ADD,3,accept)
             */
            // 将通道注册到选择器上，监听接收事件
            server.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        initServer();
        System.out.println("服务器启动========");

        try {
            while (true) {
                Set<SelectionKey> keys = selector.keys();
                System.out.println(keys.size() + "  size");

                // 采用轮询的方式，查询获取“准备就绪”的注册过的操作
                while (selector.select() > 0) {
                    // 获取当前选择器中所有注册的选择键（“已经准备就绪的操作”）
                    Iterator<SelectionKey> selectionKeys = selector.selectedKeys().iterator();
                    while (selectionKeys.hasNext()) {
                        // 获取“准备就绪”的事件
                        SelectionKey key = selectionKeys.next();
                        // 不移除会重复循环处理
                        selectionKeys.remove();
                        if (key.isAcceptable()) {
                            // 若接受的事件是“接收就绪” 操作,就获取客户端连接
                            acceptHandler(key);
                        } else if (key.isReadable()) {
                            readHandler(key);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void acceptHandler(SelectionKey key) {
        try {
            // 若接受的事件是“接收就绪” 操作,就获取客户端连接
            ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
            SocketChannel client = ssc.accept();
            // 切换为非阻塞模式
            client.configureBlocking(false);

            ByteBuffer buffer = ByteBuffer.allocate(4096);  //可以在堆内 堆外
            // 将该通道注册到selector选择器上
            client.register(selector, SelectionKey.OP_READ, buffer);
            System.out.println("新客户端： " + client.getRemoteAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readHandler(SelectionKey key) {
        // 获取该选择器上的“读就绪”状态的通道
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        buffer.clear();
        int read = 0;
        try {
            while (true) {
                read = client.read(buffer);
                if (read > 0) {
                    buffer.flip();
                    while (buffer.hasRemaining()) {
                        client.write(buffer);
                    }
                    buffer.clear();
                } else if (read == 0) {
                    break;
                } else {    //-1 close_wait bug 死循环 CPU 100%
                    client.close();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SocketMultiplexingSingleThread service = new SocketMultiplexingSingleThread();
        service.start();
    }


}
