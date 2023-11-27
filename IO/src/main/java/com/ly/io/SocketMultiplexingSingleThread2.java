package com.ly.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

/**
 * @Description
 * @Created by fun4ai
 * @Date 2020/12/25 1:41
 */
public class SocketMultiplexingSingleThread2 {
    public static void main(String[] args) throws IOException {
        // 1、获取Selector选择器
        Selector selector = Selector.open();
        // 2、获取通道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 3.设置为非阻塞
        serverSocketChannel.configureBlocking(false);
        // 4、绑定连接
        serverSocketChannel.bind(new InetSocketAddress(9999));
        // 5、将通道注册到选择器上，监听接收事件
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务器启动========");

        // 6、采用轮询的方式，查询获取“准备就绪”的注册过的操作
        while (true) {
            while (selector.select() > 0) {
                // 7、获取当前选择器中所有注册的选择键（“已经准备就绪的操作”）
                Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
                while (selectedKeys.hasNext()) {
                    // 8、获取“准备就绪”的事件
                    SelectionKey key = selectedKeys.next();
                    // 15、移除选择键，不移除会重复循环处理
                    selectedKeys.remove();
                    // 9、判断key是具体的什么事件
                    if (key.isAcceptable()) {
                        // 10、若接受的事件是“接收就绪”操作,就获取客户端连接
                        SocketChannel client = serverSocketChannel.accept();
                        // 11、切换为非阻塞模式
                        client.configureBlocking(false);
                        // 12、将该通道注册到selector选择器上，监听可读事件，等待数据
                        client.register(selector, SelectionKey.OP_READ);
                        System.out.println("新客户端： " + client.getRemoteAddress());
                    } else if (key.isReadable()) {
                        // 13、获取该选择器上的“读就绪”状态的通道
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        // 14、读取数据
                        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                        int length = 0;
                        while (true) {
                            length = socketChannel.read(byteBuffer);
                            if (length > 0) {
                                byteBuffer.flip();
                                System.out.println(new String(byteBuffer.array(), 0, length));
                                ByteBuffer writeBuffer = ByteBuffer.wrap("返回给客户端的数据...".getBytes());
                                socketChannel.write(writeBuffer);
                                byteBuffer.clear();
                            } else if (length == 0) {
                                break;
                            } else { //-1 close_wait bug 死循环 CPU 100%
                                socketChannel.close();
                                break;
                            }
                        }
                    }
                }
            }
        }
        // 7、关闭连接
        //serverSocketChannel.close();
    }

}
