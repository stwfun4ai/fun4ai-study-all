package com.ly.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

/**
 * @Description 测试Non-blocking IO
 * @Created by fun4ai
 * @Date 2020/12/24 16:55
 */
public class SocketNIO {
    public static void main(String[] args) throws Exception {

        LinkedList<SocketChannel> clients = new LinkedList<>();

        ServerSocketChannel ss = ServerSocketChannel.open();    //服务端开启监听：接受客户端
        ss.bind(new InetSocketAddress(9090));

        //OS non-blocking
        ss.configureBlocking(false);    //只接收客户端 不阻塞

        while (true){
            Thread.sleep(1000);

            //accept 调用内核 客户端连接？客户端fd=5 : -1
            //BIO卡这 NIO直接返回-1
            SocketChannel client = ss.accept(); //接收客户端的连接不会阻塞 -1 null


            if(client == null){
                // System.out.println("null.......");
            }else {
                //客户端连接后的数据读写 不阻塞
                client.configureBlocking(false);

                int port = client.socket().getPort();
                System.out.println("client port: " + port);
                clients.add(client);
            }

            ByteBuffer buffer = ByteBuffer.allocateDirect(4096);  //可以在堆内 堆外

            //遍历连接进来的客户端可不可以读写数据
            for (SocketChannel c : clients) {   //串行化 多线程
                int num = c.read(buffer);   // >0 0 -1  不会阻塞
                if (num > 0){
                    buffer.flip();
                    byte[] aa = new byte[buffer.limit()];
                    buffer.get(aa);

                    String b = new String(aa);
                    System.out.println(c.socket().getPort() + " : " + b);
                    buffer.clear();
                }

            }
        }
    }
}
