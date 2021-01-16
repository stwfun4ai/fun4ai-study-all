package com.ly.io.nio;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * @Description
 * @Created by fun4ai
 * @Date 2020/12/31 18:43
 */
public class GroupChatClientNIO {

    private SocketChannel socketChannel;
    private Selector selector;
    private String host = "127.0.0.1";
    private static final int PORT = 9999;
    private String userName;

    public GroupChatClientNIO() throws IOException {
        selector = Selector.open();
        socketChannel = SocketChannel.open(new InetSocketAddress(host, PORT));
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);

        userName = socketChannel.getLocalAddress().toString().substring(1); //去掉斜杠
        System.out.println("客户端 " + userName + " 连接成功");
    }

    /**
     * 向服务端发送消息
     */
    public void sendInfo(String info) throws IOException{
        info = userName + "： "+info;
        socketChannel.write(ByteBuffer.wrap(info.getBytes()));
    }

    /**
     * 读取消息
     * @throws IOException
     */
    public void readInfo() throws IOException {
        if (selector.select() > 0) {
            Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                keyIterator.remove();
                if (key.isReadable()) {
                    SocketChannel channel = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int read = channel.read(buffer);
                    if (read > 0) {
                        System.out.println(new String(buffer.array()).trim());
                    }
                }
            }
        }
    }



    public static void main(String[] args) throws IOException {
        GroupChatClientNIO client = new GroupChatClientNIO();
        new Thread(()->{
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(3);
                    client.readInfo();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // 发送消息给服务端
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String msg = scanner.nextLine();
            client.sendInfo(msg);
        }
    }
}
