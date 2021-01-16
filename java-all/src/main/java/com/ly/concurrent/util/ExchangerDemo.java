package com.ly.concurrent.util;

import java.util.concurrent.Exchanger;

/**
 * @Description Exchanger类用于两个线程交换数据。它支持泛型，也就是说你可以在两个线程之间传送任何数据
 *      此类提供对外的操作是同步的；
 *      用于成对出现的线程之间交换数据；
 *      可以视作双向的同步队列；
 *      可应用于基因算法、流水线设计等场景。
 * @Created by Administrator
 * @Date 2020/10/12 15:13
 */
public class ExchangerDemo {
    public static void main(String[] args) throws InterruptedException {
        Exchanger<String> exchanger = new Exchanger<>();

        new Thread(() -> {
            try {
                System.out.println("这是线程A，得到了另一个线程的数据："
                        + exchanger.exchange("这是来自线程A的数据"));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        System.out.println("这个时候线程A是阻塞的，在等待线程B的数据");
        Thread.sleep(1000);

        new Thread(() -> {
            try {
                System.out.println("这是线程B，得到了另一个线程的数据："
                        + exchanger.exchange("这是来自线程B的数据"));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

}
