package com.ly.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @Description linux单类测试需删除package
 *              否则使用javac -d . xxx.java自动生成目录 麻烦
 *              使用追踪 strace -ff -o out java xxx
 * @Created by fun4ai
 * @Date 2020/12/24 15:05
 */
public class SocketBIO {
    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(9090);
        System.out.println("step1: new ServerSocket(9090)");

        while (true){
            Socket client = ss.accept();    //阻塞1
            System.out.println("step2: client\t" + client.getPort());

            new Thread(()->{
                InputStream is= null;
                try {
                    is = client.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    while (true){
                        String s = reader.readLine();   //阻塞2
                        if (s != null){
                            System.out.println(s);
                        }else{
                            client.close();
                            break;
                        }
                    }
                    System.out.println("客户端断开！");

                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    if (is != null){
                        try {
                            is.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();

        }
    }
}
