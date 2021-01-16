package com.ly.io.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * @Description
 * @Created by fun4ai
 * @Date 2020/12/31 18:43
 */
public class GroupChatServerNIO {

    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private static final int PORT = 9999;

    public GroupChatServerNIO() {
        try {
            // 1、获取Selector选择器
            selector = Selector.open();
            // 2、获取通道
            serverSocketChannel = ServerSocketChannel.open();
            // 3.设置为非阻塞
            serverSocketChannel.configureBlocking(false);
            // 4、绑定连接
            serverSocketChannel.bind(new InetSocketAddress(PORT));
            // 5、将通道注册到选择器上，监听接收事件
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("========服务器启动========");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listen() throws IOException {

        while (true) {
            while (selector.select(2000) > 0) {
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                if (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    // 移除选择键，不移除会重复循环处理
                    keyIterator.remove();
                    if (key.isAcceptable()) {
                        SocketChannel client = serverSocketChannel.accept();
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ);
                        System.out.println("新客户端： " + client.getRemoteAddress() + "上线了...");
                    }
                    if (key.isReadable()) {
                        readData(key);
                    }
                }
            }
        }
    }

    /**
     * @param key 读数据
     * @throws Exception
     */
    private void readData(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int read = channel.read(buffer);
        if (read > 0) {
            buffer.flip();
            String msg = new String(buffer.array()).trim();
            System.out.println("客户端发送消息： " + msg);
            // 向其他客户端转发消息
            sendDataToOtherClients(msg, channel);
            buffer.clear();
        }else {
            System.out.println(channel.getRemoteAddress() + "离线了");
            key.cancel();
            channel.close();
        }
    }

    /**
     * @param msg 向其他客户端转发消息
     * @param self
     */
    private void sendDataToOtherClients(String msg, SocketChannel self) throws IOException {
        System.out.println("服务器转发消息中...");
        for (SelectionKey key : selector.keys()) {
            if (key.channel() instanceof SocketChannel) {
                SocketChannel channel = (SocketChannel) key.channel();
                if(channel != self ){
                    channel.write(ByteBuffer.wrap(msg.getBytes()));
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        GroupChatServerNIO server = new GroupChatServerNIO();
        server.listen();
    }
}
